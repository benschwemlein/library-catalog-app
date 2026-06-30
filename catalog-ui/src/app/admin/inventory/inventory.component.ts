import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { BranchService } from '../../shared/services/branch.service';
import { Branch } from '../../shared/models/branch.model';

@Component({
  selector: 'app-inventory',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './inventory.component.html'
})
export class InventoryComponent implements OnInit {
  copies: any[] = [];
  branches: Branch[] = [];
  selectedBranchId: number | null = null;
  statusFilter: string = '';
  loading: boolean = false;
  actionLoading: { [copyId: number]: boolean } = {};
  actionError: { [copyId: number]: string } = {};

  constructor(private branchService: BranchService, private http: HttpClient) {}

  ngOnInit(): void {
    this.loadBranches();
  }

  loadBranches(): void {
    this.branchService.getBranches().subscribe({
      next: (branches) => {
        this.branches = branches;
        if (branches.length > 0) {
          this.selectedBranchId = branches[0].id;
          this.loadCopies();
        }
      },
      error: (err) => console.error('Failed to load branches', err)
    });
  }

  loadCopies(): void {
    if (!this.selectedBranchId) return;
    this.loading = true;
    this.branchService.getBranchInventory(this.selectedBranchId).subscribe({
      next: (copies) => {
        this.copies = copies.map((c: any) => ({
          ...c,
          bookTitle: c.book?.title || c.bookTitle || 'Unknown'
        }));
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load inventory', err);
        this.loading = false;
      }
    });
  }

  get filteredCopies(): any[] {
    if (!this.statusFilter) return this.copies;
    return this.copies.filter(c => c.status === this.statusFilter);
  }

  transferCopy(copyId: number): void {
    const targetBranchStr = prompt('Enter target branch ID:');
    if (!targetBranchStr) return;
    const targetBranchId = Number(targetBranchStr);
    if (isNaN(targetBranchId)) return;
    this.actionLoading[copyId] = true;
    this.http.post<any>(`http://localhost:8080/api/copies/${copyId}/transfer`, { targetBranchId }).subscribe({
      next: () => {
        this.actionLoading[copyId] = false;
        this.loadCopies();
      },
      error: (err) => {
        this.actionError[copyId] = err.error?.message || 'Transfer failed.';
        this.actionLoading[copyId] = false;
      }
    });
  }

  withdrawCopy(copyId: number): void {
    if (!confirm('Withdraw this copy from circulation?')) return;
    this.actionLoading[copyId] = true;
    this.http.delete<void>(`http://localhost:8080/api/copies/${copyId}`).subscribe({
      next: () => {
        this.copies = this.copies.filter(c => c.id !== copyId);
        this.actionLoading[copyId] = false;
      },
      error: (err) => {
        this.actionError[copyId] = err.error?.message || 'Withdrawal failed.';
        this.actionLoading[copyId] = false;
      }
    });
  }
}

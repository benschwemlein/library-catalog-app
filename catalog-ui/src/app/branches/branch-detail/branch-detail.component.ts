import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { BranchService } from '../../shared/services/branch.service';
import { Branch, BranchStats } from '../../shared/models/branch.model';

@Component({
  selector: 'app-branch-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './branch-detail.component.html',
  styleUrl: './branch-detail.component.css'
})
export class BranchDetailComponent implements OnInit {
  branch: Branch | null = null;
  stats: BranchStats | null = null;
  inventory: any[] = [];
  loading: boolean = false;
  inventoryLoading: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private branchService: BranchService
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const id = Number(params.get('id'));
      if (id) {
        this.loadBranch(id);
        this.loadInventory(id);
        this.loadStats(id);
      }
    });
  }

  loadBranch(id: number): void {
    this.loading = true;
    this.branchService.getBranchById(id).subscribe({
      next: (raw: any) => {
        this.branch = {
          ...raw,
          state: raw.state || '',
          zipCode: raw.zipCode || '',
          totalCopies: raw.totalCopies ?? 0,
          hours: this.parseHours(raw.openingHours),
        };
        this.loading = false;
      },
      error: (err) => { console.error(err); this.loading = false; }
    });
  }

  loadStats(id: number): void {
    this.branchService.getBranchStats(id).subscribe({
      next: (stats) => this.stats = stats,
      error: (err) => console.error('Failed to load stats', err)
    });
  }

  loadInventory(id: number): void {
    this.inventoryLoading = true;
    this.branchService.getBranchInventory(id, 0, 50).subscribe({
      next: (rawInventory: any[]) => {
        this.inventory = rawInventory.map(c => ({
          ...c,
          bookTitle: c.book?.title || c.bookTitle || 'Unknown',
        }));
        this.inventoryLoading = false;
      },
      error: (err) => { console.error(err); this.inventoryLoading = false; }
    });
  }

  private parseHours(openingHours: string | null): any[] {
    if (!openingHours) {
      return [
        { dayOfWeek: 'Monday', openTime: '9:00 AM', closeTime: '8:00 PM', closed: false },
        { dayOfWeek: 'Tuesday', openTime: '9:00 AM', closeTime: '8:00 PM', closed: false },
        { dayOfWeek: 'Wednesday', openTime: '9:00 AM', closeTime: '8:00 PM', closed: false },
        { dayOfWeek: 'Thursday', openTime: '9:00 AM', closeTime: '8:00 PM', closed: false },
        { dayOfWeek: 'Friday', openTime: '9:00 AM', closeTime: '6:00 PM', closed: false },
        { dayOfWeek: 'Saturday', openTime: '10:00 AM', closeTime: '5:00 PM', closed: false },
        { dayOfWeek: 'Sunday', openTime: '12:00 PM', closeTime: '4:00 PM', closed: false },
      ];
    }
    return [];
  }

  isOpenNow(branch: Branch): boolean {
    const days = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
    const today = days[new Date().getDay()];
    const hours = branch.hours?.find(h => h.dayOfWeek === today);
    if (!hours || hours.closed) return false;
    const now = new Date();
    const [openH, openM] = hours.openTime.split(':').map(Number);
    const [closeH, closeM] = hours.closeTime.split(':').map(Number);
    const open = new Date(); open.setHours(openH, openM, 0);
    const close = new Date(); close.setHours(closeH, closeM, 0);
    return now >= open && now <= close;
  }
}

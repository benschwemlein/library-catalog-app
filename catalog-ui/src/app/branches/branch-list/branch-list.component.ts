import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { BranchService } from '../../shared/services/branch.service';
import { Branch } from '../../shared/models/branch.model';

@Component({
  selector: 'app-branch-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './branch-list.component.html',
  styleUrl: './branch-list.component.css'
})
export class BranchListComponent implements OnInit {
  branches: Branch[] = [];
  loading: boolean = false;
  createOpen: boolean = false;
  creating: boolean = false;
  createError: string = '';
  newBranch = { name: '', address: '', city: '', phone: '', email: '' };

  constructor(private branchService: BranchService) {}

  ngOnInit(): void {
    this.loading = true;
    this.branchService.getBranches().subscribe({
      next: (rawBranches: any[]) => {
        this.branches = rawBranches.map(b => ({
          ...b,
          state: b.state || '',
          zipCode: b.zipCode || '',
          totalCopies: b.totalCopies ?? 0,
          hours: this.parseHours(b.openingHours),
        }));
        this.loading = false;
      },
      error: (err) => { console.error(err); this.loading = false; }
    });
  }

  getTodayHours(branch: Branch): string {
    const days = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
    const today = days[new Date().getDay()];
    const hours = branch.hours?.find(h => h.dayOfWeek === today);
    if (!hours) return 'Hours unavailable';
    if (hours.closed) return 'Closed today';
    return `Open today: ${hours.openTime} - ${hours.closeTime}`;
  }

  createBranch(): void {
    if (!this.newBranch.name.trim()) { this.createError = 'Name is required.'; return; }
    this.creating = true;
    this.createError = '';
    this.branchService.createBranch(this.newBranch as any).subscribe({
      next: (created: any) => {
        const branch = {
          ...created,
          state: '',
          zipCode: '',
          totalCopies: 0,
          hours: this.parseHours(created.openingHours),
        };
        this.branches.push(branch);
        this.createOpen = false;
        this.creating = false;
        this.newBranch = { name: '', address: '', city: '', phone: '', email: '' };
      },
      error: (err: any) => {
        this.createError = err.error?.message || 'Failed to create branch.';
        this.creating = false;
      }
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
}

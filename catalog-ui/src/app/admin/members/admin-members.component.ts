import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { MemberService } from '../../shared/services/member.service';

@Component({
  selector: 'app-admin-members',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './admin-members.component.html',
  styleUrl: './admin-members.component.css'
})
export class AdminMembersComponent implements OnInit {
  members: any[] = [];
  searchQuery: string = '';
  loading: boolean = false;
  actionLoading: { [id: number]: boolean } = {};
  actionError: { [id: number]: string } = {};

  createOpen: boolean = false;
  creating: boolean = false;
  createError: string = '';
  newMember = {
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    membershipTier: 'STANDARD',
  };
  memberTiers = ['STANDARD', 'PREMIUM', 'STUDENT', 'SENIOR'];

  constructor(private memberService: MemberService, private http: HttpClient) {}

  ngOnInit(): void {
    this.loadMembers();
  }

  private mapMember(raw: any): any {
    return {
      ...raw,
      firstName: raw.user?.firstName || raw.firstName || '',
      lastName: raw.user?.lastName || raw.lastName || '',
      email: raw.user?.email || raw.email || '',
      activeLoansCount: raw.activeLoansCount ?? 0,
    };
  }

  loadMembers(): void {
    this.loading = true;
    this.memberService.getMembers().subscribe({
      next: (rawMembers: any[]) => {
        this.members = rawMembers.map(m => this.mapMember(m));
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load members', err);
        this.loading = false;
      }
    });
  }

  searchMembers(): void {
    if (!this.searchQuery.trim()) {
      this.loadMembers();
      return;
    }
    this.loading = true;
    this.memberService.searchMembers(this.searchQuery).subscribe({
      next: (rawMembers: any[]) => {
        this.members = rawMembers.map(m => this.mapMember(m));
        this.loading = false;
      },
      error: (err) => {
        console.error('Search failed', err);
        this.loading = false;
      }
    });
  }

  registerMember(): void {
    if (!this.newMember.firstName || !this.newMember.lastName || !this.newMember.email) {
      this.createError = 'First name, last name, and email are required.';
      return;
    }
    this.creating = true;
    this.createError = '';
    this.http.post<any>('http://localhost:8080/api/v1/auth/register', {
      firstName: this.newMember.firstName,
      lastName: this.newMember.lastName,
      email: this.newMember.email,
      password: 'Welcome123!',
      role: 'USER',
    }).subscribe({
      next: () => {
        this.loadMembers();
        this.createOpen = false;
        this.creating = false;
        this.newMember = { firstName: '', lastName: '', email: '', phone: '', membershipTier: 'STANDARD' };
      },
      error: (err: any) => {
        this.createError = err.error?.message || 'Failed to register member.';
        this.creating = false;
      }
    });
  }

  suspendMember(id: number): void {
    const reason = prompt('Reason for suspension:');
    if (!reason) return;
    this.actionLoading[id] = true;
    this.memberService.suspendMember(id, reason).subscribe({
      next: () => {
        const idx = this.members.findIndex(m => m.id === id);
        if (idx !== -1) this.members[idx] = { ...this.members[idx], active: false };
        this.actionLoading[id] = false;
      },
      error: (err) => {
        this.actionError[id] = err.error?.message || 'Failed to suspend member.';
        this.actionLoading[id] = false;
      }
    });
  }

  reactivateMember(id: number): void {
    this.actionLoading[id] = true;
    this.memberService.reactivateMember(id).subscribe({
      next: () => {
        const idx = this.members.findIndex(m => m.id === id);
        if (idx !== -1) this.members[idx] = { ...this.members[idx], active: true };
        this.actionLoading[id] = false;
      },
      error: (err) => {
        this.actionError[id] = err.error?.message || 'Failed to reactivate.';
        this.actionLoading[id] = false;
      }
    });
  }
}

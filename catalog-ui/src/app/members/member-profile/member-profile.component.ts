import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { MemberService } from '../../shared/services/member.service';
import { MemberProfile, UpdateMemberRequest } from '../../shared/models/member.model';
import { AuthService } from '../../shared/services/auth.service';

@Component({
  selector: 'app-member-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './member-profile.component.html'
})
export class MemberProfileComponent implements OnInit {
  profile: MemberProfile | null = null;
  editMode: boolean = false;
  loading: boolean = false;
  saving: boolean = false;
  saveSuccess: boolean = false;
  saveError: string = '';

  get memberId(): number { return this.authService.getMemberId(); }

  formData: UpdateMemberRequest = {
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    address: ''
  };

  constructor(
    private memberService: MemberService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
    this.loading = true;
    this.memberService.getMemberProfile(this.memberId).subscribe({
      next: (raw: any) => {
        this.profile = {
          id: raw.id,
          membershipNumber: raw.membershipNumber,
          firstName: raw.user?.firstName || raw.firstName || '',
          lastName: raw.user?.lastName || raw.lastName || '',
          email: raw.user?.email || raw.email || '',
          phone: raw.phone || '',
          address: raw.address || '',
          membershipTier: raw.membershipTier,
          memberSince: raw.joinDate,
          expirationDate: raw.expiryDate,
          preferences: raw.preferences || { emailNotifications: true, smsNotifications: false, preferredLanguage: 'English' },
        };
        this.formData = {
          firstName: this.profile.firstName,
          lastName: this.profile.lastName,
          email: this.profile.email,
          phone: this.profile.phone || '',
          address: this.profile.address || '',
        };
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load profile', err);
        this.loading = false;
      }
    });
  }

  saveProfile(form?: NgForm): void {
    if (form && form.invalid) {
      form.form.markAllAsTouched();
      return;
    }
    this.saving = true;
    this.saveError = '';
    this.saveSuccess = false;
    this.memberService.updateProfile(this.memberId, this.formData).subscribe({
      next: (updated) => {
        this.profile = updated;
        this.saving = false;
        this.saveSuccess = true;
        this.editMode = false;
      },
      error: (err) => {
        this.saveError = err.error?.message || 'Failed to save profile.';
        this.saving = false;
      }
    });
  }

  cancelEdit(): void {
    this.editMode = false;
    if (this.profile) {
      this.formData = {
        firstName: this.profile.firstName,
        lastName: this.profile.lastName,
        email: this.profile.email,
        phone: this.profile.phone || '',
        address: this.profile.address || ''
      };
    }
  }
}

export enum MembershipTier {
  STANDARD = 'STANDARD',
  PREMIUM = 'PREMIUM',
  STUDENT = 'STUDENT'
}

export interface Member {
  id: number;
  membershipNumber: string;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  address?: string;
  membershipTier: MembershipTier;
  memberSince: string;
  expirationDate: string;
  active: boolean;
  activeLoansCount: number;
  activeHoldsCount: number;
  unpaidFinesTotal: number;
}

export interface MemberSummary {
  id: number;
  membershipNumber: string;
  firstName: string;
  lastName: string;
  email: string;
  membershipTier: MembershipTier;
  active: boolean;
  activeLoansCount: number;
}

export interface MemberProfile {
  id: number;
  membershipNumber: string;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  address?: string;
  membershipTier: MembershipTier;
  memberSince: string;
  expirationDate: string;
  preferences: MemberPreferences;
}

export interface MemberPreferences {
  emailNotifications: boolean;
  smsNotifications: boolean;
  preferredLanguage: string;
  preferredBranchId?: number;
}

export interface UpdateMemberRequest {
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  address?: string;
  preferences?: MemberPreferences;
}

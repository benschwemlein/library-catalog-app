export type ILLStatus = 'PENDING' | 'APPROVED' | 'ORDERED' | 'RECEIVED' | 'AVAILABLE' | 'RETURNED' | 'DENIED';

export interface InterLibraryLoanRequest {
  id: number;
  requestingMemberId: number;
  requestingMemberName: string;
  requestingBranchId: number;
  bookTitle: string;
  authorName?: string;
  isbn?: string;
  requestDate: string;
  neededByDate?: string;
  status: ILLStatus;
  partnerLibraryName?: string;
  notes?: string;
  estimatedArrival?: string;
  reviewNote?: string;
  denialReason?: string;
}

export interface SubmitILLRequest {
  memberId: number;
  branchId: number;
  bookTitle: string;
  authorName?: string;
  isbn?: string;
  neededByDate?: string;
  notes?: string;
}

export interface PartnerLibrary {
  id: number;
  name: string;
  city: string;
  state: string;
  loanPeriodDays: number;
  shippingDays: number;
  active: boolean;
}

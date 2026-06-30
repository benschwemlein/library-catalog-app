export enum LoanStatus {
  ACTIVE = 'ACTIVE',
  RETURNED = 'RETURNED',
  OVERDUE = 'OVERDUE',
  RENEWED = 'RENEWED',
  LOST = 'LOST'
}

export interface Loan {
  id: number;
  bookId: number;
  bookTitle: string;
  bookIsbn: string;
  authorNames: string[];
  memberId: number;
  memberName: string;
  membershipNumber: string;
  copyId: number;
  copyBarcode: string;
  branchId: number;
  branchName: string;
  checkoutDate: string;
  dueDate: string;
  returnDate?: string;
  renewalCount: number;
  maxRenewals: number;
  status: LoanStatus;
  fineAmount?: number;
}

export interface LoanSummary {
  id: number;
  bookTitle: string;
  bookId: number;
  memberId: number;
  memberName: string;
  checkoutDate: string;
  dueDate: string;
  returnDate?: string;
  status: LoanStatus;
  daysOverdue?: number;
}

export interface CheckoutRequest {
  copyBarcode: string;
  membershipNumber: string;
  branchId: number;
  dueDays?: number;
}

export interface RenewRequest {
  loanId: number;
  extendDays?: number;
}

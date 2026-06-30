export enum FineStatus {
  UNPAID = 'UNPAID',
  PAID = 'PAID',
  WAIVED = 'WAIVED'
}

export enum FineReason {
  OVERDUE = 'OVERDUE',
  LOST = 'LOST',
  DAMAGED = 'DAMAGED',
  OTHER = 'OTHER'
}

export interface Fine {
  id: number;
  loanId: number;
  bookTitle: string;
  memberId: number;
  memberName: string;
  amount: number;
  paidAmount: number;
  remainingAmount: number;
  reason: FineReason;
  description?: string;
  status: FineStatus;
  createdDate: string;
  paidDate?: string;
  waivedDate?: string;
  waivedBy?: string;
}

export interface PayFineRequest {
  fineId: number;
  amount: number;
  paymentMethod: string;
}

export interface WaiveFineRequest {
  fineId: number;
  reason: string;
}

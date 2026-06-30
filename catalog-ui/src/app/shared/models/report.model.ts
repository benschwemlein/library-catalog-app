export interface MostBorrowedBook {
  bookId: number;
  bookTitle: string;
  isbn: string;
  authorNames: string[];
  checkoutCount: number;
  rank: number;
}

export interface MostBorrowedReport {
  period: string;
  startDate: string;
  endDate: string;
  books: MostBorrowedBook[];
}

export interface OverdueLoan {
  loanId: number;
  bookTitle: string;
  memberName: string;
  membershipNumber: string;
  memberEmail: string;
  dueDate: string;
  daysOverdue: number;
  fineAmount: number;
  branchName: string;
}

export interface OverdueReport {
  generatedAt: string;
  totalOverdue: number;
  totalFineAmount: number;
  loans: OverdueLoan[];
}

export interface BranchStatsReport {
  reportDate: string;
  branches: BranchStat[];
}

export interface BranchStat {
  branchId: number;
  branchName: string;
  totalCheckouts: number;
  activeLoans: number;
  overdueLoans: number;
  newMembers: number;
  eventsHeld: number;
  popularBooks: string[];
}

export interface MemberActivityEntry {
  memberId: number;
  memberName: string;
  membershipNumber: string;
  membershipTier: string;
  totalLoans: number;
  activeLoans: number;
  totalFines: number;
  unpaidFines: number;
  lastActivity: string;
}

export interface MemberActivityReport {
  reportDate: string;
  period: string;
  totalMembers: number;
  activeMembers: number;
  entries: MemberActivityEntry[];
}

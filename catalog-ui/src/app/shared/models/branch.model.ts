export interface BranchHours {
  dayOfWeek: string;
  openTime: string;
  closeTime: string;
  closed: boolean;
}

export interface Branch {
  id: number;
  name: string;
  code: string;
  address: string;
  city: string;
  state: string;
  zipCode: string;
  phone: string;
  email: string;
  managerId?: number;
  managerName?: string;
  active: boolean;
  hours: BranchHours[];
  totalBooks: number;
  totalCopies: number;
}

export interface BranchStats {
  branchId: number;
  branchName: string;
  totalCheckouts: number;
  activeLoans: number;
  overdueLoans: number;
  activeHolds: number;
  totalMembers: number;
  popularBooks: { bookTitle: string; checkoutCount: number }[];
}

export interface CreateBranchRequest {
  name: string;
  code: string;
  address: string;
  city: string;
  state: string;
  zipCode: string;
  phone: string;
  email: string;
  hours?: BranchHours[];
}

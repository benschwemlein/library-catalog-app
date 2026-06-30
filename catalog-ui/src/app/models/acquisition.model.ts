export type AcquisitionPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
export type AcquisitionStatus = 'PENDING' | 'UNDER_REVIEW' | 'APPROVED' | 'ORDERED' | 'RECEIVED' | 'DENIED';
export type PurchaseOrderStatus = 'DRAFT' | 'SUBMITTED' | 'CONFIRMED' | 'SHIPPED' | 'RECEIVED' | 'CANCELLED';

export interface AcquisitionRequest {
  id: number;
  requestedByMemberName?: string;
  requestedByStaff?: string;
  title: string;
  author?: string;
  isbn?: string;
  publisher?: string;
  reason?: string;
  priority: AcquisitionPriority;
  status: AcquisitionStatus;
  requestDate: string;
  reviewedByName?: string;
  reviewNote?: string;
  estimatedCost?: number;
  targetBranchName?: string;
}

export interface PurchaseOrder {
  id: number;
  supplier: string;
  orderDate?: string;
  expectedDelivery?: string;
  actualDelivery?: string;
  totalCost: number;
  status: PurchaseOrderStatus;
  notes?: string;
  requestCount: number;
}

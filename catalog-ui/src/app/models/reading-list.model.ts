export type ListVisibility = 'PRIVATE' | 'PUBLIC' | 'FRIENDS_ONLY';
export type ItemPriority = 'HIGH' | 'MEDIUM' | 'LOW';

export interface ReadingList {
  id: number;
  memberId: number;
  memberName: string;
  name: string;
  description?: string;
  visibility: ListVisibility;
  createdDate: string;
  itemCount: number;
  readCount: number;
  items?: ReadingListItem[];
}

export interface ReadingListItem {
  itemId: number;
  listId: number;
  bookId: number;
  bookTitle: string;
  bookIsbn: string;
  authorNames: string[];
  addedDate: string;
  priority: ItemPriority;
  notes?: string;
  read: boolean;
  readDate?: string;
  sortOrder: number;
}

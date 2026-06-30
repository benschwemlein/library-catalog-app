export type BookClubStatus = 'ACTIVE' | 'PAUSED' | 'CLOSED';
export type BookClubMemberRole = 'MEMBER' | 'FACILITATOR' | 'CO_FACILITATOR';

export interface BookClub {
  id: number;
  name: string;
  description?: string;
  branchName: string;
  branchId: number;
  facilitatorName?: string;
  maxMembers: number;
  memberCount: number;
  meetingSchedule?: string;
  currentBookTitle?: string;
  currentBookId?: number;
  status: BookClubStatus;
  createdAt: string;
}

export interface BookClubMeeting {
  id: number;
  clubId: number;
  clubName: string;
  bookTitle?: string;
  meetingDate: string;
  location: string;
  notes?: string;
  attendanceCount: number;
}

export interface DiscussionPost {
  id: number;
  clubId: number;
  meetingId?: number;
  posterName: string;
  posterId: number;
  content: string;
  postedAt: string;
  parentDiscussionId?: number;
  replyCount?: number;
  edited: boolean;
  editedAt?: string;
}

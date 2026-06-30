export interface ReadingChallenge {
  id: number;
  name: string;
  description?: string;
  startDate: string;
  endDate: string;
  targetBooks: number;
  targetGenres?: string;
  badge?: string;
  active: boolean;
  enrolledCount: number;
  completedCount: number;
}

export interface ChallengeParticipation {
  participationId: number;
  challengeId: number;
  challengeName: string;
  memberId: number;
  memberName?: string;
  enrollDate: string;
  completedBooks: number;
  targetBooks: number;
  badgeEarned: boolean;
  completedDate?: string;
  progressPercentage: number;
}

export interface LeaderboardEntry {
  rank: number;
  memberId: number;
  memberName: string;
  completedBooks: number;
  badgeEarned: boolean;
  completedDate?: string;
}

export interface LogProgressRequest {
  memberId: number;
  bookId?: number;
  bookTitle?: string;
  completedDate: string;
  notes?: string;
}

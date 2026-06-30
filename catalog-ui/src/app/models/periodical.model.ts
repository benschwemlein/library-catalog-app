export type PeriodicalFrequency = 'DAILY' | 'WEEKLY' | 'BIWEEKLY' | 'MONTHLY' | 'QUARTERLY' | 'BIANNUAL' | 'ANNUAL';
export type PeriodicalIssueStatus = 'CURRENT' | 'ARCHIVED' | 'MISSING' | 'DAMAGED' | 'WITHDRAWN';

export interface Periodical {
  id: number;
  title: string;
  issn?: string;
  publisher?: string;
  frequency: PeriodicalFrequency;
  category?: string;
  description?: string;
  active: boolean;
  digitalAccess: boolean;
  digitalUrl?: string;
  branchName?: string;
}

export interface PeriodicalIssue {
  id: number;
  periodicalId: number;
  periodicalTitle: string;
  volume: number;
  issueNumber: number;
  publicationDate: string;
  status: PeriodicalIssueStatus;
  condition?: string;
  location?: string;
}

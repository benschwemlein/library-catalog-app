export enum EventType {
  BOOK_CLUB = 'BOOK_CLUB',
  AUTHOR_TALK = 'AUTHOR_TALK',
  CHILDREN_STORY_TIME = 'CHILDREN_STORY_TIME',
  WORKSHOP = 'WORKSHOP',
  EXHIBITION = 'EXHIBITION',
  FILM_SCREENING = 'FILM_SCREENING',
  LECTURE = 'LECTURE',
  OTHER = 'OTHER'
}

export interface LibraryEvent {
  id: number;
  title: string;
  description?: string;
  type: EventType;
  branchId: number;
  branchName: string;
  startDate: string;
  endDate: string;
  maxAttendees: number;
  currentAttendees: number;
  spotsAvailable: number;
  registrationRequired: boolean;
  registrationDeadline?: string;
  imageUrl?: string;
  hostName?: string;
  active: boolean;
}

export interface EventRegistration {
  id: number;
  eventId: number;
  eventTitle: string;
  memberId: number;
  memberName: string;
  registeredAt: string;
  attended?: boolean;
}

export interface CreateEventRequest {
  title: string;
  description?: string;
  eventType: EventType | string;
  branchId: number;
  startDateTime: string;
  endDateTime: string;
  capacity: number;
}

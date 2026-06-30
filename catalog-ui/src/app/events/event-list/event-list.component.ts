import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { EventService } from '../../shared/services/event.service';
import { BranchService } from '../../shared/services/branch.service';
import { LibraryEvent, EventType } from '../../shared/models/event.model';

@Component({
  selector: 'app-event-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './event-list.component.html',
  styleUrl: './event-list.component.css'
})
export class EventListComponent implements OnInit {
  events: LibraryEvent[] = [];
  filteredEvents: LibraryEvent[] = [];
  typeFilter: string = '';
  loading: boolean = false;
  eventTypes = Object.values(EventType);

  createOpen: boolean = false;
  creating: boolean = false;
  createError: string = '';
  branches: any[] = [];
  newEvent = {
    title: '',
    description: '',
    eventType: 'WORKSHOP',
    branchId: 1,
    startDateTime: '',
    endDateTime: '',
    capacity: 30,
  };

  constructor(
    private eventService: EventService,
    private branchService: BranchService
  ) {}

  ngOnInit(): void {
    this.loadEvents();
    this.branchService.getBranches().subscribe({ next: (bs) => this.branches = bs });
  }

  loadEvents(): void {
    this.loading = true;
    this.eventService.getUpcomingEvents().subscribe({
      next: (rawEvents: any[]) => {
        this.events = rawEvents.map(e => ({
          ...e,
          type: e.eventType || e.type,
          startDate: e.startDateTime || e.startDate,
          endDate: e.endDateTime || e.endDate,
          maxAttendees: e.capacity || e.maxAttendees,
          currentAttendees: e.registeredCount || e.currentAttendees || 0,
          registrationRequired: e.registrationRequired ?? true,
          spotsAvailable: e.spotsAvailable ?? (e.capacity - (e.registeredCount || 0)),
          branchName: e.branchName || e.branch?.name || '',
          branchId: e.branchId || e.branch?.id,
        }));
        this.applyFilter();
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load events', err);
        this.loading = false;
      }
    });
  }

  applyFilter(): void {
    if (!this.typeFilter) {
      this.filteredEvents = [...this.events];
    } else {
      this.filteredEvents = this.events.filter(e => e.type === this.typeFilter);
    }
  }

  getEventTypeIcon(type: EventType): string {
    const icons: Record<string, string> = {
      BOOK_CLUB: '📖',
      AUTHOR_TALK: '🎤',
      CHILDREN_STORY_TIME: '🧒',
      WORKSHOP: '🔧',
      EXHIBITION: '🎨',
      FILM_SCREENING: '🎬',
      LECTURE: '🎓',
      OTHER: '📅'
    };
    return icons[type] || '📅';
  }

  isSoonFull(event: LibraryEvent): boolean {
    return event.spotsAvailable <= 5 && event.spotsAvailable > 0;
  }

  createEvent(): void {
    if (!this.newEvent.title.trim()) { this.createError = 'Title is required.'; return; }
    this.creating = true;
    this.createError = '';
    this.eventService.createEvent(this.newEvent as any).subscribe({
      next: (created: any) => {
        this.events.unshift({
          ...created,
          type: created.eventType || created.type,
          startDate: created.startDateTime || created.startDate,
          endDate: created.endDateTime || created.endDate,
          maxAttendees: created.capacity || 30,
          currentAttendees: 0,
          registrationRequired: true,
          spotsAvailable: created.capacity || 30,
          branchName: created.branchName || '',
        });
        this.applyFilter();
        this.createOpen = false;
        this.creating = false;
        this.newEvent = { title: '', description: '', eventType: 'WORKSHOP', branchId: this.branches[0]?.id || 1, startDateTime: '', endDateTime: '', capacity: 30 };
      },
      error: (err: any) => {
        this.createError = err.error?.message || 'Failed to create event.';
        this.creating = false;
      }
    });
  }
}

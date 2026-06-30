import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { EventService } from '../../shared/services/event.service';
import { BranchService } from '../../shared/services/branch.service';
import { LibraryEvent, EventType } from '../../shared/models/event.model';
import { Branch } from '../../shared/models/branch.model';

@Component({
  selector: 'app-events-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './events-management.component.html'
})
export class EventsManagementComponent implements OnInit {
  events: LibraryEvent[] = [];
  branches: Branch[] = [];
  loading: boolean = false;
  showForm: boolean = false;
  editingEvent: LibraryEvent | null = null;
  saving: boolean = false;
  saveError: string = '';
  eventTypes = Object.values(EventType);

  formData: any = {
    title: '',
    description: '',
    eventType: EventType.BOOK_CLUB,
    branchId: 0,
    startDateTime: '',
    endDateTime: '',
    capacity: 20,
  };

  constructor(
    private eventService: EventService,
    private branchService: BranchService
  ) {}

  ngOnInit(): void {
    this.loadEvents();
    this.branchService.getBranches().subscribe({
      next: (branches) => this.branches = branches,
      error: (err) => console.error('Failed to load branches', err)
    });
  }

  loadEvents(): void {
    this.loading = true;
    this.eventService.getEvents().subscribe({
      next: (rawEvents: any[]) => {
        this.events = rawEvents.map(e => ({
          ...e,
          type: e.eventType || e.type,
          startDate: e.startDateTime || e.startDate,
          endDate: e.endDateTime || e.endDate,
          maxAttendees: e.capacity || e.maxAttendees || 0,
          currentAttendees: e.registeredCount || e.currentAttendees || 0,
          registrationRequired: e.registrationRequired ?? true,
          spotsAvailable: e.spotsAvailable ?? 0,
        }));
        this.loading = false;
      },
      error: (err) => { console.error(err); this.loading = false; }
    });
  }

  openCreateForm(): void {
    this.editingEvent = null;
    this.formData = {
      title: '', description: '', eventType: EventType.BOOK_CLUB,
      branchId: this.branches[0]?.id || 0, startDateTime: '', endDateTime: '',
      capacity: 20,
    };
    this.showForm = true;
    this.saveError = '';
  }

  openEditForm(event: LibraryEvent): void {
    this.editingEvent = event;
    this.formData = {
      title: event.title,
      description: event.description || '',
      eventType: event.type,
      branchId: event.branchId,
      startDateTime: event.startDate,
      endDateTime: event.endDate,
      capacity: event.maxAttendees,
    };
    this.showForm = true;
    this.saveError = '';
  }

  saveEvent(form?: NgForm): void {
    if (form && form.invalid) {
      form.form.markAllAsTouched();
      return;
    }
    this.saving = true;
    this.saveError = '';
    const payload = {
      title: this.formData.title,
      description: this.formData.description,
      eventType: this.formData.eventType,
      branchId: this.formData.branchId,
      startDateTime: this.formData.startDateTime,
      endDateTime: this.formData.endDateTime,
      capacity: this.formData.capacity,
    };
    const obs = this.editingEvent
      ? this.eventService.updateEvent(this.editingEvent.id, payload as any)
      : this.eventService.createEvent(payload as any);
    obs.subscribe({
      next: () => {
        this.saving = false;
        this.showForm = false;
        this.loadEvents();
      },
      error: (err) => {
        this.saveError = err.error?.message || 'Failed to save event.';
        this.saving = false;
      }
    });
  }

  deleteEvent(id: number): void {
    if (!confirm('Delete this event?')) return;
    this.eventService.deleteEvent(id).subscribe({
      next: () => this.events = this.events.filter(e => e.id !== id),
      error: (err) => console.error('Failed to delete event', err)
    });
  }

  cancelForm(): void {
    this.showForm = false;
    this.editingEvent = null;
    this.saveError = '';
  }
}

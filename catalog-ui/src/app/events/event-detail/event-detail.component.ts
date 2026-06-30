import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { EventService } from '../../shared/services/event.service';
import { LibraryEvent } from '../../shared/models/event.model';
import { AuthService } from '../../shared/services/auth.service';

@Component({
  selector: 'app-event-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './event-detail.component.html',
  styleUrl: './event-detail.component.css'
})
export class EventDetailComponent implements OnInit {
  event: LibraryEvent | null = null;
  loading: boolean = false;
  registering: boolean = false;
  cancelling: boolean = false;
  isRegistered: boolean = false;
  actionError: string = '';
  actionSuccess: string = '';

  get memberId(): number { return this.authService.getMemberId(); }

  constructor(
    private route: ActivatedRoute,
    private eventService: EventService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const id = Number(params.get('id'));
      if (id) this.loadEvent(id);
    });
  }

  loadEvent(id: number): void {
    this.loading = true;
    this.eventService.getEventById(id).subscribe({
      next: (raw: any) => {
        this.event = {
          ...raw,
          type: raw.eventType || raw.type,
          startDate: raw.startDateTime || raw.startDate,
          endDate: raw.endDateTime || raw.endDate,
          maxAttendees: raw.capacity || raw.maxAttendees || 0,
          currentAttendees: raw.registeredCount || raw.currentAttendees || 0,
          registrationRequired: raw.registrationRequired ?? true,
          spotsAvailable: raw.spotsAvailable ?? (raw.capacity - (raw.registeredCount || 0)),
        };
        this.loading = false;
        this.checkRegistration(this.event!.id);
      },
      error: (err) => {
        console.error('Failed to load event', err);
        this.loading = false;
      }
    });
  }

  checkRegistration(eventId: number): void {
    this.eventService.getMemberRegistrations(this.memberId).subscribe({
      next: (registrations) => {
        this.isRegistered = registrations.some(r => r.eventId === eventId);
      },
      error: () => {}
    });
  }

  registerForEvent(): void {
    if (!this.event) return;
    this.registering = true;
    this.actionError = '';
    this.actionSuccess = '';
    this.eventService.registerForEvent(this.event.id, this.memberId).subscribe({
      next: () => {
        this.isRegistered = true;
        this.registering = false;
        this.actionSuccess = 'Successfully registered for this event!';
        if (this.event) this.event.spotsAvailable--;
      },
      error: (err) => {
        this.actionError = err.error?.message || 'Registration failed.';
        this.registering = false;
      }
    });
  }

  cancelRegistration(): void {
    if (!this.event || !confirm('Cancel your registration for this event?')) return;
    this.cancelling = true;
    this.actionError = '';
    this.actionSuccess = '';
    this.eventService.cancelRegistration(this.event.id, this.memberId).subscribe({
      next: () => {
        this.isRegistered = false;
        this.cancelling = false;
        this.actionSuccess = 'Registration cancelled.';
        if (this.event) this.event.spotsAvailable++;
      },
      error: (err) => {
        this.actionError = err.error?.message || 'Cancellation failed.';
        this.cancelling = false;
      }
    });
  }

  getDuration(): string {
    if (!this.event) return '';
    const start = new Date(this.event.startDate);
    const end = new Date(this.event.endDate);
    const mins = Math.round((end.getTime() - start.getTime()) / 60000);
    if (mins < 60) return `${mins} minutes`;
    const hours = Math.floor(mins / 60);
    const rem = mins % 60;
    return rem > 0 ? `${hours}h ${rem}m` : `${hours} hour${hours > 1 ? 's' : ''}`;
  }
}

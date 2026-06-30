import { TestBed, discardPeriodicTasks, fakeAsync, flush, tick } from '@angular/core/testing';
import { ToastService, Toast } from './toast.service';

describe('ToastService', () => {
  let service: ToastService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ToastService);
  });

  afterEach(() => {
    service.dismissAll();
  });

  describe('success()', () => {
    it('adds a toast with type "success"', () => {
      service.success('Operation completed');
      expect(service.toasts.length).toBe(1);
      expect(service.toasts[0].type).toBe('success');
    });

    it('uses default title "Success"', () => {
      service.success('Operation completed');
      expect(service.toasts[0].title).toBe('Success');
    });

    it('uses default duration of 3000ms', () => {
      service.success('Operation completed');
      expect(service.toasts[0].duration).toBe(3000);
    });

    it('accepts a custom title', () => {
      service.success('Operation completed', 'Custom Title');
      expect(service.toasts[0].title).toBe('Custom Title');
    });

    it('accepts a custom duration', () => {
      service.success('Operation completed', 'Success', 1000);
      expect(service.toasts[0].duration).toBe(1000);
    });

    it('sets the message correctly', () => {
      service.success('My success message');
      expect(service.toasts[0].message).toBe('My success message');
    });
  });

  describe('error()', () => {
    it('adds a toast with type "error"', () => {
      service.error('Something went wrong');
      expect(service.toasts.length).toBe(1);
      expect(service.toasts[0].type).toBe('error');
    });

    it('uses a default duration of 5000ms', () => {
      service.error('Something went wrong');
      expect(service.toasts[0].duration).toBe(5000);
    });

    it('uses default title "Error"', () => {
      service.error('Something went wrong');
      expect(service.toasts[0].title).toBe('Error');
    });
  });

  describe('warning()', () => {
    it('adds a toast with type "warning"', () => {
      service.warning('Proceed with caution');
      expect(service.toasts.length).toBe(1);
      expect(service.toasts[0].type).toBe('warning');
    });

    it('uses default duration of 4000ms', () => {
      service.warning('Proceed with caution');
      expect(service.toasts[0].duration).toBe(4000);
    });

    it('uses default title "Warning"', () => {
      service.warning('Proceed with caution');
      expect(service.toasts[0].title).toBe('Warning');
    });
  });

  describe('info()', () => {
    it('adds a toast with type "info"', () => {
      service.info('Just so you know');
      expect(service.toasts.length).toBe(1);
      expect(service.toasts[0].type).toBe('info');
    });

    it('uses default duration of 3000ms', () => {
      service.info('Just so you know');
      expect(service.toasts[0].duration).toBe(3000);
    });

    it('uses default title "Info"', () => {
      service.info('Just so you know');
      expect(service.toasts[0].title).toBe('Info');
    });
  });

  describe('toast accumulation', () => {
    it('accumulates multiple toasts without clearing previous ones', () => {
      service.success('First');
      service.error('Second');
      service.warning('Third');
      expect(service.toasts.length).toBe(3);
    });

    it('preserves insertion order', () => {
      service.success('First');
      service.error('Second');
      service.info('Third');
      expect(service.toasts[0].message).toBe('First');
      expect(service.toasts[1].message).toBe('Second');
      expect(service.toasts[2].message).toBe('Third');
    });

    it('assigns a unique id to each toast', () => {
      service.success('First');
      service.error('Second');
      const ids = service.toasts.map(t => t.id);
      expect(new Set(ids).size).toBe(ids.length);
    });

    it('sets dismissible to true for all toasts', () => {
      service.success('First');
      service.error('Second');
      service.toasts.forEach(t => expect(t.dismissible).toBeTrue());
    });
  });

  describe('dismiss()', () => {
    it('removes a specific toast by id', () => {
      service.success('First');
      service.error('Second');
      const idToRemove = service.toasts[0].id;
      service.dismiss(idToRemove);
      expect(service.toasts.length).toBe(1);
      expect(service.toasts.find(t => t.id === idToRemove)).toBeUndefined();
    });

    it('leaves the remaining toast intact after removal', () => {
      service.success('First');
      service.error('Second');
      const remainingId = service.toasts[1].id;
      service.dismiss(service.toasts[0].id);
      expect(service.toasts[0].id).toBe(remainingId);
    });

    it('leaves the list unchanged when given an unknown id', () => {
      service.success('First');
      service.error('Second');
      service.dismiss('non-existent-id');
      expect(service.toasts.length).toBe(2);
    });

    it('results in an empty list when the only toast is dismissed', () => {
      service.info('Only one');
      const id = service.toasts[0].id;
      service.dismiss(id);
      expect(service.toasts.length).toBe(0);
    });
  });

  describe('dismissAll()', () => {
    it('clears all toasts', () => {
      service.success('First');
      service.error('Second');
      service.warning('Third');
      service.dismissAll();
      expect(service.toasts.length).toBe(0);
    });

    it('is a no-op when there are no toasts', () => {
      expect(() => service.dismissAll()).not.toThrow();
      expect(service.toasts.length).toBe(0);
    });
  });

  describe('toasts getter', () => {
    it('returns the current array of toasts', () => {
      service.success('Hello');
      const toasts: Toast[] = service.toasts;
      expect(Array.isArray(toasts)).toBeTrue();
      expect(toasts.length).toBe(1);
      expect(toasts[0].message).toBe('Hello');
    });

    it('returns an empty array before any toasts are added', () => {
      expect(service.toasts).toEqual([]);
    });
  });

  describe('toasts$ observable', () => {
    it('emits the current list when subscribed', fakeAsync(() => {
      service.success('Initial');
      let received: Toast[] = [];
      const sub = service.toasts$.subscribe(toasts => (received = toasts));
      expect(received.length).toBe(1);
      expect(received[0].message).toBe('Initial');
      sub.unsubscribe();
      flush();
    }));

    it('emits an updated list when a toast is added', fakeAsync(() => {
      const emitted: Toast[][] = [];
      const sub = service.toasts$.subscribe(toasts => emitted.push([...toasts]));

      service.success('First');
      tick();
      service.error('Second');
      tick();

      expect(emitted.length).toBeGreaterThanOrEqual(3);
      const lastEmission = emitted[emitted.length - 1];
      expect(lastEmission.length).toBe(2);
      expect(lastEmission[0].message).toBe('First');
      expect(lastEmission[1].message).toBe('Second');

      sub.unsubscribe();
      flush(); // drain auto-dismiss timers so none are left pending
    }));

    it('emits an updated list when a toast is dismissed', () => {
      service.success('To dismiss');
      service.error('To keep');
      const idToRemove = service.toasts[0].id;

      let lastEmission: Toast[] = [];
      const sub = service.toasts$.subscribe(toasts => (lastEmission = toasts));

      service.dismiss(idToRemove);

      expect(lastEmission.length).toBe(1);
      expect(lastEmission[0].message).toBe('To keep');
      sub.unsubscribe();
    });

    it('emits an empty array after dismissAll()', () => {
      service.success('One');
      service.error('Two');

      let lastEmission: Toast[] = [new Object() as Toast];
      const sub = service.toasts$.subscribe(toasts => (lastEmission = toasts));

      service.dismissAll();

      expect(lastEmission).toEqual([]);
      sub.unsubscribe();
    });
  });

  describe('auto-dismiss with jasmine.clock()', () => {
    beforeEach(() => jasmine.clock().install());
    afterEach(() => jasmine.clock().uninstall());

    it('auto-dismisses a toast after its duration has elapsed', () => {
      service.success('Auto-dismiss me', 'Success', 3000);
      expect(service.toasts.length).toBe(1);

      jasmine.clock().tick(3001);

      expect(service.toasts.length).toBe(0);
    });

    it('does NOT auto-dismiss before the duration has elapsed', () => {
      service.success('Not yet', 'Success', 3000);
      jasmine.clock().tick(2999);
      expect(service.toasts.length).toBe(1);
    });

    it('auto-dismisses each toast according to its own duration', () => {
      service.success('Short-lived', 'Success', 3000);
      service.error('Long-lived', 'Error', 5000);

      jasmine.clock().tick(3001);
      expect(service.toasts.length).toBe(1);
      expect(service.toasts[0].message).toBe('Long-lived');

      jasmine.clock().tick(2000);
      expect(service.toasts.length).toBe(0);
    });

    it('does NOT auto-dismiss when duration is 0', () => {
      service.success('Persistent', 'Success', 0);
      jasmine.clock().tick(60000);
      expect(service.toasts.length).toBe(1);
    });

    it('does not throw when auto-dismiss fires for an already-dismissed toast', () => {
      service.success('Manual dismiss', 'Success', 3000);
      const id = service.toasts[0].id;
      service.dismiss(id);
      expect(() => jasmine.clock().tick(3001)).not.toThrow();
      expect(service.toasts.length).toBe(0);
    });
  });
});

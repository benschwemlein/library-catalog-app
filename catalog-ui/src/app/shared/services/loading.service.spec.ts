import { TestBed } from '@angular/core/testing';
import { LoadingService } from './loading.service';

describe('LoadingService', () => {
  let service: LoadingService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(LoadingService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  // 1. Initially not loading
  describe('initial state', () => {
    it('should not be loading initially', () => {
      expect(service.isLoading).toBeFalse();
    });

    it('isLoading$ should emit false initially', (done) => {
      service.isLoading$.subscribe((value) => {
        expect(value).toBeFalse();
        done();
      });
    });
  });

  // 2. show() sets isLoading to true
  describe('show()', () => {
    it('should set isLoading to true when show() is called', () => {
      service.show();
      expect(service.isLoading).toBeTrue();
    });

    it('isLoading$ should emit true after show() is called', (done) => {
      const emitted: boolean[] = [];
      service.isLoading$.subscribe((value) => emitted.push(value));

      service.show();

      expect(emitted).toContain(true);
      done();
    });
  });

  // 3. Two show() calls — still loading after one hide()
  describe('multiple show() calls', () => {
    it('should still be loading after one hide() when show() was called twice', () => {
      service.show();
      service.show();
      service.hide();
      expect(service.isLoading).toBeTrue();
    });
  });

  // 4. hide() after all show() calls — isLoading becomes false
  describe('hide() matching all show() calls', () => {
    it('should set isLoading to false after hiding all pending show() calls', () => {
      service.show();
      service.show();
      service.show();
      service.hide();
      service.hide();
      service.hide();
      expect(service.isLoading).toBeFalse();
    });

    it('isLoading$ should emit false after all show() calls are balanced by hide()', (done) => {
      const emitted: boolean[] = [];
      service.isLoading$.subscribe((value) => emitted.push(value));

      service.show();
      service.hide();

      expect(emitted).toEqual([false, true, false]);
      done();
    });
  });

  // 5. hide() below zero does not go negative
  describe('hide() below zero', () => {
    it('should not go negative when hide() is called more times than show()', () => {
      service.hide();
      service.hide();
      expect(service.isLoading).toBeFalse();
    });

    it('should recover correctly after excess hide() calls when show() is eventually called', () => {
      service.hide();
      service.hide();
      service.show();
      expect(service.isLoading).toBeTrue();
      service.hide();
      expect(service.isLoading).toBeFalse();
    });
  });

  // 6. reset() clears all pending counts
  describe('reset()', () => {
    it('should set isLoading to false when reset() is called with pending show() calls', () => {
      service.show();
      service.show();
      service.show();
      service.reset();
      expect(service.isLoading).toBeFalse();
    });

    it('should allow normal show()/hide() cycle after reset()', () => {
      service.show();
      service.show();
      service.reset();

      service.show();
      expect(service.isLoading).toBeTrue();

      service.hide();
      expect(service.isLoading).toBeFalse();
    });

    it('should be a no-op when already not loading', () => {
      const emitted: boolean[] = [];
      service.isLoading$.subscribe((value) => emitted.push(value));

      service.reset();

      expect(emitted).toEqual([false]);
    });
  });

  // 7. isLoading$ emits only on changes (distinctUntilChanged)
  describe('isLoading$ with distinctUntilChanged', () => {
    it('should emit only once when show() is called multiple times in a row', (done) => {
      const emitted: boolean[] = [];
      service.isLoading$.subscribe((value) => emitted.push(value));

      service.show();
      service.show();
      service.show();

      expect(emitted).toEqual([false, true]);
      done();
    });

    it('should emit only once when hide() is called multiple times after reaching zero', (done) => {
      const emitted: boolean[] = [];
      service.isLoading$.subscribe((value) => emitted.push(value));

      service.show();
      service.hide();
      service.hide();
      service.hide();

      expect(emitted).toEqual([false, true, false]);
      done();
    });
  });

  // 8. isLoading getter reflects current state
  describe('isLoading getter', () => {
    it('should return false when not loading', () => {
      expect(service.isLoading).toBeFalse();
    });

    it('should return true after show() is called', () => {
      service.show();
      expect(service.isLoading).toBeTrue();
    });

    it('should return false after show() then hide()', () => {
      service.show();
      service.hide();
      expect(service.isLoading).toBeFalse();
    });

    it('should return false after reset()', () => {
      service.show();
      service.show();
      service.reset();
      expect(service.isLoading).toBeFalse();
    });

    it('should match the latest value emitted by isLoading$', (done) => {
      let lastEmitted: boolean | null = null;
      service.isLoading$.subscribe((value) => (lastEmitted = value));

      service.show();
      expect(service.isLoading).toBe(lastEmitted as unknown as boolean);

      service.hide();
      expect(service.isLoading).toBe(lastEmitted as unknown as boolean);
      done();
    });
  });
});

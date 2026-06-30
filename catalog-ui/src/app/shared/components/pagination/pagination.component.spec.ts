import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SimpleChange, SimpleChanges } from '@angular/core';
import { PaginationComponent } from './pagination.component';

describe('PaginationComponent', () => {
  let component: PaginationComponent;
  let fixture: ComponentFixture<PaginationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PaginationComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(PaginationComponent);
    component = fixture.componentInstance;
  });

  /**
   * Helper: set inputs and trigger ngOnChanges so totalPages and
   * pageNumbers are computed exactly as they would be by Angular.
   */
  function setInputs(opts: {
    totalItems?: number;
    pageSize?: number;
    currentPage?: number;
    maxVisiblePages?: number;
  }): void {
    const changes: SimpleChanges = {};

    if (opts.totalItems !== undefined) {
      changes['totalItems'] = new SimpleChange(component.totalItems, opts.totalItems, false);
      component.totalItems = opts.totalItems;
    }
    if (opts.pageSize !== undefined) {
      changes['pageSize'] = new SimpleChange(component.pageSize, opts.pageSize, false);
      component.pageSize = opts.pageSize;
    }
    if (opts.currentPage !== undefined) {
      changes['currentPage'] = new SimpleChange(component.currentPage, opts.currentPage, false);
      component.currentPage = opts.currentPage;
    }
    if (opts.maxVisiblePages !== undefined) {
      changes['maxVisiblePages'] = new SimpleChange(
        component.maxVisiblePages,
        opts.maxVisiblePages,
        false
      );
      component.maxVisiblePages = opts.maxVisiblePages;
    }

    component.ngOnChanges(changes);
  }

  // ---------------------------------------------------------------------------
  // getPageNumbers()
  // ---------------------------------------------------------------------------

  describe('getPageNumbers()', () => {
    it('test 1: returns all pages when total pages is fewer than maxVisiblePages', () => {
      setInputs({ totalItems: 30, pageSize: 10, currentPage: 1, maxVisiblePages: 5 });
      expect(component.pageNumbers).toEqual([1, 2, 3]);
    });

    it('test 2: returns first N pages when currentPage is near the start', () => {
      setInputs({ totalItems: 200, pageSize: 10, currentPage: 1, maxVisiblePages: 5 });
      expect(component.pageNumbers).toEqual([1, 2, 3, 4, 5]);
    });

    it('test 3: centers the window around currentPage when in the middle', () => {
      setInputs({ totalItems: 200, pageSize: 10, currentPage: 10, maxVisiblePages: 5 });
      expect(component.pageNumbers).toEqual([8, 9, 10, 11, 12]);
    });

    it('test 4: returns last N pages when currentPage is near the end', () => {
      setInputs({ totalItems: 200, pageSize: 10, currentPage: 20, maxVisiblePages: 5 });
      expect(component.pageNumbers).toEqual([16, 17, 18, 19, 20]);
    });

    it('test 5: returns all pages without windowing when total equals maxVisiblePages', () => {
      setInputs({ totalItems: 50, pageSize: 10, currentPage: 3, maxVisiblePages: 5 });
      expect(component.pageNumbers).toEqual([1, 2, 3, 4, 5]);
    });
  });

  // ---------------------------------------------------------------------------
  // goToPage()
  // ---------------------------------------------------------------------------

  describe('goToPage()', () => {
    beforeEach(() => {
      setInputs({ totalItems: 100, pageSize: 10, currentPage: 5, maxVisiblePages: 5 });
    });

    it('test 6: emits pageChange with the correct page number', () => {
      const emitted: number[] = [];
      component.pageChange.subscribe((p: number) => emitted.push(p));
      component.goToPage(3);
      expect(emitted).toEqual([3]);
    });

    it('test 7: does NOT emit when page equals currentPage', () => {
      const emitted: number[] = [];
      component.pageChange.subscribe((p: number) => emitted.push(p));
      component.goToPage(5);
      expect(emitted).toEqual([]);
    });

    it('test 8: does NOT emit when page is less than 1', () => {
      const emitted: number[] = [];
      component.pageChange.subscribe((p: number) => emitted.push(p));
      component.goToPage(0);
      component.goToPage(-1);
      expect(emitted).toEqual([]);
    });

    it('test 9: does NOT emit when page exceeds totalPages', () => {
      const emitted: number[] = [];
      component.pageChange.subscribe((p: number) => emitted.push(p));
      component.goToPage(11);
      expect(emitted).toEqual([]);
    });
  });

  // ---------------------------------------------------------------------------
  // totalPages
  // ---------------------------------------------------------------------------

  describe('totalPages', () => {
    it('test 10: calculates totalPages as Math.ceil(totalItems / pageSize)', () => {
      setInputs({ totalItems: 95, pageSize: 10 });
      expect(component.totalPages).toBe(10);

      setInputs({ totalItems: 100, pageSize: 10 });
      expect(component.totalPages).toBe(10);

      setInputs({ totalItems: 1, pageSize: 10 });
      expect(component.totalPages).toBe(1);

      setInputs({ totalItems: 0, pageSize: 10 });
      expect(component.totalPages).toBe(0);
    });
  });

  // ---------------------------------------------------------------------------
  // showLeadingEllipsis
  // ---------------------------------------------------------------------------

  describe('showLeadingEllipsis', () => {
    it('test 11: is true when the first visible page is greater than 1', () => {
      setInputs({ totalItems: 200, pageSize: 10, currentPage: 18, maxVisiblePages: 5 });
      expect(component.pageNumbers[0]).toBeGreaterThan(1);
      expect(component.showLeadingEllipsis).toBeTrue();
    });

    it('test 12: is false when the first visible page is 1', () => {
      setInputs({ totalItems: 200, pageSize: 10, currentPage: 1, maxVisiblePages: 5 });
      expect(component.pageNumbers[0]).toBe(1);
      expect(component.showLeadingEllipsis).toBeFalse();
    });
  });

  // ---------------------------------------------------------------------------
  // showTrailingEllipsis
  // ---------------------------------------------------------------------------

  describe('showTrailingEllipsis', () => {
    it('test 13: is true when the last visible page is less than totalPages', () => {
      setInputs({ totalItems: 200, pageSize: 10, currentPage: 1, maxVisiblePages: 5 });
      const lastVisible = component.pageNumbers[component.pageNumbers.length - 1];
      expect(lastVisible).toBeLessThan(component.totalPages);
      expect(component.showTrailingEllipsis).toBeTrue();
    });

    it('test 14: is false when the last visible page equals totalPages', () => {
      setInputs({ totalItems: 200, pageSize: 10, currentPage: 20, maxVisiblePages: 5 });
      const lastVisible = component.pageNumbers[component.pageNumbers.length - 1];
      expect(lastVisible).toBe(component.totalPages);
      expect(component.showTrailingEllipsis).toBeFalse();
    });
  });
});

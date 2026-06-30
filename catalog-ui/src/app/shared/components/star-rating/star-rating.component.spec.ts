import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CommonModule } from '@angular/common';
import { StarRatingComponent } from './star-rating.component';

describe('StarRatingComponent', () => {
  let component: StarRatingComponent;
  let fixture: ComponentFixture<StarRatingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StarRatingComponent, CommonModule],
    }).compileComponents();

    fixture = TestBed.createComponent(StarRatingComponent);
    component = fixture.componentInstance;
  });

  // ---------------------------------------------------------------------------
  // buildStars / ngOnChanges
  // ---------------------------------------------------------------------------

  describe('buildStars / ngOnChanges', () => {
    it('1. rating=3 produces first 3 stars filled and last 2 empty', () => {
      component.rating = 3;
      component.ngOnChanges({} as any);

      expect(component.stars.length).toBe(5);
      expect(component.stars[0].state).toBe('filled');
      expect(component.stars[1].state).toBe('filled');
      expect(component.stars[2].state).toBe('filled');
      expect(component.stars[3].state).toBe('empty');
      expect(component.stars[4].state).toBe('empty');
    });

    it('2. rating=3.5 produces first 3 filled, 4th half, 5th empty', () => {
      component.rating = 3.5;
      component.ngOnChanges({} as any);

      expect(component.stars[0].state).toBe('filled');
      expect(component.stars[1].state).toBe('filled');
      expect(component.stars[2].state).toBe('filled');
      expect(component.stars[3].state).toBe('half');
      expect(component.stars[4].state).toBe('empty');
    });

    it('3. rating=0 produces all stars empty', () => {
      component.rating = 0;
      component.ngOnChanges({} as any);

      expect(component.stars.every(s => s.state === 'empty')).toBeTrue();
    });

    it('4. rating=5 produces all stars filled', () => {
      component.rating = 5;
      component.ngOnChanges({} as any);

      expect(component.stars.every(s => s.state === 'filled')).toBeTrue();
    });

    it('5. maxStars=3 produces exactly 3 stars', () => {
      component.maxStars = 3;
      component.rating = 2;
      component.ngOnChanges({} as any);

      expect(component.stars.length).toBe(3);
    });
  });

  // ---------------------------------------------------------------------------
  // onMouseEnter
  // ---------------------------------------------------------------------------

  describe('onMouseEnter', () => {
    it('6. hovering star index 4 marks stars 1-4 as filled and star 5 as empty', () => {
      component.rating = 2;
      component.readonly = false;
      component.ngOnChanges({} as any);

      component.onMouseEnter(4);

      expect(component.stars[0].state).toBe('filled');
      expect(component.stars[1].state).toBe('filled');
      expect(component.stars[2].state).toBe('filled');
      expect(component.stars[3].state).toBe('filled');
      expect(component.stars[4].state).toBe('empty');
    });

    it('7. hovering a star sets hoverRating to that star index', () => {
      component.readonly = false;
      component.ngOnChanges({} as any);

      component.onMouseEnter(3);

      expect(component.hoverRating).toBe(3);
    });

    it('8. readonly=true: hover has no effect on stars or hoverRating', () => {
      component.rating = 2;
      component.readonly = true;
      component.ngOnChanges({} as any);

      const statesBefore = component.stars.map(s => s.state);
      component.onMouseEnter(5);

      expect(component.hoverRating).toBeNull();
      component.stars.forEach((star, i) => {
        expect(star.state).toBe(statesBefore[i]);
      });
    });
  });

  // ---------------------------------------------------------------------------
  // onMouseLeave
  // ---------------------------------------------------------------------------

  describe('onMouseLeave', () => {
    it('9. leaving hover reverts stars to the original rating', () => {
      component.rating = 2;
      component.readonly = false;
      component.ngOnChanges({} as any);

      component.onMouseEnter(5);
      component.onMouseLeave();

      expect(component.hoverRating).toBeNull();
      expect(component.stars[0].state).toBe('filled');
      expect(component.stars[1].state).toBe('filled');
      expect(component.stars[2].state).toBe('empty');
      expect(component.stars[3].state).toBe('empty');
      expect(component.stars[4].state).toBe('empty');
    });

    it('10. readonly=true: mouse leave has no effect on stars', () => {
      component.rating = 3;
      component.readonly = true;
      component.ngOnChanges({} as any);

      const statesBefore = component.stars.map(s => s.state);
      component.onMouseLeave();

      component.stars.forEach((star, i) => {
        expect(star.state).toBe(statesBefore[i]);
      });
    });
  });

  // ---------------------------------------------------------------------------
  // onStarClick
  // ---------------------------------------------------------------------------

  describe('onStarClick', () => {
    it('11. clicking a star emits ratingChange with that star index', () => {
      component.readonly = false;
      component.ngOnChanges({} as any);

      const emittedValues: number[] = [];
      component.ratingChange.subscribe((v: number) => emittedValues.push(v));

      component.onStarClick(4);

      expect(emittedValues.length).toBe(1);
      expect(emittedValues[0]).toBe(4);
    });

    it('12. readonly=true: clicking a star does NOT emit ratingChange', () => {
      component.readonly = true;
      component.ngOnChanges({} as any);

      const emittedValues: number[] = [];
      component.ratingChange.subscribe((v: number) => emittedValues.push(v));

      component.onStarClick(3);

      expect(emittedValues.length).toBe(0);
    });
  });

  // ---------------------------------------------------------------------------
  // starClass
  // ---------------------------------------------------------------------------

  describe('starClass', () => {
    it("13. 'filled' star returns 'bi-star-fill text-warning'", () => {
      const star = { index: 1, state: 'filled' as const };
      expect(component.starClass(star)).toBe('bi-star-fill text-warning');
    });

    it("14. 'half' star returns 'bi-star-half text-warning'", () => {
      const star = { index: 1, state: 'half' as const };
      expect(component.starClass(star)).toBe('bi-star-half text-warning');
    });

    it("15. 'empty' star returns 'bi-star text-secondary'", () => {
      const star = { index: 1, state: 'empty' as const };
      expect(component.starClass(star)).toBe('bi-star text-secondary');
    });
  });
});

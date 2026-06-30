import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';

interface Star {
  index: number;
  state: 'filled' | 'half' | 'empty';
}

@Component({
  selector: 'app-star-rating',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './star-rating.component.html',
})
export class StarRatingComponent implements OnChanges {
  @Input() rating = 0;
  @Input() readonly = false;
  @Input() maxStars = 5;

  @Output() ratingChange = new EventEmitter<number>();

  stars: Star[] = [];
  hoverRating: number | null = null;

  ngOnChanges(_changes: SimpleChanges): void {
    this.buildStars(this.rating);
  }

  private buildStars(currentRating: number): void {
    this.stars = Array.from({ length: this.maxStars }, (_, i) => {
      const index = i + 1;
      let state: Star['state'];
      if (currentRating >= index) {
        state = 'filled';
      } else if (currentRating >= index - 0.5) {
        state = 'half';
      } else {
        state = 'empty';
      }
      return { index, state };
    });
  }

  onMouseEnter(starIndex: number): void {
    if (this.readonly) return;
    this.hoverRating = starIndex;
    this.buildStars(starIndex);
  }

  onMouseLeave(): void {
    if (this.readonly) return;
    this.hoverRating = null;
    this.buildStars(this.rating);
  }

  onStarClick(starIndex: number): void {
    if (this.readonly) return;
    this.ratingChange.emit(starIndex);
  }

  starClass(star: Star): string {
    switch (star.state) {
      case 'filled': return 'bi-star-fill text-warning';
      case 'half':   return 'bi-star-half text-warning';
      default:       return 'bi-star text-secondary';
    }
  }
}

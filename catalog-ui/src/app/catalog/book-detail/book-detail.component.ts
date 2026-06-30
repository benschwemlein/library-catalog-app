import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { BookService } from '../../shared/services/book.service';
import { HoldService } from '../../shared/services/hold.service';
import { LoanService } from '../../shared/services/loan.service';
import { AuthService } from '../../shared/services/auth.service';
import { Book } from '../../shared/models/book.model';
import { BookReview } from '../../shared/models/review.model';

@Component({
  selector: 'app-book-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './book-detail.component.html',
  styleUrl: './book-detail.component.css'
})
export class BookDetailComponent implements OnInit {
  book: Book | null = null;
  copies: any[] = [];
  reviews: BookReview[] = [];
  loading: boolean = false;
  copiesLoading: boolean = false;
  holdLoading: boolean = false;
  holdSuccess: boolean = false;
  holdError: string = '';

  checkoutOpen: boolean = false;
  checkoutLoading: boolean = false;
  checkoutSuccess: boolean = false;
  checkoutError: string = '';

  newReview = { rating: 5, title: '', body: '' };
  reviewSubmitting: boolean = false;
  reviewError: string = '';
  reviewSuccess: boolean = false;
  get memberId(): number { return this.authService.getMemberId(); }

  ratingStars: number[] = [1, 2, 3, 4, 5];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private bookService: BookService,
    private holdService: HoldService,
    private loanService: LoanService,
    private http: HttpClient,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const id = Number(params.get('id'));
      if (id) this.loadBook(id);
    });
  }

  loadBook(id: number): void {
    this.loading = true;
    this.bookService.getBookById(id).subscribe({
      next: (raw: any) => {
        this.book = {
          ...raw,
          authorNames: (raw.authors || []).map((a: any) => `${a.firstName} ${a.lastName}`),
          publisherName: raw.publisher?.name || '',
          genreNames: (raw.genres || []).map((g: any) => g.name),
          subjectNames: (raw.subjects || []),
          availableCopies: 0,
          totalCopies: 0,
        };
        this.loading = false;
        this.loadCopies(id);
        this.loadReviews(id);
      },
      error: (err) => {
        console.error('Failed to load book', err);
        this.loading = false;
      }
    });
  }

  loadCopies(bookId: number): void {
    this.copiesLoading = true;
    this.bookService.getBookCopies(bookId).subscribe({
      next: (rawCopies: any[]) => {
        this.copies = rawCopies.map(c => ({
          ...c,
          branchName: c.branch?.name || '',
        }));
        if (this.book) {
          this.book.totalCopies = rawCopies.length;
          this.book.availableCopies = rawCopies.filter(c => c.status === 'AVAILABLE').length;
        }
        this.copiesLoading = false;
      },
      error: (err) => {
        console.error('Failed to load copies', err);
        this.copiesLoading = false;
      }
    });
  }

  loadReviews(bookId: number): void {
    this.bookService.getBookReviews(bookId).subscribe({
      next: (rawReviews: any[]) => {
        this.reviews = rawReviews.map(r => ({
          ...r,
          memberName: `${r.member?.user?.firstName || ''} ${r.member?.user?.lastName || ''}`.trim() || 'Anonymous',
          body: r.reviewText,
          createdAt: r.reviewDate,
          bookId: r.book?.id,
          bookTitle: r.book?.title || '',
          verified: r.approved,
          helpfulCount: 0,
        }));
      },
      error: (err) => console.error('Failed to load reviews', err)
    });
  }

  placeHold(): void {
    if (!this.book) return;
    this.holdLoading = true;
    this.holdError = '';
    this.holdSuccess = false;
    this.holdService.placeHold({ bookId: this.book.id, memberId: this.memberId, pickupBranchId: 1 }).subscribe({
      next: () => {
        this.holdSuccess = true;
        this.holdLoading = false;
      },
      error: (err) => {
        this.holdError = err.error?.message || 'Failed to place hold.';
        this.holdLoading = false;
      }
    });
  }

  openCheckout(): void {
    this.checkoutOpen = true;
    this.checkoutSuccess = false;
    this.checkoutError = '';
  }

  doCheckout(): void {
    const firstAvailable = this.copies.find(c => c.status === 'AVAILABLE');
    if (!firstAvailable) { this.checkoutError = 'No available copies.'; return; }
    this.checkoutLoading = true;
    this.checkoutError = '';
    this.loanService.checkoutByIds(this.memberId, firstAvailable.id).subscribe({
      next: () => {
        this.checkoutSuccess = true;
        this.checkoutLoading = false;
        this.checkoutOpen = false;
        if (this.book) this.book.availableCopies = Math.max(0, this.book.availableCopies - 1);
        const idx = this.copies.findIndex(c => c.barcode === firstAvailable.barcode);
        if (idx !== -1) this.copies[idx] = { ...this.copies[idx], status: 'CHECKED_OUT' };
      },
      error: (err) => {
        this.checkoutError = err.error?.message || 'Checkout failed.';
        this.checkoutLoading = false;
      }
    });
  }

  submitReview(): void {
    if (!this.book) return;
    this.reviewSubmitting = true;
    this.reviewError = '';
    const payload = {
      memberId: this.memberId,
      rating: this.newReview.rating,
      title: this.newReview.title,
      reviewText: this.newReview.body
    };
    this.http.post<any>(`http://localhost:8080/api/books/${this.book.id}/reviews`, payload).subscribe({
      next: () => {
        this.reviewSuccess = true;
        this.reviewSubmitting = false;
        this.newReview = { rating: 0, title: '', body: '' };
        this.loadReviews(this.book!.id);
      },
      error: (err) => {
        this.reviewError = err.error?.message || 'Failed to submit review.';
        this.reviewSubmitting = false;
      }
    });
  }

  getStarArray(rating: number): number[] {
    return Array(5).fill(0).map((_, i) => i < Math.round(rating) ? 1 : 0);
  }

  goBack(): void {
    this.router.navigate(['/books']);
  }
}

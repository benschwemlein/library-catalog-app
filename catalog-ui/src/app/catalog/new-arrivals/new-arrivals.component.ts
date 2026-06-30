import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { BookService } from '../../shared/services/book.service';
import { BookSummary } from '../../shared/models/book.model';

@Component({
  selector: 'app-new-arrivals',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './new-arrivals.component.html',
  styleUrl: './new-arrivals.component.css'
})
export class NewArrivalsComponent implements OnInit {
  books: BookSummary[] = [];
  loading: boolean = false;
  error: string = '';

  constructor(private bookService: BookService) {}

  ngOnInit(): void {
    this.loadNewArrivals();
  }

  loadNewArrivals(): void {
    this.loading = true;
    this.bookService.getNewArrivals(30).subscribe({
      next: (books) => {
        this.books = books;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load new arrivals.';
        console.error(err);
        this.loading = false;
      }
    });
  }
}

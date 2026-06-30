import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { BookService } from '../../shared/services/book.service';
import { Book } from '../../shared/models/book.model';

@Component({
  selector: 'app-book-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './book-list.component.html',
  styleUrl: './book-list.component.css'
})
export class BookListComponent implements OnInit {
  allBooks: Book[] = [];
  books: Book[] = [];
  searchQuery: string = '';
  loading: boolean = false;
  totalElements: number = 0;
  isSearchMode: boolean = false;

  constructor(private bookService: BookService) {}

  ngOnInit(): void {
    this.loadBooks();
  }

  loadBooks(): void {
    this.loading = true;
    this.isSearchMode = false;
    this.bookService.getBooks().subscribe({
      next: (books) => {
        this.allBooks = books;
        this.books = books;
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load books', err);
        this.loading = false;
      }
    });
  }

  search(): void {
    if (!this.searchQuery.trim()) {
      this.books = this.allBooks;
      this.isSearchMode = false;
      return;
    }
    const q = this.searchQuery.toLowerCase();
    this.books = this.allBooks.filter(b =>
      b.title.toLowerCase().includes(q) ||
      b.authorNames?.some(a => a.toLowerCase().includes(q)) ||
      b.isbn?.toLowerCase().includes(q)
    );
    this.totalElements = this.books.length;
    this.isSearchMode = true;
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.books = this.allBooks;
    this.isSearchMode = false;
  }
}

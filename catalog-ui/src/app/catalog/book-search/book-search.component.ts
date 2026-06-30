import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { BookService } from '../../shared/services/book.service';
import { SearchService } from '../../shared/services/search.service';
import { Book } from '../../shared/models/book.model';
import { AdvancedSearchRequest } from '../../shared/models/search.model';

interface FilteredResult {
  books: Book[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
}

@Component({
  selector: 'app-book-search',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './book-search.component.html',
  styleUrl: './book-search.component.css'
})
export class BookSearchComponent implements OnInit {
  searchRequest: AdvancedSearchRequest = {
    title: '',
    author: '',
    isbn: '',
    genre: '',
    language: '',
    yearFrom: undefined,
    yearTo: undefined,
    availableOnly: false,
    page: 0,
    size: 20
  };

  results: FilteredResult | null = null;
  genres: string[] = [];
  languages: string[] = [];
  loading: boolean = false;
  searched: boolean = false;

  private allBooks: Book[] = [];

  constructor(
    private bookService: BookService,
    private searchService: SearchService
  ) {}

  ngOnInit(): void {
    this.bookService.getBooks().subscribe({
      next: (books) => this.allBooks = books,
      error: (err) => console.error('Failed to load books', err)
    });
    this.searchService.getGenres().subscribe({
      next: (genres) => this.genres = genres,
      error: (err) => console.error('Failed to load genres', err)
    });
    this.searchService.getLanguages().subscribe({
      next: (languages) => this.languages = languages,
      error: (err) => console.error('Failed to load languages', err)
    });
  }

  search(): void {
    this.loading = true;
    this.searched = true;
    const r = this.searchRequest;

    let filtered = [...this.allBooks];

    if (r.title?.trim()) {
      const t = r.title.toLowerCase();
      filtered = filtered.filter(b => b.title.toLowerCase().includes(t));
    }
    if (r.author?.trim()) {
      const a = r.author.toLowerCase();
      filtered = filtered.filter(b => b.authorNames?.some(n => n.toLowerCase().includes(a)));
    }
    if (r.isbn?.trim()) {
      filtered = filtered.filter(b => b.isbn?.includes(r.isbn!.trim()));
    }
    if (r.genre) {
      filtered = filtered.filter(b => b.genreNames?.includes(r.genre!));
    }
    if (r.language) {
      filtered = filtered.filter(b => b.language === r.language);
    }
    if (r.yearFrom !== undefined && r.yearFrom !== null) {
      filtered = filtered.filter(b => b.publicationYear >= r.yearFrom!);
    }
    if (r.yearTo !== undefined && r.yearTo !== null) {
      filtered = filtered.filter(b => b.publicationYear <= r.yearTo!);
    }
    if (r.availableOnly) {
      filtered = filtered.filter(b => b.availableCopies > 0);
    }

    this.results = {
      books: filtered,
      totalElements: filtered.length,
      totalPages: 1,
      currentPage: 0,
      pageSize: filtered.length
    };
    this.loading = false;
  }

  reset(): void {
    this.searchRequest = {
      title: '', author: '', isbn: '', genre: '', language: '',
      yearFrom: undefined, yearTo: undefined, availableOnly: false, page: 0, size: 20
    };
    this.results = null;
    this.searched = false;
  }

  loadPage(_page: number): void {
    // All results are shown client-side; pagination not needed
  }
}

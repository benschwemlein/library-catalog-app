import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { of, throwError } from 'rxjs';

import { BookListComponent } from './book-list.component';
import { BookService } from '../../shared/services/book.service';

const MOCK_BOOKS = [
  {
    id: 1,
    title: 'Clean Code',
    authorNames: ['Robert C. Martin'],
    isbn: '978-0132350884'
  },
  {
    id: 2,
    title: 'The Pragmatic Programmer',
    authorNames: ['Andrew Hunt', 'David Thomas'],
    isbn: '978-0201616224'
  },
  {
    id: 3,
    title: 'Design Patterns',
    authorNames: ['Gang of Four'],
    isbn: '978-0201633610'
  }
];

describe('BookListComponent', () => {
  let component: BookListComponent;
  let fixture: ComponentFixture<BookListComponent>;
  let bookServiceSpy: jasmine.SpyObj<BookService>;

  beforeEach(async () => {
    const spy = jasmine.createSpyObj('BookService', ['getBooks']);
    spy.getBooks.and.returnValue(of([...MOCK_BOOKS]));

    await TestBed.configureTestingModule({
      imports: [BookListComponent, RouterTestingModule, FormsModule, CommonModule],
      providers: [{ provide: BookService, useValue: spy }],
    }).compileComponents();

    bookServiceSpy = TestBed.inject(BookService) as jasmine.SpyObj<BookService>;
    fixture = TestBed.createComponent(BookListComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should call getBooks() on init', () => {
    fixture.detectChanges();
    expect(bookServiceSpy.getBooks).toHaveBeenCalledTimes(1);
  });

  it('should populate books from service response', () => {
    fixture.detectChanges();
    expect(component.books.length).toBe(3);
    expect(component.allBooks.length).toBe(3);
  });

  it('should set loading to false after books load', () => {
    fixture.detectChanges();
    expect(component.loading).toBeFalse();
  });

  it('should set loading to false on error', () => {
    bookServiceSpy.getBooks.and.returnValue(throwError(() => new Error('Network error')));
    fixture.detectChanges();
    expect(component.loading).toBeFalse();
    expect(component.books.length).toBe(0);
  });

  it('search() with empty query resets to allBooks', () => {
    fixture.detectChanges();
    component.searchQuery = '';
    component.search();
    expect(component.books).toEqual(component.allBooks);
    expect(component.isSearchMode).toBeFalse();
  });

  it('search() filters books by title (case insensitive)', () => {
    fixture.detectChanges();
    component.searchQuery = 'clean';
    component.search();
    expect(component.books.length).toBe(1);
    expect(component.books[0].title).toBe('Clean Code');
    expect(component.isSearchMode).toBeTrue();
  });

  it('search() filters books by author name', () => {
    fixture.detectChanges();
    component.searchQuery = 'gang of four';
    component.search();
    expect(component.books.length).toBe(1);
    expect(component.books[0].title).toBe('Design Patterns');
  });

  it('search() filters books by isbn', () => {
    fixture.detectChanges();
    component.searchQuery = '978-0201616224';
    component.search();
    expect(component.books.length).toBe(1);
    expect(component.books[0].title).toBe('The Pragmatic Programmer');
  });

  it('search() sets totalElements to filtered count', () => {
    fixture.detectChanges();
    component.searchQuery = 'clean';
    component.search();
    expect(component.totalElements).toBe(1);
  });

  it('clearSearch() resets searchQuery, books, and isSearchMode', () => {
    fixture.detectChanges();
    component.searchQuery = 'clean';
    component.search();
    component.clearSearch();
    expect(component.searchQuery).toBe('');
    expect(component.books).toEqual(component.allBooks);
    expect(component.isSearchMode).toBeFalse();
  });

  it('search() with whitespace-only query resets to allBooks', () => {
    fixture.detectChanges();
    component.searchQuery = '   ';
    component.search();
    expect(component.books).toEqual(component.allBooks);
    expect(component.isSearchMode).toBeFalse();
  });
});

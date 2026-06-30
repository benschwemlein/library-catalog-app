import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { BookService } from './book.service';
import { Book, BookSummary, BookSearchResult, CreateBookRequest } from '../models/book.model';

describe('BookService', () => {
  let service: BookService;
  let httpMock: HttpTestingController;

  const baseUrl = 'http://localhost:8080/api/books';
  const searchUrl = 'http://localhost:8080/api/search/books';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [BookService]
    });
    service = TestBed.inject(BookService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  // ---------------------------------------------------------------------------
  // getBooks
  // ---------------------------------------------------------------------------
  describe('getBooks', () => {
    it('should GET to base URL and return an array of books', () => {
      const mockBooks: Book[] = [
        { id: 1, title: 'Clean Code', isbn: '978-0132350884', author: 'Robert C. Martin' } as unknown as Book,
        { id: 2, title: 'The Pragmatic Programmer', isbn: '978-0135957059', author: 'David Thomas' } as unknown as Book
      ];

      service.getBooks().subscribe(books => {
        expect(books.length).toBe(2);
        expect(books).toEqual(mockBooks);
      });

      const req = httpMock.expectOne(baseUrl);
      expect(req.request.method).toBe('GET');
      req.flush(mockBooks);
    });

    it('should return an empty array when the server returns no books', () => {
      service.getBooks().subscribe(books => {
        expect(books).toEqual([]);
      });

      const req = httpMock.expectOne(baseUrl);
      expect(req.request.method).toBe('GET');
      req.flush([]);
    });
  });

  // ---------------------------------------------------------------------------
  // getBookById
  // ---------------------------------------------------------------------------
  describe('getBookById', () => {
    it('should GET to /{id} and return the matching book', () => {
      const mockBook = { id: 42, title: 'Refactoring', isbn: '978-0201485677' } as unknown as Book;

      service.getBookById(42).subscribe(book => {
        expect(book).toEqual(mockBook);
      });

      const req = httpMock.expectOne(`${baseUrl}/42`);
      expect(req.request.method).toBe('GET');
      req.flush(mockBook);
    });

    it('should interpolate different id values correctly', () => {
      const mockBook = { id: 99, title: 'Design Patterns', isbn: '978-0201633610' } as unknown as Book;

      service.getBookById(99).subscribe(book => {
        expect(book.id).toBe(99);
      });

      const req = httpMock.expectOne(`${baseUrl}/99`);
      expect(req.request.method).toBe('GET');
      req.flush(mockBook);
    });
  });

  // ---------------------------------------------------------------------------
  // getBookByIsbn
  // ---------------------------------------------------------------------------
  describe('getBookByIsbn', () => {
    it('should GET to /isbn/{isbn} with a hyphenated ISBN', () => {
      const isbn = '978-0132350884';
      const mockBook = { id: 1, isbn } as unknown as Book;

      service.getBookByIsbn(isbn).subscribe(book => {
        expect(book).toEqual(mockBook);
      });

      const req = httpMock.expectOne(`${baseUrl}/isbn/${isbn}`);
      expect(req.request.method).toBe('GET');
      req.flush(mockBook);
    });

    it('should GET to /isbn/{isbn} with an ISBN that has no hyphens', () => {
      const isbn = '9780132350884';
      const mockBook = { id: 1, isbn } as unknown as Book;

      service.getBookByIsbn(isbn).subscribe(book => {
        expect(book).toEqual(mockBook);
      });

      const req = httpMock.expectOne(`${baseUrl}/isbn/${isbn}`);
      expect(req.request.method).toBe('GET');
      req.flush(mockBook);
    });
  });

  // ---------------------------------------------------------------------------
  // searchBooks
  // ---------------------------------------------------------------------------
  describe('searchBooks', () => {
    it('should GET to search URL with explicit query, page, and size params', () => {
      const mockResult: BookSearchResult = { content: [], totalElements: 0, totalPages: 0, number: 0, size: 10 } as unknown as unknown as BookSearchResult;

      service.searchBooks('angular', 2, 10).subscribe(result => {
        expect(result).toEqual(mockResult);
      });

      const req = httpMock.expectOne(r =>
        r.url === searchUrl &&
        r.params.get('q') === 'angular' &&
        r.params.get('page') === '2' &&
        r.params.get('size') === '10'
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockResult);
    });

    it('should use default page=0 and size=20 when not provided', () => {
      const mockResult: BookSearchResult = { content: [], totalElements: 0, totalPages: 1, number: 0, size: 20 } as unknown as unknown as BookSearchResult;

      service.searchBooks('typescript').subscribe(result => {
        expect(result).toEqual(mockResult);
      });

      const req = httpMock.expectOne(r =>
        r.url === searchUrl &&
        r.params.get('q') === 'typescript' &&
        r.params.get('page') === '0' &&
        r.params.get('size') === '20'
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockResult);
    });

    it('should return a result with the expected paginated shape', () => {
      const mockBooks = [{ id: 1, title: 'TypeScript Deep Dive' } as unknown as Book];
      const mockResult: BookSearchResult = {
        content: mockBooks,
        totalElements: 1,
        totalPages: 1,
        number: 0,
        size: 20
      } as unknown as unknown as BookSearchResult;

      service.searchBooks('deep dive').subscribe(result => {
        expect((result as any).content.length).toBe(1);
        expect((result as any).totalElements).toBe(1);
        expect((result as any).number).toBe(0);
      });

      const req = httpMock.expectOne(r => r.url === searchUrl);
      expect(req.request.method).toBe('GET');
      req.flush(mockResult);
    });
  });

  // ---------------------------------------------------------------------------
  // getBookCopies
  // ---------------------------------------------------------------------------
  describe('getBookCopies', () => {
    it('should GET to /{bookId}/copies and return the copies', () => {
      const mockCopies = [{ id: 1, bookId: 5, status: 'AVAILABLE' }];

      service.getBookCopies(5).subscribe(copies => {
        expect(copies).toEqual(mockCopies);
      });

      const req = httpMock.expectOne(`${baseUrl}/5/copies`);
      expect(req.request.method).toBe('GET');
      req.flush(mockCopies);
    });

    it('should return an empty array when no copies exist', () => {
      service.getBookCopies(5).subscribe(copies => {
        expect(copies).toEqual([]);
      });

      const req = httpMock.expectOne(`${baseUrl}/5/copies`);
      expect(req.request.method).toBe('GET');
      req.flush([]);
    });
  });

  // ---------------------------------------------------------------------------
  // getBookReviews
  // ---------------------------------------------------------------------------
  describe('getBookReviews', () => {
    it('should GET to /{bookId}/reviews and return the reviews', () => {
      const mockReviews = [{ id: 1, bookId: 7, rating: 5, comment: 'Excellent read.' }];

      service.getBookReviews(7).subscribe(reviews => {
        expect(reviews).toEqual(mockReviews);
      });

      const req = httpMock.expectOne(`${baseUrl}/7/reviews`);
      expect(req.request.method).toBe('GET');
      req.flush(mockReviews);
    });

    it('should return an empty array when no reviews exist', () => {
      service.getBookReviews(7).subscribe(reviews => {
        expect(reviews).toEqual([]);
      });

      const req = httpMock.expectOne(`${baseUrl}/7/reviews`);
      expect(req.request.method).toBe('GET');
      req.flush([]);
    });
  });

  // ---------------------------------------------------------------------------
  // getNewArrivals
  // ---------------------------------------------------------------------------
  describe('getNewArrivals', () => {
    it('should GET to /new-arrivals with an explicit days param', () => {
      const mockArrivals: BookSummary[] = [{ id: 3, title: 'New Book', isbn: '111' } as unknown as BookSummary];

      service.getNewArrivals(7).subscribe(arrivals => {
        expect(arrivals).toEqual(mockArrivals);
      });

      const req = httpMock.expectOne(r =>
        r.url === `${baseUrl}/new-arrivals` &&
        r.params.get('days') === '7'
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockArrivals);
    });

    it('should use default days=30 when no argument is provided', () => {
      const mockArrivals: BookSummary[] = [];

      service.getNewArrivals().subscribe(arrivals => {
        expect(arrivals).toEqual(mockArrivals);
      });

      const req = httpMock.expectOne(r =>
        r.url === `${baseUrl}/new-arrivals` &&
        r.params.get('days') === '30'
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockArrivals);
    });
  });

  // ---------------------------------------------------------------------------
  // createBook
  // ---------------------------------------------------------------------------
  describe('createBook', () => {
    it('should POST to base URL with the book payload in the request body', () => {
      const payload: CreateBookRequest = { title: 'New Book', isbn: '978-1234567890' } as unknown as CreateBookRequest;
      const mockResponse: Book = { id: 10, ...payload } as unknown as unknown as Book;

      service.createBook(payload).subscribe(book => {
        expect(book).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(baseUrl);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(payload);
      req.flush(mockResponse);
    });

    it('should verify the full payload is sent in the body', () => {
      const payload: CreateBookRequest = {
        title: 'Full Payload Book',
        isbn: '978-0000000001',
        authorId: 5,
        publishedYear: 2024,
        genreId: 3
      } as unknown as CreateBookRequest;
      const mockResponse: Book = { id: 11, ...payload } as unknown as unknown as Book;

      service.createBook(payload).subscribe(book => {
        expect(book.id).toBe(11);
      });

      const req = httpMock.expectOne(baseUrl);
      expect(req.request.method).toBe('POST');
      expect(req.request.body['title']).toBe('Full Payload Book');
      expect(req.request.body['isbn']).toBe('978-0000000001');
      expect(req.request.body['authorId']).toBe(5);
      req.flush(mockResponse);
    });
  });

  // ---------------------------------------------------------------------------
  // updateBook
  // ---------------------------------------------------------------------------
  describe('updateBook', () => {
    it('should PUT to /{id} with a partial payload', () => {
      const partial: Partial<CreateBookRequest> = { title: 'Updated Title' };
      const mockResponse: Book = { id: 20, title: 'Updated Title', isbn: '978-0000000002' } as unknown as Book;

      service.updateBook(20, partial).subscribe(book => {
        expect(book.title).toBe('Updated Title');
      });

      const req = httpMock.expectOne(`${baseUrl}/20`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(partial);
      req.flush(mockResponse);
    });

    it('should correctly interpolate a different id in the URL', () => {
      const partial: Partial<CreateBookRequest> = { isbn: '978-9999999999' };
      const mockResponse: Book = { id: 55, isbn: '978-9999999999' } as unknown as Book;

      service.updateBook(55, partial).subscribe(book => {
        expect(book.id).toBe(55);
      });

      const req = httpMock.expectOne(`${baseUrl}/55`);
      expect(req.request.method).toBe('PUT');
      req.flush(mockResponse);
    });
  });

  // ---------------------------------------------------------------------------
  // deleteBook
  // ---------------------------------------------------------------------------
  describe('deleteBook', () => {
    it('should DELETE to /{id}', () => {
      service.deleteBook(30).subscribe(response => {
        expect(response).toBeNull();
      });

      const req = httpMock.expectOne(`${baseUrl}/30`);
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });

    it('should send no request body with the DELETE request', () => {
      service.deleteBook(30).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/30`);
      expect(req.request.method).toBe('DELETE');
      expect(req.request.body).toBeNull();
      req.flush(null);
    });

    it('should correctly interpolate a different id for DELETE', () => {
      service.deleteBook(77).subscribe(response => {
        expect(response).toBeNull();
      });

      const req = httpMock.expectOne(`${baseUrl}/77`);
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });
  });
});

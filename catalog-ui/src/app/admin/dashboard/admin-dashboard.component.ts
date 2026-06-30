import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { LoanService } from '../../shared/services/loan.service';
import { BookService } from '../../shared/services/book.service';
import { MemberService } from '../../shared/services/member.service';
import { ReportService } from '../../shared/services/report.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.css'
})
export class AdminDashboardComponent implements OnInit {
  totalBooks: number = 0;
  totalMembers: number = 0;
  activeLoans: number = 0;
  overdueCount: number = 0;
  loading: boolean = false;

  constructor(
    private bookService: BookService,
    private memberService: MemberService,
    private loanService: LoanService,
    private reportService: ReportService
  ) {}

  ngOnInit(): void {
    this.loadStats();
  }

  loadStats(): void {
    this.loading = true;

    this.bookService.getBooks().subscribe({
      next: (books) => this.totalBooks = books.length,
      error: (err) => console.error('Failed to load books count', err)
    });

    this.memberService.getMembers().subscribe({
      next: (members) => this.totalMembers = members.length,
      error: (err) => console.error('Failed to load members count', err)
    });

    this.loanService.getActiveLoans().subscribe({
      next: (loans) => {
        this.activeLoans = loans.length;
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load active loans', err);
        this.loading = false;
      }
    });

    this.loanService.getOverdueLoans().subscribe({
      next: (loans) => this.overdueCount = loans.length,
      error: (err) => console.error('Failed to load overdue count', err)
    });
  }
}

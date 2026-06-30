import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReportService } from '../../shared/services/report.service';
import { MostBorrowedReport, OverdueReport, BranchStatsReport, MemberActivityReport } from '../../shared/models/report.model';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reports.component.html',
  styleUrl: './reports.component.css'
})
export class ReportsComponent implements OnInit {
  activeTab: string = 'most-borrowed';
  mostBorrowedReport: MostBorrowedReport | null = null;
  overdueReport: OverdueReport | null = null;
  branchStatsReport: BranchStatsReport | null = null;
  memberActivityReport: MemberActivityReport | null = null;
  loading: boolean = false;

  constructor(private reportService: ReportService) {}

  ngOnInit(): void {
    this.loadMostBorrowed();
  }

  setTab(tab: string): void {
    this.activeTab = tab;
    if (tab === 'most-borrowed' && !this.mostBorrowedReport) this.loadMostBorrowed();
    if (tab === 'overdue' && !this.overdueReport) this.loadOverdueReport();
    if (tab === 'branch-stats' && !this.branchStatsReport) this.loadBranchStats();
    if (tab === 'member-activity' && !this.memberActivityReport) this.loadMemberActivity();
  }

  loadMostBorrowed(): void {
    this.loading = true;
    this.reportService.getMostBorrowed().subscribe({
      next: (report) => { this.mostBorrowedReport = report; this.loading = false; },
      error: (err) => { console.error(err); this.loading = false; }
    });
  }

  loadOverdueReport(): void {
    this.loading = true;
    this.reportService.getOverdueReport().subscribe({
      next: (report) => { this.overdueReport = report; this.loading = false; },
      error: (err) => { console.error(err); this.loading = false; }
    });
  }

  loadBranchStats(): void {
    this.loading = true;
    this.reportService.getBranchStats().subscribe({
      next: (report) => { this.branchStatsReport = report; this.loading = false; },
      error: (err) => { console.error(err); this.loading = false; }
    });
  }

  loadMemberActivity(): void {
    this.loading = true;
    this.reportService.getMemberActivity().subscribe({
      next: (report) => { this.memberActivityReport = report; this.loading = false; },
      error: (err) => { console.error(err); this.loading = false; }
    });
  }

  exportReport(type: string): void {
    this.reportService.exportReport(type, 'CSV').subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `${type}-report.csv`;
        a.click();
        URL.revokeObjectURL(url);
      },
      error: (err) => console.error('Export failed', err)
    });
  }
}

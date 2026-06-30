import { Routes } from '@angular/router';
import { authGuard } from './shared/guards/auth.guard';
import { CatalogItemCreateComponent } from './pages/catalog-item-create/catalog-item-create.component';
import { CatalogItemsComponent } from './pages/catalog-items/catalog-items.component';
import { LoginComponent } from './pages/login/login.component';
import { RegisterComponent } from './pages/register/register.component';
import { BookListComponent } from './catalog/book-list/book-list.component';
import { BookDetailComponent } from './catalog/book-detail/book-detail.component';
import { BookSearchComponent } from './catalog/book-search/book-search.component';
import { NewArrivalsComponent } from './catalog/new-arrivals/new-arrivals.component';
import { MemberDashboardComponent } from './members/member-dashboard/member-dashboard.component';
import { MemberProfileComponent } from './members/member-profile/member-profile.component';
import { CurrentLoansComponent } from './members/current-loans/current-loans.component';
import { LoanHistoryComponent } from './members/loan-history/loan-history.component';
import { HoldsComponent } from './members/holds/holds.component';
import { FinesComponent } from './members/fines/fines.component';
import { NotificationsComponent } from './members/notifications/notifications.component';
import { EventListComponent } from './events/event-list/event-list.component';
import { EventDetailComponent } from './events/event-detail/event-detail.component';
import { BranchListComponent } from './branches/branch-list/branch-list.component';
import { BranchDetailComponent } from './branches/branch-detail/branch-detail.component';
import { ClubListComponent } from './book-clubs/club-list/club-list.component';
import { ClubDetailComponent } from './book-clubs/club-detail/club-detail.component';
import { DiscussionComponent } from './book-clubs/discussion/discussion.component';
import { DigitalCatalogComponent } from './digital/digital-catalog/digital-catalog.component';
import { DigitalReaderComponent } from './digital/digital-reader/digital-reader.component';
import { MyListsComponent } from './reading-lists/my-lists/my-lists.component';
import { PublicListsComponent } from './reading-lists/public-lists/public-lists.component';
import { RecommendationsComponent } from './recommendations/recommendations.component';
import { PeriodicalListComponent } from './periodicals/periodical-list/periodical-list.component';
import { ChallengeListComponent } from './challenges/challenge-list/challenge-list.component';
import { ChallengeDetailComponent } from './challenges/challenge-detail/challenge-detail.component';
import { IllRequestComponent } from './interlibrary/ill-request/ill-request.component';
import { StaffDashboardComponent } from './staff/staff-dashboard/staff-dashboard.component';
import { CheckoutComponent } from './staff/checkout/checkout.component';
import { ReturnsComponent } from './staff/returns/returns.component';
import { IllManagementComponent } from './staff/ill-management/ill-management.component';
import { AdminDashboardComponent } from './admin/dashboard/admin-dashboard.component';
import { AdminMembersComponent } from './admin/members/admin-members.component';
import { InventoryComponent } from './admin/inventory/inventory.component';
import { ReportsComponent } from './admin/reports/reports.component';
import { EventsManagementComponent } from './admin/events/events-management.component';
import { AcquisitionsComponent } from './admin/acquisitions/acquisitions.component';
import { DonationsComponent } from './admin/donations/donations.component';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'books' },

  // Public auth routes
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },

  // Books / Catalog
  { path: 'books', component: BookListComponent, canActivate: [authGuard] },
  { path: 'books/search', component: BookSearchComponent, canActivate: [authGuard] },
  { path: 'books/new-arrivals', component: NewArrivalsComponent, canActivate: [authGuard] },
  { path: 'books/:id', component: BookDetailComponent, canActivate: [authGuard] },

  // Member area
  { path: 'member', component: MemberDashboardComponent, canActivate: [authGuard] },
  { path: 'member/profile', component: MemberProfileComponent, canActivate: [authGuard] },
  { path: 'member/loans', component: CurrentLoansComponent, canActivate: [authGuard] },
  { path: 'member/loan-history', component: LoanHistoryComponent, canActivate: [authGuard] },
  { path: 'member/holds', component: HoldsComponent, canActivate: [authGuard] },
  { path: 'member/fines', component: FinesComponent, canActivate: [authGuard] },
  { path: 'member/notifications', component: NotificationsComponent, canActivate: [authGuard] },

  // Events
  { path: 'events', component: EventListComponent, canActivate: [authGuard] },
  { path: 'events/:id', component: EventDetailComponent, canActivate: [authGuard] },

  // Branches
  { path: 'branches', component: BranchListComponent, canActivate: [authGuard] },
  { path: 'branches/:id', component: BranchDetailComponent, canActivate: [authGuard] },

  // Book Clubs
  { path: 'book-clubs', component: ClubListComponent, canActivate: [authGuard] },
  { path: 'book-clubs/:id', component: ClubDetailComponent, canActivate: [authGuard] },
  { path: 'book-clubs/:id/discussion', component: DiscussionComponent, canActivate: [authGuard] },

  // Digital
  { path: 'digital', component: DigitalCatalogComponent, canActivate: [authGuard] },
  { path: 'digital/reader/:id', component: DigitalReaderComponent, canActivate: [authGuard] },

  // Reading Lists
  { path: 'reading-lists', component: MyListsComponent, canActivate: [authGuard] },
  { path: 'reading-lists/public', component: PublicListsComponent, canActivate: [authGuard] },

  // Recommendations
  { path: 'recommendations', component: RecommendationsComponent, canActivate: [authGuard] },

  // Periodicals
  { path: 'periodicals', component: PeriodicalListComponent, canActivate: [authGuard] },

  // Challenges
  { path: 'challenges', component: ChallengeListComponent, canActivate: [authGuard] },
  { path: 'challenges/:id', component: ChallengeDetailComponent, canActivate: [authGuard] },

  // Interlibrary
  { path: 'interlibrary', component: IllRequestComponent, canActivate: [authGuard] },

  // Staff
  { path: 'staff', component: StaffDashboardComponent, canActivate: [authGuard] },
  { path: 'staff/checkout', component: CheckoutComponent, canActivate: [authGuard] },
  { path: 'staff/returns', component: ReturnsComponent, canActivate: [authGuard] },
  { path: 'staff/ill', component: IllManagementComponent, canActivate: [authGuard] },

  // Admin
  { path: 'admin', component: AdminDashboardComponent, canActivate: [authGuard] },
  { path: 'admin/members', component: AdminMembersComponent, canActivate: [authGuard] },
  { path: 'admin/inventory', component: InventoryComponent, canActivate: [authGuard] },
  { path: 'admin/reports', component: ReportsComponent, canActivate: [authGuard] },
  { path: 'admin/events', component: EventsManagementComponent, canActivate: [authGuard] },
  { path: 'admin/acquisitions', component: AcquisitionsComponent, canActivate: [authGuard] },
  { path: 'admin/donations', component: DonationsComponent, canActivate: [authGuard] },

  // Legacy catalog pages
  { path: 'catalog-items', component: CatalogItemsComponent, canActivate: [authGuard] },
  { path: 'create-catalog-item', component: CatalogItemCreateComponent, canActivate: [authGuard] },
];

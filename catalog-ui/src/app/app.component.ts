import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from './shared/services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CommonModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
})
export class AppComponent implements OnInit {
  isLoggedIn = false;
  isAdmin = false;
  isStaff = false;
  firstName: string | null = null;
  lastName: string | null = null;

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe((user) => {
      this.isLoggedIn = !!user;
      this.firstName = user?.firstName ?? null;
      this.lastName = user?.lastName ?? null;
      this.isAdmin = user?.roles?.includes('ADMIN') ?? false;
      this.isStaff = user?.roles?.some(r => r === 'ADMIN' || r === 'MANAGER') ?? false;
    });
  }

  logout(): void {
    this.authService.logout();
  }

  title = 'catalog';
}

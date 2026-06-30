import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, NavigationEnd, Router, RouterModule } from '@angular/router';
import { Subject } from 'rxjs';
import { filter, takeUntil } from 'rxjs/operators';

export interface BreadcrumbItem {
  label: string;
  url: string | null;
}

@Component({
  selector: 'app-breadcrumb',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './breadcrumb.component.html',
})
export class BreadcrumbComponent implements OnInit, OnDestroy {
  breadcrumbs: BreadcrumbItem[] = [];
  private destroy$ = new Subject<void>();

  constructor(private router: Router, private activatedRoute: ActivatedRoute) {}

  ngOnInit(): void {
    this.buildBreadcrumbs();

    this.router.events
      .pipe(
        filter((event) => event instanceof NavigationEnd),
        takeUntil(this.destroy$)
      )
      .subscribe(() => this.buildBreadcrumbs());
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private buildBreadcrumbs(): void {
    this.breadcrumbs = this.resolveBreadcrumbs(this.activatedRoute.root, '');
  }

  private resolveBreadcrumbs(
    route: ActivatedRoute,
    parentUrl: string
  ): BreadcrumbItem[] {
    const items: BreadcrumbItem[] = [];

    // Always start with Home
    if (parentUrl === '') {
      items.push({ label: 'Home', url: '/' });
    }

    const routeUrl = parentUrl + '/' + route.snapshot.url.map((s) => s.path).join('/');

    const title: string | undefined = route.snapshot.data['title'];
    if (title && route.snapshot.url.length > 0) {
      const isLast = route.children.length === 0;
      items.push({ label: title, url: isLast ? null : routeUrl });
    }

    for (const child of route.children) {
      items.push(...this.resolveBreadcrumbs(child, routeUrl));
    }

    return items;
  }
}

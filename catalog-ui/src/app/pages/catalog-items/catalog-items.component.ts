import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { CatalogItemResponseDTO } from '../../models/catalog-item.models';
import { CatalogItemService } from '../../services/catalog-item.service';
import { CheckoutService } from '../../services/checkout.service';
@Component({
  selector: 'app-catalog-items',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './catalog-items.component.html',
  styleUrl: '../../common/form.css',
})
export class CatalogItemsComponent implements OnInit {
  catalogItems$!: Observable<CatalogItemResponseDTO[]>;

  constructor(
    private catalogItemService: CatalogItemService,
    private checkoutService: CheckoutService
  ) {}

  ngOnInit(): void {
    this.refreshCatalogItems();
  }

  refreshCatalogItems(): void {
    this.catalogItems$ = this.catalogItemService.getAllCatalogItems();
  }

  checkoutItem(item: CatalogItemResponseDTO): void {
    // Call your service to checkout item
    this.checkoutService
      .checkout({
        itemId: item.id,
        userEmail: sessionStorage.getItem('userEmail'),
      })
      .subscribe({
        next: () => this.refreshCatalogItems(),
        // Handle any errors
        error: (err: any) => console.error(err),
      });
  }

  checkinItem(item: CatalogItemResponseDTO): void {
    // Call your service to checkin item
    this.checkoutService
      .checkin({
        itemId: item.id,
        userEmail: sessionStorage.getItem('userEmail'),
      })
      .subscribe({
        next: () => this.refreshCatalogItems(),
        // Handle any errors
        error: (err: any) => console.error(err),
      });
  }
}

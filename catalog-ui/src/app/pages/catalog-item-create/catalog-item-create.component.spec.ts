import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';

import { CatalogItemCreateComponent } from './catalog-item-create.component';

describe('CatalogItemCreateComponent', () => {
  let component: CatalogItemCreateComponent;
  let fixture: ComponentFixture<CatalogItemCreateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CatalogItemCreateComponent, HttpClientTestingModule]
    }).compileComponents();

    fixture = TestBed.createComponent(CatalogItemCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

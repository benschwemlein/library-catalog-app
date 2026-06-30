import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';

import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import {
  CatalogIdType,
  CatalogItemRequestDTO,
  Type,
} from '../../models/catalog-item.models';
import { CatalogIdTypeService } from '../../services/catalog-id-type-service';
import { CatalogItemService } from '../../services/catalog-item.service';

@Component({
  selector: 'app-catalog-item-create',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './catalog-item-create.component.html',
  styleUrls: ['../../common/form.css'],
})
export class CatalogItemCreateComponent {
  catalogItemForm: FormGroup;
  catalogIdTypes: CatalogIdType[] = [];
  catalogIdPairs: { type: Type; value: string }[] = [];
  toastMessage: string = '';
  // private cd!: ChangeDetectorRef;

  constructor(
    private formBuilder: FormBuilder,
    private catalogItemService: CatalogItemService,
    private catalogIdTypeService: CatalogIdTypeService
  ) {
    // Initialize the form with empty default values
    this.catalogItemForm = this.formBuilder.group({
      title: ['', [Validators.required]],
      description: ['', [Validators.required]],
      catalogIdType: [null],
      //catalogIds: this.formBuilder.array([]),
      // Dropdown for selecting Catalog ID Type
      catalogIdValue: [''],
      // Here we assume you have access to the 'createdBy' value or getting it in some other way
      // createdBy: [null, [Validators.required]], // For UserDTO
    });
  }

  ngOnInit(): void {
    this.catalogIdTypeService.getAllCatalogIdTypes().subscribe(
      (types) => {
        console.log('Types loaded:', types); // Logging for debugging
        this.catalogIdTypes = types;
        // this.cd.markForCheck(); // Manually mark for check
      },
      (error) => {
        console.error('Failed to load catalog ID types:', error);
      }
    );
  }

  // Inside Class CatalogItemCreateComponent
  selectCatalogIdType(type: CatalogIdType) {
    const catalogIdTypeControl = this.catalogItemForm.get('catalogIdType');
    if (catalogIdTypeControl) {
      catalogIdTypeControl.setValue(type);
    }
  }

  addCatalogIdPair() {
    const selectedType = this.catalogItemForm.get('catalogIdType')?.value;
    const enteredValue = this.catalogItemForm.get('catalogIdValue')?.value;
    if (selectedType && enteredValue) {
      this.catalogIdPairs.push({
        type: { name: selectedType.name }, // Assuming we just want to store 'name' of CatalogIdType
        value: enteredValue,
      });

      // Optionally clear the controls after adding
      this.catalogItemForm.patchValue({
        catalogIdType: null,
        catalogIdValue: '',
      });
    }
  }

  createCatalogItem(): void {
    if (this.catalogItemForm.invalid) {
      // Trigger validation and exit if invalid
      this.catalogItemForm.markAllAsTouched();
      return;
    }

    const catalogItem: CatalogItemRequestDTO = {
      ...this.catalogItemForm.value,
      createdBy: { email: sessionStorage.getItem('userEmail') }, // User information, needs actual data
      catalogIds: this.catalogIdPairs,
    };

    this.catalogItemService.createCatalogItem(catalogItem).subscribe({
      next: (response) => {
        console.log('Catalog item created successfully:', response);
        this.toastMessage = 'Catalog item created successfully.';
        this.catalogIdPairs = [];
        // Optionally perform actions like routing to another page or clearing the form
        this.catalogItemForm.reset();
      },
      error: (error) => {
        console.error('Failed to create catalog item:', error);
        this.toastMessage = 'Catalog item failed to be created.';
      },
    });
  }
}

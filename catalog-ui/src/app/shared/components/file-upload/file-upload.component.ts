import {
  Component,
  EventEmitter,
  Input,
  Output,
} from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-file-upload',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './file-upload.component.html',
})
export class FileUploadComponent {
  @Input() acceptedTypes: string[] = ['image/*', 'application/pdf'];
  @Input() maxSizeMb = 5;

  @Output() fileSelected = new EventEmitter<File>();
  @Output() uploadError = new EventEmitter<string>();

  isDragging = false;
  selectedFile: File | null = null;
  previewUrl: string | null = null;
  errorMessage: string | null = null;

  get acceptAttribute(): string {
    return this.acceptedTypes.join(',');
  }

  get formattedSize(): string {
    if (!this.selectedFile) return '';
    const kb = this.selectedFile.size / 1024;
    if (kb < 1024) return `${kb.toFixed(1)} KB`;
    return `${(kb / 1024).toFixed(1)} MB`;
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = true;
  }

  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = false;
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = false;
    const files = event.dataTransfer?.files;
    if (files && files.length > 0) {
      this.handleFile(files[0]);
    }
  }

  onFileInputChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.handleFile(input.files[0]);
    }
  }

  triggerFileInput(fileInput: HTMLInputElement): void {
    fileInput.click();
  }

  private handleFile(file: File): void {
    this.errorMessage = null;

    if (!this.isTypeAccepted(file)) {
      const msg = `File type not accepted. Allowed: ${this.acceptedTypes.join(', ')}`;
      this.errorMessage = msg;
      this.uploadError.emit(msg);
      return;
    }

    const maxBytes = this.maxSizeMb * 1024 * 1024;
    if (file.size > maxBytes) {
      const msg = `File too large. Maximum size is ${this.maxSizeMb} MB.`;
      this.errorMessage = msg;
      this.uploadError.emit(msg);
      return;
    }

    this.selectedFile = file;
    this.previewUrl = null;

    if (file.type.startsWith('image/')) {
      const reader = new FileReader();
      reader.onload = (e) => {
        this.previewUrl = e.target?.result as string;
      };
      reader.readAsDataURL(file);
    }

    this.fileSelected.emit(file);
  }

  private isTypeAccepted(file: File): boolean {
    return this.acceptedTypes.some((accepted) => {
      if (accepted.endsWith('/*')) {
        return file.type.startsWith(accepted.slice(0, -2));
      }
      return file.type === accepted;
    });
  }

  clearSelection(): void {
    this.selectedFile = null;
    this.previewUrl = null;
    this.errorMessage = null;
  }
}

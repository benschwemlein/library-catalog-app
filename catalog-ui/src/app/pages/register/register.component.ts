import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Role } from '../../models/authentication.models';
import { AuthenticationService } from '../../services/authentication.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './register.component.html',
  styleUrl: '../../common/form.css',
})
export class RegisterComponent {
  registerForm: FormGroup;
  toastMessage: string = '';
  // roles = Role; // You might need to get the available roles from the backend or define them as constants
  roles: { label: string; value: Role }[] = Object.keys(Role).map((key) => ({
    label: Role[key as keyof typeof Role],
    value: Role[key as keyof typeof Role],
  }));

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthenticationService
  ) {
    this.registerForm = this.formBuilder.group({
      firstname: ['', [Validators.required]],
      lastname: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      role: ['USER'],
    });

    // Initialize roles here if they are constant or get them from the backend
  }

  onSubmit(): void {
    if (this.registerForm.valid) {
      this.authService.register(this.registerForm.value).subscribe({
        next: (response) => {
          this.toastMessage = 'User was successfully registered.';
          console.log('User was successfully registered', response);
          // Registration successful - handle response
        },
        error: (error) => {
          console.log('Error registering user.', error);
          this.toastMessage = 'Error registering user.';
          // Registration failed - handle error
        },
      });
    }
  }
}

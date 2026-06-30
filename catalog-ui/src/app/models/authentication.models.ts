export interface AuthenticationRequest {
  username: string;
  password: string;
}

export interface AuthenticationResponse {
  jwtToken: string;
  access_token: string;
  first_name: string;
  last_name: string;
  email: string;
  // Add other fields that may be returned in the response
}

export interface UserRequestDTO {
  email: string;
  // Add other fields that may be returned in the response
}

export interface UserResponseDTO {
  email: string;
  firstName: string;
  lastName: string;
  // Add other fields that may be returned in the response
}

// role.models.ts
export enum Role {
  USER = 'USER',
  ADMIN = 'ADMIN',
  MANAGER = 'MANAGER',
}

// register-request.models.ts
export interface RegisterRequest {
  firstname: string;
  lastname: string;
  email: string;
  password: string;
  role: Role;
}

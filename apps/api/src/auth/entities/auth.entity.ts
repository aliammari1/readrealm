export class Auth {}

export interface AuthResponse {
  message?: string;
  tokens?: {
    accessToken: string;
    refreshToken: string;
  };
  userId?: string;
}

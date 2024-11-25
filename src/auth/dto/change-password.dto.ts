import { IsString } from 'class-validator';

export class ChangePasswordDto {
  static oldPassword(oldPassword: any) {
    throw new Error('Method not implemented.');
  }
  @IsString()
  oldPassword: string;

  @IsString()
  newPassword: string;
}

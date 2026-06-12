import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsEmail, IsNotEmpty, IsString, MinLength } from 'class-validator';

export class SignupDto {
  @ApiProperty({ example: 'jane_reader' })
  @IsNotEmpty()
  username: string;

  @ApiProperty({ example: 'jane@example.com', format: 'email' })
  @IsNotEmpty()
  @IsEmail()
  email: string;

  @ApiProperty({ minLength: 6, example: 'hunter2!' })
  @IsNotEmpty()
  @IsString()
  @MinLength(6)
  password: string;

  @ApiPropertyOptional({ description: 'URL or base64 avatar' })
  profilePicture?: string;
}

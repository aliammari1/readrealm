import { ApiProperty } from '@nestjs/swagger';
import { IsEmail, IsNotEmpty, IsString, MinLength } from 'class-validator';

export class loginDto {
  @ApiProperty({ example: 'jane@example.com', format: 'email' })
  @IsNotEmpty()
  @IsEmail()
  email: string;

  @ApiProperty({ minLength: 6, example: 'hunter2!' })
  @IsNotEmpty()
  @IsString()
  @MinLength(6)
  password: string;
}

import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import {
  IsNotEmpty,
  IsString,
  IsNumber,
  IsArray,
  IsOptional,
} from 'class-validator';
import { Bookmark } from '../entities/book.entity';

export class CreateBookDto {
  @ApiProperty({ example: 12345, description: 'Open Library work id' })
  @IsNotEmpty()
  @IsNumber()
  id: number;

  @ApiProperty({ example: 'George Orwell' })
  @IsNotEmpty()
  @IsString()
  author: string;

  @ApiProperty({ example: '1984' })
  @IsNotEmpty()
  @IsString()
  title: string;

  @ApiProperty({ example: 1949 })
  @IsNotEmpty()
  @IsNumber()
  publicationDate: number;

  @ApiProperty({ example: 328 })
  @IsNotEmpty()
  @IsNumber()
  numOfPages: number;

  @ApiProperty({ example: 'https://covers.openlibrary.org/b/id/123-M.jpg' })
  @IsNotEmpty()
  @IsString()
  coverImage: string;

  @ApiProperty({ example: 'Dystopian' })
  @IsNotEmpty()
  @IsString()
  genre: string;

  @ApiPropertyOptional({ description: 'Full text used for TTS / summary' })
  @IsOptional()
  @IsString()
  textData?: string;

  @ApiPropertyOptional({ type: [Object] })
  @IsOptional()
  @IsArray()
  bookmarks?: Bookmark[];
}

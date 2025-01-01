import {
  IsNotEmpty,
  IsString,
  IsNumber,
  IsArray,
  IsOptional,
} from 'class-validator';
import { Bookmark } from '../entities/book.entity';

export class CreateBookDto {
  @IsNotEmpty()
  @IsNumber()
  id: number;

  @IsNotEmpty()
  @IsString()
  author: string;

  @IsNotEmpty()
  @IsString()
  title: string;

  @IsNotEmpty()
  @IsNumber()
  publicationDate: number;

  @IsNotEmpty()
  @IsNumber()
  numOfPages: number;

  @IsNotEmpty()
  @IsString()
  coverImage: string;

  @IsNotEmpty()
  @IsString()
  genre: string;

  @IsOptional()
  @IsString()
  textData?: string;

  @IsOptional()
  @IsArray()
  bookmarks?: Bookmark[];
}

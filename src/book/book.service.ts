import { Injectable } from '@nestjs/common';
import { CreateBookDto } from './dto/create-book.dto';
import { UpdateBookDto } from './dto/update-book.dto';
import { HttpService } from '@nestjs/axios';
import { map } from 'rxjs/operators';

@Injectable()
export class BookService {

  constructor(private readonly httpService: HttpService) {}

  create(createBookDto: CreateBookDto) {
    return 'This action adds a new book';
  }

  findAll() {
    // first_publish_year, author_name,cover_i,number_of_pages_median
    // let books = this.httpService.get('https://openlibrary.org/search.json?q=atomic+habits&fields=author_name,first_publish_year,number_of_pages_median,cover_i&limit=10').pipe(
    //   map(response => response.data)
    // );
    let books = this.httpService.get('https://openlibrary.org/subjects/adventure.json').pipe(
      map(response => response.data)
    );
    return books;
  }

  findOne(id: number) {
    return `This action returns a #${id} book`;
  }

  update(id: number, updateBookDto: UpdateBookDto) {
    return `This action updates a #${id} book`;
  }

  remove(id: number) {
    return `This action removes a #${id} book`;
  }
}

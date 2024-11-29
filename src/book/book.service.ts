import { Injectable } from '@nestjs/common';
import { CreateBookDto } from './dto/create-book.dto';
import { UpdateBookDto } from './dto/update-book.dto';
import { HttpService } from '@nestjs/axios';
import { map, mergeMap } from 'rxjs/operators';
import { Book } from './entities/book.entity';
import { forkJoin, merge } from 'rxjs';
import { randomInt } from 'crypto';

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
    let books = this.httpService
      .get('https://openlibrary.org/subjects/adventure.json')
      .pipe(map((response) => response.data));
    return books;
  }

  findAllGenres() {
    console.log("begin action")
    const actionBooks = this.findAllAction();
    console.log("begin adventure")
    const adventureBooks = this.findAllAdventure();
    console.log("begin fantasy")
    const fantasyBooks = this.findAllFantasy();
    console.log("begin merge")
    let books = forkJoin([actionBooks, adventureBooks, fantasyBooks]).pipe(
      map(([action, adventure, fantasy]) => [...action as any, ...adventure as any, ...fantasy as any])
    );
    books.subscribe((allBooks) => {
      console.log(allBooks);
    });
    return books;
  }

  findAllAction() {
    // first_publish_year, author_name,cover_i,number_of_pages_median
    // let books = this.httpService.get('https://openlibrary.org/search.json?q=atomic+habits&fields=author_name,first_publish_year,number_of_pages_median,cover_i&limit=10').pipe(
    //   map(response => response.data)
    // );
    let books = this.httpService
    .get('https://openlibrary.org/subjects/action.json?limit=10')
    .pipe(
      mergeMap((response) => {
        let works = response.data.works;
        let bookObservables = works.map((work) => {
          let titleEncoded = encodeURIComponent(work.title);
          let searchUrl = `https://openlibrary.org/search.json?q=${titleEncoded}&fields=*,availability&limit=1&lang=en`;
          return this.httpService.get(searchUrl).pipe(
            map((searchResponse) => {
              let searchResult = searchResponse.data.docs[0];
              let book = new Book();
              book.title = work.title;
              book.author = searchResult.author_name[0];
              book.publicationDate = searchResult.first_publish_year;
              book.numOfPages = searchResult.number_of_pages_median;
              book.coverImage = `https://covers.openlibrary.org/b/id/${work.cover_id}-M.jpg`;
              book.genre = 'Action';
              // Add availability information
              // book.availability = searchResult.availability;
              return book;
            }),
          );
        });
        return forkJoin(bookObservables);
      }),
    );

    return books;
  }

  findAllAdventure() {
    // first_publish_year, author_name,cover_i,number_of_pages_median
    // let books = this.httpService.get('https://openlibrary.org/search.json?q=atomic+habits&fields=author_name,first_publish_year,number_of_pages_median,cover_i&limit=10').pipe(
    //   map(response => response.data)
    // );
    let books = this.httpService
      .get('https://openlibrary.org/subjects/adventure.json?limit=10')
      .pipe(
        mergeMap((response) => {
          let works = response.data.works;
          let bookObservables = works.map((work) => {
            let titleEncoded = encodeURIComponent(work.title);
            let searchUrl = `https://openlibrary.org/search.json?q=${titleEncoded}&fields=*,availability&limit=1&lang=en`;
            return this.httpService.get(searchUrl).pipe(
              map((searchResponse) => {
                let searchResult = searchResponse.data.docs[0];
                let book = new Book();
                book.title = work.title;
                book.author = searchResult.author_name[0];
                book.publicationDate = searchResult.first_publish_year;
                book.numOfPages = searchResult.number_of_pages_median;
                book.coverImage = `https://covers.openlibrary.org/b/id/${work.cover_id}-M.jpg`;
                // Add availability information
                // book.availability = searchResult.availability;
                book.genre = 'Adventure';
                return book;
              }),
            );
          });
          return forkJoin(bookObservables);
        }),
      );
    return books;
  }

  findAllFantasy() {
    // first_publish_year, author_name,cover_i,number_of_pages_median
    // let books = this.httpService.get('https://openlibrary.org/search.json?q=atomic+habits&fields=author_name,first_publish_year,number_of_pages_median,cover_i&limit=10').pipe(
    //   map(response => response.data)
    // );
    let books = this.httpService
    .get('https://openlibrary.org/subjects/fantasy.json?limit=10')
    .pipe(
      mergeMap((response) => {
        let works = response.data.works;
        let bookObservables = works.map((work) => {
          let titleEncoded = encodeURIComponent(work.title);
          let searchUrl = `https://openlibrary.org/search.json?q=${titleEncoded}&fields=*,availability&limit=1&lang=en`;
          return this.httpService.get(searchUrl).pipe(
            map((searchResponse) => {
              let searchResult = searchResponse.data.docs[0];
              let book = new Book();
              book.title = work.title;
              book.author = searchResult.author_name[0];
              book.publicationDate = searchResult.first_publish_year;
              book.numOfPages = searchResult.number_of_pages_median;
              book.coverImage = `https://covers.openlibrary.org/b/id/${work.cover_id}-M.jpg`;
              book.genre = 'Fantasy';
              // Add availability information
              // book.availability = searchResult.availability;
              return book;
            }),
          );
        });
        return forkJoin(bookObservables);
      }),
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

[Reading 55 lines from start (total: 55 lines, 0 remaining)]

import { Injectable } from '@nestjs/common';
import { HttpService } from '@nestjs/axios';
import { firstValueFrom } from 'rxjs';

@Injectable()
export class EpubService {
  private readonly GUTEDEX_API_ENDPOINT = 'https://gutendex.com';

  constructor(private readonly httpService: HttpService) {}

  async getBookEpubLinkByTitle(title: string) {
    if (!title) {
      console.log('No title provided');
      return '';
    }

    const encodedTitle = encodeURIComponent(title.trim());
    const url = `${this.GUTEDEX_API_ENDPOINT}/books?search=${encodedTitle}`;

    try {
      const response = await firstValueFrom(this.httpService.get(url));
      const books = response.data.results;

      if (!books || books.length === 0) {
        console.log(`No books found with title: ${title}`);
        return '';
      }

      // Try to find an exact or close match
      const matchedBook =
        books.find(
          (book) =>
            book.title.toLowerCase().includes(title.toLowerCase()) ||
            title.toLowerCase().includes(book.title.toLowerCase()),
        ) || books[0];

      // Check all possible EPUB format keys
      const epubFormats = ['application/epub+zip'];

      for (const format of epubFormats) {
        if (matchedBook.formats[format]) {
          console.log(`Found ${format} link for book: ${title}`);
          return matchedBook.formats[format];
        }
      }

      console.log(`No supported format found for book: ${title}`);
      return '';
    } catch (error) {
      console.error(`Error fetching book link: ${error.message}`);
      console.error('Request URL:', url);
      return '';
    }
  }
}

[executed on device: thethirdone (5346b035-2835-4d41-8950-4054b200ff7b)]
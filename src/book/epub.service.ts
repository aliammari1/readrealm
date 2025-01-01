import { Injectable } from '@nestjs/common';
import { HttpService } from '@nestjs/axios';
import { firstValueFrom } from 'rxjs';

@Injectable()
export class EpubService {
    private readonly GUTEDEX_API_ENDPOINT = 'https://gutendex.com';

    constructor(private readonly httpService: HttpService) {}

    async getBookEpubLinkByTitle(title: string) {
        const encodedTitle = encodeURIComponent(title);
        const url = `${this.GUTEDEX_API_ENDPOINT}/books?search=${encodedTitle}`;

        try {
            const response = await firstValueFrom(this.httpService.get(url));
            const books = response.data.results;
            
            if (!books || books.length === 0) {
                console.log(`No books found with title: ${title}`);
                return '';
            }
            
            const link = books[0].formats['application/epub+zip'];
            return link || '';
        } catch (error) {
            console.log(`Error fetching book link: ${error.message}`);
            return '';
        }
    }
}

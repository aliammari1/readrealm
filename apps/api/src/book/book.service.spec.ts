import { BookService } from './book.service';

describe('BookService', () => {
  it('should be defined with mocked collaborators', () => {
    const service = new BookService(
      {} as any,
      {} as any,
      {} as any,
      { get: jest.fn() } as any,
      {} as any,
      {} as any,
      {} as any,
    );

    expect(service).toBeDefined();
  });
});

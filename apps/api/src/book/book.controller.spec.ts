import { BookController } from './book.controller';

describe('BookController', () => {
  it('should be defined with mocked services', () => {
    const controller = new BookController(
      {} as any,
      {} as any,
      {} as any,
      {} as any,
    );

    expect(controller).toBeDefined();
  });
});

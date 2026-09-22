import { AppController } from './app.controller';
import { AppService } from './app.service';

describe('AppController', () => {
  it('should return "Hello World!"', async () => {
    const controller = new AppController(new AppService());
    await expect(controller.getHello()).resolves.toBe('Hello World!');
  });

  it('should report a healthy service', () => {
    const controller = new AppController(new AppService());
    expect(controller.health()).toEqual(
      expect.objectContaining({
        status: 'ok',
        service: 'readrealm-api',
      }),
    );
  });
});

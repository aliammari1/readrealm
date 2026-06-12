import { Test, TestingModule } from '@nestjs/testing';
import { INestApplication } from '@nestjs/common';
import { MongooseModule } from '@nestjs/mongoose';
import request from 'supertest';
import { MongoMemoryServer } from 'mongodb-memory-server';
import { AppController } from '../src/app.controller';
import { AppService } from '../src/app.service';

/**
 * End-to-end smoke test exercising the supertest + mongodb-memory-server stack.
 *
 * We mount AppController against a real (in-memory) MongoDB rather than the full
 * AppModule: the SpeechRealtime module pulls in the `node-av` native FFmpeg
 * binding, which Jest's transformer can't load. This keeps the HTTP + Mongo
 * round-trip honest without the native dependency.
 */
describe('AppController (e2e)', () => {
  let app: INestApplication;
  let mongod: MongoMemoryServer;

  beforeAll(async () => {
    mongod = await MongoMemoryServer.create();

    const moduleFixture: TestingModule = await Test.createTestingModule({
      imports: [MongooseModule.forRoot(mongod.getUri())],
      controllers: [AppController],
      providers: [AppService],
    }).compile();

    app = moduleFixture.createNestApplication();
    await app.init();
  }, 60000);

  afterAll(async () => {
    await app?.close();
    await mongod?.stop();
  });

  it('/ (GET) returns the hello payload', () => {
    return request(app.getHttpServer())
      .get('/')
      .expect(200)
      .expect('Hello World!');
  });
});

import { Test, TestingModule } from '@nestjs/testing';
import { INestApplication } from '@nestjs/common';
import request from 'supertest';
import { MongoMemoryServer } from 'mongodb-memory-server';
import { AppModule } from '../src/app.module';

describe('AppController (e2e)', () => {
  let app: INestApplication;
  let mongod: MongoMemoryServer;

  beforeAll(async () => {
    mongod = await MongoMemoryServer.create();
    // AppModule's MongooseModule reads database.connectionString from MONGO_URL.
    process.env.MONGO_URL = mongod.getUri();
    process.env.JWT_SECRET = process.env.JWT_SECRET ?? 'test-secret';

    const moduleFixture: TestingModule = await Test.createTestingModule({
      imports: [AppModule],
    }).compile();

    app = moduleFixture.createNestApplication();
    await app.init();
  }, 60000);

  afterAll(async () => {
    await app?.close();
    await mongod?.stop();
  });

  it('/ (GET) returns the hello payload', () => {
    return request(app.getHttpServer()).get('/').expect(200);
  });
});

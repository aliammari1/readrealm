/**
 * Generates shared/api-spec/openapi.yaml from the live Nest application metadata.
 *
 * Run with:  pnpm run generate:openapi
 *
 * It bootstraps the AppModule *without* opening a port or a DB connection
 * (NestFactory.create with logger disabled is enough to collect the Swagger
 * metadata), serializes the OpenAPI document to YAML and writes it to the
 * monorepo-shared spec consumed by Mintlify and the native clients.
 */
import { NestFactory } from '@nestjs/core';
import { SwaggerModule } from '@nestjs/swagger';
import { writeFileSync, mkdirSync } from 'fs';
import { dirname, resolve } from 'path';
import * as yaml from 'js-yaml';
import { MongoMemoryServer } from 'mongodb-memory-server';
import { AppModule } from '../src/app.module';
import { buildSwaggerConfig } from '../src/main';

async function generate() {
  // AppModule wires MongooseModule eagerly and Nest awaits the connection, so a
  // bogus URI would hang forever. Spin up an ephemeral in-memory MongoDB purely
  // so module instantiation resolves; we only need the Swagger metadata.
  const mongod = await MongoMemoryServer.create();
  process.env.MONGO_URL = mongod.getUri();
  process.env.JWT_SECRET = process.env.JWT_SECRET ?? 'openapi-gen-secret';

  const app = await NestFactory.create(AppModule, {
    logger: false,
    abortOnError: false,
  });

  const document = SwaggerModule.createDocument(app, buildSwaggerConfig());

  const outPath = resolve(
    __dirname,
    '..',
    '..',
    '..',
    'shared',
    'api-spec',
    'openapi.yaml',
  );
  mkdirSync(dirname(outPath), { recursive: true });
  writeFileSync(outPath, yaml.dump(document, { noRefs: true, lineWidth: 120 }));

  await app.close();
  await mongod.stop();
  // eslint-disable-next-line no-console
  console.log(`OpenAPI spec written to ${outPath}`);
}

generate()
  .then(() => process.exit(0))
  .catch((err) => {
    // eslint-disable-next-line no-console
    console.error('Failed to generate OpenAPI spec:', err);
    process.exit(1);
  });

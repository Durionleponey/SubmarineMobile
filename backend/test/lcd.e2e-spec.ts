import { INestApplication } from '@nestjs/common';
import { Test } from '@nestjs/testing';
import { AppModule } from '../src/app.module';
import * as request from 'supertest';
import { HttpService } from '@nestjs/axios';
import { of } from 'rxjs';

describe('LCD GraphQL (e2e)', () => {
  let app: INestApplication;
  let httpService: HttpService;

  beforeAll(async () => {
    const moduleRef = await Test.createTestingModule({
      imports: [AppModule],
    })
      .overrideProvider(HttpService)
      .useValue({
        post: jest.fn().mockReturnValue(of({ data: { success: true } })),
      })
      .compile();

    app = moduleRef.createNestApplication();
    await app.init();

    httpService = app.get<HttpService>(HttpService);
  });

  afterAll(async () => {
    await app.close();
  });

  it('sendAdminThanks doit retourner success = true', async () => {
    const query = `
      mutation {
        sendAdminThanks {
          success
          message
        }
      }
    `;

    const response = await request(app.getHttpServer())
      .post('/graphql')
      .send({ query })
      .expect(200);

    expect(httpService.post).toHaveBeenCalled();
    expect(
      response.body.data.sendAdminThanks.success,
    ).toBe(true);
    expect(
      response.body.data.sendAdminThanks.message,
    ).toContain('remerciement');
  });

  it('sendAlertMessage doit retourner success = true', async () => {
    const query = `
      mutation {
        sendAlertMessage {
          success
          message
        }
      }
    `;

    const response = await request(app.getHttpServer())
      .post('/graphql')
      .send({ query })
      .expect(200);

    expect(httpService.post).toHaveBeenCalled();
    expect(
      response.body.data.sendAlertMessage.success,
    ).toBe(true);
    expect(
      response.body.data.sendAlertMessage.message,
    ).toContain('alerte');
  });
});

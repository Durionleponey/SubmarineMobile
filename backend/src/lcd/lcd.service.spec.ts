import { Test, TestingModule } from '@nestjs/testing';
import { HttpService } from '@nestjs/axios';
import { LcdService } from './lcd.service';
import { of } from 'rxjs';

describe('LcdService', () => {
  let service: LcdService;
  let httpService: HttpService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [
        LcdService,
        {
          provide: HttpService,
          useValue: {
            post: jest.fn(),
          },
        },
      ],
    }).compile();

    service = module.get<LcdService>(LcdService);
    httpService = module.get<HttpService>(HttpService);
  });

  it('doit être défini', () => {
    expect(service).toBeDefined();
  });

  it('sendAdminThanks appelle bien /lcd/thank-admin', async () => {
    const fakeResponse = { data: { success: true } };

    (httpService.post as jest.Mock).mockReturnValue(of(fakeResponse as any));

    await service.sendAdminThanks();

    expect(httpService.post).toHaveBeenCalledTimes(1);
    expect(httpService.post).toHaveBeenCalledWith(
      expect.stringContaining('/lcd/thank-admin'),
      {}, // body vide comme dans le service
    );
  });

  it('sendAlert appelle bien /lcd/alert', async () => {
    const fakeResponse = { data: { success: true } };

    (httpService.post as jest.Mock).mockReturnValue(of(fakeResponse as any));

    await service.sendAlert();

    expect(httpService.post).toHaveBeenCalledTimes(1);
    expect(httpService.post).toHaveBeenCalledWith(
      expect.stringContaining('/lcd/alert'),
      {}, // pareil ici
    );
  });
});

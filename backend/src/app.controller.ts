import { Controller, Get } from '@nestjs/common';
import { AppService } from './app.service';

@Controller()
export class AppController {
  constructor(private readonly appService: AppService) {}

  @Get()
  getHello(): string {
    return this.appService.getHello();
  }


  @Get('gg')
  getHello2(): string {
    return this.appService.getHello2();
  }

  @Get('ping')
  pong(): string {
    return this.appService.pong();
  }
}

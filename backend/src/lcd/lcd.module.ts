import { Module } from '@nestjs/common';
import { HttpModule } from '@nestjs/axios';
import { LcdService } from './lcd.service';
import { LcdResolver } from './lcd.resolver';

@Module({
  imports: [
    // Fournit HttpService à LcdService
    HttpModule,
  ],
  providers: [
    LcdService,
    LcdResolver,
  ],
  exports: [
    LcdService,
  ],
})
export class LcdModule {}

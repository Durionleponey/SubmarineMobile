import { Module, Global } from '@nestjs/common';
import { MailService } from './mail.service';

@Global() // Important : permet d'utiliser le mail partout sans le réimporter
@Module({
  providers: [MailService],
  exports: [MailService],
})
export class MailModule {}
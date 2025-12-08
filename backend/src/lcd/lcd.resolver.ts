import { Mutation, Resolver } from '@nestjs/graphql';
import { LcdService } from './lcd.service';
import { LcdMessageResult } from './lcd-response.type';

@Resolver()
export class LcdResolver {
  constructor(private readonly lcdService: LcdService) {}

  @Mutation(() => LcdMessageResult)
  async sendAdminThanks(): Promise<LcdMessageResult> {
    await this.lcdService.sendAdminThanks();
    return {
      success: true,
      message: "Le message de remerciement a été pris en compte par le backend.",
    };
  }

  @Mutation(() => LcdMessageResult)
  async sendAlertMessage(): Promise<LcdMessageResult> {
    await this.lcdService.sendAlert();
    return {
      success: true,
      message: "Le message d'alerte a été pris en compte par le backend.",
    };
  }
}

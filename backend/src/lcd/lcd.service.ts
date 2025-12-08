// backend/src/lcd/lcd.service.ts
import { Injectable, Logger } from '@nestjs/common';
import { HttpService } from '@nestjs/axios';
import { firstValueFrom } from 'rxjs';
import { LcdMessageResult } from './dto/lcd-message-result.dto';

@Injectable()
export class LcdService {
  private readonly logger = new Logger(LcdService.name);

  // ⚠️ METS ICI L'IP DE TA PICO (celle affichée dans ifconfig())
  private readonly picoBaseUrl = 'http://192.168.1.37:8080';

  constructor(private readonly http: HttpService) {}

  async sendAdminThanks(): Promise<LcdMessageResult> {
    this.logger.log('LCD: sendAdminThanks() appelé');
    try {
      await firstValueFrom(
        this.http.post(`${this.picoBaseUrl}/lcd/thank-admin`, {}),
      );

      return {
        success: true,
        message: "Le message de remerciement a été pris en compte par le backend.",
      };
    } catch (e) {
      this.logger.error('Erreur en envoyant au LCD (thank-admin)', e);
      return {
        success: false,
        message: "Erreur lors de l'envoi du message au LCD.",
      };
    }
  }

  async sendAlert(): Promise<LcdMessageResult> {
    this.logger.log('LCD: sendAlert() appelé');
    try {
      await firstValueFrom(
        this.http.post(`${this.picoBaseUrl}/lcd/alert`, {}),
      );

      return {
        success: true,
        message: "Le message d'alerte a été pris en compte par le backend.",
      };
    } catch (e) {
      this.logger.error('Erreur en envoyant au LCD (alert)', e);
      return {
        success: false,
        message: "Erreur lors de l'envoi de l'alerte au LCD.",
      };
    }
  }
}

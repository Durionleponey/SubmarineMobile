import { Injectable } from '@nestjs/common';
import { MailerService } from '@nestjs-modules/mailer';

@Injectable()
export class MailService {
  constructor(private readonly mailerService: MailerService) {}

  async sendFriendRequestEmail(receiverEmail: string, senderPseudo: string, requestId: string) {
    // Lien de validation (10.0.2.2 = l'hôte pour l'émulateur Android, localhost pour le web)
    const url = `http://10.0.2.2:4000/friends/confirm-email?id=${requestId}`;

    await this.mailerService.sendMail({
      to: receiverEmail,
      subject: `Submarine : ${senderPseudo} veut être ton ami !`,
      html: `
        <div style="font-family: Arial; text-align: center; padding: 20px;">
          <h1 style="color: #3b82f6;">Nouvelle demande d'ami 🚢</h1>
          <p><strong>${senderPseudo}</strong> veut t'ajouter sur Submarine.</p>
          <br/>
          <a href="${url}" style="background-color: #3b82f6; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">
            ACCEPTER LA DEMANDE
          </a>
          <p style="color: gray; font-size: 12px; margin-top: 20px;">Si tu n'as rien demandé, ignore cet email.</p>
        </div>
      `,
    });
  }
}
import { Controller, Get, Query, Res } from '@nestjs/common';
import { FriendsService } from './friends.service';
import { Response } from 'express';

@Controller('friends')
export class FriendsController {
  constructor(private readonly friendsService: FriendsService) {}

  // Cette route capture le clic sur le lien de l'email
  @Get('confirm-email')
  async confirmFriendRequest(@Query('id') id: string, @Res() res: Response) {
    try {
      // On valide la demande via le service
      await this.friendsService.acceptRequestByEmail(id);

      // On renvoie une jolie page HTML de confirmation
      return res.send(`
        <html>
          <body style="display:flex; justify-content:center; align-items:center; height:100vh; font-family:sans-serif; background-color:#f0f2f5;">
            <div style="text-align:center; padding:40px; background:white; border-radius:10px; box-shadow:0 4px 12px rgba(0,0,0,0.1);">
              <h1 style="color:#22c55e; margin-bottom:10px;">Succès ! 🎉</h1>
              <p style="font-size:18px; color:#333;">La demande d'ami a été acceptée.</p>
              <p style="color:#666;">Tu peux retourner sur l'application Submarine.</p>
            </div>
          </body>
        </html>
      `);
    } catch (error) {
      return res.status(400).send("<h1>Erreur</h1><p>Ce lien est invalide ou a déjà été utilisé.</p>");
    }
  }
  // 👇 AJOUTE CETTE ROUTE POUR LE REFUS
  @Get('reject-email')
  async rejectFriendRequest(@Query('id') id: string, @Res() res: Response) {
    try {
      await this.friendsService.rejectFriendRequest(id);

      return res.send(`
        <html>
          <body style="display:flex; justify-content:center; align-items:center; height:100vh; font-family:sans-serif; background-color:#f0f2f5;">
            <div style="text-align:center; padding:40px; background:white; border-radius:10px; box-shadow:0 4px 12px rgba(0,0,0,0.1);">
              <h1 style="color:#ef4444; margin-bottom:10px;">Demande refusée 🚫</h1>
              <p style="font-size:18px; color:#333;">Tu as refusé cette invitation.</p>
              <p style="color:#666;">L'expéditeur ne sera pas notifié directement.</p>
            </div>
          </body>
        </html>
      `);
    } catch (error) {
      return res.status(400).send("<h1>Erreur</h1><p>Ce lien est invalide.</p>");
    }
  }
}

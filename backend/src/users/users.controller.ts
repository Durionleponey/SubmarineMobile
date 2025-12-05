import { Controller, Get, Query, UseGuards, Req } from '@nestjs/common';
import { UsersService } from './users.service';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';

@Controller('users')
export class UsersController {
  constructor(private readonly usersService: UsersService) {}

  // 🔒 Accessible uniquement avec un token JWT valide
  @UseGuards(JwtAuthGuard)
  @Get('search')
  async searchUsers(@Query('pseudo') pseudo: string, @Req() req) {
    if (!pseudo || pseudo.trim() === '') {
      return { message: 'Veuillez entrer un pseudo à rechercher.' };
    }

    const currentUser = req.user; // Ton TokenPayload
    const results = await this.usersService.findByPseudo(pseudo);

    // Facultatif : exclure l'utilisateur connecté
    return results.filter(u => u._id.toString() !== currentUser._id);
  }

  @UseGuards(JwtAuthGuard)
  @Get('me')
  getMe(@Req() req) {
    // Retourne simplement les infos du token (payload)
    return req.user;
  }

}

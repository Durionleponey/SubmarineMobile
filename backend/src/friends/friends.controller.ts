import {
  Controller,
  Post,
  Body,
  UseGuards,
  Req,
  Get,
  Param,
} from '@nestjs/common';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { FriendsService } from './friends.service';

@Controller('friends')
export class FriendsController {
  constructor(private readonly friendsService: FriendsService) {}

  // ➕ Envoyer une demande d’amis
  @UseGuards(JwtAuthGuard)
  @Post('request')
  async sendFriendRequest(@Body('targetId') targetId: string, @Req() req) {
    return this.friendsService.sendFriendRequest(req.user._id, targetId);
  }

  // 📥 Voir les demandes reçues (en attente)
  @UseGuards(JwtAuthGuard)
  @Get('pending')
  async getPending(@Req() req) {
    return this.friendsService.getPendingRequests(req.user._id);
  }

  // ✅ Accepter une demande
  @UseGuards(JwtAuthGuard)
  @Post('accept/:id')
  async accept(@Param('id') id: string) {
    return this.friendsService.acceptFriendRequest(id);
  }

  // ❌ Refuser une demande
  @UseGuards(JwtAuthGuard)
  @Post('reject/:id')
  async reject(@Param('id') id: string) {
    return this.friendsService.rejectFriendRequest(id);
  }
}

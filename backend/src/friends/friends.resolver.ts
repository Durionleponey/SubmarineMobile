import { Resolver, Mutation, Query, Args, Context } from '@nestjs/graphql';
import { FriendsService } from './friends.service';
import { Friend } from './dto/friend.model';
import { UseGuards } from '@nestjs/common';
import { GqlAuthGuard } from '../auth/guards/gql-auth.guard';

@Resolver(() => Friend)
export class FriendsResolver {
  constructor(private readonly friendsService: FriendsService) {}

  // ➕ Envoyer une demande d’amis
  @UseGuards(GqlAuthGuard)
  @Mutation(() => Friend)
  async sendFriendRequest(
    @Args('receiverId') receiverId: string,
    @Context() context
  ) {
    const senderId = context.req.user._id;
    return this.friendsService.sendFriendRequest(senderId, receiverId);
  }


  // 📥 Voir les demandes reçues (en attente)
  @UseGuards(GqlAuthGuard)
  @Query(() => [Friend])
  async pendingRequests(@Context() context) {
    const userId = context.req.user._id;
    return this.friendsService.getPendingRequests(userId);
  }

  // ✅ Accepter une demande
  @UseGuards(GqlAuthGuard)
  @Mutation(() => Friend)
  async acceptFriendRequest(@Args('id') id: string) {
    return this.friendsService.acceptFriendRequest(id);
  }

  // ❌ Refuser une demande
  @UseGuards(GqlAuthGuard)
  @Mutation(() => Friend)
  async rejectFriendRequest(@Args('id') id: string) {
    return this.friendsService.rejectFriendRequest(id);
  }
}

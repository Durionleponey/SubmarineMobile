import { Resolver, Query, Mutation, Args, Context } from '@nestjs/graphql';
import { FriendsService } from './friends.service';
import { Friend } from './entities/friend.entity';
import { GqlAuthGuard } from '../auth/guards/gql-auth.guard';
import { UseGuards } from '@nestjs/common';
import { User } from '../users/entities/user.entity';

@Resolver(() => User)
export class FriendsResolver {
  constructor(private readonly friendsService: FriendsService) {}

  @UseGuards(GqlAuthGuard)
  @Mutation(() => Friend)
  async sendFriendRequest(
    @Args('receiverId') receiverId: string,
    @Context() context,
  ) {
    const senderId = context.req.user._id;
    return this.friendsService.sendFriendRequest(senderId, receiverId);
  }

  @UseGuards(GqlAuthGuard)
  @Query(() => [Friend], { name: 'pendingRequests' })
  async pendingRequests(@Context() context) {
    const userId = context.req.user._id;
    return this.friendsService.getPendingFriendRequests(userId);
  }

  @UseGuards(GqlAuthGuard)
  @Mutation(() => Friend)
  async acceptFriendRequest(
    @Args('id') id: string,
    @Context() context,
  ) {
    const userId = context.req.user._id;
    return this.friendsService.acceptFriendRequest(id, userId);
  }

  @UseGuards(GqlAuthGuard)
  @Mutation(() => Friend)
  async rejectFriendRequest(@Args('id') id: string) {
    return this.friendsService.rejectFriendRequest(id);
  }

  @Query(() => [User])
  @UseGuards(GqlAuthGuard)
  async friendsList(@Context() ctx) {
    const userId = ctx.req.user._id;
    return this.friendsService.getFriendsList(userId);
  }
}

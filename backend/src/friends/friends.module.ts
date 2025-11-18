import { Module } from '@nestjs/common';
import { FriendsService } from './friends.service';
import { FriendsResolver } from './friends.resolver';
import { MongooseModule } from '@nestjs/mongoose';
import { Friend, FriendSchema } from './entities/friend.entity';

@Module({
  imports: [
    MongooseModule.forFeature([
      { name: Friend.name, schema: FriendSchema },
    ]),
  ],
  providers: [
    FriendsService,
    FriendsResolver,
  ],
})
export class FriendsModule {}

import { Module } from '@nestjs/common';
import { MongooseModule } from '@nestjs/mongoose';
import { FriendsService } from './friends.service';
import { FriendsResolver } from './friends.resolver';
import { Friend, FriendSchema } from './entities/friend.entity';
import { UsersModule } from '../users/users.module';

@Module({
  imports: [
    MongooseModule.forFeature([{ name: Friend.name, schema: FriendSchema }]),
    UsersModule,
  ],
  providers: [FriendsService, FriendsResolver],
  exports: [FriendsService],
})
export class FriendsModule {}

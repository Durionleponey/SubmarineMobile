import { Module, forwardRef } from '@nestjs/common'; // 👈 1. Importe forwardRef
import { MongooseModule } from '@nestjs/mongoose';
import { FriendsService } from './friends.service';
import { FriendsResolver } from './friends.resolver';
import { Friend, FriendSchema } from './entities/friend.entity';
import { FriendsController } from './friends.controller';
import { UsersModule } from '../users/users.module'; 

@Module({
  imports: [
    MongooseModule.forFeature([{ name: Friend.name, schema: FriendSchema }]),
    
    // 👇 2. MODIFIE CETTE LIGNE EXACTEMENT COMME ÇA :
    forwardRef(() => UsersModule), 
  ],
  providers: [FriendsResolver, FriendsService],
  controllers: [FriendsController],
  exports: [FriendsService]
})
export class FriendsModule {}
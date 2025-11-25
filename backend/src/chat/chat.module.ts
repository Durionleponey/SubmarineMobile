import { forwardRef, Module } from '@nestjs/common';
import { ChatService } from './chat.service';
import { ChatResolver } from './chat.resolver';
import { ChatRepository } from './chat.repository';
import { MongooseModule } from '@nestjs/mongoose';
import { Chat, ChatSchema } from './entities/chat.entity';
import { MessagesModule } from './messages/messages.module';
// Tu peux garder l'import du fichier, mais ne l'utilise pas dans providers
import { UsersModule } from '../users/users.module';

@Module({
  imports: [
    MongooseModule.forFeature([
      { name: Chat.name, schema: ChatSchema }
    ]),
    
    UsersModule, // ✅ C'est LUI qui apporte le UsersRepository

    forwardRef(() => MessagesModule),
  ],
  providers: [
    ChatResolver, 
    ChatService, 
    ChatRepository, 
    // ❌ UsersRepository,  <--- SUPPRIME CETTE LIGNE !
  ],
  exports: [ChatRepository, ChatService],
})
export class ChatModule {}
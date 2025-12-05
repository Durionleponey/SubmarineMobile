import { forwardRef, Module } from '@nestjs/common';
import { MessagesService } from './messages.service';
import { MessagesResolver } from './messages.resolver';
import { ChatModule } from "../chat.module";
// Tu n'as même plus besoin d'importer le fichier repository ici en haut
import { UsersModule } from "../../users/users.module";

@Module({
  imports: [
      forwardRef(() => ChatModule),
      UsersModule, // ✅ C'est lui qui fournit le UsersRepository
  ],
  providers: [
    MessagesResolver, 
    MessagesService,
    // ❌ UsersRepository, <--- SUPPRIME CETTE LIGNE !
  ],
})
export class MessagesModule {}
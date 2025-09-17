import {forwardRef, Module} from '@nestjs/common';
import { MessagesService } from './messages.service';
import { MessagesResolver } from './messages.resolver';
import {ChatModule} from "../chat.module";
import {UsersRepository} from "../../users/users.repository";
import {UsersModule} from "../../users/users.module";

@Module({
  imports: [
      forwardRef(() => ChatModule),UsersModule,
  ],
  providers: [MessagesResolver, MessagesService,UsersRepository],
})
export class MessagesModule {}

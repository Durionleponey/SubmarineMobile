import {Resolver, Query, Mutation, Args, Int, Subscription} from '@nestjs/graphql';
import { ChatService } from './chat.service';
import { Chat } from './entities/chat.entity';
import { CreateChatInput } from './dto/create-chat.input';
import { UpdateChatInput } from './dto/update-chat.input';
import {UseGuards} from "@nestjs/common";
import {GqlAuthGuard} from "../auth/guards/gql-auth.guard";
import {CurrentUser} from "../auth/current-user.decorator";
import {TokenPayload} from "../auth/token-payload.interface";
import {string} from "joi";
import {Message} from "./messages/entities/message.entity";
import {MessageCreatedArgs} from "./dto/message-created.args";

@Resolver(() => Chat)
export class ChatResolver {
  constructor(private readonly chatService: ChatService) {}

  @UseGuards(GqlAuthGuard)
  @Mutation(() => Chat)
  createChat(@Args('createChatInput') createChatInput: CreateChatInput, @CurrentUser() user:TokenPayload) {
    return this.chatService.create(createChatInput, user._id);
  }


  @UseGuards(GqlAuthGuard)
  @Mutation(() => Int)
  leaveAllChat(@CurrentUser() user:TokenPayload) {
    return this.chatService.leaveAllChat(user._id);
  }

  @UseGuards(GqlAuthGuard)
  @Query(() => [Chat], { name: 'chatss' })
  findAll(@CurrentUser() user:TokenPayload) {
    return this.chatService.findAll(user._id);
  }

  @Query(() => Chat, { name: 'chat' })
  findOne(@Args('_id')_id:string) {
    return this.chatService.findOne(_id);
  }

  @UseGuards(GqlAuthGuard)
  @Mutation(() => String)
  addUserToChat(@Args('email') email: string,@Args('chatId') chatId: string,@CurrentUser() user:TokenPayload) {
    //console.log("hello frot resolveur")
    return this.chatService.addUserToChat(user._id,email,chatId);
  }

  @Mutation(() => Chat)
  removeChat(@Args('id', { type: () => Int }) id: number) {
    return this.chatService.remove(id);
  }


  @Subscription(() => Chat, {
    filter:(payload, variables, context) => {//payload --> in the message to waiting to be published, variables --> graphQL request execuse in every publi

      const userId= context.req.user._id

      //console.log("🏓🏓",context.req.user._id);

      //console.log("🍕 payload",payload)//its the data send
      //console.log("🚩 variable",variables)//var

      //return payload.messageCreated.chatId === variables.chatId && userId !== payload.messageCreated.userId;

      //return payload.messageCreated.chatId === variables.chatId && userId !== payload.messageCreated.userId;

      //console.log("userid",userId)
      //console.log(payload.newuserid)

      return userId === payload.chatCreated.newuserid.toString()




    }
  })
  chatCreated(@CurrentUser() user:TokenPayload) {
    //console.log("aaaa",user)


    return this.chatService.chatCreated(user._id)
  }
}

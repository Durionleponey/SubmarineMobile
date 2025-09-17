import {Args, Context, Mutation, Query, Resolver, Subscription} from '@nestjs/graphql';
import { MessagesService } from './messages.service';
import {Message} from "./entities/message.entity";
import {Inject, UseGuards} from "@nestjs/common";
import {GqlAuthGuard} from "../../auth/guards/gql-auth.guard";
import {CreateChatInput} from "../dto/create-chat.input";
import {CreateMessageInput} from "./dto/create-message.input";
import {CurrentUser} from "../../auth/current-user.decorator";
import {TokenPayload} from "../../auth/token-payload.interface";
import {GetMessages} from "./dto/get-messages";
import {PubSub} from "graphql-subscriptions";
import {MessageCreatedArgs} from "../dto/message-created.args";
import { Request } from 'express';
import { GqlExecutionContext } from '@nestjs/graphql';

@Resolver(() => Message)
export class MessagesResolver {
  constructor(private readonly messagesService: MessagesService, @Inject('PUB_SUB') private readonly pubSub:PubSub) {}


  @Mutation(() => Message)
  @UseGuards(GqlAuthGuard)
  async createMessage(
      @Args('createMessageInput') createMessageInput: CreateMessageInput,
      @CurrentUser() user:TokenPayload
  ) {
    //console.log("hello from resolveur 🥳", createMessageInput);
    //console.log(user);
    return this.messagesService.createMessage(createMessageInput, user._id)
  }



  @Mutation(() => String)
  @UseGuards(GqlAuthGuard)
  async viewMessage(
      @Args('messageId', { type: () => String, nullable: true })messageId:string,
      @Args('chatId', { type: () => String })chatId:string,
      @CurrentUser() user:TokenPayload
  ) {
    //console.log("hello from resolveur 🥳", createMessageInput);
    //console.log(user);
    return this.messagesService.viewMessage(messageId, user._id, chatId)
  }



  @Query(() => [Message])
  @UseGuards(GqlAuthGuard)
  async getMessages(
      @Args() getMessageArgs: GetMessages,
      @CurrentUser() user: TokenPayload,
      @Context() context: { req: Request } // ← ici
  ) {
    const ip =
        typeof context.req.headers['x-forwarded-for'] === 'string'
            ? context.req.headers['x-forwarded-for']
            : context.req.ip || '0.0.0.0';

    return this.messagesService.getMessages(getMessageArgs, user._id, ip);
  }

  @Query(() => [String])
  @UseGuards(GqlAuthGuard)
  async getMessageViewers(
      @Args('messageId', { type: () => String })messageId:string,
      @Args('chatId', { type: () => String })chatId:string,
      @CurrentUser() user:TokenPayload
  ) {
    return this.messagesService.getMessageViewers(messageId, chatId, user._id);
  }

  @Subscription(() => Message, {
    filter:(payload, variables, context) => {//payload --> in the message, variables --> graphQL request

      const userId= context.req.user._id

      //console.log("🏓🏓",context.req.user._id);

      return payload.messageCreated.chatId === variables.chatId && userId !== payload.messageCreated.userId;
    }
  })

  messageCreated(@Args()chatId:MessageCreatedArgs, @CurrentUser() user:TokenPayload) {


    return this.messagesService.messageCreated(chatId,user._id)
  }


}


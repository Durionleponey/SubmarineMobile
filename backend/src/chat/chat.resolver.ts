import {Resolver, Query, Mutation, Args, Int, Subscription,Context} from '@nestjs/graphql';
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
import { GqlExecutionContext } from '@nestjs/graphql'; 

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
    // Notez que le décorateur @Context est ajouté pour le filtre
    filter:(payload, variables, context) => {

      // CORRECTION DU FILTRE : Accède à l'utilisateur depuis le contexte du WS
      // L'utilisateur est désormais sous context.user grâce à la configuration 'onConnect'
      const userId = context.user?._id; 

      if (!userId) {
          console.error("Erreur: Utilisateur non trouvé dans le contexte du filtre.");
          return false; 
      }

      // ... le reste de votre logique de filtre
      return userId === payload.chatCreated.newuserid.toString()
    }
  })
chatCreated(@Context() context: any) { 
    
    // Accès direct à la propriété 'user'
    let user = context.user; 
    
    // Vérification alternative
    if (!user?._id) {
        user = context.connection?.context?.user;
    }
    
    if (!user?._id) {
        console.error("Erreur: Utilisateur non trouvé dans le contexte du résolveur de souscription.");
        return null;
    }
    
    // On passe l'ID utilisateur valide à la couche service
    return this.chatService.chatCreated(user._id)
  }
}

import {Inject, Injectable, NotFoundException, UseGuards} from '@nestjs/common';
import { CreateChatInput } from './dto/create-chat.input';
import { UpdateChatInput } from './dto/update-chat.input';
import {ChatRepository} from "./chat.repository";
import {GqlAuthGuard} from "../auth/guards/gql-auth.guard";
import {Chat} from "./entities/chat.entity";
import {string} from "joi";
import {UsersRepository} from "../users/users.repository";
import {MessageCreatedArgs} from "./dto/message-created.args";
import {PubSub} from "graphql-subscriptions";

@Injectable()
export class ChatService {

  constructor(private readonly chatRepository: ChatRepository, private readonly usersRepository: UsersRepository, @Inject('PUB_SUB') private readonly pubSub:PubSub) {} // i need a instance of chatRepostroy give it to me please



  async create(createChatInput: CreateChatInput, userId:string): Promise<Chat> {

   // if (!createChatInput.name) {throw new Error("Your input is empty")}

    //console.log("🛑🛑🛑🛑",createChatInput.name);

   // if (createChatInput.name.length>25) {{throw new Error("The name for the group must have maximum 25 caracters. but good try !😉")}}


    return this.chatRepository.create({...createChatInput, userId, userIds: createChatInput.userIds || [], messages:[],});
  }

  async findAll(userId:string) {
    //console.log("💿💿💿💿",userId);
    return await this.chatRepository.find({
      $or: [
        { userId },
        { userIds: { $in: [userId] } }
      ]
    })
/*    return chats.map(chat => ({
      ...chat,
      lastMessage: chat.messages?.[chat.messages.length - 1] ?? null,
    }));*/
  }

  async leaveAllChat(userId:string) {

    const guests = await this.chatRepository.updateMany(
        { userIds: { $in: [userId] } },
        { $pull: { userIds: userId } }
    );


    const creator = await this.chatRepository.updateMany(
        { userId },
        { $set: { userId: "noAdmin" } }
    );

    return (guests.modifiedCount + creator.matchedCount)
    ;

  }





  async findOne(_id: string) {
    return this.chatRepository.findOne({_id});
  }


  async addUserToChat(userId:string,email: string,chatId:string) {


    let user


    try {
      user = await this.usersRepository.findOne({email:email});

    } catch (err){

      try{

        user = await this.usersRepository.findOne({pseudo:email});

      }catch(err){
        throw new Error('User not found!');
      }


    }

    let chatName = await this.chatRepository.findOne({_id:chatId})



    if (
        chatName.userId === user._id.toString() || chatName.userIds.includes(user._id.toString())
    ) {
      throw new Error("User already in the chat!");
    }

    try{

      await this.chatRepository.findOneAndUpdate(
          {
            _id: chatId,
            $or: [//a leaste one of the two condition shoud be true
              { userId },
              { userIds: { $in: [userId] } }
            ]
          },
          {
            $push: {
              userIds: user._id,
            }

          }
      );

    }catch (err){

      throw new Error("unknow error are you trying to hack the system 😉? it's was a good try!");
    }

    //console.log("🤮",chatName)

    chatName["newuserid"] = user._id



    await this.pubSub.publish('chatCreated', {
      chatCreated: chatName
    })


      return("succes")
  }

  update(id: number, updateChatInput: UpdateChatInput) {
    return `This action updates a #${id} chat`;
  }

  remove(id: number) {
    return `This action removes a #${id} chat`;
  }



  async chatCreated(userId:String) {

/*    await this.chatRepository.findOne(
        {   //findOneAndUpdate take two argument, first is the filter and the second is the update
          //_id: chatId --> finding the correct chat to update
          _id: chatId,
          $or: [//a leaste one of the two condition shoud be true
            { userId },
            { userIds: { $in: [userId] } }
          ]
        })*/

    //console.log("🐺🐺--->",userId)
    return this.pubSub.asyncIterableIterator('chatCreated');

  }
}

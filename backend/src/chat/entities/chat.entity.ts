import { ObjectType, Field, Int } from '@nestjs/graphql';
import {AbstractEntity} from "../../common/database/abstact.entity";
import {Prop, Schema, SchemaFactory} from "@nestjs/mongoose";
import {Message} from "../messages/entities/message.entity";

@ObjectType()
@Schema()
export class Chat extends AbstractEntity{
  @Field()
  @Prop()
  userId: string

  @Field()
  @Prop()
  isPrivate: boolean;

  @Field(() => [String])//for non simple type we need to provide the type in field and in prop
  @Prop([String])
  userIds: string[];


  @Field( {nullable:true} )
  @Prop()
  name?: string;// it's optional because if the chat is private we don't need a name it's juste the name of the guy

  @Prop([Message])
  messages: Message[];

  @Field(() => Message, { nullable: true })
  lastMessage?: Message;

}


export const ChatSchema = SchemaFactory.createForClass(Chat);//use to transformt chat decorated with @schema to a real mongodbclass

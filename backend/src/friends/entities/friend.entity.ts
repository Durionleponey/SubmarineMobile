import { Field, ObjectType, ID } from '@nestjs/graphql';
import { Prop, Schema, SchemaFactory } from '@nestjs/mongoose';
import { Document, Types } from 'mongoose';
import { User } from '../../users/entities/user.entity';

export type FriendDocument = Friend & Document;

@Schema({ timestamps: true })
@ObjectType()
export class Friend {
  @Field(() => ID)
  _id: string;

  @Prop({ type: Types.ObjectId, ref: 'User', required: true })
  @Field(() => User)
  sender: User;

  @Prop({ type: Types.ObjectId, ref: 'User', required: true })
  @Field(() => User)
  receiver: User;

  @Prop({ required: true })
  @Field()
  status: string;

  @Field(() => Date)
  createdAt: Date;

  @Field(() => Date)
  updatedAt: Date;
}

export const FriendSchema = SchemaFactory.createForClass(Friend);

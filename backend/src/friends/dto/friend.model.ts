import { ObjectType, Field, ID } from '@nestjs/graphql';
import { User } from '../../users/entities/user.entity';

@ObjectType()
export class Friend {
  @Field(() => ID)
  _id: string;

  @Field(() => User)
  sender: User;

  @Field(() => User)
  receiver: User;

  @Field()
  status: string;

  @Field(() => Date)
  createdAt: Date;

  @Field(() => Date)
  updatedAt: Date;
}


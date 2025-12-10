import { Field, ID, ObjectType } from '@nestjs/graphql';
import { User } from '../../users/entities/user.entity';

@ObjectType()
export class FriendItem {
  @Field(() => ID)
  relationId: string;

  @Field(() => User)
  user: User;
}

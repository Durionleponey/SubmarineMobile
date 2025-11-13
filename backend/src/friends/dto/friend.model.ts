import { ObjectType, Field, ID } from '@nestjs/graphql';

@ObjectType()
export class Friend {
  @Field(() => ID)
  _id: string;

  // On expose juste les IDs au lieu d'objets User
  @Field(() => ID)
  sender: string;

  @Field(() => ID)
  receiver: string;

  @Field()
  status: string;

  @Field()
  createdAt: Date;

  @Field()
  updatedAt: Date;
}

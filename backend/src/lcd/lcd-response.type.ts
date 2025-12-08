import { Field, ObjectType } from '@nestjs/graphql';

@ObjectType()
export class LcdMessageResult {
  @Field()
  success: boolean;

  @Field()
  message: string;
}

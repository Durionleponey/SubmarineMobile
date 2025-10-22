import { CreateUserInput } from './create-user.input';
import { InputType, Field, Int, PartialType } from '@nestjs/graphql';

@InputType()//inputType say that 'this class is for mutation'
export class UpdateUserBio extends PartialType(CreateUserInput) {
  @Field()
  bio: string;
}

import { CreateUserInput } from './create-user.input';
import { InputType, Field, Int, PartialType } from '@nestjs/graphql';

@InputType()//inputType say that 'this class is for mutation'
export class updateUserPseudo extends PartialType(CreateUserInput) {


@Field({ nullable: false })
  pseudo: string;
}

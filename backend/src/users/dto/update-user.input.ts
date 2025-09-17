import { CreateUserInput } from './create-user.input';
import { InputType, Field, Int, PartialType } from '@nestjs/graphql';

@InputType()//inputType say that 'this class is for mutation'
export class UpdateUserInput extends PartialType(CreateUserInput) {
  //@Field()
  //_id: string; i rm this because only the user can update they owns info
}

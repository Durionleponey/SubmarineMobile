import { InputType, Field } from '@nestjs/graphql';
import {IsEmail, IsStrongPassword} from 'class-validator';


@InputType()
export class CreateUserInput {


  @Field()
  @IsEmail()
  email: string;

  @Field()
  @IsStrongPassword()
  password: string;

  @Field({nullable : true})
  publicKey?: string;
}
//dta means DATA TRANSFERT OBJET BTW
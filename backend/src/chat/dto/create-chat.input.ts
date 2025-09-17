import { InputType, Int, Field } from '@nestjs/graphql';
import {IsArray, IsBoolean, IsNotEmpty, IsOptional, IsString} from "class-validator";
import {Transform} from "class-transformer";


@InputType()
export class CreateChatInput {
  @Field()
  @Transform(({value}) => value === "true") //if value === "true" <string> then its return true else false
  @IsBoolean()
  isPrivate: boolean;

  @Field(() => [String], {nullable:true})
  @IsArray()
  @IsString({each: true}) //each element of the array must be string
  @IsNotEmpty({each: true})
  @IsOptional()
  userIds?: string[];


  @Field({nullable:true})
  @IsNotEmpty()
  @IsString()
  @IsOptional()//if name is empty all the other validator are dropped
  name?: string;
}

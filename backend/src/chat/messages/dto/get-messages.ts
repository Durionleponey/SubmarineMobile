import {Args, ArgsType, Field} from "@nestjs/graphql";
import {IsNotEmpty} from "class-validator";

@ArgsType()
export class GetMessages {
    @Field()
    @IsNotEmpty()
    chatId: string;

}
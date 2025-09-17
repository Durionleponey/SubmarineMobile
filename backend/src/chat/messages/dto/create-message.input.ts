import {Field, InputType} from "@nestjs/graphql";
import {IsNotEmpty} from "class-validator";


@InputType()
export class CreateMessageInput {
    @Field()
    @IsNotEmpty()
    content:string;

    @Field()
    @IsNotEmpty()
    chatId:string;

    @Field({ nullable: true })
    latitude?: number;

    @Field({ nullable: true })
    longitude?: number;

    @Field({ nullable: true })
    radius?: number;

    @Field({ nullable: true })
    city?: string;
}
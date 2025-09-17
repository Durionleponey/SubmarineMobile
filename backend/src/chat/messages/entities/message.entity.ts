import {Field, ObjectType} from "@nestjs/graphql";
import {Prop, Schema} from "@nestjs/mongoose";
import {AbstractEntity} from "../../../common/database/abstact.entity";


@ObjectType()
@Schema()
export class Message extends AbstractEntity{
    @Field()
    @Prop()
    content:string

    @Field()
    @Prop()
    createdAt:Date;

    @Field()
    @Prop()
    userId:string;

    @Field()
    @Prop()
    userPseudo:string;

    @Field()
    @Prop()
    chatId:string;

    @Field(() => [String])
    @Prop()
    views:string[];


    @Field({ nullable: true })
    @Prop()
    latitude?: number;

    @Field({ nullable: true })
    @Prop()
    longitude?: number;

    @Field({ nullable: true })
    @Prop()
    radius?: number; // en km

    @Field({ nullable: true })
    @Prop()
    city?: string;

}
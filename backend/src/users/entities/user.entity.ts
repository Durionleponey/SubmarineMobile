import { Prop, Schema, SchemaFactory } from '@nestjs/mongoose';
import { AbstractEntity } from '../../common/database/abstact.entity';
import { Field, ObjectType } from '@nestjs/graphql';

@Schema({ _id: true, versionKey: false })
@ObjectType()
export class User extends AbstractEntity {

    @Prop()
    @Field()
    pseudo: string;

    @Prop()
    @Field()
    email: string;

    @Prop()
    password: string;

    @Prop({ maxlength: 150 })
    @Field({ nullable: true })
    bio: string;

    @Prop({ type: [String], default: [] })  // <-- IMPORTANT
    @Field(() => [String])
    friends: string[];
}

export const UserSchema = SchemaFactory.createForClass(User);

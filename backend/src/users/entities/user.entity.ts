import { Prop, Schema, SchemaFactory } from '@nestjs/mongoose';
import { AbstractEntity } from '../../common/database/abstact.entity';
import { Field, ObjectType, registerEnumType } from '@nestjs/graphql';

export enum UserStatus {
    ACTIVE = 'ACTIVE',
    DELETED = 'DELETED',
}

registerEnumType(UserStatus, {
    name: 'UserStatus',
});

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

    //---- E2EE

    @Prop()
    @Field({nullable: true})
    publicKey: string;

    @Prop({ 
        type: String, 
        enum: UserStatus,
        default: UserStatus.ACTIVE 
    })
    @Field(() => UserStatus, { defaultValue: UserStatus.ACTIVE })
    status: UserStatus;
}

export const UserSchema = SchemaFactory.createForClass(User);

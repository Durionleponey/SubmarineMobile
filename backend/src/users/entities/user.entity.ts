import {Prop, Schema, SchemaFactory} from "@nestjs/mongoose";
import {AbstractEntity} from "../../common/database/abstact.entity";//we grap the _id from de AbstractEntity
import {Field, ObjectType} from "@nestjs/graphql";


//we import our abstarct docuement to extend userDocument with our abstrat document

@Schema({ _id:true, versionKey: false})// delete the versionning in the table
@ObjectType()// this mean 'hey user is graphQL type' is not no resend to the request
export class User extends AbstractEntity {

    @Prop()//prop mean this thing have to be stored in the db prop -> proprity a proprety have to be stored
    @Field()//accessible via a graphQL request, field comme from expose field
    pseudo:string;

    @Prop()//prop mean this thing have to be stored in the db prop -> proprity a proprety have to be stored
    @Field()//accessible via a graphQL request, field comme from expose field
    email:string;

    @Prop()// here with a password we cannot expose it so no field || if you try to query pasword you will gate a error
    // @Field()//just to test
    password: string;

    @Prop({ maxlength: 150 })//prop mean this thing have to be stored in the db prop -> proprity a proprety have to be stored
    @Field()//accessible via a graphQL request, field comme from expose field
    bio: string;

}

export const UserSchema = SchemaFactory.createForClass(User)//convert the class for mongodb
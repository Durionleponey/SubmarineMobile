import {Prop, Schema} from "@nestjs/mongoose";
import {Types, SchemaTypes} from "mongoose";
import {Field, ID, ObjectType} from "@nestjs/graphql";

//a schema is use as a template because all doc have a id
//we will use reuse this in like a model to create other mougoo doc

@Schema()//this mean that it is a moogo shema or template as you like
@ObjectType({ isAbstract:true})
export class AbstractEntity {
    @Prop( {type: SchemaTypes.ObjectId} )
    @Field(() => ID)
    _id: Types.ObjectId;
}
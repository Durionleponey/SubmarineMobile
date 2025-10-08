// contact.model.ts
import { ObjectType, Field } from '@nestjs/graphql';

@ObjectType()
export class Contact {
  @Field()
  nom: string;

  @Field()
  numero: string;
}

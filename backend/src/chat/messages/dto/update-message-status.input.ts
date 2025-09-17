import { InputType, Field } from "@nestjs/graphql";
import { MessageStatus } from "../message-status.enum";

@InputType()
export class UpdateMessageStatusInput {
  @Field()
  messageId: string;

  @Field(() => MessageStatus)
  status: MessageStatus;
}

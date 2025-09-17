import {AuthGuard} from "@nestjs/passport";
import {ExecutionContext} from "@nestjs/common";
import {GqlExecutionContext} from "@nestjs/graphql";

export class GqlAuthGuard extends AuthGuard('jwt'){
    getRequest(context: ExecutionContext){
        const ctx = GqlExecutionContext.create(context)
        return ctx.getContext().req;
    }


}
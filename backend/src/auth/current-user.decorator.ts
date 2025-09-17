import { ExecutionContext, createParamDecorator } from '@nestjs/common';
import { User } from '../users/entities/user.entity';
import {GqlContextType, GqlExecutionContext} from "@nestjs/graphql";

// @ts-ignore
const getCurrentUserByContext = (context: ExecutionContext): User => {

    if (context.getType() === 'http'){
        return context.switchToHttp().getRequest().user;

    }else if(context.getType<GqlContextType>() === 'graphql'){
        return GqlExecutionContext.create(context).getContext().req.user;
    }//this decorator allow to retriv user data form a http request


};

export const CurrentUser = createParamDecorator(//this code convert getCurrent... to a decorator
    (_data: unknown, context: ExecutionContext) =>
        getCurrentUserByContext(context),
);

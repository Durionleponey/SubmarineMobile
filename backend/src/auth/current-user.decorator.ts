import { ExecutionContext, createParamDecorator } from '@nestjs/common';
import { User } from '../users/entities/user.entity';
import {GqlExecutionContext} from "@nestjs/graphql";

// @ts-ignore
type GqlContextType = 'http' | 'graphql' | 'ws' | 'subscriptions';


// src/auth/current-user.decorator.ts

const getCurrentUserByContext = (context: ExecutionContext): User | undefined => {
    
try {
const gqlContext = GqlExecutionContext.create(context).getContext();

        const req = gqlContext.req;
        
        if (req?.extra?.request?.user) {
           // console.log("SUCCESS WS: Utilisateur trouvé via req.extra.request.user");
            return req.extra.request.user;
        }

        // 2. CAS WS : Attaché à l'objet Socket lui-même (comme propriété simple)
        if (req?.extra?.socket?.user) {
           // console.log("SUCCESS WS: Utilisateur trouvé via req.extra.socket.user");
            return req.extra.socket.user;
        }

        // 3. CAS WS : Le contexte est directement sur la connexion WS (le plus robuste)
        if (gqlContext.connection?.context?.user) {
           // console.log("SUCCESS WS: Utilisateur trouvé via gqlContext.connection.context.user");
            return gqlContext.connection.context.user;
        }

        // 4. CAS HTTP/GQL standard
        if (gqlContext.req?.user) {
           // console.log("SUCCESS HTTP/GQL: Utilisateur trouvé via gqlContext.req.user");
            return gqlContext.req.user;
        }

    } catch (e) {
      //  console.error("Erreur lors de l'extraction du contexte GQL:", e);
    }
    
   // console.log(`FAIL: Utilisateur non trouvé. Type: ${context.getType()}`);
    return undefined;
};

export const CurrentUser = createParamDecorator(//this code convert getCurrent... to a decorator
    (_data: unknown, context: ExecutionContext) =>{
       return getCurrentUserByContext(context)}
);

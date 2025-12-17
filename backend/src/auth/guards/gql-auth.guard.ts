import { AuthGuard } from "@nestjs/passport";
import { ExecutionContext, Injectable } from "@nestjs/common";
import { GqlExecutionContext } from "@nestjs/graphql";
import { Observable } from 'rxjs';

@Injectable()
export class GqlAuthGuard extends AuthGuard('jwt') {
  canActivate(context: ExecutionContext): boolean | Promise<boolean> | Observable<boolean> {
    const gqlContext = GqlExecutionContext.create(context);
    const ctx = gqlContext.getContext();

    const user = ctx.extra?.user || ctx.user || (ctx.req && ctx.req.user);

    if (user) {
      ctx.req = ctx.req || {};
      ctx.req.user = user;
     // console.log("✅ GqlAuthGuard: Authentification WS réussie pour", user.email);
      return true; 
    }

    //console.log("❌ GqlAuthGuard: Pas d'user trouvé, tentative Passport HTTP...");
    return super.canActivate(context);
  }


    getRequest(context: ExecutionContext) {
        const ctx = GqlExecutionContext.create(context);
        return ctx.getContext().req;
    }
}
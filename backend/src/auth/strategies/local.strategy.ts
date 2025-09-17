import {Injectable, InternalServerErrorException, UnauthorizedException} from "@nestjs/common";
import {PassportStrategy} from "@nestjs/passport";
import {Strategy} from 'passport-local'
import {array} from "joi";
import passport from "passport";
import {UsersService} from "../../users/users.service";


@Injectable()
export class LocalStrategy extends PassportStrategy(Strategy) {//with a second parametter we can rename the stratgey
    constructor(private readonly userService:UsersService) {
        super({//we overwrite the parent class because by default bcrypt use heuuuuu usernameFied
            usernameField: 'email',

        });
    }


    async validate(email:string, password: string){
        try {
            return await this.userService.verifyUser(email, password);

        }
        catch(err){
            throw new UnauthorizedException(err);
        }
    }
}

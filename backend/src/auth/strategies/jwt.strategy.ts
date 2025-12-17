// src/auth/jwt.strategy.ts (MODIFIÉ)

import {Injectable} from "@nestjs/common";
import {PassportStrategy} from "@nestjs/passport";
import {ExtractJwt, Strategy} from "passport-jwt";
import { Request } from "express"; // Assurez-vous d'avoir cet import si vous utilisez 'Request'
import {ConfigService} from "@nestjs/config";
import {TokenPayload} from "../token-payload.interface";


// Fonction sécurisée pour extraire le Bearer Token
const extractBearerTokenSafely = (req: Request) => {
    // ⚠️ Vérification cruciale : Si req ou req.headers est undefined, on retourne null.
    // Cela empêche l'erreur "Cannot read properties of undefined".
    if (req && req.headers && req.headers.authorization) {
        const parts = req.headers.authorization.split(' ');
        if (parts.length === 2 && parts[0] === 'Bearer') {
            return parts[1]; // Retourne le token si le format Bearer est trouvé
        }
    }
    return null; // Échec propre
};


@Injectable()
export class JwtStrategy extends PassportStrategy(Strategy){
    constructor(configService: ConfigService) {
        super({
            jwtFromRequest: ExtractJwt.fromExtractors([
                // 1. Lecteur de cookie (déjà sécurisé car vous utilisez l'opérateur '?')
                (req) => req?.cookies?.Authentification, 
                
                // 2. Le NOUVEL extracteur Bearer Token sécurisé
                // Remplacement de ExtractJwt.fromAuthHeaderAsBearerToken() par notre fonction
                extractBearerTokenSafely as (req: Request) => string | null, 
            ]),
            
            secretOrKey: configService.getOrThrow('JWT_SECRET')
        });
    }

    validate(payload:TokenPayload){
        return payload;
    }
}
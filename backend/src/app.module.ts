import { Logger, Module, UnauthorizedException } from '@nestjs/common';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { ConfigModule } from '@nestjs/config';
import * as Joi from 'joi';
import { DatabaseModule } from './common/database/database.module';
import { ApolloDriver, ApolloDriverConfig } from '@nestjs/apollo';
import { GraphQLModule } from '@nestjs/graphql';
import { UsersModule } from './users/users.module';
import { LoggerModule } from 'nestjs-pino';
import { AuthModule } from './auth/auth.module';
import { ChatModule } from './chat/chat.module';
import { PubSubModule } from './common/pubsub/pubsub.module';
import { AuthService } from './auth/auth.service';
import { FriendsModule } from './friends/friends.module';
import { MailerModule } from '@nestjs-modules/mailer'; 
import { MailModule } from './mail/mail.module'; // <--- 1. AJOUTE L'IMPORT DU MODULE MAIL
import { LcdModule } from './lcd/lcd.module';

@Module({
  imports: [
    ConfigModule.forRoot({
      isGlobal: true,
      validationSchema: Joi.object({
        MONGODB_URI: Joi.string().required(),
      }),
    }),
    GraphQLModule.forRootAsync<ApolloDriverConfig>({
      driver: ApolloDriver,
      imports: [AuthModule],
      inject: [AuthService],
      useFactory: (authService: AuthService) => ({
        autoSchemaFile: true,
          context: (ctx) => {
            const user = 
              ctx.extra?.user || 
              ctx.user || 
              ctx.connectionParams?.user || 
              (ctx as any).user;

            if (user) {
             // console.log("🎯 USER ENFIN LOCALISÉ ! ID:", user._id);
              return { 
                req: { user }, 
                user 
              };
            }

           // console.log("❓ Keys dans ctx.extra:", ctx.extra ? Object.keys(ctx.extra) : "pas d'extra");
            return { req: ctx.req };
          },
        subscriptions: {
          'graphql-ws': {
          onConnect: (context: any) => {
            try {
                const userPayload = authService.verifyWs(context.connectionParams);
                
                context.user = userPayload; 
                if (context.extra) context.extra.user = userPayload;

               // console.log("✅ onConnect: User injecté manuellement");
                return { user: userPayload };     
              } catch (e) {
                throw new UnauthorizedException();
              }
            },
          },
        },
      }),
    }),
    DatabaseModule,
    UsersModule,
    LoggerModule.forRoot({
      pinoHttp: {
        transport: {
          target: 'pino-pretty',
          options: { singleLine: true },
        },
      },
    }),
    AuthModule,
    ChatModule,
    PubSubModule,
    LcdModule,

    // Configuration de l'envoi de mail
    MailerModule.forRoot({
      transport: {
        host: 'smtp.gmail.com',
        auth: {
          user: 'submarine.app.contact@gmail.com',
          pass: 'muejqlazswagcgbh', 
        },
      },
      defaults: {
        from: '"Submarine Team" <no-reply@submarine.com>',
      },
    }),

    MailModule, 
    FriendsModule,
  ],
  controllers: [AppController],
  providers: [AppService],
})
export class AppModule {}
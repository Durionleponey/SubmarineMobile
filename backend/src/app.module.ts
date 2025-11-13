import {Logger, Module, UnauthorizedException} from '@nestjs/common';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { ConfigModule } from '@nestjs/config'
import * as Joi from 'joi';
import {DatabaseModule} from "./common/database/database.module";
import { ApolloDriver, ApolloDriverConfig } from '@nestjs/apollo'
import {GraphQLModule} from "@nestjs/graphql";
import { UsersModule } from './users/users.module';
import {LoggerModule} from "nestjs-pino";
import {pinoHttp} from "pino-http";
import { AuthModule } from './auth/auth.module';
import { ChatModule } from './chat/chat.module';
import {PubSubModule} from "./common/pubsub/pubsub.module";
import {AuthService} from "./auth/auth.service";
import { FriendsModule } from './friends/friends.module';



@Module({
  imports: [
      ConfigModule.forRoot({
        isGlobal:true,
          validationSchema: Joi.object({
              MONGODB_URI: Joi.string().required(),
          })
      }),
      GraphQLModule.forRootAsync<ApolloDriverConfig>({
          driver: ApolloDriver,
          imports: [AuthModule],
          inject: [AuthService],
          useFactory: (authService: AuthService) => ({
              autoSchemaFile: true,
              subscriptions: {
                  'graphql-ws': {
                      onConnect: (context: any) => {//security check of day

                          try {
                              const request: Request = context.extra.request;
                              //console.log('✨✨✨',request);
                              const user = authService.verifyWs(request as any);

                              context.user = user;
                              //console.log('✨✨✨', user, '🥰🥰🥰');
                          } catch (err) {
                              //console.log('no auth cookie 👻👻👻👻👻👻');
                              new Logger().error(err);
                              throw new UnauthorizedException();
                          }
                      }
                  }
              },
          }),
      }),
      DatabaseModule,
      UsersModule,
      LoggerModule.forRoot({
          pinoHttp:{
              transport:{
                  target: "pino-pretty",
                  options:{singleLine:true}
              }
          }
      }),
      AuthModule,
      ChatModule,
      PubSubModule,
      FriendsModule
  ],
  controllers: [AppController],
  providers: [AppService],
})
export class AppModule {}

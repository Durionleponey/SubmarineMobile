import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';
import {ValidationPipe} from "@nestjs/common";
import {Logger} from "nestjs-pino"
import {ConfigService} from "@nestjs/config";//ce fichier c'est genre tt ce qui se passe à haut niveau en global les modules ectect
import * as cookieParser from "cookie-parser";

//here we can install the middelware in our stack

async function bootstrap() {
  const app = await NestFactory.create(AppModule, {bufferLogs: true});
  app.useGlobalPipes(
      new ValidationPipe()
  )
  app.useLogger(app.get(Logger))//for the log
  const configService = app.get(ConfigService);
  app.use(cookieParser());//to parse user cookie

  await app.listen(process.env.PORT ?? configService.getOrThrow('PORT'));//port config
}
bootstrap();//idk meaby to start the back

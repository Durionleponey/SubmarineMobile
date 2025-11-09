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

  app.enableCors({
    origin: ['http://localhost:3000', 'http://192.168.1.43'], // l’URL de ton front (ex: React)
    credentials: true, // 🔥 indispensable pour les cookies
  });
  

  await app.listen(process.env.PORT ?? configService.getOrThrow('PORT'),
  '0.0.0.0');//port config
}
bootstrap();//idk meaby to start the back

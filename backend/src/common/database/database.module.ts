    import {Module} from "@nestjs/common";
    import {ModelDefinition, MongooseModule} from '@nestjs/mongoose'
    import {ConfigService} from "@nestjs/config"; //no clue about how it work but i just add a .env file and it's work so great
    import {model} from "mongoose";
    import {DbMigrationService} from "./db-migration.service";


    //connexion with the db with ven protection

    @Module({
        imports: [MongooseModule.forRootAsync({
            useFactory: (configService: ConfigService) => ({
                uri: configService.get('MONGODB_URI')
            }),
            inject: [ConfigService],
        })],
        providers:[DbMigrationService]
    })
    export class DatabaseModule {
        static forFeature(models: ModelDefinition[]) {
        return MongooseModule.forFeature(models);
        }
    }
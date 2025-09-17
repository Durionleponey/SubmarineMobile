import { Injectable, OnModuleInit } from "@nestjs/common";
import { ConfigService } from "@nestjs/config";
import { config, database, up } from "migrate-mongo";
import { Logger } from "nestjs-pino";
import * as path from "path";

@Injectable()
export class DbMigrationService implements OnModuleInit {
    private readonly dbMigrationConfig: Partial<config.Config>;

    constructor(private readonly configService: ConfigService) {
        this.dbMigrationConfig = {
            mongodb: {
                databaseName: this.configService.getOrThrow("DB_NAME"),
                url: this.configService.getOrThrow("MONGODB_URI"),
            },
            migrationsDir: path.join(__dirname, "/../../migrations"),
            changelogCollectionName: "changelogs",
            migrationFileExtension: ".js",
        };
    }

    async onModuleInit(): Promise<void> {

        try {
            config.set(this.dbMigrationConfig);

            const { db, client } = await database.connect();
            const migratedFiles = await up(db, client);

        } catch (error) {
        }
    }
}

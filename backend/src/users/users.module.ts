import { Module } from '@nestjs/common';
import { MongooseModule } from '@nestjs/mongoose';
import { UsersService } from './users.service';
import { UsersController } from './users.controller'; // Si tu l'utilises
import { User, UserSchema } from './entities/user.entity';
import { UsersRepository } from './users.repository';
// 👇 1. AJOUTE L'IMPORT DU RESOLVER
import { UsersResolver } from './users.resolver'; 

@Module({
  imports: [
    MongooseModule.forFeature([{ name: User.name, schema: UserSchema }]),
  ],
  controllers: [UsersController],
  providers: [
    UsersService, 
    UsersRepository,
    // 👇 2. IL MANQUAIT LUI ! C'est lui qui contient la mutation "createUser"
    UsersResolver 
  ],
  exports: [
    UsersService, 
    UsersRepository
  ], 
})
export class UsersModule {}
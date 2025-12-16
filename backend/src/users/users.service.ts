import {Injectable, UnauthorizedException,BadRequestException, NotFoundException} from '@nestjs/common';
import { CreateUserInput } from './dto/create-user.input';
import { UpdateUserInput } from './dto/update-user.input';
import {UsersRepository} from "./users.repository";
import * as bcrypt from 'bcryptjs'
import {uniqueNamesGenerator, adjectives, colors, animals} from 'unique-names-generator';
import {UpdateUserBio} from "./dto/update-user-input";
import {updateUserPseudo} from "./dto/update-user-pseudo";
import { UserStatus } from "./entities/user.entity";


@Injectable()
export class UsersService {
  constructor(private readonly userRepository: UsersRepository) {}

  async create(createUserInput: CreateUserInput) {
    //console.log("frffrrfe", createUserInput);

    const pseudoBeforeNumber = uniqueNamesGenerator({
      dictionaries: [adjectives, colors, animals],
      separator: '',
      style: 'capital',

    });

    const randomNumber = Math.floor(Math.random() * 9000) + 1000;
    const pseudo = `${pseudoBeforeNumber}${randomNumber}`;

    return this.userRepository.create({
      ...createUserInput,
      password: await this.hashPassword(createUserInput.password),
      pseudo,
      bio:"",
      friends: [],
      publicKey:"",
      status: UserStatus.ACTIVE,
    });
  }

  private async hashPassword(password:string){//bcrypt is a library
    return bcrypt.hash(password, 10)
  }

  async findAll(arg: string | { search?: string, status?: UserStatus }) {

    if (typeof arg === 'string') {
        const search = arg;
        console.log(`[LOG] Ancien findAll utilisé avec la recherche : "${search}"`);


      if (!search) {
        throw new UnauthorizedException("Type a character to search for a pseudo");
      }

      const r1 =await this.userRepository.find({
        pseudo: {// the $ in search is not related to mongo it's just regular js like ${hello}
          $regex: new RegExp(`^${search}`, 'i')//$ is a special operator, pseudo is a regular opertator options i is unsensible to the case
        }
      },5);

      const r2 =await this.userRepository.find({
        email: {// the $ in search is not related to mongo it's just regular js like ${hello}
          $regex: new RegExp(`^${search}`, 'i')//$ is a special operator, pseudo is a regular opertator options i is unsensible to the case
        }
      },5);

      console.log("-->",r2)
      const merged = [...r1, ...r2];
      return merged
    }

    const filters = arg;
    console.log(`[LOG] Nouveau findAll utilisé avec les filtres :`, filters);
    const mongoQuery: any = {};

    if (filters.status) {
        mongoQuery.status = filters.status;
    } else {
        // Par défaut, si aucun statut n'est donné, on retourne que les actifs.
        mongoQuery.status = UserStatus.ACTIVE;
    }

    if (filters.search) {
        mongoQuery.$or = [
            { pseudo: { $regex: filters.search, $options: 'i' } },
            { email: { $regex: filters.search, $options: 'i' } }
        ];
    }

    return this.userRepository.find(mongoQuery);

  }

  async deactivate(userId: string) {
    const user = await this.userRepository.findOneAndUpdate(
        { _id: userId },
        { $set: { status: UserStatus.DELETED } }
    );
    if (!user) {
        throw new NotFoundException(`Utilisateur avec l'ID ${userId} non trouvé.`);
    }
    return user;
  }

  async reactivate(userId: string) {
      const user = await this.userRepository.findOneAndUpdate(
          { _id: userId },
          { $set: { status: UserStatus.ACTIVE } }
      );
      if (!user) {
          throw new NotFoundException(`Utilisateur avec l'ID ${userId} non trouvé.`);
      }
      return user;
  }

  async findOne(id: string) {//mongo db's _id can be search as a string apparently
    return this.userRepository.findOne({ _id:id});
  }

  async findOneWithMail(mail: string) {//useless but i want to exercice
    return this.userRepository.findOne({ email:mail});
  }

  async update(_id: string, updateUserInput: UpdateUserInput) {
    if (updateUserInput.password) {
      updateUserInput.password = await bcrypt.hash(updateUserInput.password, 10);
    }
    return this.userRepository.findOneAndUpdate({ _id:_id}, {
      $set:{
        ...updateUserInput,//set update only the UserInputed field
      }
    })
  }

  async remove(_id: string) {
    return this.userRepository.findOneAndDelete({_id})//in fact here '_id:_id' or '_id' i'ts the same because there are the same but is faster to type '_id'
  }


  async verifyUser(email:string, password:string){
    const user = await this.userRepository.findOne({ email:email});
    //console.log(user);
    const passwordIsValid = await bcrypt.compare(password, user.password);

    if (!passwordIsValid) {
      throw new UnauthorizedException('Error Credentials not valid!');
    }
    return user;
  }
  async updateBio(_id: string, UpdateUserBio: UpdateUserBio) {
    if (UpdateUserBio.bio && UpdateUserBio.bio.length > 150) {
      throw new Error('Bio must not exceed 150 characters');
    }
    return this.userRepository.findOneAndUpdate({ _id: _id }, {
      $set: {
        ...UpdateUserBio, // set update only the UserInputed field
      }
    });
  }

  async updatePseudo(_id: string, updateUserPseudo: updateUserPseudo) {
    
    // 1. Vérification de la longueur
    if (updateUserPseudo.pseudo && updateUserPseudo.pseudo.length > 25) {
      throw new BadRequestException('Le pseudo ne doit pas dépasser 25 caractères');
    }

    // 2. Vérification de l'unicité (AVEC TRY/CATCH)
    try {
        const existingUser = await this.userRepository.findOne({ 
            pseudo: updateUserPseudo.pseudo 
        });

        // Si on arrive ici, c'est qu'un utilisateur a été trouvé.
        // On vérifie que ce n'est pas nous-même
        if (existingUser && existingUser._id.toString() !== _id.toString()) {
            throw new BadRequestException('Ce pseudo est déjà pris.');
        }

    } catch (error) {
        // C'est ici que la magie opère :
        // Si l'erreur est "Document not found", c'est une BONNE nouvelle : le pseudo est libre !
        // Si c'est une autre erreur (ex: Base de données éteinte), on la relance.
        if (error.response?.statusCode !== 404 && error.message !== 'Document not found.') {
            throw error;
        }
        // Sinon, on ignore l'erreur et on continue vers la mise à jour
    }

    // 3. Mise à jour
    return this.userRepository.findOneAndUpdate(
      { _id: _id }, 
      {
        $set: {
          ...updateUserPseudo, 
        }
      }
    );
  }



  async getBio(_id: string) {
    return this.userRepository.findOne({ _id: _id });
  }

  async findByPseudo(pseudo: string) {
    return this.userRepository.find({
      pseudo: { $regex: new RegExp(pseudo, 'i') },
    });
  }


}

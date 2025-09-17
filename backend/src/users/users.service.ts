import {Injectable, UnauthorizedException} from '@nestjs/common';
import { CreateUserInput } from './dto/create-user.input';
import { UpdateUserInput } from './dto/update-user.input';
import {UsersRepository} from "./users.repository";
import * as bcrypt from 'bcryptjs'
import {uniqueNamesGenerator, adjectives, colors, animals} from 'unique-names-generator';

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
    });
  }

  private async hashPassword(password:string){//bcrypt is a library
    return bcrypt.hash(password, 10)
  }

  async findAll(search: string) {

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
}

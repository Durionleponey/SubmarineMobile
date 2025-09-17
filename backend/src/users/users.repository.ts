import {Injectable, Logger} from "@nestjs/common";
import {AbstractRepository} from "../common/database/abstract.repository";
import {User} from "./entities/user.entity";
import {InjectModel} from "@nestjs/mongoose";
import {Model} from "mongoose";

@Injectable()
export class UsersRepository extends AbstractRepository<User>{
    protected readonly logger = new Logger(UsersRepository.name);

    constructor(@InjectModel(User.name) userModel: Model<User>) {super(userModel)}


    async findMailWithId(userId: { _id: string }): Promise<string | null> {
        const user = await this.model.findOne({_id:userId}, { email: true, _id: false }).lean();
        return user?.email ?? null; //if the value is null or undifined take the one on the left
    }

    async findPseudoWithId(userId: { _id: string }): Promise<string | null> {
        const user = await this.model.findOne({_id:userId}, { pseudo: true, _id: false }).lean();
        return user?.pseudo ?? null; //if the value is null or undifined take the one on the left
    }

}
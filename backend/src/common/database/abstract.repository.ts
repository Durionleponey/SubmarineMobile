import {AbstractEntity} from "./abstact.entity";
import {Logger, NotFoundException} from "@nestjs/common";
import {FilterQuery, Model, Types, UpdateQuery} from "mongoose";


//generic repository to interact with mongo DB



export abstract class AbstractRepository<TDocument extends AbstractEntity> {//this repo is use when the docu (mongofile) use the abstract entity template
    protected abstract readonly logger: Logger//debug stugg


    constructor(protected readonly model:Model<TDocument>) {}//master mongo model

    async create(document: Omit<TDocument, '_id'>): Promise<TDocument> {
        //console.log("Dfdfd:", document);

        const createdDocument = new this.model({
            ...document,
            _id: new Types.ObjectId(),
        });

        //console.log("Document après model instanciation:", createdDocument);
        return (await createdDocument.save()).toJSON() as unknown as TDocument;
    }




    //basic CRUD method

    async findOne(
        filterQuery: FilterQuery<TDocument>,
        projection?: Record<string, any>
    ): Promise<TDocument> {
        const document = await this.model.findOne(filterQuery, projection).lean<TDocument>();
        if (!document) {
            this.logger.warn("document was not found with filter query", filterQuery)
        }
        if (!document) {
            this.logger.warn('document was not found with filter query', filterQuery);
            throw new NotFoundException('Document not found.');
        }
        return document;
    }

    async findOneAndUpdate(filterQuery:FilterQuery<TDocument>,update: UpdateQuery<TDocument>): Promise<TDocument>{
        const document = await this.model.findOneAndUpdate(filterQuery, update, {
            new: true,
        }).lean<TDocument>();

/*        console.log("repository-->", document);
        console.log("fQuery-->", filterQuery);
        console.log("up", update);*/

        if (!document){
            this.logger.warn("", filterQuery)
            throw new NotFoundException('Document not found.');

        }
        return document;

    }

    async updateMany(
        filterQuery: FilterQuery<TDocument>,
        update: UpdateQuery<TDocument>
    ): Promise<{ matchedCount: number; modifiedCount: number }> {
        const result = await this.model.updateMany(filterQuery, update);

        return {
            matchedCount: result.matchedCount,
            modifiedCount: result.modifiedCount,
        };
    }

    async deleteMany(
        filterQuery: FilterQuery<TDocument>
    ): Promise<{ deletedCount: number }> {
        const result = await this.model.deleteMany(filterQuery);


        return {
            deletedCount: result.deletedCount,
        };
    }


    async find(
        filterQuery: FilterQuery<TDocument>,//refactor to limit the number of result
        limit?: number
    ): Promise<TDocument[]> {
        return this.model.find(filterQuery).limit(limit ?? 0).lean<TDocument[]>();
    }


    async findOneAndDelete(filterQuery:FilterQuery<TDocument>,): Promise<TDocument | null>{
        const document = await this.model.findOne(filterQuery).lean<TDocument>();


        if (!document){
            this.logger.warn("", filterQuery)
            throw new NotFoundException('Document not found.');

        }

        return this.model.findOneAndDelete(filterQuery).lean<TDocument>();
    }
}
import { Inject, Injectable } from '@nestjs/common';
import { ChatRepository } from "../chat.repository";
import { Chat } from "../entities/chat.entity";
import { CreateMessageInput } from "./dto/create-message.input";
import { Message } from "./entities/message.entity";
import { Types } from "mongoose";
import {GetMessages} from "./dto/get-messages";
import {PubSub} from "graphql-subscriptions";
import {string} from "joi";
import {MessageCreatedArgs} from "../dto/message-created.args";
import {UsersRepository} from "../../users/users.repository";
import {response} from "express";
import {ObjectId} from "mongodb";
import {getLocationFromIp} from "./dto/ip-localisation";

@Injectable()
export class MessagesService {
    constructor(private readonly chatRepository: ChatRepository, @Inject('PUB_SUB') private readonly pubSub:PubSub,private readonly usersRepository: UsersRepository) {}

    async createMessage({ content, chatId, latitude, longitude, radius, city }: CreateMessageInput, userId: string) {


        const userPseudo = await this.usersRepository.findPseudoWithId({_id:userId});

        //console.log(userPseudo)

        if (!userPseudo) {
            throw new Error("Impossible to math a email with UserId");
        }

        if (content.length < 1) {throw new Error("Message can't be empty");}
        if (content.length > 2000) {throw new Error("Message to long");}

        const views: string[] = [];

        const message: Message = {
            content,
            userId,
            userPseudo,
            chatId,
            views,
            createdAt: new Date(),
            _id: new Types.ObjectId(),
            latitude,
            longitude,
            radius,
            city,
        };

        //console.log(message)

        await this.chatRepository.findOneAndUpdate(
            {   //findOneAndUpdate take two argument, first is the filter and the second is the update
                //_id: chatId --> finding the correct chat to update
                _id: chatId,
                $or: [//a leaste one of the two condition shoud be true
                    { userId },
                    { userIds: { $in: [userId] } }
                ]
            },
            {
                $push: {
                    messages: message,
                }
            }
        );

        const chat = await this.chatRepository.findOne({_id:chatId});
        if (!chat) return;

        if (chat.messages.length >= 50) {

            await this.chatRepository.findOneAndUpdate(
                {   //findOneAndUpdate take two argument, first is the filter and the second is the update
                    //_id: chatId --> finding the correct chat to update
                    _id: chatId,
                },
                {
                    $pop: {
                        messages: -1,
                    }
                }
            );


        }
        //console.log("--->", message)


        /*        await this.pubSub.publish('messageCreated', {
                    messageCreated: 'pommes de terre'
                })*/


        if (message?.city){


            message.content = ("Message with geolocalisation restrictions please reload page to view")

        }

        await this.pubSub.publish('messageCreated', {
            messageCreated: message
        })


        return message;
    }

    async getMessages({ chatId }: GetMessages, userId: string, ip: string) {
        const userLocation = await getLocationFromIp(ip); // appel API
        const chat = await this.chatRepository.findOne({ _id: chatId });

        const messages = chat?.messages ?? [];

        const visibleMessages = messages
            .map((msg) => {
                if (msg.userId === userId || !msg.latitude || !msg.longitude || !msg.radius) return { ...msg, isVisible: true };
                const distance = getDistanceFromLatLonInKm(userLocation.lat, userLocation.lon, msg.latitude, msg.longitude);
                let isVisible = distance <= msg.radius;
                //                return { ...msg, isVisible };
                if (!isVisible) {
                    msg.content = "Not visible due to geolocalization restriction (visible nearby " + msg.city + "(" + msg.radius + "Km))";
                    isVisible = true;
                }
                return { ...msg, isVisible };
            })
            .filter((msg) => msg.isVisible)
            .sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime());

        return visibleMessages;
    }


    async messageCreated({chatId}: MessageCreatedArgs, userId:string) {

        await this.chatRepository.findOne(
            {   //findOneAndUpdate take two argument, first is the filter and the second is the update
                //_id: chatId --> finding the correct chat to update
                _id: chatId,
                $or: [//a leaste one of the two condition shoud be true
                    { userId },
                    { userIds: { $in: [userId] } }
                ]
            })
        return this.pubSub.asyncIterableIterator('messageCreated');

    }



    async viewMessage(messageId:string, userId:string, chatId:string) {

        try{
            await this.chatRepository.findOne(

                {   //findOneAndUpdate take two argument, first is the filter and the second is the update
                    //_id: chatId --> finding the correct chat to update
                    _id: chatId,
                    $or: [//a leaste one of the two condition shoud be true
                        { userId },
                        { userIds: { $in: [userId] } }
                    ]
                })
        }catch{throw new Error("You don't have access to this chat!")}

        const userPseudo = await this.usersRepository.findPseudoWithId({_id:userId});
        //console.log("--->", userPseudo);
        //console.log("---> message ID", messageId);

        if (!messageId){
            try{
                const rep = await this.chatRepository.findOneAndUpdate(
                    {
                        _id: chatId
                    },
                    {
                        $addToSet: { 'messages.$[].views': userPseudo }
                    }
                )


            }catch (e){throw new Error("Error viewing messages!")}


        }else{
            const rep =  await this.chatRepository.findOneAndUpdate(


                {
                    _id: chatId,
                    messages: {
                        $elemMatch: {
                            _id: new Types.ObjectId(messageId),
                            views: { $ne: userPseudo }}
                    }
                },
                {
                    $addToSet: { 'messages.$.views': userPseudo }
                },)
        }


        return("succes!")



    }


    async getMessageViewers(messageId:string, chatId:string, userId:string) {


        const message = await this.chatRepository.findOne({
            _id: new Types.ObjectId(chatId),
            $or: [
                { userId },
                { userIds: { $in: [userId] } }
            ]
        },{ messages: { $elemMatch: { _id: new Types.ObjectId(messageId) } } });

        //console.log("😱😱😱", message.messages[0].views)

        const user = await this.usersRepository.findOne({ _id:userId});



        return (message.messages[0].views).filter((x)=>{return x !== user.pseudo})
    }

}
function getDistanceFromLatLonInKm(lat1: number, lon1: number, lat2: number, lon2: number): number {
    const R = 6371; // Rayon de la Terre en km
    const dLat = deg2rad(lat2 - lat1);
    const dLon = deg2rad(lon2 - lon1);
    const a =
        Math.sin(dLat / 2) ** 2 +
        Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) *
        Math.sin(dLon / 2) ** 2;
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
}

function deg2rad(deg: number): number {
    return deg * (Math.PI / 180);
}

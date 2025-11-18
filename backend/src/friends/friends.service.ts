import {
  Injectable,
  BadRequestException,
  NotFoundException,
  UnauthorizedException,
} from '@nestjs/common';
import { InjectModel } from '@nestjs/mongoose';
import { Model } from 'mongoose';
import { Friend, FriendDocument } from './entities/friend.entity';

@Injectable()
export class FriendsService {
  constructor(
    @InjectModel(Friend.name) private friendModel: Model<FriendDocument>,
  ) {}

  async sendFriendRequest(senderId: string, receiverId: string) {
    if (senderId === receiverId) {
      throw new BadRequestException("Tu ne peux pas t'ajouter toi-même.");
    }

    const exists = await this.friendModel.findOne({
      $or: [
        { sender: senderId, receiver: receiverId },
        { sender: receiverId, receiver: senderId },
      ]
    });

    if (exists) {
      throw new BadRequestException('Une relation existe déjà entre ces deux utilisateurs.');
    }

    const newRequest = new this.friendModel({
      sender: senderId,
      receiver: receiverId,
      status: 'pending',
    });

    return newRequest.save();
  }


  async getPendingFriendRequests(userId: string) {
    return this.friendModel
      .find({ receiver: userId, status: 'pending' })
      .populate('sender')
      .populate('receiver')
      .exec();
  }

  async acceptFriendRequest(friendId: string, userId: string) {
    const request = await this.friendModel.findById(friendId);

    if (!request) {
      throw new NotFoundException('Demande introuvable.');
    }

    if (request.receiver.toString() !== userId) {
      throw new UnauthorizedException("Tu ne peux accepter que les demandes qui te sont destinées.");
    }

    request.status = 'accepted';
    await request.save();

    return request.populate('sender receiver');
  }

  async rejectFriendRequest(id: string) {
    return this.friendModel
      .findByIdAndUpdate(id, { status: 'rejected' }, { new: true })
      .populate('sender')
      .populate('receiver');
  }


  async getFriendsList(userId: string) {
    const relations = await this.friendModel
      .find({
        status: 'accepted',
        $or: [
          { sender: userId },
          { receiver: userId }
        ]
      })
      .populate('sender')
      .populate('receiver')
      .exec();

    return relations.map(rel => {
      return rel.sender._id.toString() === userId.toString()
        ? rel.receiver
        : rel.sender;
    });
  }





}

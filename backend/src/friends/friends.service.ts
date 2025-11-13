import { Injectable, BadRequestException } from '@nestjs/common';
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

    const exists = await this.friendModel.findOne({ sender: senderId, receiver: receiverId });
    if (exists) {
      throw new BadRequestException('Demande déjà envoyée.');
    }

    const newRequest = new this.friendModel({
      sender: senderId,
      receiver: receiverId,
      status: 'pending',
    });

    return newRequest.save();
  }

  async getPendingRequests(userId: string) {
    return this.friendModel
      .find({ receiver: userId, status: 'pending' })
      .populate('sender')
      .populate('receiver')
      .exec();
  }

  async acceptFriendRequest(id: string) {
    return this.friendModel.findByIdAndUpdate(id, { status: 'accepted' }, { new: true });
  }

  async rejectFriendRequest(id: string) {
    return this.friendModel.findByIdAndUpdate(id, { status: 'rejected' }, { new: true });
  }
}

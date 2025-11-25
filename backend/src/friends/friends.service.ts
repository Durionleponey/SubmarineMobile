import {
  Injectable,
  Inject,     // 👈 1. Ajoute Inject
  forwardRef, // 👈 2. Ajoute forwardRef
  BadRequestException,
  NotFoundException,
  UnauthorizedException,
} from '@nestjs/common';
import { InjectModel } from '@nestjs/mongoose';
import { Model } from 'mongoose';
import { Friend, FriendDocument } from './entities/friend.entity';
import { MailService } from '../mail/mail.service';
import { UsersService } from '../users/users.service';

@Injectable()
export class FriendsService {
  constructor(
    @InjectModel(Friend.name) private friendModel: Model<FriendDocument>,
    private mailService: MailService,
    
    // 👇 3. MODIFIE L'INJECTION ICI :
    @Inject(forwardRef(() => UsersService)) 
    private usersService: UsersService,
  ) {}

  // ... tout le reste de ton code ne change pas ...

  async sendFriendRequest(senderId: string, receiverId: string) {
    if (senderId === receiverId) {
      throw new BadRequestException("Tu ne peux pas t'ajouter toi-même.");
    }

    // Vérifier si relation existe déjà
    const exists = await this.friendModel.findOne({
      $or: [
        { sender: senderId, receiver: receiverId },
        { sender: receiverId, receiver: senderId },
      ]
    });

    if (exists) {
      throw new BadRequestException('Une relation existe déjà.');
    }

    // 1. Créer la demande en base
    const newRequest = new this.friendModel({
      sender: senderId,
      receiver: receiverId,
      status: 'pending',
    });
    const savedRequest = await newRequest.save();

    // 2. ENVOYER LE MAIL 📧
    try {
      // ⚠️ CORRECTION ICI : Utilise findById pour être sûr de trouver par l'ID
      // (Si ton UsersService n'a pas findById, remets findOne mais vérifie qu'il accepte un ID string)
      const sender = await this.usersService.findOne(senderId); 
      const receiver = await this.usersService.findOne(receiverId);

      if (receiver && receiver.email) {
        await this.mailService.sendFriendRequestEmail(
          receiver.email,
          sender.pseudo,
          savedRequest._id.toString()
        );
        console.log(`Email envoyé à ${receiver.email}`);
      }
    } catch (e) {
      console.error("Erreur envoi email:", e);
      // On ne bloque pas la demande si le mail échoue
    }

    return savedRequest;
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
    if (!request) throw new NotFoundException('Demande introuvable.');
    if (request.receiver.toString() !== userId) throw new UnauthorizedException("Interdit.");
    
    request.status = 'accepted';
    await request.save();
    return request.populate('sender receiver');
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
      return rel.sender['_id'].toString() === userId.toString()
        ? rel.receiver
        : rel.sender;
    });
  }

  // 👇👇👇 C'EST LA FONCTION QU'IL TE MANQUAIT POUR LE CONTROLLER 👇👇👇
  async acceptRequestByEmail(requestId: string) {
    const request = await this.friendModel.findById(requestId);
    
    if (!request) {
      throw new NotFoundException('Demande introuvable.');
    }
    
    // Si déjà accepté, on ne fait rien et on renvoie l'objet
    if (request.status === 'accepted') {
      return request;
    }

    request.status = 'accepted';
    return request.save();
  }
  // 👇 AJOUTE CETTE MÉTHODE
  async rejectFriendRequest(requestId: string) {
    const request = await this.friendModel.findById(requestId);
    
    if (!request) {
      throw new NotFoundException('Demande introuvable.');
    }

    // On passe le statut à "rejected"
    request.status = 'rejected';
    return request.save();
    
    // Alternative : Si tu préfères supprimer complètement la demande :
    // return this.friendModel.findByIdAndDelete(requestId);
  }
}

import { Resolver, Query, Mutation, Args, Int } from '@nestjs/graphql';
import { UsersService } from './users.service';
import { User } from './entities/user.entity';
import { CreateUserInput } from './dto/create-user.input';
import { UpdateUserInput } from './dto/update-user.input';
import {UseGuards} from "@nestjs/common";
import {GqlAuthGuard} from "../auth/guards/gql-auth.guard";
import {CurrentUser} from "../auth/current-user.decorator";
import {TokenPayload} from "../auth/token-payload.interface";
import * as sea from "node:sea";

@Resolver(() => User)
export class UsersResolver {
  constructor(private readonly usersService: UsersService) {}

  @Mutation(() => User)
  async createUser(@Args('createUserInput') createUserInput: CreateUserInput) {
    return this.usersService.create(createUserInput);
  }

  @Query(() => [User], { name: 'users' })
  @UseGuards(GqlAuthGuard)
  findAll(@Args('search', { type: () => String! }) search: string) {
    return this.usersService.findAll(search);
  }


  @Query(() => User, { name: 'user' })
  @UseGuards(GqlAuthGuard)//test
  findOne(@Args('id', { type: () => String }) id: string) {
    return this.usersService.findOne(id);
  }



  @Query(() => User, { name: 'userMail' })
  findOneWithMail(@Args('email', { type: () => String }) email: string) {
    return this.usersService.findOneWithMail(email);
  }

  @Mutation(() => User)
  @UseGuards(GqlAuthGuard)
  updateUser(
      @Args('updateUserInput') updateUserInput: UpdateUserInput,
      @CurrentUser() user: TokenPayload,
  ) {
    return this.usersService.update(user._id, updateUserInput);
  }


  @Mutation(() => User)
  @UseGuards(GqlAuthGuard)
  removeUser(
      //@Args('id') id: string,
      @CurrentUser() user: TokenPayload) {
    return this.usersService.remove(user._id);
  }

  @Query(() => User, { name: 'me' })
  @UseGuards(GqlAuthGuard)
  getMe(@CurrentUser() user: TokenPayload) {//getMe will return the current login user idk how it's work but it's work
    return user;
  }
/*
  @Mutation(() => User)
  @UseGuards(GqlAuthGuard, AdminGuard)
  removeUserAdmin(@Args('id') id: string) {
    return this.usersService.remove(id);
  }

 */

}

import { Injectable } from '@nestjs/common';
import { CreateUserDto } from './dto/create-user.dto';
import { UpdateUserDto } from './dto/update-user.dto';
import { InjectModel } from '@nestjs/mongoose';
import { Model } from 'mongoose';
import { User } from './entities/user.entity';

@Injectable()
export class UserService {
  constructor(@InjectModel(User.name) private UserModel: Model<User>) {}
  async create(createUserDto: CreateUserDto): Promise<User> {
    const createdUser = new this.UserModel(createUserDto);
    return createdUser.save();
  }
  async findAll(): Promise<User[]> {
    return this.UserModel.find().exec();
  }

  async findByEmail(email: string) {
    return this.UserModel.findOne({ email }).exec();
  }

  async findById(id: string) {
    return this.UserModel.findById(id).exec();
  }

  async update(id: number, updateUserDto: UpdateUserDto) {
    return this.UserModel.findByIdAndUpdate(id, updateUserDto, { new: true });
  }

  async remove(id: string) {
    return this.UserModel.findByIdAndDelete(id).exec();
  }
}

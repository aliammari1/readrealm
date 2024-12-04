import { BadRequestException, Injectable } from '@nestjs/common';
import { CreateUserDto } from './dto/create-user.dto';
import { UpdateUserDto } from './dto/update-user.dto';
import { InjectModel } from '@nestjs/mongoose';
import { Model } from 'mongoose';
import { User } from './entities/user.entity';
import * as bcrypt from 'bcryptjs';

@Injectable()
export class UserService {
  constructor(@InjectModel(User.name) private UserModel: Model<User>) { }
  async create(createUserDto: CreateUserDto) {
    // const createdUser = new this.UserModel(createUserDto);
    const { email, password, username } = createUserDto;

    const emailInUse = await this.findByEmail(email);
    if (emailInUse) {
      throw new BadRequestException('Email already in use');
    }

    const hashedPassword = await bcrypt.hash(password, 10);
    const newUser = new this.UserModel({
      username,
      email,
      password: hashedPassword,
    });

    newUser.save();

    return {
      message: 'User registered successfully',
      user: { email: newUser.email },
    };
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

  async update(id: string, updateUserDto: UpdateUserDto) {
    return this.UserModel.findByIdAndUpdate(id, updateUserDto, { new: true });
  }

  async remove(id: string) {
    return this.UserModel.findByIdAndDelete(id).exec();
  }
}

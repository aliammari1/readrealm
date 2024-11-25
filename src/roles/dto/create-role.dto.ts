import { ArrayUnique, IsEnum, IsString, ValidateNested } from 'class-validator';
import { permission } from 'process';
import { Resource } from '../enums/recource.enum';
import { Action } from '../enums/action.enum';

export class CreateRoleDto {
  @IsString()
  name: string;

  @ValidateNested()
  //@Type(()=> Permission)
  permissions: Permission[];
}

export class Permission {
  @IsEnum(Resource)
  resource: Resource;

  @IsEnum(Action, { each: true })
  @ArrayUnique()
  action: Action[];
}

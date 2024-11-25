// import { SetMetadata } from "@nestjs/common";
// import { Permission } from "src/roles/dto/create-role.dto";

import { SetMetadata } from '@nestjs/common';
import { Role } from 'src/roles/enums/role.enum';

// export const Permissions =(Permissions:Permission[])=>
//     SetMetadata('permissions',Permissions);

export const ROLES_KEY = 'roles';
export const Roles = (...roles: Role[]) => SetMetadata(ROLES_KEY, roles);

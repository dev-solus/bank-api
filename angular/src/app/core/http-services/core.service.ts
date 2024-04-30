import { environment } from 'environments/environment.development';
import { Injectable, inject } from '@angular/core';
import { UtilsService } from './utils.service';
import { HttpClient } from '@angular/common/http';


@Injectable({
    providedIn: 'root'
})
export class CoreService {
    http = inject(HttpClient);
    readonly utils = inject(UtilsService);

    readonly account = this.utils.extendClass<User, AccountsService>(AccountsService, environment.apiUrl, 'Accounts');

    readonly users = this.utils.extendClass<User, UsersService>(UsersService, environment.apiUrl , 'Users');
readonly roles = this.utils.extendClass<Role, RolesService>(RolesService, environment.apiUrl , 'Roles');
readonly accounts = this.utils.extendClass<Account, AccountsService>(AccountsService, environment.apiUrl , 'Accounts');
readonly operations = this.utils.extendClass<Operation, OperationsService>(OperationsService, environment.apiUrl , 'Operations');

}

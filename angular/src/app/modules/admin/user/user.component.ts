import { Component, ViewChild,Signal, AfterViewInit, ChangeDetectionStrategy, inject } from '@angular/core';
import { merge, Subject, switchMap, filter, map, startWith, tap, delay } from 'rxjs';
import { User } from 'app/core/api/core';
import { UowService, TypeForm } from 'app/core/http-services/uow.service';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { FuseAlertComponent } from '@fuse/components/alert';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatSelectModule } from '@angular/material/select';
import { toSignal } from '@angular/core/rxjs-interop';


import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { MyImageComponent } from '@fuse/upload-file/display-image/my-image.component';


@Component({
    standalone: true,
    selector: 'app-user',
    templateUrl: './user.component.html',
    styles: [``],
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        CommonModule,
        FuseAlertComponent,
        MatPaginatorModule,
        FormsModule,
        ReactiveFormsModule,
        MatSortModule,
        MatFormFieldModule,
        MatDatepickerModule,
        MatInputModule,
        MatButtonModule,
        MatSelectModule,
        MatIconModule,
        MatProgressSpinnerModule,
        ,
        MyImageComponent,
        RouterLink,
    ],
})
export class UserComponent implements AfterViewInit {
    //DI
    readonly uow = inject(UowService);
    readonly router = inject(Router);
    readonly route = inject(ActivatedRoute);
    

    @ViewChild(MatPaginator, { static: true })
    readonly paginator: MatPaginator;
    @ViewChild(MatSort, { static: true })
    readonly sort: MatSort;

    readonly update = new Subject<number>();

    public isLoadingResults = true;
    public totalRecords = 0;

    readonly viewInitDone = new Subject<void>();
    readonly dataSource: Signal<(User)[]> = toSignal(this.viewInitDone.pipe(
        delay(50),
        switchMap(_ => merge(
            this.sort.sortChange,
            this.paginator.page,
            this.update,
            this.#delete$,
        )),
        startWith(null as any),
        map(_ => [
            this.paginator.pageIndex * (this.paginator.pageSize ?? 10),// startIndex
            this.paginator.pageSize ?? 10,
            this.sort.active ? this.sort.active : 'id',
            this.sort.direction ? this.sort.direction : 'desc',
            this.firstname.value === '' ? '*' : this.firstname.value,
this.cin.value === '' ? '*' : this.cin.value,
this.email.value === '' ? '*' : this.email.value,
        ]),
        tap(e => this.isLoadingResults = true),
        switchMap(e => this.uow.core.users.getList(e).pipe(
            tap(e => this.totalRecords = e.count),
            map(e => e.list))
        ),
        tap(e => this.isLoadingResults = false),
    ), { initialValue: [] }) as any;

    readonly showMessage$ = new Subject<any>();

    readonly delete$ = new Subject<User>();
    readonly #delete$ = this.delete$.pipe(
        switchMap(item => this.uow.fuseConfirmation.open({ message: 'User' }).afterClosed().pipe(
            filter((e: 'confirmed' | 'cancelled') => e === 'confirmed'),
            tap(e => console.warn(e)),
            switchMap(_ => this.uow.core.users.delete(item.id).pipe(
                catchError(this.uow.handleError),
                map((e: any) => ({ code: e.code < 0 ? -1 : 1, message: e.code < 0 ? e.message : 'Enregistrement réussi' })),
                tap(r => this.showMessage$.next({ message: r.message, code: r.code })),
            )),
        )),
    );

    // select
    readonly roles$ = this.uow.core.roles.getForSelect$;

    readonly firstname = new FormControl('');
readonly cin = new FormControl('');
readonly email = new FormControl('');

    ngAfterViewInit(): void {
        this.viewInitDone.next();
    }

    trackById(index: number, item: any): number {
        return item.id; // Assuming "id" is a unique identifier for each item
    }

    reset() {
        this.firstname.setValue('');
this.cin.setValue('');
this.email.setValue('');

        this.update.next(0);
    }

    search() {
        this.update.next(0);
    }
    

    add() {
        this.router.navigate(['/admin/user', 0]);
    }

    edit(o: User) {
        this.router.navigate(['/admin/user', o.id]);
    }

    remove(o: User) {
        this.delete$.next(o);
    }

    
}

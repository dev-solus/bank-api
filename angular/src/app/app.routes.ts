import { Route } from '@angular/router';
import { initialDataResolver } from 'app/app.resolvers';
import { AuthGuard } from 'app/core/auth/guards/auth.guard';
import { NoAuthGuard } from 'app/core/auth/guards/noAuth.guard';
import { LayoutComponent } from 'app/layout/layout.component';

// @formatter:off
/* eslint-disable max-len */
/* eslint-disable @typescript-eslint/explicit-function-return-type */
export const appRoutes: Route[] = [

    // Redirect empty path to '/example'
    { path: '', pathMatch: 'full', redirectTo: '' },

    // Redirect signed-in user to the '/example'
    //
    // After the user signs in, the sign-in page will redirect the user to the 'signed-in-redirect'
    // path. Below is another redirection for that path to redirect the user to the desired
    // location. This is a small convenience to keep all main routes together here on this file.
    { path: 'signed-in-redirect', pathMatch: 'full', redirectTo: '' },

    // Auth routes for authenticated users
    {
        path: '',
        canActivate: [NoAuthGuard],
        canActivateChild: [NoAuthGuard],
        component: LayoutComponent,
        data: {
            layout: 'empty'
        },
        children: [
            // { path: 'sign-out', loadChildren: () => import('app/modules/auth/sign-out/sign-out.routes') },
            // { path: 'unlock-session', loadChildren: () => import('app/modules/auth/unlock-session/unlock-session.routes') },

            {
                path: '',
                loadChildren: () => import('app/modules/home/welcome.routes') },
        ]
    },

    // Auth routes for guests
    {
        path: '',
        canActivate: [NoAuthGuard],
        canActivateChild: [NoAuthGuard],
        component: LayoutComponent,
        data: {
            layout: 'empty'
        },
        children: [
            { path: 'confirmation-required', loadChildren: () => import('app/modules/auth/confirmation-required/confirmation-required.routes') },
            { path: 'forgot-password', loadChildren: () => import('app/modules/auth/forgot-password/forgot-password.routes') },
            { path: 'reset-password', loadChildren: () => import('app/modules/auth/reset-password/reset-password.routes') },
            { path: 'sign-in', loadChildren: () => import('app/modules/auth/sign-in/sign-in.routes') },
            { path: 'sign-up', loadChildren: () => import('app/modules/auth/sign-up/sign-up.routes') },
        ]
    },





    // Admin routes
    {
        path: 'admin',
        canActivate: [AuthGuard],
        canActivateChild: [AuthGuard],
        component: LayoutComponent,
        resolve: { initialData: initialDataResolver },
        children: [
            { path: '', pathMatch: 'full', redirectTo: 'product' },
            { path: 'user', loadChildren: () => import('app/modules/admin/user/user.routes') },
            { path: 'role', loadChildren: () => import('app/modules/admin/role/role.routes') },
            { path: 'job', loadChildren: () => import('app/modules/admin/job/job.routes') },
            { path: 'category-tree', loadChildren: () => import('app/modules/admin/category-tree/category-tree.routes') },
            { path: 'product', loadChildren: () => import('app/modules/admin/product/product.routes') },
            { path: 'order', loadChildren: () => import('app/modules/admin/order/order.routes') },
            { path: 'orderStatus', loadChildren: () => import('app/modules/admin/orderStatus/orderStatus.routes') },
            { path: 'paymentStatus', loadChildren: () => import('app/modules/admin/paymentStatus/paymentStatus.routes') },
            { path: 'paymentType', loadChildren: () => import('app/modules/admin/paymentType/paymentType.routes') },
            { path: 'paymentMethod', loadChildren: () => import('app/modules/admin/paymentMethod/paymentMethod.routes') },
        ]
    }
];

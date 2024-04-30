/* eslint-disable */
import { FuseNavigationItem } from '@fuse/components/navigation';

export const defaultNavigation: FuseNavigationItem[] = [
    {
        id: 'Marketplace',
        title: 'Marketplace',
        type: 'collapsable',
        icon: 'heroicons_outline:clipboard',
        subtitle: 'Admin Paramètres',
        // link: '',
        children: [
            // {
            //     id: 'Job',
            //     title: 'Job',
            //     type: 'basic',
            //     link: '/admin/job',
            // },
            {
                id: 'Category',
                title: 'Category',
                type: 'basic',
                link: '/admin/category-tree',
            },
            {
                id: 'Product',
                title: 'Produit',
                type: 'basic',
                link: '/admin/product',
            },
            {
                id: 'Order',
                title: 'Commande',
                type: 'basic',
                link: '/admin/order',
            },

        ]
    },
    {
        id: 'settings',
        title: 'Settings',
        type: 'collapsable',
        icon: 'heroicons_outline:cog',
        subtitle: 'Admin Paramètres',
        // link: '',
        children: [
            {
                id: 'User',
                title: 'Utilisateur',
                type: 'basic',
                link: '/admin/user',
            },
            {
                id: 'Role',
                title: 'Role',
                type: 'basic',
                link: '/admin/role',
            },


        ]
    },
    {
        id: 'management',
        title: 'management',
        type: 'collapsable',
        icon: 'heroicons_outline:adjustments-vertical',
        subtitle: 'Admin management',
        // link: '',
        children: [
            {
                id: 'OrderStatus',
                title: 'Statut de la commande',
                type: 'basic',
                link: '/admin/orderStatus',
            },
            {
                id: 'PaymentStatus',
                title: 'Statut de paiement',
                type: 'basic',
                link: '/admin/paymentStatus',
            },
            {
                id: 'PaymentType',
                title: 'Type de paiement',
                type: 'basic',
                link: '/admin/paymentType',
            },
            {
                id: 'PaymentMethod',
                title: 'Méthode de paiement',
                type: 'basic',
                link: '/admin/paymentMethod',
            },
        ]
    },


];
export const compactNavigation: FuseNavigationItem[] = [...defaultNavigation];
export const futuristicNavigation: FuseNavigationItem[] = [...defaultNavigation];
export const horizontalNavigation: FuseNavigationItem[] = [
    // {
    //     id: 'example',
    //     title: 'Profil',
    //     type: 'basic',
    //     icon: 'heroicons_outline:chart-pie',
    //     link: '/example'
    // },
];

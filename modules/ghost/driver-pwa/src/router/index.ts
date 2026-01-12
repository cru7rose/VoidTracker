import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            name: 'Home',
            component: LoginView
        },
        {
            path: '/login',
            name: 'Login',
            component: LoginView
        },
        {
            path: '/auth/magic/:token',
            name: 'MagicLogin',
            component: LoginView,
            props: true
        },
        {
            path: '/scan',
            name: 'Scan',
            component: () => import('../views/ScanView.vue')
        },
        {
            path: '/route',
            name: 'MyRoute',
            component: () => import('../views/MyRouteView.vue')
        }
    ]
})

export default router

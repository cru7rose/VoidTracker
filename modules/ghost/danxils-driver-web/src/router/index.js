import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import TaskBoardView from '../views/TaskBoardView.vue'
import VehicleCheckView from '../views/VehicleCheckView.vue'
import RouteView from '../views/RouteView.vue'
import NavigationMode from '../views/NavigationMode.vue'
import ScanView from '../views/ScanView.vue'

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/login',
            name: 'login',
            component: LoginView
        },
        {
            path: '/claim',
            name: 'claim',
            component: () => import('../views/ClaimRouteView.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/',
            redirect: '/check'
        },
        {
            path: '/check',
            name: 'check',
            component: VehicleCheckView,
            meta: { requiresAuth: true }
        },
        {
            path: '/tasks',
            name: 'tasks',
            component: TaskBoardView,
            meta: { requiresAuth: true }
        },
        {
            path: '/route',
            name: 'route',
            component: RouteView,
            meta: { requiresAuth: true }
        },
        {
            path: '/navigation',
            name: 'navigation',
            component: NavigationMode,
            meta: { requiresAuth: true }
        },
        {
            path: '/scan',
            name: 'scan',
            component: ScanView,
            meta: { requiresAuth: true }
        }
    ]
})

router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('driverToken')
    if (to.meta.requiresAuth && !token) {
        next('/login')
    } else if (to.path === '/login' && token) {
        next('/check')
    } else {
        next()
    }
})

export default router

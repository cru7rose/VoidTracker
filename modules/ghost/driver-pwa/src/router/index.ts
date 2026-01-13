import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import { useAuthStore } from '../stores/auth'

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            name: 'Home',
            redirect: '/route'
        },
        {
            path: '/login',
            name: 'Login',
            component: LoginView,
            meta: { requiresAuth: false }
        },
        {
            path: '/auth',
            name: 'Auth',
            component: LoginView,
            meta: { requiresAuth: false },
            beforeEnter: (to, from, next) => {
                // Handle magic link from query parameter
                const token = to.query.token as string
                if (token) {
                    next()
                } else {
                    next('/login')
                }
            }
        },
        {
            path: '/auth/magic/:token',
            name: 'MagicLogin',
            component: LoginView,
            props: true,
            meta: { requiresAuth: false }
        },
        {
            path: '/route',
            name: 'MyRoute',
            component: () => import('../views/MyRouteView.vue'),
            meta: { requiresAuth: true }
        },
        {
            path: '/scan',
            name: 'Scan',
            component: () => import('../views/ScanView.vue'),
            meta: { requiresAuth: true }
        }
    ]
})

// Route guard - check authentication
router.beforeEach(async (to, from, next) => {
    const authStore = useAuthStore()
    
    // Try to load session from IndexedDB if not already loaded
    if (!authStore.isAuthenticated) {
        await authStore.loadSession()
    }
    
    // Check if route requires authentication
    if (to.meta.requiresAuth && !authStore.isAuthenticated) {
        // Redirect to login, but save the intended destination
        next({
            path: '/login',
            query: { redirect: to.fullPath }
        })
    } else if (to.path === '/login' && authStore.isAuthenticated) {
        // If already logged in, redirect to route view
        next('/route')
    } else {
        next()
    }
})

export default router

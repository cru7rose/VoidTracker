import { createRouter, createWebHistory } from 'vue-router';

import { useAuthStore } from '../stores/authStore';
import BulkUpload from '../views/customer/BulkUpload.vue';
import AddressBook from '../views/customer/AddressBook.vue';
import InternalDashboard from '../views/internal/Dashboard.vue';
import Leaderboard from '../views/internal/driver/Leaderboard.vue';
import ZoneManager from '../views/internal/dispatch/ZoneManager.vue';
import AuditLogs from '../views/internal/admin/AuditLogs.vue';
import VehicleProfiles from '../views/internal/fleet/VehicleProfiles.vue';
import CarrierCompliance from '../views/internal/fleet/CarrierCompliance.vue';
import ReportBuilder from '../views/internal/analytics/ReportBuilder.vue';
import LensesView from '../views/LensesView.vue';

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            name: 'home',
            component: () => import('../views/Home.vue')
        },
        {
            path: '/tracking/:token',
            name: 'public-tracking',
            component: () => import('../views/public/TrackingView.vue'),
            meta: { public: true }
        },
        {
            path: '/forgot-password',
            name: 'forgot-password',
            component: () => import('../views/ForgotPassword.vue')
        },
        {
            path: '/reset-password',
            name: 'reset-password',
            component: () => import('../views/ResetPassword.vue')
        },

        // Customer Routes
        {
            path: '/customer/login',
            name: 'customer-login',
            component: () => import('../views/customer/Login.vue')
        },
        {
            path: '/customer/register',
            name: 'customer-register',
            component: () => import('../views/customer/Register.vue')
        },
        {
            path: '/customer',
            component: () => import('../components/Layout.vue'),
            meta: { requiresAuth: true, role: 'ROLE_CUSTOMER' },
            children: [
                {
                    path: 'dashboard',
                    name: 'customer-dashboard',
                    component: () => import('../views/customer/Dashboard.vue')
                },
                {
                    path: 'orders',
                    name: 'customer-orders',
                    component: () => import('../views/customer/OrderList.vue')
                },
                {
                    path: 'orders/create',
                    name: 'customer-create-order',
                    component: () => import('../views/customer/CreateOrder.vue')
                },
                {
                    path: 'bulk-upload',
                    name: 'customer-bulk-upload',
                    component: BulkUpload
                },
                {
                    path: 'address-book',
                    name: 'customer-address-book',
                    component: AddressBook
                },
                {
                    path: 'orders/:id',
                    name: 'customer-order-detail',
                    component: () => import('../views/customer/OrderDetail.vue')
                },
                {
                    path: 'profile',
                    name: 'customer-profile',
                    component: () => import('../views/customer/Profile.vue')
                },
                {
                    path: 'invoices',
                    name: 'customer-invoices',
                    component: () => import('../views/customer/Invoices.vue')
                }
            ]
        },
        // Internal Routes
        {
            path: '/internal/login',
            name: 'internal-login',
            component: () => import('../views/internal/Login.vue')
        },
        {
            path: '/driver',
            component: () => import('../views/driver/DriverLayout.vue'),
            children: [
                {
                    path: 'login',
                    name: 'DriverLogin',
                    component: () => import('../views/driver/DriverLogin.vue')
                },
                {
                    path: 'inspection',
                    name: 'DriverInspection',
                    component: () => import('../views/driver/DriverInspection.vue'),
                    meta: { role: 'DRIVER' }
                },
                {
                    path: 'expenses',
                    name: 'TripExpenses',
                    component: () => import('../views/driver/TripExpenses.vue'),
                    meta: { role: 'DRIVER' }
                },
                {
                    path: 'onboarding',
                    name: 'DriverOnboarding',
                    component: () => import('../views/driver/OnboardingView.vue'),
                    meta: { role: 'DRIVER' }
                },
                {
                    path: 'tasks',
                    name: 'DriverTasks',
                    component: () => import('../views/driver/DriverTaskView.vue'),
                    meta: { role: 'DRIVER' }
                }
            ]
        },
        {
            path: '/internal',
            component: () => import('../components/Layout.vue'),
            meta: { requiresAuth: true, role: 'ROLE_ADMIN' },
            children: [
                {
                    path: 'dashboard',
                    name: 'internal-dashboard',
                    component: () => import('../views/internal/Dashboard.vue')
                },
                {
                    path: 'lenses',
                    name: 'lenses',
                    component: LensesView
                },
                {
                    path: 'tile-dashboard',
                    name: 'tile-dashboard',
                    component: () => import('../views/internal/TileDashboardView.vue')
                },
                {
                    path: 'orders',
                    name: 'internal-orders',
                    component: () => import('../views/internal/OrderList.vue')
                },
                {
                    path: 'orders/:id',
                    name: 'internal-order-detail',
                    component: () => import('../views/internal/OrderDetail.vue')
                },
                {
                    path: 'manifests',
                    name: 'internal-manifests',
                    component: () => import('../views/internal/Manifests.vue')
                },
                {
                    path: 'map',
                    name: 'internal-map',
                    component: () => import('../views/internal/LiveMap.vue')
                },
                {
                    path: 'tracking',
                    name: 'internal-tracking',
                    component: () => import('../views/internal/LiveMap.vue')
                },
                {
                    path: 'config',
                    name: 'internal-config',
                    component: () => import('../views/internal/OrderConfig.vue')
                },
                {
                    path: 'admin/audit-logs',
                    name: 'AuditLogs',
                    component: AuditLogs
                },
                {
                    path: 'fleet/profiles',
                    name: 'VehicleProfiles',
                    component: VehicleProfiles
                },
                {
                    path: 'fleet/compliance',
                    name: 'CarrierCompliance',
                    component: CarrierCompliance
                },
                {
                    path: 'analytics/reports',
                    name: 'ReportBuilder',
                    component: ReportBuilder
                },
                {
                    path: 'dispatch',
                    name: 'DispatchBoard',
                    component: () => import('../views/internal/dispatch/DispatchBoard.vue')
                },
                {
                    path: 'dispatch/void-map',
                    name: 'VoidMap',
                    component: () => import('../views/internal/LiveMap.vue')
                },
                {
                    path: 'dispatch/zones',
                    name: 'ZoneManager',
                    component: ZoneManager
                },
                {
                    path: 'dispatch/assignments',
                    name: 'DispatchAssignments',
                    component: () => import('../views/internal/dispatch/Assignments.vue')
                },
                {
                    path: 'dispatch/history',
                    name: 'PlanHistory',
                    component: () => import('../views/internal/dispatch/PlanHistoryList.vue')
                },
                {
                    path: 'settings',
                    component: () => import('../views/internal/settings/SettingsLayout.vue'),
                    children: [
                        {
                            path: 'profiles',
                            name: 'OptimizationProfiles',
                            component: () => import('../views/internal/settings/OptimizationProfiles.vue')
                        },
                        {
                            path: 'rules',
                            name: 'BusinessRules',
                            component: () => import('../views/internal/settings/BusinessRules.vue')
                        },
                        {
                            path: 'views',
                            name: 'ViewConfigurations',
                            component: () => import('../views/internal/settings/ViewConfigurations.vue')
                        },
                        {
                            path: 'config',
                            name: 'SystemSettings',
                            component: () => import('../views/internal/settings/SystemSettings.vue')
                        },
                        {
                            path: 'milkruns',
                            name: 'MilkrunConfig',
                            component: () => import('../views/internal/settings/MilkrunConfig.vue')
                        },
                        {
                            path: 'workflows',
                            name: 'WorkflowManager',
                            component: () => import('../views/internal/settings/WorkflowManager.vue')
                        }
                    ]
                },
                {
                    path: '/internal/admin/audit',
                    name: 'AuditLog',
                    component: () => import('../views/internal/admin/AuditLogView.vue'),
                    meta: { role: 'ADMIN' }
                },
                {
                    path: '/internal/admin/communications',
                    name: 'CommunicationCenter',
                    component: () => import('../views/internal/admin/CommunicationCenter.vue'),
                    meta: { role: 'ADMIN' }
                },
                {
                    path: 'users',
                    name: 'UserList',
                    component: () => import('../views/internal/users/UserList.vue')
                },
                {
                    path: 'users/create',
                    name: 'UserCreate',
                    component: () => import('../views/internal/users/UserDetail.vue')
                },
                {
                    path: 'users/:userId',
                    name: 'UserDetail',
                    component: () => import('../views/internal/users/UserDetail.vue')
                },
                {
                    path: 'organizations',
                    name: 'OrganizationDashboard',
                    component: () => import('../views/internal/organizations/OrganizationDashboard.vue')
                },
                {
                    path: 'leaderboard',
                    name: 'Leaderboard',
                    component: Leaderboard
                }
            ]
        }
    ]
});

router.beforeEach((to, from, next) => {
    const authStore = useAuthStore();

    if (to.meta.requiresAuth && !authStore.isAuthenticated) {
        const isInternal = to.path.startsWith('/internal');
        next(isInternal ? '/internal/login' : '/customer/login');
    } else if (to.meta.role && authStore.user) {
        // Check if user has the required role (roles is an array/set from backend)
        // Ensure we handle Proxy objects correctly by creating a clean array
        const rawRoles = authStore.user.roles || [];
        const userRoles = Array.isArray(rawRoles) ? rawRoles : Array.from(rawRoles);

        const hasRole = userRoles.includes(to.meta.role) ||
            userRoles.includes(`ROLE_${to.meta.role}`) ||
            userRoles.includes('ROLE_ADMIN') || // Admin has access to everything
            // Super users have access to admin routes
            (to.meta.role === 'ROLE_ADMIN' && userRoles.includes('ROLE_SUPER_USER'));

        if (!hasRole) {
            console.warn(`User doesn't have required role: ${to.meta.role}. User roles:`, userRoles);
            next('/');
        } else {
            next();
        }
    } else {
        next();
    }
});

export default router;

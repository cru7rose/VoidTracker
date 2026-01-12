import { defineStore } from 'pinia';
import axios from 'axios';
import { ref, computed } from 'vue';

const IAM_BASE_URL = ''; // Use relative path to leverage Vite proxy

export const useAuthStore = defineStore('auth', () => {
    const token = ref(localStorage.getItem('token') || null);
    const refreshToken = ref(localStorage.getItem('refreshToken') || null);

    // Safely parse user from localStorage
    const storedUser = localStorage.getItem('user');
    const user = ref(storedUser && storedUser !== 'undefined' ? JSON.parse(storedUser) : null);

    const currentOrg = ref(localStorage.getItem('currentOrg') ? JSON.parse(localStorage.getItem('currentOrg')) : null);

    const isAuthenticated = computed(() => !!token.value);
    const userRole = computed(() => user.value?.role || null); // This might need adjustment based on org context
    const organizations = computed(() => user.value?.organizations || []);

    function setAuth(data) {
        const { accessToken, refreshToken: newRefreshToken, user: userData } = data;

        token.value = accessToken;
        refreshToken.value = newRefreshToken;
        user.value = userData;

        localStorage.setItem('token', accessToken);
        localStorage.setItem('refreshToken', newRefreshToken);
        localStorage.setItem('user', JSON.stringify(userData));

        // Set default organization if available
        if (userData.organizations && userData.organizations.length > 0) {
            // If previously selected org is still valid, keep it. Otherwise default to first.
            const validOrg = currentOrg.value && userData.organizations.find(o => o.orgId === currentOrg.value.orgId);
            if (validOrg) {
                currentOrg.value = validOrg;
            } else {
                currentOrg.value = userData.organizations[0];
            }
            localStorage.setItem('currentOrg', JSON.stringify(currentOrg.value));
        } else {
            currentOrg.value = null;
            localStorage.removeItem('currentOrg');
        }
    }

    async function login(username, password) {
        try {
            const response = await axios.post(`${IAM_BASE_URL}/api/auth/login`, {
                username,
                password
            });

            setAuth(response.data);

            return { success: true, user: response.data.user };
        } catch (error) {
            console.error('Login failed:', error);
            return {
                success: false,
                error: error.response?.data?.message || 'Login failed. Please check your credentials.'
            };
        }
    }

    async function refreshAccessToken() {
        if (!refreshToken.value) {
            logout();
            return false;
        }

        try {
            const response = await axios.post(`${IAM_BASE_URL}/api/auth/refresh`, {
                refreshToken: refreshToken.value
            });

            const { accessToken } = response.data;
            token.value = accessToken;
            localStorage.setItem('token', accessToken);

            return true;
        } catch (error) {
            console.error('Token refresh failed:', error);
            logout();
            return false;
        }
    }

    function logout() {
        token.value = null;
        refreshToken.value = null;
        user.value = null;
        currentOrg.value = null;
        localStorage.removeItem('token');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('user');
        localStorage.removeItem('currentOrg');

        // Redirect to appropriate login page
        const currentPath = window.location.pathname;
        if (currentPath.startsWith('/internal')) {
            window.location.href = '/internal/login';
        } else {
            window.location.href = '/customer/login';
        }
    }

    function switchOrganization(orgId) {
        const org = organizations.value.find(o => o.orgId === orgId);
        if (org) {
            currentOrg.value = org;
            localStorage.setItem('currentOrg', JSON.stringify(org));
            // Ideally reload or trigger re-fetch of data
            window.location.reload();
        }
    }

    function hasRole(role) {
        // Check global role or org-specific role if needed
        // For now, simple check on user.roles (which are global/default)
        // In future, this should check currentOrg.role
        return userRole.value === role;
    }

    function hasAnyRole(roles) {
        return roles.includes(userRole.value);
    }

    return {
        token,
        refreshToken,
        user,
        currentOrg,
        organizations,
        isAuthenticated,
        userRole,
        login,
        logout,
        refreshAccessToken,
        switchOrganization,
        refreshAccessToken,
        switchOrganization,
        hasRole,
        hasAnyRole,
        setAuth
    };
});

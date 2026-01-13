import { ref } from 'vue';

const toasts = ref([]);

export function useToast() {
    const addToast = (message, type = 'info', duration = 5000, title = null) => {
        const id = Date.now() + Math.random();
        toasts.value.push({ 
            id, 
            message, 
            type,
            title: title || getDefaultTitle(type)
        });

        if (duration > 0) {
            setTimeout(() => {
                removeToast(id);
            }, duration);
        }
        
        return id;
    };

    const removeToast = (id) => {
        const index = toasts.value.findIndex(t => t.id === id);
        if (index > -1) {
            toasts.value.splice(index, 1);
        }
    };

    const success = (msg, title = 'Success') => addToast(msg, 'success', 5000, title);
    const error = (msg, title = 'Error') => addToast(msg, 'error', 7000, title);
    const warning = (msg, title = 'Warning') => addToast(msg, 'warning', 6000, title);
    const info = (msg, title = 'Info') => addToast(msg, 'info', 5000, title);

    function getDefaultTitle(type) {
        const titles = {
            success: 'Success',
            error: 'Error',
            warning: 'Warning',
            info: 'Info'
        };
        return titles[type] || 'Notification';
    }

    return {
        toasts,
        addToast,
        removeToast,
        success,
        error,
        warning,
        info,
        show: addToast // Alias for compatibility
    };
}

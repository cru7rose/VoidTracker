export default {
    async getConfig() {
        // Simulate API call
        return new Promise(resolve => {
            setTimeout(() => {
                resolve({
                    showStartRoute: true,
                    showScanner: true,
                    showNavigation: true,
                    enableCashCollection: true,
                    requireSignature: false,
                    requirePhoto: true
                })
            }, 500)
        })
    }
}

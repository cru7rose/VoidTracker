import axios from 'axios';

/**
 * Sends a natural language command to the backend.
 * @param {string} query The command query.
 * @returns {Promise<{action: string, target: string, message: string}>} The parsed command response.
 */
export async function sendCommand(query) {
    if (!query || !query.trim()) {
        throw new Error("Query cannot be empty");
    }
    const response = await axios.post('/api/dashboard/command', { query });
    return response.data;
}

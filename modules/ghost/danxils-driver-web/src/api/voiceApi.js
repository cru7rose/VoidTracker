import axios from 'axios';

const API_URL = import.meta.env.VITE_API_URL || '/api';

export const voiceApi = {
    async transcribe(audioBlob) {
        const formData = new FormData();
        formData.append('file', audioBlob, 'recording.webm');

        const response = await axios.post(`${API_URL}/voice/transcribe`, formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        });
        return response.data;
    },
};

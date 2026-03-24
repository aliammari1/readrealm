export default () => ({
  jwt: {
    secret: process.env.JWT_SECRET,
  },
  database: {
    connectionString: process.env.MONGO_URL,
  },
  mail: {
    host: process.env.MAIL_HOST,
    user: process.env.MAIL_USER,
    pass: process.env.MAIL_PASS,
  },
  azure: {
    tts: {
      key: process.env.AZURE_API_TTS_KEY,
      endpoint: process.env.AZURE_API_TTS_ENDPOINT,
      model: process.env.AZURE_API_TTS_MODEL,
    },
    realtime: {
      key: process.env.AZURE_API_REALTIME_KEY,
      endpoint: process.env.AZURE_API_REALTIME_ENDPOINT,
      model: process.env.AZURE_API_REALTIME_MODEL,
    },
  },
  gemini: {
    key: process.env.GEMINI_API_KEY,
    model: process.env.GEMINI_API_MODEL,
  },
});

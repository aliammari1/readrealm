export default () => ({
  jwt: {
    secret: process.env.JWT_SECRET,
  },
  database: {
    connectionString: process.env.MONGODB_URL ?? process.env.MONGO_URL,
  },
  mail: {
    host: process.env.MAIL_HOST,
    port: Number(process.env.MAIL_PORT ?? 587),
    user: process.env.MAIL_USER,
    pass: process.env.MAIL_PASS,
  },
  elevenlabs: {
    apiKey: process.env.ELEVENLABS_API_KEY,
    voiceId: process.env.ELEVENLABS_VOICE_ID,
    modelId: process.env.ELEVENLABS_MODEL_ID ?? 'eleven_multilingual_v2',
    agentId: process.env.ELEVENLABS_AGENT_ID,
  },
  gemini: {
    key: process.env.GEMINI_API_KEY ?? process.env.GOOGLE_AI_KEY,
    model:
      process.env.GEMINI_API_MODEL ??
      process.env.GOOGLE_AI_MODEL ??
      'gemini-2.0-flash',
  },
  cors: {
    origin: process.env.CORS_ORIGIN ?? 'http://localhost:3000',
  },
  server: {
    maxJsonBodySize: process.env.MAX_JSON_BODY_SIZE ?? '10mb',
  },
});

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
  ollama: {
    url: process.env.OLLAMA_URL ?? 'http://localhost:11434',
    model: process.env.OLLAMA_MODEL ?? 'qwen3:8b',
  },
  cors: {
    origin: process.env.CORS_ORIGIN ?? 'http://localhost:3000',
  },
  server: {
    maxJsonBodySize: process.env.MAX_JSON_BODY_SIZE ?? '10mb',
  },
});

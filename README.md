# 📚 Library App Backend - NestJS API

[![NestJS](https://img.shields.io/badge/NestJS-10.4+-E0234E.svg)](https://nestjs.com/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.6+-3178C6.svg)](https://www.typescriptlang.org/)
[![Node.js](https://img.shields.io/badge/Node.js-20+-339933.svg)](https://nodejs.org/)
[![MongoDB](https://img.shields.io/badge/MongoDB-8.9+-47A248.svg)](https://www.mongodb.com/)
[![Socket.IO](https://img.shields.io/badge/Socket.IO-4.8+-010101.svg)](https://socket.io/)
[![AI Powered](https://img.shields.io/badge/AI-Powered-FF6F00.svg)](#ai-features)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

> A powerful, AI-enhanced NestJS backend API for the Library Management System, featuring real-time communication, advanced AI integrations (OpenAI, Azure Cognitive Services, Google Generative AI), multimedia processing, and comprehensive authentication systems.

## ✨ Features

### 🤖 AI & Machine Learning
- **OpenAI Integration**: GPT-4 powered content generation and analysis
- **Azure Cognitive Services**: Speech-to-text, text-to-speech, and translation
- **Google Generative AI**: Advanced content processing and insights
- **Hugging Face Models**: Text classification and sentiment analysis
- **Smart Recommendations**: AI-powered book recommendations
- **Content Analysis**: Automated book categorization and tagging

### 🔐 Authentication & Security
- **JWT Authentication**: Secure token-based authentication
- **Passport Integration**: Multiple authentication strategies
- **Role-Based Access Control**: Fine-grained permission system
- **Password Encryption**: bcrypt-secured password hashing
- **API Rate Limiting**: Protection against abuse
- **Input Validation**: Class-validator for request validation

### 🌐 Real-Time Communication
- **WebSocket Support**: Socket.IO for real-time features
- **Live Notifications**: Instant user notifications
- **Real-time Chat**: Book discussion rooms
- **Live Updates**: Real-time data synchronization
- **Collaborative Features**: Multi-user book sharing

### 📊 Database & Storage
- **MongoDB Integration**: Mongoose ODM for data modeling
- **Schema Validation**: Type-safe database operations
- **Data Aggregation**: Complex queries and analytics
- **Indexing**: Optimized database performance
- **Backup & Recovery**: Automated data protection

### 🎵 Multimedia Processing
- **Audio Processing**: FFmpeg integration for audio conversion
- **Speech Recognition**: Microsoft Cognitive Services Speech SDK
- **Audio Format Support**: MP3, WAV, and other formats
- **Voice Commands**: Speech-to-text for accessibility
- **Audio Streaming**: Real-time audio processing

### 📧 Communication Services
- **Email Integration**: Nodemailer for email notifications
- **Template System**: Dynamic email templates
- **Bulk Messaging**: Mass communication capabilities
- **Notification Center**: Centralized messaging system

## 🚀 Quick Start

### Prerequisites

- **Node.js**: 20.0.0 or higher
- **npm**: 10.0.0 or higher (or yarn/pnpm)
- **MongoDB**: 6.0 or higher
- **TypeScript**: 5.6.0 or higher

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/aliammari1/libraryapp-nest-back.git
   cd libraryapp-nest-back
   ```

2. **Install dependencies**
   ```bash
   npm install
   # or
   yarn install
   # or
   pnpm install
   ```

3. **Environment Configuration**
   ```bash
   # Copy environment template
   cp .env.exemple .env
   
   # Edit environment variables
   nano .env
   ```

4. **Start the application**
   ```bash
   # Development mode
   npm run start:dev
   
   # Production mode
   npm run start:prod
   
   # Debug mode
   npm run start:debug
   ```

### Environment Setup

Create a `.env` file with the following configuration:

```env
# Database
MONGODB_URI=mongodb://localhost:27017/libraryapp
DATABASE_NAME=libraryapp

# JWT Configuration
JWT_SECRET=your_super_secure_jwt_secret
JWT_EXPIRES_IN=7d

# OpenAI Configuration
OPENAI_API_KEY=your_openai_api_key
OPENAI_MODEL=gpt-4-turbo-preview

# Azure Cognitive Services
AZURE_COGNITIVE_SERVICES_KEY=your_azure_key
AZURE_COGNITIVE_SERVICES_REGION=your_region
AZURE_SPEECH_KEY=your_speech_key

# Google Generative AI
GOOGLE_GENERATIVE_AI_KEY=your_google_ai_key

# Hugging Face
HUGGINGFACE_API_TOKEN=your_huggingface_token

# Email Configuration
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=your_email@gmail.com
SMTP_PASS=your_app_password

# Application
PORT=3000
NODE_ENV=development
API_VERSION=v1

# File Upload
MAX_FILE_SIZE=10MB
UPLOAD_DEST=./uploads

# Rate Limiting
RATE_LIMIT_TTL=60
RATE_LIMIT_LIMIT=100
```

## 🏗️ Project Architecture

### Directory Structure

```
src/
├── main.ts                    # Application entry point
├── app.module.ts              # Root application module
│
├── auth/                      # Authentication module
│   ├── auth.module.ts
│   ├── auth.service.ts
│   ├── auth.controller.ts
│   ├── guards/               # Authentication guards
│   ├── strategies/           # Passport strategies
│   └── decorators/           # Custom decorators
│
├── users/                     # User management
│   ├── users.module.ts
│   ├── users.service.ts
│   ├── users.controller.ts
│   ├── schemas/              # User schemas
│   └── dto/                  # Data transfer objects
│
├── books/                     # Book management
│   ├── books.module.ts
│   ├── books.service.ts
│   ├── books.controller.ts
│   ├── schemas/              # Book schemas
│   └── dto/                  # Book DTOs
│
├── ai/                        # AI services
│   ├── ai.module.ts
│   ├── openai/               # OpenAI integration
│   ├── azure/                # Azure Cognitive Services
│   ├── google/               # Google Generative AI
│   └── huggingface/          # Hugging Face models
│
├── realtime/                  # WebSocket services
│   ├── realtime.module.ts
│   ├── realtime.gateway.ts
│   ├── chat/                 # Chat functionality
│   └── notifications/        # Real-time notifications
│
├── multimedia/                # Media processing
│   ├── multimedia.module.ts
│   ├── audio/                # Audio processing
│   ├── speech/               # Speech services
│   └── converters/           # Format converters
│
├── common/                    # Shared utilities
│   ├── decorators/           # Custom decorators
│   ├── filters/              # Exception filters
│   ├── guards/               # Authorization guards
│   ├── interceptors/         # Request/response interceptors
│   ├── pipes/                # Validation pipes
│   └── utils/                # Utility functions
│
├── config/                    # Configuration
│   ├── configuration.ts      # App configuration
│   ├── database.config.ts    # Database configuration
│   └── validation.schema.ts  # Environment validation
│
└── schemas/                   # Database schemas
    ├── user.schema.ts
    ├── book.schema.ts
    ├── review.schema.ts
    └── category.schema.ts
```

### Core Architecture Patterns

#### Module Structure
```typescript
@Module({
  imports: [
    MongooseModule.forFeature([
      { name: Book.name, schema: BookSchema }
    ]),
    ConfigModule,
    HttpModule,
  ],
  controllers: [BooksController],
  providers: [
    BooksService,
    AIRecommendationService,
    BookAnalyticsService,
  ],
  exports: [BooksService],
})
export class BooksModule {}
```

#### Service Layer Pattern
```typescript
@Injectable()
export class BooksService {
  constructor(
    @InjectModel(Book.name) private bookModel: Model<Book>,
    private aiService: AIService,
    private cacheService: CacheService,
  ) {}

  async findAll(filters: BookFiltersDto): Promise<Book[]> {
    const query = this.buildQuery(filters);
    return this.bookModel.find(query).exec();
  }

  async getRecommendations(userId: string): Promise<Book[]> {
    const userPreferences = await this.getUserPreferences(userId);
    return this.aiService.generateRecommendations(userPreferences);
  }
}
```

#### Controller Layer
```typescript
@Controller('books')
@UseGuards(JwtAuthGuard)
@ApiBearerAuth()
export class BooksController {
  constructor(private booksService: BooksService) {}

  @Get()
  @ApiOperation({ summary: 'Get all books' })
  async findAll(@Query() filters: BookFiltersDto): Promise<Book[]> {
    return this.booksService.findAll(filters);
  }

  @Post()
  @UseInterceptors(FileInterceptor('cover'))
  @ApiConsumes('multipart/form-data')
  async create(
    @Body() createBookDto: CreateBookDto,
    @UploadedFile() coverFile: Express.Multer.File,
  ): Promise<Book> {
    return this.booksService.create(createBookDto, coverFile);
  }
}
```

## 🤖 AI Integration

### OpenAI Services
```typescript
@Injectable()
export class OpenAIService {
  private openai: OpenAI;

  constructor(private configService: ConfigService) {
    this.openai = new OpenAI({
      apiKey: this.configService.get<string>('OPENAI_API_KEY'),
    });
  }

  async generateBookSummary(bookContent: string): Promise<string> {
    const completion = await this.openai.chat.completions.create({
      model: 'gpt-4-turbo-preview',
      messages: [
        {
          role: 'system',
          content: 'You are a literary expert. Generate a concise book summary.',
        },
        {
          role: 'user',
          content: `Summarize this book content: ${bookContent}`,
        },
      ],
      max_tokens: 500,
      temperature: 0.7,
    });

    return completion.choices[0].message.content;
  }

  async generateRecommendations(userPreferences: any): Promise<string[]> {
    // AI-powered recommendation logic
    const prompt = this.buildRecommendationPrompt(userPreferences);
    const response = await this.openai.chat.completions.create({
      model: 'gpt-4-turbo-preview',
      messages: [{ role: 'user', content: prompt }],
    });

    return JSON.parse(response.choices[0].message.content);
  }
}
```

### Azure Cognitive Services
```typescript
@Injectable()
export class AzureSpeechService {
  private speechConfig: SpeechConfig;

  constructor(private configService: ConfigService) {
    this.speechConfig = SpeechConfig.fromSubscription(
      this.configService.get<string>('AZURE_SPEECH_KEY'),
      this.configService.get<string>('AZURE_SPEECH_REGION'),
    );
  }

  async speechToText(audioBuffer: Buffer): Promise<string> {
    const audioConfig = AudioConfig.fromWavFileInput(audioBuffer);
    const recognizer = new SpeechRecognizer(this.speechConfig, audioConfig);

    return new Promise((resolve, reject) => {
      recognizer.recognizeOnceAsync(
        (result) => {
          recognizer.close();
          resolve(result.text);
        },
        (error) => {
          recognizer.close();
          reject(error);
        },
      );
    });
  }

  async textToSpeech(text: string): Promise<Buffer> {
    const synthesizer = new SpeechSynthesizer(this.speechConfig);
    
    return new Promise((resolve, reject) => {
      synthesizer.speakTextAsync(
        text,
        (result) => {
          synthesizer.close();
          resolve(Buffer.from(result.audioData));
        },
        (error) => {
          synthesizer.close();
          reject(error);
        },
      );
    });
  }
}
```

### Google Generative AI
```typescript
@Injectable()
export class GoogleAIService {
  private genAI: GoogleGenerativeAI;

  constructor(private configService: ConfigService) {
    this.genAI = new GoogleGenerativeAI(
      this.configService.get<string>('GOOGLE_GENERATIVE_AI_KEY'),
    );
  }

  async analyzeBookContent(content: string): Promise<BookAnalysis> {
    const model = this.genAI.getGenerativeModel({ model: 'gemini-pro' });
    
    const prompt = `
      Analyze this book content and provide:
      1. Genre classification
      2. Reading difficulty level
      3. Key themes
      4. Target audience
      5. Similar books recommendations
      
      Content: ${content}
    `;

    const result = await model.generateContent(prompt);
    const response = await result.response;
    
    return JSON.parse(response.text());
  }
}
```

## 🌐 Real-Time Features

### WebSocket Gateway
```typescript
@WebSocketGateway({
  cors: {
    origin: '*',
  },
})
export class RealtimeGateway implements OnGatewayConnection, OnGatewayDisconnect {
  @WebSocketServer()
  server: Server;

  private connectedUsers = new Map<string, Socket>();

  handleConnection(client: Socket) {
    console.log(`Client connected: ${client.id}`);
  }

  handleDisconnect(client: Socket) {
    console.log(`Client disconnected: ${client.id}`);
    this.connectedUsers.delete(client.id);
  }

  @SubscribeMessage('joinRoom')
  handleJoinRoom(client: Socket, room: string) {
    client.join(room);
    client.emit('joinedRoom', room);
  }

  @SubscribeMessage('sendMessage')
  handleMessage(client: Socket, payload: ChatMessageDto) {
    this.server.to(payload.room).emit('newMessage', payload);
  }

  // Broadcast notifications to all connected clients
  broadcastNotification(notification: NotificationDto) {
    this.server.emit('notification', notification);
  }

  // Send notification to specific user
  sendToUser(userId: string, data: any) {
    const userSocket = this.getUserSocket(userId);
    if (userSocket) {
      userSocket.emit('userNotification', data);
    }
  }
}
```

### Chat Service
```typescript
@Injectable()
export class ChatService {
  constructor(
    @InjectModel(ChatMessage.name) private chatModel: Model<ChatMessage>,
    private realtimeGateway: RealtimeGateway,
  ) {}

  async sendMessage(messageDto: CreateMessageDto): Promise<ChatMessage> {
    const message = new this.chatModel(messageDto);
    await message.save();

    // Broadcast to room
    this.realtimeGateway.server
      .to(messageDto.room)
      .emit('newMessage', message);

    return message;
  }

  async getRoomMessages(room: string, page = 1, limit = 50): Promise<ChatMessage[]> {
    return this.chatModel
      .find({ room })
      .sort({ createdAt: -1 })
      .limit(limit * page)
      .populate('sender', 'username avatar')
      .exec();
  }
}
```

## 🎵 Multimedia Processing

### Audio Processing Service
```typescript
@Injectable()
export class AudioProcessingService {
  constructor(private configService: ConfigService) {}

  async convertToMp3(inputBuffer: Buffer, inputFormat: string): Promise<Buffer> {
    return new Promise((resolve, reject) => {
      const outputPath = `./temp/output_${Date.now()}.mp3`;
      
      ffmpeg()
        .input(stream.Readable.from(inputBuffer))
        .inputFormat(inputFormat)
        .audioCodec('libmp3lame')
        .audioBitrate(128)
        .format('mp3')
        .on('end', () => {
          const outputBuffer = fs.readFileSync(outputPath);
          fs.unlinkSync(outputPath); // Cleanup
          resolve(outputBuffer);
        })
        .on('error', (err) => {
          reject(err);
        })
        .save(outputPath);
    });
  }

  async extractAudioMetadata(buffer: Buffer): Promise<AudioMetadata> {
    return new Promise((resolve, reject) => {
      ffprobe(stream.Readable.from(buffer), (err, metadata) => {
        if (err) {
          reject(err);
        } else {
          resolve({
            duration: metadata.format.duration,
            bitrate: metadata.format.bit_rate,
            sampleRate: metadata.streams[0].sample_rate,
            channels: metadata.streams[0].channels,
          });
        }
      });
    });
  }
}
```

## 📊 Database Schema

### User Schema
```typescript
@Schema({ timestamps: true })
export class User {
  @Prop({ required: true, unique: true })
  email: string;

  @Prop({ required: true })
  username: string;

  @Prop({ required: true })
  password: string;

  @Prop({ enum: UserRole, default: UserRole.USER })
  role: UserRole;

  @Prop({ default: '' })
  avatar: string;

  @Prop({ type: [{ type: mongoose.Schema.Types.ObjectId, ref: 'Book' }] })
  favoriteBooks: Book[];

  @Prop({ type: Object })
  preferences: UserPreferences;

  @Prop({ default: Date.now })
  lastLogin: Date;

  @Prop({ default: true })
  isActive: boolean;
}

export const UserSchema = SchemaFactory.createForClass(User);
```

### Book Schema
```typescript
@Schema({ timestamps: true })
export class Book {
  @Prop({ required: true })
  title: string;

  @Prop({ required: true })
  author: string;

  @Prop({ required: true })
  isbn: string;

  @Prop()
  description: string;

  @Prop({ enum: BookGenre })
  genre: BookGenre;

  @Prop({ default: 0 })
  rating: number;

  @Prop({ default: 0 })
  reviewCount: number;

  @Prop()
  coverImage: string;

  @Prop()
  audioBookUrl: string;

  @Prop({ type: [String] })
  tags: string[];

  @Prop({ type: mongoose.Schema.Types.ObjectId, ref: 'User' })
  addedBy: User;

  @Prop({ default: true })
  isAvailable: boolean;

  @Prop({ type: Object })
  aiAnalysis: BookAIAnalysis;
}

export const BookSchema = SchemaFactory.createForClass(Book);
```

## 🧪 Testing

### Unit Testing
```typescript
describe('BooksService', () => {
  let service: BooksService;
  let model: Model<Book>;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [
        BooksService,
        {
          provide: getModelToken(Book.name),
          useValue: {
            find: jest.fn(),
            findById: jest.fn(),
            create: jest.fn(),
            findByIdAndUpdate: jest.fn(),
            findByIdAndDelete: jest.fn(),
          },
        },
      ],
    }).compile();

    service = module.get<BooksService>(BooksService);
    model = module.get<Model<Book>>(getModelToken(Book.name));
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });

  it('should create a book', async () => {
    const createBookDto: CreateBookDto = {
      title: 'Test Book',
      author: 'Test Author',
      isbn: '1234567890',
    };

    const mockBook = { ...createBookDto, _id: 'mockId' };
    jest.spyOn(model, 'create').mockResolvedValue(mockBook as any);

    const result = await service.create(createBookDto);
    expect(result).toEqual(mockBook);
    expect(model.create).toHaveBeenCalledWith(createBookDto);
  });
});
```

### Integration Testing
```typescript
describe('BooksController (e2e)', () => {
  let app: INestApplication;
  let authToken: string;

  beforeEach(async () => {
    const moduleFixture: TestingModule = await Test.createTestingModule({
      imports: [AppModule],
    }).compile();

    app = moduleFixture.createNestApplication();
    await app.init();

    // Get auth token
    const response = await request(app.getHttpServer())
      .post('/auth/login')
      .send({ email: 'test@example.com', password: 'password' });
    
    authToken = response.body.access_token;
  });

  it('/books (GET)', () => {
    return request(app.getHttpServer())
      .get('/books')
      .set('Authorization', `Bearer ${authToken}`)
      .expect(200)
      .expect((res) => {
        expect(Array.isArray(res.body)).toBe(true);
      });
  });

  it('/books (POST)', () => {
    return request(app.getHttpServer())
      .post('/books')
      .set('Authorization', `Bearer ${authToken}`)
      .send({
        title: 'Test Book',
        author: 'Test Author',
        isbn: '1234567890',
      })
      .expect(201);
  });
});
```

### Running Tests
```bash
# Unit tests
npm run test

# Watch mode
npm run test:watch

# Coverage
npm run test:cov

# E2E tests
npm run test:e2e

# Debug tests
npm run test:debug
```

## 🔧 API Documentation

### Authentication Endpoints
```typescript
// POST /auth/register
{
  "email": "user@example.com",
  "username": "user123",
  "password": "strongPassword"
}

// POST /auth/login
{
  "email": "user@example.com",
  "password": "strongPassword"
}

// Response
{
  "access_token": "jwt_token_here",
  "user": {
    "id": "user_id",
    "email": "user@example.com",
    "username": "user123",
    "role": "user"
  }
}
```

### Books API
```typescript
// GET /books?page=1&limit=10&search=query&genre=fiction
// GET /books/:id
// POST /books (with file upload for cover)
// PUT /books/:id
// DELETE /books/:id
// GET /books/:id/recommendations
// POST /books/:id/reviews
```

### AI Services API
```typescript
// POST /ai/analyze-book
{
  "content": "book content here"
}

// POST /ai/speech-to-text
// (multipart/form-data with audio file)

// POST /ai/text-to-speech
{
  "text": "Text to convert to speech"
}

// GET /ai/recommendations/:userId
```

### WebSocket Events
```typescript
// Client -> Server
{
  "event": "joinRoom",
  "data": "room_id"
}

{
  "event": "sendMessage",
  "data": {
    "room": "room_id",
    "message": "Hello world",
    "sender": "user_id"
  }
}

// Server -> Client
{
  "event": "newMessage",
  "data": {
    "id": "message_id",
    "message": "Hello world",
    "sender": {...},
    "timestamp": "2024-01-01T00:00:00Z"
  }
}
```

## 🚀 Deployment

### Docker Deployment
```dockerfile
# Dockerfile
FROM node:20-alpine

WORKDIR /app

COPY package*.json ./
RUN npm ci --only=production

COPY . .
RUN npm run build

EXPOSE 3000

CMD ["node", "dist/main"]
```

```yaml
# docker-compose.yml
version: '3.8'

services:
  app:
    build: .
    ports:
      - "3000:3000"
    environment:
      - NODE_ENV=production
      - MONGODB_URI=mongodb://mongo:27017/libraryapp
    depends_on:
      - mongo
    volumes:
      - ./uploads:/app/uploads

  mongo:
    image: mongo:6
    ports:
      - "27017:27017"
    volumes:
      - mongo_data:/data/db
    environment:
      - MONGO_INITDB_DATABASE=libraryapp

volumes:
  mongo_data:
```

### Cloud Deployment (Azure)
```bash
# Build for production
npm run build

# Deploy to Azure App Service
az webapp up --sku F1 --name libraryapp-backend

# Configure environment variables
az webapp config appsettings set --name libraryapp-backend \
  --settings MONGODB_URI=your_mongo_connection_string \
             JWT_SECRET=your_jwt_secret \
             OPENAI_API_KEY=your_openai_key
```

### Environment-Specific Configurations
```typescript
// config/configuration.ts
export default () => ({
  port: parseInt(process.env.PORT, 10) || 3000,
  database: {
    uri: process.env.MONGODB_URI,
    name: process.env.DATABASE_NAME,
  },
  jwt: {
    secret: process.env.JWT_SECRET,
    expiresIn: process.env.JWT_EXPIRES_IN || '7d',
  },
  ai: {
    openai: {
      apiKey: process.env.OPENAI_API_KEY,
      model: process.env.OPENAI_MODEL || 'gpt-4-turbo-preview',
    },
    azure: {
      speechKey: process.env.AZURE_SPEECH_KEY,
      speechRegion: process.env.AZURE_SPEECH_REGION,
    },
    google: {
      apiKey: process.env.GOOGLE_GENERATIVE_AI_KEY,
    },
  },
});
```

## 📈 Performance Optimization

### Caching Strategy
```typescript
@Injectable()
export class CacheService {
  private cache = new Map<string, any>();
  private readonly TTL = 1000 * 60 * 5; // 5 minutes

  set(key: string, value: any, ttl?: number): void {
    const expiresAt = Date.now() + (ttl || this.TTL);
    this.cache.set(key, { value, expiresAt });
  }

  get(key: string): any {
    const item = this.cache.get(key);
    if (!item) return null;

    if (Date.now() > item.expiresAt) {
      this.cache.delete(key);
      return null;
    }

    return item.value;
  }

  @Cron('0 */10 * * * *') // Every 10 minutes
  cleanup(): void {
    const now = Date.now();
    for (const [key, item] of this.cache.entries()) {
      if (now > item.expiresAt) {
        this.cache.delete(key);
      }
    }
  }
}
```

### Database Optimization
```typescript
// Indexing strategy
BookSchema.index({ title: 'text', author: 'text', description: 'text' });
BookSchema.index({ genre: 1, rating: -1 });
BookSchema.index({ createdAt: -1 });
BookSchema.index({ 'aiAnalysis.tags': 1 });

// Aggregation pipeline example
async getBookStatistics(): Promise<BookStatistics> {
  return this.bookModel.aggregate([
    {
      $group: {
        _id: '$genre',
        count: { $sum: 1 },
        avgRating: { $avg: '$rating' },
        totalReviews: { $sum: '$reviewCount' },
      },
    },
    {
      $sort: { count: -1 },
    },
  ]);
}
```

## 🛡️ Security Best Practices

### Input Validation
```typescript
// DTOs with validation
export class CreateBookDto {
  @IsNotEmpty()
  @IsString()
  @MaxLength(200)
  title: string;

  @IsNotEmpty()
  @IsString()
  @MaxLength(100)
  author: string;

  @IsNotEmpty()
  @IsISBN()
  isbn: string;

  @IsOptional()
  @IsString()
  @MaxLength(2000)
  description?: string;

  @IsOptional()
  @IsEnum(BookGenre)
  genre?: BookGenre;
}
```

### Rate Limiting
```typescript
@Controller('api')
@UseGuards(ThrottlerGuard)
export class ApiController {
  @Throttle(10, 60) // 10 requests per minute
  @Post('ai/analyze')
  async analyzeBook(@Body() data: AnalyzeBookDto) {
    // AI analysis logic
  }
}
```

### Authentication Guard
```typescript
@Injectable()
export class JwtAuthGuard extends AuthGuard('jwt') {
  canActivate(context: ExecutionContext) {
    return super.canActivate(context);
  }

  handleRequest(err: any, user: any, info: any) {
    if (err || !user) {
      throw err || new UnauthorizedException();
    }
    return user;
  }
}
```

## 🗺️ Roadmap

### Version 2.0 Features
- [ ] GraphQL API implementation
- [ ] Advanced AI analytics dashboard
- [ ] Microservices architecture
- [ ] Redis caching layer
- [ ] Elasticsearch integration
- [ ] Advanced file processing

### Version 2.1 Features
- [ ] Blockchain integration for book ownership
- [ ] Advanced recommendation algorithms
- [ ] Multi-language support
- [ ] Advanced analytics and reporting
- [ ] Integration with external library systems

## 🤝 Contributing

### Development Setup
```bash
# Install dependencies
npm install

# Start MongoDB
docker run -d -p 27017:27017 --name mongodb mongo:6

# Start development server
npm run start:dev

# Run tests
npm run test
npm run test:e2e
```

### Code Standards
```bash
# Linting
npm run lint

# Formatting
npm run format

# Type checking
npm run build
```

### Pull Request Guidelines
1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Make your changes and add tests
4. Ensure all tests pass: `npm run test`
5. Commit your changes: `git commit -m 'Add amazing feature'`
6. Push to branch: `git push origin feature/amazing-feature`
7. Submit a pull request

## 👥 Author

### Ali Ammari
**Senior Backend Developer & AI Integration Specialist**

- 📧 **Email**: ali.ammari.dev@gmail.com
- 💼 **LinkedIn**: [Ali Ammari](https://linkedin.com/in/ali-ammari)
- 🐙 **GitHub**: [@aliammari1](https://github.com/aliammari1)
- 🌐 **Website**: [aacoder.me](https://aacoder.me)

### Expertise
- 🎯 **Specialization**: NestJS, TypeScript, AI/ML integration, microservices
- 🏆 **Experience**: 4+ years in backend development and system architecture
- 🔧 **Skills**: Node.js, MongoDB, WebSocket, Docker, Cloud services
- 📱 **Focus**: Scalable APIs, real-time systems, AI-powered applications

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

```
MIT License

Copyright (c) 2024 Ali Ammari

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
```

## 🙏 Acknowledgments

### Technologies & Libraries
- **NestJS Framework** - For the powerful Node.js framework
- **OpenAI** - For GPT models and AI capabilities
- **Microsoft Azure** - For Cognitive Services and Speech SDK
- **Google AI** - For Generative AI services
- **MongoDB & Mongoose** - For database and ODM
- **Socket.IO** - For real-time communication

### AI & ML Platforms
- **Hugging Face** - For transformer models and NLP
- **FFmpeg** - For multimedia processing
- **LameJS** - For audio encoding
- **Nodemailer** - For email services

### Development Tools
- **TypeScript** - For type-safe development
- **Jest** - For testing framework
- **ESLint & Prettier** - For code quality
- **Docker** - For containerization

### Special Thanks
- **Open Source Community** - For excellent libraries and tools
- **AI Research Community** - For advancing AI technologies
- **NestJS Community** - For framework support and examples
- **Contributors** - For bug reports and feature suggestions

## 📞 Support

### Getting Help
- **Documentation**: Check the inline code comments and type definitions
- **Issues**: [GitHub Issues](https://github.com/aliammari1/libraryapp-nest-back/issues)
- **Discussions**: [GitHub Discussions](https://github.com/aliammari1/libraryapp-nest-back/discussions)
- **Email**: ali.ammari.dev@gmail.com

### API Documentation
- **Swagger UI**: Available at `/api/docs` when running in development
- **Postman Collection**: Import the API collection for testing
- **OpenAPI Spec**: Available at `/api/docs-json`

---

<div align="center">
  <p>Built with ❤️ and ☕ by <a href="https://github.com/aliammari1">Ali Ammari</a></p>
  <p>
    <a href="https://github.com/aliammari1/libraryapp-nest-back">⭐ Star this repo</a> •
    <a href="https://github.com/aliammari1/libraryapp-nest-back/issues">🐛 Report Bug</a> •
    <a href="https://github.com/aliammari1/libraryapp-nest-back/issues">✨ Request Feature</a>
  </p>
</div>
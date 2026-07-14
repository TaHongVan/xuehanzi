# 汉字 Hanzii - Chinese Learning Platform

Production-ready fullstack web application for learning Chinese (Vietnamese UI).

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 3.4.5, Java 25, Maven |
| Frontend | React 18, Vite, Ant Design |
| Database | SQL Server |
| Auth | JWT (USER / ADMIN roles) |
| Cache | Redis (optional) |
| AI | OpenAI API |
| Excel | Apache POI |
| Deploy | Docker Compose |

## Project Structure

```
hanziiii/
├── backend/                 # Spring Boot REST API
│   └── src/main/java/com/hanzii/
│       ├── config/          # Security, Redis, auditing, async, cache
│       ├── controller/      # REST controllers
│       ├── dto/             # Request/Response DTOs
│       ├── entity/          # JPA entities
│       ├── exception/       # Global exception handling
│       ├── mapper/          # MapStruct mappers
│       ├── repository/      # Spring Data JPA
│       ├── security/        # JWT authentication
│       ├── service/         # Business logic
│       └── specification/   # Dynamic JPA queries
├── frontend/                # React SPA
│   └── src/
│       ├── api/             # Axios services
│       ├── components/      # Layout components
│       ├── context/         # Auth context
│       └── pages/           # Feature pages
├── sql/                     # Database scripts
├── docs/                    # API documentation
└── docker-compose.yml
```

## Features

- **Vocabulary Learning** - Filter by status (NEW/LEARNING/MASTERED), HSK level, topic; pagination
- **Vocabulary Test** - Vietnamese → Chinese writing; repeat wrong answers
- **Sentence Arrangement** - Drag-and-drop Chinese word ordering
- **AI Chat** - OpenAI-powered tutor with conversation history; speech-to-text & text-to-speech
- **Handwriting Practice** - Canvas drawing with stroke evaluation
- **Excel Import** (Admin) - Bulk vocabulary upload with template download

## Quick Start

### Prerequisites

- Java 25+
- Node.js 18+
- SQL Server 2019+ (or Docker)
- Maven 3.9+

### 1. Database Setup

```bash
# Option A: Run SQL scripts
sqlcmd -S localhost -U sa -P "<your-db-password>" -i sql/01_schema.sql
sqlcmd -S localhost -U sa -P "<your-db-password>" -i sql/02_sample_data.sql

# Option B: Let Spring Boot auto-create (ddl-auto: update in dev profile)
```

### 2. Backend

```bash
cd backend

# Set environment variables (or copy .env.example to ../.env)
set DB_HOST=localhost
set DB_USERNAME=sa
set DB_PASSWORD=your-strong-sqlserver-password
set JWT_SECRET=your-random-256-bit-or-longer-secret
set OPENAI_API_KEY=<optional-ai-api-key>

mvn spring-boot:run
```

Backend runs at `http://localhost:8080`


### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend runs at `http://localhost:5173` (proxies API to backend)

### 4. Docker (Full Stack)

```bash
# Copy and configure environment
cp .env.example .env

docker-compose up -d
```

- Frontend: `http://localhost`
- Backend API: `http://localhost:8080/api`
- SQL Server: `localhost:1433`
- Redis: `localhost:6379`

## API Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/auth/login` | Login | Public |
| POST | `/api/auth/register` | Register | Public |
| GET | `/api/vocabularies` | List vocabularies (filter + pagination) | JWT |
| PATCH | `/api/vocabularies/{id}/status` | Update learning status | JWT |
| GET | `/api/topics` | List topics | JWT |
| GET | `/api/test/questions` | Get test questions | JWT |
| POST | `/api/test/submit` | Submit test answer | JWT |
| GET | `/api/sentence/exercises` | Get sentence exercises | JWT |
| POST | `/api/sentence/check` | Check sentence arrangement | JWT |
| POST | `/api/chat` | Send chat message | JWT |
| GET | `/api/chat/conversations` | List conversations | JWT |
| POST | `/api/handwriting/check` | Check handwriting | JWT |
| GET | `/api/admin/template` | Download Excel template | ADMIN |
| POST | `/api/admin/upload-excel` | Upload vocabulary Excel | ADMIN |

See [docs/API_SAMPLES.md](docs/API_SAMPLES.md) for full request/response examples.

## Excel Import Template

| Column | Description | Required |
|--------|-------------|----------|
| Chinese | Chữ Hán | Yes |
| Pinyin | Phiên âm | Yes |
| Meaning | Nghĩa tiếng Việt | Yes |
| Example | Câu ví dụ | No |
| HSK Level | 1-6 | Yes |
| Topic | Tên chủ đề | Yes |

## Free API Recommendations

### Chinese Dictionary
- **[HanziDB](https://hanzidb.org/)** - Open-source Chinese character database
- **[MDBG Chinese Dictionary API](https://www.mdbg.net/chinese/dictionary)** - Free web lookup
- **[CC-CEDICT](https://cc-cedict.org/)** - Open-source Chinese-English dictionary

### Speech Recognition (STT)
- **Web Speech API** (built-in browser, free) - Used in ChatPage
- **[Azure Speech Services](https://azure.microsoft.com/en-us/products/ai-services/speech-to-text)** - Free tier: 5 hours/month
- **[Google Cloud Speech-to-Text](https://cloud.google.com/speech-to-text)** - Free tier available

### Text-to-Speech (TTS)
- **Web Speech Synthesis API** (built-in browser, free) - Used in ChatPage
- **[Azure TTS](https://azure.microsoft.com/en-us/products/ai-services/text-to-speech)** - Natural Chinese voices
- **[Google Cloud TTS](https://cloud.google.com/text-to-speech)** - Free tier available

### Handwriting Recognition
- **[Google Cloud Vision API](https://cloud.google.com/vision)** - OCR for handwritten Chinese
- **[Azure Computer Vision](https://azure.microsoft.com/en-us/products/ai-services/computer-vision)** - Handwriting OCR
- **[Hanzi Writer](https://hanziwriter.org/)** - Stroke-based character practice (client-side)
- **[Baidu OCR API](https://ai.baidu.com/tech/ocr)** - Chinese handwriting recognition (free tier)

## Performance & Scalability

1. **Redis caching** - Topics and vocabulary cached (30min TTL)
2. **JPA Specifications** - Dynamic queries without N+1 (lazy loading)
3. **Pagination** - All list endpoints support page/size
4. **Stateless JWT** - Horizontal scaling without session affinity
5. **Connection pooling** - HikariCP (Spring Boot default)
6. **Frontend code splitting** - Vite automatic chunking
7. **Database indexes** - On hsk_level, topic_id, user_id, status columns

### Production Checklist

- [ ] Keep `.env` out of git and set `JWT_SECRET` to a strong random key (256+ bits)
- [ ] Set `ddl-auto: validate` and run migrations via SQL scripts
- [ ] Configure HTTPS (reverse proxy: Nginx/Traefik)
- [ ] Set up SQL Server backups
- [ ] Enable Redis for production caching
- [ ] Configure OpenAI API key with rate limiting
- [ ] Set up monitoring (Spring Actuator + Prometheus)
- [ ] Configure CORS for production domain

## Step-by-Step Implementation Guide

### Phase 1: Foundation (Day 1-2)
1. Set up SQL Server and run schema scripts
2. Start backend, verify health endpoint: `GET /api/health`
3. Test auth: register → login → get JWT token
4. Start frontend, verify login flow

### Phase 2: Core Learning (Day 3-4)
1. Test vocabulary list with filters and pagination
2. Test vocabulary status updates
3. Implement and test vocabulary quiz flow
4. Implement sentence arrangement exercises

### Phase 3: Advanced Features (Day 5-6)
1. Configure OpenAI API key for AI chat
2. Test conversation history persistence
3. Test speech-to-text and text-to-speech in browser
4. Test handwriting canvas and evaluation

### Phase 4: Admin & Deploy (Day 7)
1. Login as admin, download Excel template
2. Fill template and upload vocabulary
3. Build Docker images: `docker-compose build`
4. Deploy full stack: `docker-compose up -d`
5. Run end-to-end smoke tests

## License

MIT

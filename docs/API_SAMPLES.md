# Hanzii API - Sample Responses

Base URL: `http://localhost:8080/api`

All responses follow this wrapper format:

```json
{
  "success": true,
  "message": "optional message",
  "data": { },
  "timestamp": "2026-06-07T10:00:00"
}
```

---

## Authentication

### POST /api/auth/register

**Request:**
```json
{
  "username": "newuser",
  "password": "password123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Registration successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "username": "newuser",
    "role": "USER",
    "userId": 3
  }
}
```

### POST /api/auth/login

**Request:**
```json
{
  "username": "user",
  "password": "user123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "username": "user",
    "role": "USER",
    "userId": 2
  }
}
```

---

## Vocabulary

### GET /api/vocabularies?status=NEW&hsk=1&topic=1&page=0&size=10

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "chineseWord": "你好",
        "pinyin": "nǐ hǎo",
        "meaning": "Xin chào",
        "example": "你好，我是小明。",
        "hskLevel": 1,
        "topicId": 1,
        "topicName": "Giao tiếp hàng ngày",
        "status": "NEW"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 20,
    "totalPages": 2,
    "first": true,
    "last": false
  }
}
```

### PATCH /api/vocabularies/1/status?status=LEARNING

**Response:**
```json
{
  "success": true,
  "message": "Status updated",
  "data": null
}
```

---

## Vocabulary Test

### GET /api/test/questions?hsk=1&topic=1

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "vocabularyId": 1,
      "meaning": "Xin chào",
      "hskLevel": 1,
      "topicName": "Giao tiếp hàng ngày"
    }
  ]
}
```

### POST /api/test/submit

**Request:**
```json
{
  "vocabularyId": 1,
  "answer": "你好"
}
```

**Response (correct):**
```json
{
  "success": true,
  "data": {
    "correct": true,
    "correctAnswer": "你好",
    "feedback": "Chính xác! 🎉",
    "vocabularyId": 1
  }
}
```

**Response (wrong):**
```json
{
  "success": true,
  "data": {
    "correct": false,
    "correctAnswer": "你好",
    "feedback": "Chưa đúng, hãy thử lại!",
    "vocabularyId": 1
  }
}
```

---

## Sentence Arrangement

### GET /api/sentence/exercises?hsk=2

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "sentenceId": 5,
      "vietnameseSentence": "Ngày mai tôi đi Bắc Kinh.",
      "shuffledWords": ["北京", "我", "明天", "去。"],
      "hskLevel": 2,
      "topicName": "Du lịch"
    }
  ]
}
```

### POST /api/sentence/check

**Request:**
```json
{
  "sentenceId": 5,
  "arrangedSentence": "我明天去北京。"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "correct": true,
    "correctSentence": "我明天去北京。",
    "feedback": "Sắp xếp chính xác! 🎉"
  }
}
```

---

## AI Chat

### POST /api/chat

**Request:**
```json
{
  "conversationId": null,
  "message": "Dạy tôi cách chào hỏi bằng tiếng Trung"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "conversationId": 1,
    "userMessage": "Dạy tôi cách chào hỏi bằng tiếng Trung",
    "aiMessage": "Cách chào hỏi cơ bản nhất là 你好 (nǐ hǎo)..."
  }
}
```

### GET /api/chat/conversations

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "title": "Dạy tôi cách chào hỏi...",
      "createdAt": "2026-06-07T10:00:00",
      "updatedAt": "2026-06-07T10:05:00",
      "messages": [
        {
          "id": 1,
          "sender": "USER",
          "content": "Dạy tôi cách chào hỏi bằng tiếng Trung",
          "createdAt": "2026-06-07T10:00:00"
        },
        {
          "id": 2,
          "sender": "AI",
          "content": "Cách chào hỏi cơ bản nhất là 你好...",
          "createdAt": "2026-06-07T10:00:05"
        }
      ]
    }
  ]
}
```

---

## Handwriting

### POST /api/handwriting/check

**Request:**
```json
{
  "expectedCharacter": "你",
  "drawnData": "[[{\"x\":100,\"y\":50}]]"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "correct": true,
    "expectedCharacter": "你",
    "recognizedCharacter": "你",
    "confidence": 0.75,
    "feedback": "Viết đúng rồi! 🎉"
  }
}
```

---

## Admin (ADMIN role only)

### GET /api/admin/template

Downloads Excel template file.

### POST /api/admin/upload-excel

**Request:** `multipart/form-data` with `file` field

**Response:**
```json
{
  "success": true,
  "message": "Import completed",
  "data": {
    "totalRows": 15,
    "imported": 14,
    "skipped": 1,
    "errors": ["Row 5: HSK level must be 1-6"]
  }
}
```

---

## Error Responses

### 401 Unauthorized
```json
{
  "success": false,
  "message": "Invalid username or password"
}
```

### 403 Forbidden
```json
{
  "success": false,
  "message": "Access denied"
}
```

### 400 Validation Error
```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "username": "Username is required"
  }
}
```

### 404 Not Found
```json
{
  "success": false,
  "message": "Vocabulary not found: 999"
}
```

# SecureChat: подробная документация

## Общая картина
SecureChat — микросервис Spring Boot 3.2, предоставляющий REST API и WebSocket-уведомления для end-to-end мессенджера. Он хранит метаданные чатов и ключей в PostgreSQL, кеширует вспомогательные данные в Redis и использует JWT для аутентификации. Вся криптография клиентская: сервис оперирует только публичными ключами и зашифрованными полезными нагрузками.

## Архитектура и основные компоненты
- **Транспорт**: REST-контроллеры (`ChatController`, `MessageController`, `SessionController`, `KeyController`, `ReceiptController`, `MediaController`) принимают JSON-запросы и возвращают DTO. WebSocket/STOMP контроллер `ChatWebSocketController` обслуживает события «печатает».
- **Безопасность**: фильтр `JwtAuthenticationFilter` извлекает токен Bearer, валидация выполняется через `JwtTokenProvider`, поддерживающий issuer/audience. Все запросы, кроме `/actuator/**` и открытия сокета `GET /ws/**`, требуют аутентификации. WebSocket-соединения повторно валидируют токен из заголовка `Authorization` в STOMP CONNECT.
- **Доменные сервисы**: `ChatService`, `MessageService`, `SessionService`, `KeyManagementService`, `ReceiptService`, `ObjectStorageService`, `WebSocketNotificationService` инкапсулируют бизнес-логику и работают с репозиториями JPA.
- **Хранение**: сущности JPA (`Chat`, `ChatMember`, `UserPublicKeyBundle`, `SessionMetadata`, `Message`, `MessageReceipt`, `User`, `Device`) записываются в PostgreSQL. Redis используется для кеширования выданных ключевых бандлов по сессии (24 часа).
- **Уведомления**: `SimpWebSocketNotificationService` публикует события в топики `/topic/chats/{chatId}` и `/topic/users/{userId}` для новых сообщений, статусов доставок и индикатора набора текста.
- **Объектное хранилище**: через `MockObjectStorageService` предоставляется заглушка для генерации presigned URL. В проде сюда подключается S3/GCS и реализуется `ObjectStorageService`.

## Модели данных
- **Chat** (`chats`): `id`, `type` (`PRIVATE`/`GROUP`), `title`, `createdBy`, `createdAt`, `updatedAt` и связка `members`.
- **ChatMember** (`chat_members`): `id`, ссылка на чат, `userId`, `role` (`ADMIN`/`MEMBER`), `joinedAt`, `isMuted`.
- **Message** (`messages`): `id`, ссылка на чат, `senderId`, `type` (`TEXT`, `IMAGE`, `VIDEO`, `VOICE`, `FILE`, `SYSTEM`), зашифрованные поля `encryptedPayload`, опциональные `encryptedMediaKey`, `mediaUrl`, временные метки `createdAt/editedAt/deletedAt`.
- **MessageReceipt** (`message_receipts`): `id`, ссылка на сообщение, `userId`, `status` (`DELIVERED` или `READ`), `updatedAt`.
- **UserPublicKeyBundle** (`user_public_key_bundles`): `id`, `userId`, `deviceId`, публичные ключи (`identityKeyPublic`, `signedPreKeyPublic`, список `oneTimePreKeysPublic` как CSV), `updatedAt`.
- **SessionMetadata** (`session_metadata`): `id`, `chatId`, `userId`, `clientSessionId`, `createdAt` — только вспомогательные данные без секретов.
- **User** (`users`): `id`, `displayName`, `avatarUrl`, `createdAt`.
- **Device** (`devices`): `id`, `userId`, `deviceLabel`, `createdAt`.

## Конфигурация и окружение
- **PostgreSQL**: `spring.datasource.*` настраивается через `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` (по умолчанию `jdbc:postgresql://localhost:5432/securechat`, `securechat/securechat`).
- **Redis**: `REDIS_HOST`, `REDIS_PORT` (по умолчанию `localhost:6379`).
- **JWT**: `security.jwt.public-key` — публичный ключ (base64 или сырой) минимум 256 бит; `security.jwt.issuer` и `security.jwt.audience` опциональны.
- **Логи**: уровни `INFO` для корня и пакета `com.example.securechat`.

## REST API
Все пути начинаются с `/api/v1` и требуют заголовок `Authorization: Bearer <jwt>`. DTO описаны в `domain/dto`.

### Управление ключами
- `POST /api/v1/keys/bundle` — регистрирует или обновляет публичный ключевой пакет устройства. Тело: `deviceId`, `identityKeyPublic`, `signedPreKeyPublic`, массив `oneTimePreKeysPublic`. Возвращает `KeyBundleDto` (с `id`, `userId`, `deviceId`, ключи, `updatedAt`).
- `GET /api/v1/keys/bundle/{userId}` — получает все ключевые пакеты пользователя для инициации сессий.

### Чаты
- `POST /api/v1/chats/private` — создает или возвращает существующий приватный чат с `peerUserId`.
- `POST /api/v1/chats/group` — создает групповой чат с `title` и наборами `memberIds`; инициатор добавляется админом.
- `GET /api/v1/chats` — список чатов текущего пользователя с последним сообщением и участниками.
- `GET /api/v1/chats/{chatId}` — детали конкретного чата (проверка членства обязательна).
- `POST /api/v1/chats/{chatId}/members` — добавляет участников (требует роль `ADMIN`). Тело: `memberIds`.
- `DELETE /api/v1/chats/{chatId}/members/{memberId}` — удаляет участника (требует `ADMIN`).

### Сообщения
- `GET /api/v1/chats/{chatId}/messages?limit=50&beforeMessageId=` — пагинированная выдача сообщений (desc). Проверяется членство.
- `POST /api/v1/chats/{chatId}/messages` — отправка зашифрованного сообщения. Тело: `type`, `encryptedPayload`, опционально `encryptedMediaKey`, `mediaUrl`, `clientMessageId`. Уведомление уходит в `/topic/chats/{chatId}` с событием `NEW_MESSAGE`.

### Сессии E2E
- `POST /api/v1/chats/{chatId}/sessions` — регистрирует клиентскую сессию (`deviceId`, `clientSessionId`). Возвращает `SessionDto` с закешированным `KeyBundleDto` (TTL 24ч в Redis).
- `GET /api/v1/chats/{chatId}/sessions` — список сессий чата и привязанные публичные ключи (из кеша, если есть).
- `GET /api/v1/chats/{chatId}/sessions/bundles` — агрегирует публичные ключи всех участников для инициации групповых сессий.

### Квитанции и медиаконтент
- `POST /api/v1/messages/{messageId}/receipts` — помечает статус доставки/прочтения (`status`: `DELIVERED`/`READ`). Отправляет событие `RECEIPT_UPDATED` в топик отправителя `/topic/users/{senderId}`.
- `POST /api/v1/media/presign` — запрашивает presigned URL для загрузки медиа (`mediaType`, `fileName`, `contentType`). Возвращает `uploadUrl` и `finalUrl` (заглушка).

## WebSocket/STOMP
- **Endpoint**: `GET /ws` с разрешенными CORS `*`. STOMP-префиксы: `/app` для исходящих команд, `/topic` для подписок.
- **Аутентификация**: заголовок `Authorization: Bearer <jwt>` в STOMP CONNECT; токен валидируется, в `Principal` помещается `userId`.
- **Подписки**:
  - `/topic/chats/{chatId}` — события `NEW_MESSAGE` (payload `MessageDto`), `TYPING` (поля `chatId`, `userId`, `isTyping`).
  - `/topic/users/{userId}` — события `RECEIPT_UPDATED` с `MessageReceiptDto`.
- **Команды клиента**: `SEND /app/typing` с JSON `{ "chatId": "<uuid>", "isTyping": true|false }` — транслирует событие TYPING всем участникам чата.

## Потоки данных
1. **Регистрация ключей устройства**: клиент отправляет `POST /api/v1/keys/bundle`; сервис сохраняет/обновляет `UserPublicKeyBundle` и возвращает DTO. Эти бандлы затем раздаются другим пользователям для установки сессий.
2. **Создание чата**: приватный чат ищется по парам участников; при отсутствии создается запись `Chat` + две записи `ChatMember`. Групповой чат создает `Chat` с `ChatType.GROUP`, добавляет всех `memberIds`, а инициатора помечает `ADMIN`.
3. **Инициация E2E-сессии**: клиент вызывает `POST /api/v1/chats/{chatId}/sessions`, сервер проверяет членство, ищет зарегистрированный бандл для `deviceId`, сохраняет `SessionMetadata`, кладет `KeyBundleDto` в Redis (`session:bundle:<sessionId>`), возвращает DTO. При запросах списка сессий бандл читается из кеша.
4. **Отправка сообщения**: `MessageService` проверяет членство, записывает `Message`, пушит `NEW_MESSAGE` в `/topic/chats/{chatId}`. Получатели вытягивают шифротекст из payload.
5. **Квитанции доставки/прочтения**: `ReceiptService` создает/обновляет `MessageReceipt`, затем пушит `RECEIPT_UPDATED` отправителю в `/topic/users/{senderId}`.
6. **Индикатор набора текста**: через STOMP-команду `/app/typing` вызывается `ChatWebSocketController`, который извлекает `chatId` и `isTyping`, публикует `TYPING` в `/topic/chats/{chatId}`.

## Запуск и тестирование
1. **Зависимости**: JDK 17, PostgreSQL, Redis. Настройте переменные окружения (см. выше) или используйте значения по умолчанию из `application.yml`.
2. **Запуск**: `./gradlew bootRun` — скачивает wrapper при необходимости и стартует приложение.
3. **Тесты**: `./gradlew test` — выполняет модульные и интеграционные тесты сервисов.

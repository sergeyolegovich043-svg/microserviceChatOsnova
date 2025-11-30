# SecureChat Microservice

SecureChat — это микросервис на Spring Boot 3.2 для end-to-end мессенджера по мотивам Signal. Он объединяет REST API, WebSocket-уведомления и инфраструктуру для хранения ключей, сообщений и участников чатов.

## Возможности
- **Управление ключами устройств** — прием и хранение публичных ключей устройства (identity, signed pre-key, one-time pre-keys) через `KeyManagementService`.
- **Обработка сессий E2E** — `SessionService` регистрирует клиентские сессии, кеширует привязанные ключевые пакеты в Redis и отдает наборы публичных ключей участников чата для сквозного шифрования.
- **Создание приватных чатов** — `ChatService` находит существующие приватные диалоги или создает новый чат и участников.
- **Отправка зашифрованных сообщений** — `MessageService` валидирует членство в чате, сохраняет ciphertext и уведомляет адресатов через WebSocket.
- **JWT-аутентификация** — валидация JWT по публичному ключу и параметрам issuer/audience.
- **Персистентность** — PostgreSQL для сущностей (чаты, сообщения, ключевые пакеты), Redis для кэша и Pub/Sub нотификаций.

## Архитектура
- **Spring Boot Web + Validation** — REST-контроллеры и валидация DTO.
- **Spring Data JPA** — репозитории `ChatRepository`, `ChatMemberRepository`, `MessageRepository`, `UserPublicKeyBundleRepository`.
- **WebSocket** — сервис уведомлений `WebSocketNotificationService` отправляет события о новых сообщениях.
- **Безопасность** — Spring Security + JWT (публичный ключ задается в конфигурации).
- **Тестирование** — модульные тесты на сервисы (`ChatServiceImplTest`, `KeyManagementServiceImplTest`, `MessageServiceImplTest`) и интеграционные тесты с PostgreSQL/Redis (`SessionServiceIntegrationTest`).

## Основные доменные модели
- `Chat`, `ChatMember`, `ChatType`, `MemberRole` — представляют чаты и участников.
- `Message`, `MessageType` — зашифрованные сообщения с типом (например, текст).
- `UserPublicKeyBundle` — связка публичных ключей устройства пользователя.
- DTO и запросы: `ChatDto`, `PrivateChatRequest`, `MessageDto`, `MessageCreateRequest`, `KeyBundleDto`, `KeyBundleRequest`.

## Конфигурация
Параметры задаются через переменные окружения (см. `src/main/resources/application.yml`):
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` — подключение к PostgreSQL (по умолчанию `jdbc:postgresql://localhost:5432/securechat`).
- `REDIS_HOST`, `REDIS_PORT` — параметры Redis (по умолчанию `localhost:6379`).
- `JWT_PUBLIC_KEY` — публичный ключ для верификации токенов.
- `JWT_ISSUER`, `JWT_AUDIENCE` — необязательные ограничения токена.

## Быстрый старт
1. Установите JDK 17, запустите PostgreSQL и Redis.
2. Экспортируйте переменные окружения при необходимости:
   ```bash
   export DB_URL=jdbc:postgresql://localhost:5432/securechat
   export DB_USERNAME=securechat
   export DB_PASSWORD=securechat
   export REDIS_HOST=localhost
   export REDIS_PORT=6379
   export JWT_PUBLIC_KEY="-----BEGIN PUBLIC KEY-----..."
   ```
3. Соберите и запустите приложение через Gradle Wrapper:
   ```bash
   ./gradlew bootRun
   ```
   Скрипт автоматически загрузит `gradle-wrapper.jar`, если он отсутствует.
4. Прогоните модульные тесты:
   ```bash
   ./gradlew test
   ```

## Структура проекта
- `src/main/java/com/example/securechat/` — точка входа Spring Boot и сервисы домена.
- `src/main/resources/` — конфигурация (`application.yml`).
- `src/test/java/com/example/securechat/` — модульные тесты сервисов.

## Подробная документация
Расширенное описание архитектуры, моделей данных, REST/WebSocket API и рабочих потоков см. в [docs/DETAILED_DOCUMENTATION.md](docs/DETAILED_DOCUMENTATION.md).

## Дальнейшее развитие
- Добавить контроллеры REST/WebSocket и спецификацию API.

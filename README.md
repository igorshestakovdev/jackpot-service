# 🎰 Jackpot Service

Jackpot Service is a backend service for betting products that manages jackpot mechanics end to end: it receives player bets, allocates contributions to the shared prize pool, determines reward eligibility, and resets the pool after a winning event.

> Spring Boot jackpot backend with embedded Kafka, H2, Liquibase, and Swagger.

| Java | Framework | Runtime | Default HTTP |
| --- | --- | --- | --- |
| 21 | Spring Boot 3.5 | Embedded Kafka + H2 | `http://localhost:8080` |

## ✨ At a glance

The service handles a four-step jackpot flow:

```text
publish bet -> persist contribution -> evaluate reward -> reset pool on win
```

- `POST /api/v1/bets` publishes a bet event
- embedded Kafka starts automatically with the application
- Kafka consumer calculates and persists the jackpot contribution
- `POST /api/v1/jackpots/evaluate` returns the reward decision after contribution is stored

## 🚀 Run locally

Requirement: JDK `21`. Docker, external Kafka, and local Maven are not required.

```bash
./mvnw clean package
java -jar jackpot-service-impl/target/jackpot-service-impl-1.0.0.jar
```

Windows PowerShell:

```powershell
.\mvnw.cmd clean package
java -jar .\jackpot-service-impl\target\jackpot-service-impl-1.0.0.jar
```

## 🌐 Access points

Everything needed for local review starts with the app.

| Item | Value |
| --- | --- |
| HTTP API | `http://localhost:8080` |
| Swagger UI | [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) |
| OpenAPI JSON | [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs) |
| Kafka broker | embedded, starts automatically |
| Main topic | `jackpot-bets` |
| Dead-letter topic | `jackpot-bets-dlt` |
| Database | in-memory H2 |
| Migrations | Liquibase on startup |

Tip: open Swagger first and try the flow from there.

## 🔌 Endpoints

### 1. Publish bet

```bash
curl -X POST http://localhost:8080/api/v1/bets \
  -H "Content-Type: application/json" \
  -d '{
    "betId": "11111111-1111-1111-1111-111111111111",
    "userId": "user-001",
    "jackpotId": "fixed-jackpot",
    "betAmount": 100.00
  }'
```

Response:

```json
{
  "betId": "11111111-1111-1111-1111-111111111111",
  "status": "ACCEPTED"
}
```

`ACCEPTED` means the event was published successfully. Contribution processing continues asynchronously via Kafka.

### 2. Evaluate reward

Call this after the contribution has already been consumed and stored.

```bash
curl -X POST http://localhost:8080/api/v1/jackpots/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "betId": "11111111-1111-1111-1111-111111111111",
    "userId": "user-001",
    "jackpotId": "fixed-jackpot"
  }'
```

Example response:

```json
{
  "betId": "11111111-1111-1111-1111-111111111111",
  "userId": "user-001",
  "jackpotId": "fixed-jackpot",
  "won": false,
  "rewardAmount": null,
  "evaluatedChance": 0.0010000000,
  "evaluatedAt": "2026-07-15T18:00:00Z"
}
```

## 🧠 Rules

- default HTTP port is `8080`
- Swagger UI is available at `/swagger-ui/index.html`
- `betId` must be UUID
- `betAmount` must be positive and have at most 2 decimal places
- reward evaluation is idempotent
- contribution processing is asynchronous
- evaluation returns `404 CONTRIBUTION_NOT_FOUND` if the contribution is not stored yet
- pool updates and payouts are protected by transactional locking
- embedded Kafka is enabled by default and binds to a free local port

## 🎯 Jackpot modes

| ID | Initial pool | Contribution model | Reward model |
| --- | ---: | --- | --- |
| `fixed-jackpot` | `1000.00` | fixed rate `0.10` | fixed chance `0.001` |
| `variable-jackpot` | `500.00` | `0.20 -> 0.05` with decrement `0.0001` | `0.001 -> 1.0`, guaranteed at `2500.00` |

## 🛠 Validation and coverage

```bash
./mvnw clean verify
```

Windows PowerShell:

```powershell
.\mvnw.cmd clean verify
```

Generated JaCoCo report:

`jackpot-service-impl/target/site/jacoco/index.html`

Formatting:

```bash
./mvnw spotless:apply
```

## 🌐 External Kafka

To disable embedded Kafka and connect to an external broker:

```bash
KAFKA_BOOTSTRAP_SERVERS=kafka.example:9092 \
./mvnw spring-boot:run -Dspring-boot.run.profiles=external-kafka
```

PowerShell:

```powershell
$env:KAFKA_BOOTSTRAP_SERVERS = "localhost:9092"
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=external-kafka"
```

## 📦 Project layout

```text
jackpot-service-api   REST contracts and DTOs
jackpot-service-db    Liquibase changelogs
jackpot-service-impl  application logic, Kafka, persistence, config
```

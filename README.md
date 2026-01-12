# FinOps Cost API

비용 데이터를 제공하는 REST API 서비스

## 기능

- 일별 비용 조회 API
- 서비스별 비용 조회 API
- 대시보드 요약 API

## API 엔드포인트

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | /api/costs/daily | 일별 비용 조회 |
| GET | /api/costs/services | 서비스별 비용 조회 |
| GET | /api/dashboard/summary | 대시보드 요약 |

## 기술 스택

- Java 17
- Spring Boot 3.2
- Spring Data JPA
- PostgreSQL

## 로컬 실행

```bash
./gradlew bootRun
```

## Docker 빌드

```bash
docker build -t finops-cost-api .
```

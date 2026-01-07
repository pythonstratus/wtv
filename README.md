# Weekly Time Verification (WTV) Service v2.0

Modernized from legacy Pro*C code (entity_common.pc, ent_timeverify.pc)

## Overview

This service provides REST APIs for the Weekly Time Verification system:
- **Group Weekly Hours Verification** - Main table view with 10 columns
- **Employee Timesheet Detail** - Drill-down with 3 tables (Daily Summary, Case TIN, Non-Case Time)
- **Pay Period Navigation** - Month/Week selection and Previous/Next navigation

## Tech Stack

| Component | Technology | Version |
|-----------|------------|---------|
| Language | Java | 17 (LTS) |
| Framework | Spring Boot | 3.2.5 |
| ORM | Spring Data JPA | 3.2.5 |
| Database | Oracle / H2 | 19c / 2.x |
| API Docs | SpringDoc OpenAPI | 2.3.0 |
| Build | Maven | 3.9.6 |

## Quick Start

### Option 1: Docker (Recommended for Mac ARM64)

```bash
# Build and run
docker-compose up --build

# Or build manually
docker build -t wtv-service .
docker run -p 8080:8080 wtv-service
```

### Option 2: Maven

```bash
# Run with H2 (local profile)
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# Run with Oracle (requires Oracle connection)
./mvnw spring-boot:run -Dspring-boot.run.profiles=oracle
```

## API Endpoints

### Base URL: `http://localhost:8080/wtv/api/wtv`

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/reporting-months` | Get all reporting months with weeks |
| GET | `/weeks?month={rptmonth}` | Get weeks for a specific month |
| GET | `/pay-period?date={yyyy-MM-dd}` | Get pay period by date |
| GET | `/pay-period/previous?currentStartDate={date}` | Navigate to previous week |
| GET | `/pay-period/next?currentStartDate={date}` | Navigate to next week |
| GET | `/summaries?startDate={}&endDate={}&assignmentNumber={}` | Get group summaries |
| GET | `/employees/{roid}/timesheet?startDate={}&endDate={}` | Get employee timesheet |
| GET | `/health` | Health check |

### Access Points

| Service | URL |
|---------|-----|
| Swagger UI | http://localhost:8080/wtv/swagger-ui.html |
| API Docs | http://localhost:8080/wtv/api-docs |
| H2 Console | http://localhost:8080/wtv/h2-console |
| Health | http://localhost:8080/wtv/api/wtv/health |

## Database Tables

| Table | Purpose |
|-------|---------|
| ENTEMP | Employee/Assignment master (39 columns) |
| ENTMONTH | Pay period definitions |
| ENTCODE | Time code reference |
| TIMENON | Non-case time entries |
| TIMETIN | Case/TIN time entries |
| ENT | Case/TIN master |
| CFF (View) | Security filter for valid ROIDs |

## Business Logic (from Legacy Pro*C)

### Tour of Duty Hours
```
SUM(TIMENON hours where TIMEDEF in M,U,C,G,N,R,O,E)
+ SUM(TIMETIN hours)
- SUM(TIMENON hours where TIMEDEF='A')
- SUM(TIMENON hours where TIMEDEF='S')
```

### Adjusted Tour
```
SUM(TIMENON hours where TIMEDEF='A') - SUM(TIMENON hours where TIMEDEF='S')
```

### Tour of Duty Type Decode
```
1 = REG, 2 = 5/4/9, 3 = 4/10, 4 = PT, 5 = MAXI
```

## TODO Items

1. **Timecode 750**: Excluded from Report Days count - need clarification from Samuel
2. **Timecode 760**: Special binary handling (decode(hours,0,1,0)) - need clarification
3. **Daily Summary Table**: Stub implementation - waiting for data entry screen details

## Project Structure

```
wtv-service/
├── src/main/java/com/entity/wtv/
│   ├── WtvApplication.java
│   ├── config/           # CORS, Swagger configs
│   ├── controller/       # REST controllers
│   ├── service/          # Business logic
│   ├── repository/       # JPA repositories
│   ├── entity/           # JPA entities
│   ├── dto/              # Data transfer objects
│   └── exception/        # Exception handling
├── src/main/resources/
│   ├── application.yml   # Configuration
│   ├── schema.sql        # H2 schema
│   └── data.sql          # Sample data
├── Dockerfile            # ARM64 compatible
├── docker-compose.yml
└── pom.xml
```

## Configuration

### H2 (Local Development)
```yaml
spring.profiles.active: local
```

### Oracle (Production)
```yaml
spring.profiles.active: oracle
DB_USERNAME: entitydev
DB_PASSWORD: <your-password>
```

## Version History

- **v2.0.0** - Modernized from legacy Pro*C
- **v1.0.0** - Original POC with synthetic data

---
*Last Updated: January 2026*

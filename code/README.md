# Source and Configuration

Import `pom.xml` into the IDE. Production code lives in `src/main`.
From the repository root:

```powershell
.\code\mvnw.cmd -f code/pom.xml spring-boot:run
.\code\mvnw.cmd -f code/pom.xml clean verify
docker compose -f code/docker-compose.yml up --build
```

Tests are in `../test/java`; Maven packages runtime images from `../img/web` into `static/images`.
Dockerfile uses repository root as build context. GitHub Actions stays in the root `.github/workflows`.

# User Service

Sample User Service built with Java 21 and Spring Boot.

## Features

- User registration
- Login with JWT authentication
- Refresh token support
- Logout
- Forgot/reset password flow (sample token handling)
- User profile retrieval and update
- Change password
- Soft delete account
- Role-based authorization: `ROLE_USER`, `ROLE_ADMIN`
- Input validation with `jakarta.validation`
- Global exception handling
- Audit fields: `createdAt`, `updatedAt`, `deletedAt`
- Swagger API documentation via `springdoc-openapi`
- In-memory H2 database for easy local testing

## Getting Started

### Build

```bash
mvn clean package
```

### Run

```bash
mvn spring-boot:run
```

The service starts on `http://localhost:8080`.

### API Documentation

Open Swagger UI at:

```
http://localhost:8080/swagger-ui/index.html
```

### Default Endpoints

- `POST /api/auth/register` - register a new user
- `POST /api/auth/login` - authenticate and receive access + refresh token
- `POST /api/auth/refresh-token` - refresh access token
- `POST /api/auth/logout` - invalidate refresh token
- `POST /api/auth/forgot-password` - request password reset
- `POST /api/auth/reset-password` - reset password
- `GET /api/users/me` - current user profile
- `PUT /api/users/me` - update current user profile
- `PUT /api/users/me/change-password` - change current user password
- `DELETE /api/users/me` - soft delete current user account

### JWT Configuration

Controlled by `src/main/resources/application.yml`:

- `jwt.secret`
- `jwt.expiration-ms`
- `jwt.refresh-expiration-ms`

### Testing

Run unit tests with:

```bash
mvn test
```

## Notes

This sample stores refresh tokens and user accounts in H2 memory. For production, replace H2 with a persistent store and implement secure password reset token delivery.

## CI/CD

This project includes a GitHub Actions workflow that builds, runs tests, and packages the application on push and PRs to `main`/`master`.

You can find the workflow in `.github/workflows/ci.yml`.

To enable the status badge in your repository's `README.md` copy the following snippet and replace `<OWNER>` and `<REPO>` with your GitHub owner and repository name:

```
[![CI](https://github.com/<OWNER>/<REPO>/actions/workflows/ci.yml/badge.svg)](https://github.com/<OWNER>/<REPO>/actions/workflows/ci.yml)
```

You can also run the workflow manually from GitHub using the **Actions → CI → Run workflow** button because this workflow supports `workflow_dispatch`.

Secrets and publishing:

- If you want to publish artifacts or Docker images from the workflow, provide registry credentials as repository secrets (`DOCKER_USERNAME`, `DOCKER_PASSWORD`, `GITHUB_TOKEN`, etc.) and I can update the workflow to include image build & push steps.

Security scanning:

- The workflow includes a `security` job that runs GitHub CodeQL analysis and Snyk vulnerability tests.
- CodeQL results are uploaded to the repository's CodeQL alerts (requires repository code scanning enabled).
- Snyk requires a GitHub repository secret named `SNYK_TOKEN` to authenticate.
- Snyk will fail the job if it detects vulnerabilities based on your organization’s policy; review the Snyk report and fix or ignore findings as needed.



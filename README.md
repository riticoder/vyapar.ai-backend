# Vyapar.ai Backend

Spring Boot backend for Vyapar.ai inventory management system.

## Railway Deployment

This project is configured for Railway deployment using Docker.

### Environment Variables

Set these in Railway:
- `MYSQL_URL`: Your MySQL database URL
- `MYSQL_USER`: Database username
- `MYSQL_PASSWORD`: Database password
- `JWT_SECRET`: Secret key for JWT tokens
- `GOOGLE_CLIENT_ID`: Google OAuth client ID

### Deployment

Railway will automatically build using the Dockerfile and deploy on port 8080.

Backend API: https://vyaparapi.ecellecb.com/
Frontend: https://vyaparai.ecellecb.com/

## Local Development

```bash
mvn spring-boot:run
```

Access at: http://localhost:8080

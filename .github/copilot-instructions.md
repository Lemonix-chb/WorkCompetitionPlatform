# Project Guidelines

## Code Style
- **Java (Backend)**: Use camelCase for variables/methods, PascalCase for classes. Leverage Lombok annotations (@Data, @NoArgsConstructor) for boilerplate reduction. Reference: `src/main/java/com/example/workcompetitionplatform/entity/User.java`
- **Vue.js (Frontend)**: Use kebab-case for component files (e.g., `StudentLayout.vue`), camelCase for props/data. Employ Vue 3 Options API with Element Plus components. Reference: `frontend-vue/src/components/Navigation.vue`
- **Database**: Snake_case for table/column names (e.g., `user` table, `real_name` column). Reference: `database/init_database.sql`

## Architecture
- **Backend (Spring Boot)**: Monolithic with layered architecture—Controllers for REST APIs, Services for business logic, Entities for data models (MyBatis Plus), Mappers for DB queries. JWT-based authentication with role-based access (STUDENT, JUDGE, ADMIN). Redis for caching, MySQL for persistence.
- **Frontend (Vue.js)**: Component-based with role-specific layouts and views. Pinia for state management, Vue Router for client-side routing with meta guards. Axios for API calls.
- **Data Flow**: Frontend communicates with backend via REST APIs; backend handles file uploads (up to 300MB) and integrates with MySQL/Redis.
- Key decisions: MyBatis Plus over JPA for ORM simplicity; Element Plus for consistent UI; Swagger for API documentation.

## Build and Test
- **Backend**: `mvn clean package` to build (use `mvn test` for tests). Run with `java -jar target/WorkCompetitionPlatform-0.0.1-SNAPSHOT.jar`. Convenience: `build-and-run.bat` (Windows) or `build.sh` (Linux/Mac).
- **Frontend**: `npm run dev` for development, `npm run build` for production. Lint with `npm run lint`.
- **Full Stack**: Ensure Java 17, MySQL (localhost:3306), and Redis (localhost:6379) are running. Run DB init scripts from `database/`.
- Agents will attempt to run build/test commands automatically.

## Conventions
- **Naming**: Consistent casing as above; avoid Hungarian notation.
- **Error Handling**: Custom exceptions in `exception/` package; frontend uses Element Plus alerts for user feedback.
- **Configuration**: Centralized in `application.properties` for backend; hardcoded paths in scripts (adjust for environment).
- **Security**: JWT tokens with 24h expiration; passwords in config (consider environment variables for production).
- **File Structure**: Standard Maven for backend, Vite for frontend. Group views by role (student/, judge/, admin/).
- See `docs/` for detailed documentation: `PROJECT_SUMMARY.md` (overview), `API_DESIGN.md` (endpoints), `database-design.md` (schema), `FRONTEND_DESIGN.md` (UI principles).
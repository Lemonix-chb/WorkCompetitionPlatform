# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a **Work Competition Platform** (信息管理与智能评价系统) for Hunan Agricultural University's computer science competitions. It's a full-stack monolithic application with Spring Boot backend and Vue.js frontend, supporting multi-role workflows (students, judges, administrators).

**Core Purpose**: Manage competitions, team formation, work submissions, AI-assisted reviews, judge evaluations, and appeals.

## Development Commands

### Backend (Spring Boot + Maven)

```bash
# Build the project
mvn clean package

# Build without tests
mvn clean package -DskipTests

# Run tests
mvn test

# Run a single test class
mvn test -Dtest=WorkCompetitionPlatformApplicationTests

# Run the application
java -jar target/WorkCompetitionPlatform-0.0.1-SNAPSHOT.jar

# Development mode (hot reload not configured)
mvn spring-boot:run

# Windows convenience script
build-and-run.bat      # Builds and starts backend
build.bat              # Just builds
start-backend.bat      # Starts pre-built jar

# Linux/Mac convenience script
bash build.sh
```

**Backend starts on**: `http://localhost:8080`
**API Documentation**: `http://localhost:8080/swagger-ui.html` or `http://localhost:8080/doc.html`

### Frontend (Vue 3 + Vite)

```bash
# Install dependencies (already done)
cd frontend-vue
npm install

# Development server
npm run dev

# Production build
npm run build

# Lint code
npm run lint

# Preview production build
npm run preview
```

**Frontend starts on**: `http://localhost:3000`
**API proxy**: `/api` → `http://localhost:8080` (configured in vite.config.js)

### Dependencies

Ensure these services are running before development:
1. **MySQL 8.0+** at `localhost:3306` (database: `work_competition_platform`, user: `root`, password: `123456`)
2. **Redis** at `localhost:6379` (no password)
3. **Java 17** (JDK must be JDK 17, not earlier versions)

Database initialization: Run `database/init_database.sql` via MySQL CLI or Workbench. Creates 23 tables with test data (14 users across 3 roles).

## Architecture

### Backend Layered Architecture

Standard Spring Boot layered architecture with clear separation:

```
Controller (REST API endpoints)
    ↓
Service (Business logic, transaction management)
    ↓
Mapper (MyBatis Plus ORM, database queries)
    ↓
Entity (Data models, mapped to DB tables)
```

**Key Packages**:
- `controller/`: 10 REST controllers for auth, users, teams, competitions, works, submissions, reviews, appeals, notifications
- `service/`: Business logic layer with interface + impl pattern (11 service interfaces)
- `mapper/`: MyBatis Plus mappers (23 mappers, one per entity)
- `entity/`: JPA-style entities with MyBatis Plus annotations (23 entities)
- `config/`: SecurityConfig (JWT + RBAC), CorsConfig, RedisConfig, GlobalExceptionHandler, JwtUtils
- `security/`: JwtAuthenticationFilter, custom security components
- `dto/`: Data Transfer Objects for API requests/responses
- `exception/`: Custom business exceptions

**Authentication Flow**:
- JWT-based stateless auth (24h expiration)
- BCrypt password encoding
- Role-based access: STUDENT, JUDGE, ADMIN
- Public endpoints: `/api/auth/login`, `/api/auth/register`, `/api/competitions/*`, Swagger docs
- All other endpoints require valid JWT in `Authorization` header

**MyBatis Plus Features Used**:
- Auto-fill for created/updated timestamps (`MyMetaObjectHandler`)
- Logic delete (deleted=0/1)
- ID auto-generation
- No XML mappers required (all annotation-based)

### Frontend Architecture

Vue 3 component-based architecture with role-specific layouts:

```
App.vue (Root)
    ↓
Layouts (StudentLayout, JudgeLayout, AdminLayout)
    ↓
Views (role-specific pages + shared pages)
    ↓
Components (Navigation, Sidebar)
```

**Key Structure**:
- `layouts/`: Role-specific layout wrappers with navigation/sidebar
- `views/student/`: Student pages (Home, Teams, Works, Registration, Results, Invitations, Profile)
- `views/judge/`: Judge pages (Home, Pending, Reviewed, Stats, Profile)
- `views/admin/`: Admin pages (Home, Competitions, Students, Judges, Works, Reviews, Stats)
- `views/`: Shared pages (Login, Register, Competitions, Tracks)
- `components/`: Reusable UI components (Navigation, Sidebar)
- `router/`: Vue Router with role-based route guards (meta: `{ requiresAuth, role }`)
- `stores/`: Pinia state management (user auth state, role-based permissions)
- `api/`: Axios HTTP client configured with JWT interceptor

**Route Guards**: Check JWT validity and user role before allowing access to role-specific routes.

### Data Flow

1. Frontend calls `/api/*` → Vite proxy → Backend `localhost:8080`
2. Backend JWT filter validates token, extracts user/role
3. Controller receives request, delegates to Service
4. Service executes business logic, calls Mapper for DB operations
5. Mapper executes MyBatis Plus queries on MySQL
6. Response flows back through layers, serialized to JSON
7. Redis used for caching (sessions, frequently accessed data)

## Testing Accounts

| Role | Username | Password | Default Redirect |
|------|----------|----------|------------------|
| Student | student001, 2021001 | 123456 | `/student` |
| Judge | judge001 | 123456 | `/judge` |
| Admin | admin | 123456 | `/admin` |

**Note**: Database README says admin password is `admin123`, judge/student passwords are `judge123`/`student123`, but STARTUP_GUIDE says all use `123456`. Check actual database state.

## Design System

This project uses an **Apple-inspired design system** documented in `DESIGN.md`. Key principles:

- **Typography**: SF Pro Display (20px+), SF Pro Text (<20px) with negative letter-spacing
- **Colors**: Binary light/dark rhythm (`#f5f5f7` vs `#000000`), single accent: Apple Blue (`#0071e3`)
- **Layout**: Full-viewport sections, cinematic whitespace, centered content (max ~980px)
- **Components**: Pill CTAs (980px radius), translucent glass navigation, minimal shadows
- **Philosophy**: Product-as-hero, controlled drama, minimalism as reverence

When implementing UI components, reference `DESIGN.md` for typography scales, color roles, spacing units, and component specs.

## Code Conventions

### Backend (Java)
- **Naming**: camelCase for methods/variables, PascalCase for classes
- **Lombok**: Use `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor` to reduce boilerplate
- **MyBatis Plus**: Entities use `@TableName`, `@TableId`, `@TableField` annotations
- **Controller Pattern**: Return `Result<T>` wrapper for consistent API responses
- **Service Pattern**: Interface + Impl class, annotate impl with `@Service`
- **Transaction**: Use `@Transactional` on service methods modifying multiple tables
- **Security**: Use `@PreAuthorize("hasRole('ADMIN')")` for role-based method security

### Frontend (Vue.js)
- **Component Files**: kebab-case naming (e.g., `StudentLayout.vue`, `student-works.vue`)
- **Props/Data**: camelCase in JavaScript
- **API Style**: Options API (not Composition API)
- **UI Library**: Element Plus components for forms, tables, dialogs, alerts
- **State**: Pinia stores for global state (user auth, notifications), local state for component-specific
- **Routing**: Vue Router with route guards checking `meta.requiresAuth` and `meta.role`
- **HTTP**: Axios instance with JWT interceptor adding `Authorization` header

### Database
- **Table/Column Names**: snake_case (e.g., `user`, `real_name`, `team_member`)
- **Primary Keys**: Auto-increment integers, named `id`
- **Foreign Keys**: Named `entity_id` (e.g., `user_id`, `team_id`, `competition_id`)
- **Timestamps**: `created_at`, `updated_at` (auto-filled by MyMetaObjectHandler)
- **Logic Delete**: `deleted` column (0=active, 1=deleted)

## Security Practices

- **Passwords**: Always use BCrypt encoding (never store plain text)
- **JWT**: 24-hour expiration, stored in frontend localStorage, sent in `Authorization: Bearer <token>` header
- **CORS**: Configured to allow all origins with credentials (adjust for production)
- **File Uploads**: Max 300MB, validate file types in backend, sanitize filenames
- **SQL Injection**: MyBatis Plus parameter binding prevents this automatically
- **XSS**: Frontend uses Vue's template system (auto-escapes), sanitize any user-generated HTML
- **RBAC**: Spring Security `@PreAuthorize` on sensitive endpoints, frontend route guards

## Configuration

**Backend**: `application.properties` contains all config. Key settings:
- Database connection (MySQL localhost:3306)
- Redis connection (localhost:6379)
- JWT secret/expiration
- File upload size limits
- MyBatis Plus settings

**Environment Variables**: None currently. For production, move sensitive configs (DB password, JWT secret) to environment variables or external config.

**Frontend**: `vite.config.js` defines dev server port (3000) and API proxy to backend.

## Key Domain Concepts

### Competition Lifecycle
1. Admin creates competition with tracks (CODE, PPT, VIDEO)
2. Admin publishes competition → Students can view/register
3. Students form teams (within track min/max size constraints)
4. Teams submit works before deadline
5. AI performs initial review (code similarity check, format validation)
6. Judges review assigned works, score based on rubrics
7. Final scores = AI_weight × AI_score + Judge_weight × Judge_score
8. Students can appeal unfair reviews
9. Admin finalizes results, publishes rankings

### Team Formation
- Students create teams or join via invitation/applications
- Team leader invites members (invitation expires after N days, configurable in `system_config`)
- Members accept/reject invitations
- Team size must meet track constraints (`min_team_size` to `max_team_size`)
- Teams register for specific competition track

### Multi-Track System
- **CODE_TRACK**: Programming projects (requires code files, executable)
- **PPT_TRACK**: Presentation slides (PowerPoint/PDF)
- **VIDEO_TRACK**: Digital media, animations, short videos

Each track has distinct file requirements and review criteria.

### Role Permissions
- **Student**: Create teams, submit works, view own results, appeal reviews
- **Judge**: Review assigned works, score, add comments, view review history
- **Admin**: Manage competitions, users, assignments, system configs, finalize results

## Common Work Patterns

### Adding a New API Endpoint
1. Create/modify Entity if new data model needed
2. Create Mapper interface extending `BaseMapper<Entity>`
3. Create Service interface + Impl with business logic
4. Create Controller with `@RestController`, `@RequestMapping`
5. Add endpoint to SecurityConfig's `permitAll()` if public, or rely on default auth
6. Test via Swagger UI or frontend integration

### Adding a New Frontend View
1. Create Vue component in appropriate `views/` subdirectory
2. Add route in `router/index.js` with `meta: { requiresAuth: true, role: 'STUDENT' }`
3. Use Element Plus components for UI (tables, forms, buttons)
4. Connect to backend API via Axios in component's `methods`
5. Add navigation link in appropriate layout's sidebar/router

### Debugging Backend Issues
1. Check console logs (MyBatis Plus logs SQL queries at DEBUG level)
2. Use Swagger UI to test API endpoints directly
3. Check `GlobalExceptionHandler` for error handling patterns
4. Verify JWT token validity in frontend localStorage
5. Check MySQL/Redis connection status

### Running Integration Tests
Currently only `WorkCompetitionPlatformApplicationTests` exists (basic context load test). When adding tests:
- Use `@SpringBootTest` for integration tests
- Use `@AutoConfigureMockMvc` for API endpoint tests
- Use `spring-security-test` for authenticated endpoint testing
- Test with realistic data from database init scripts

## Documentation References

- `DESIGN.md`: Apple design system principles, typography, colors, components
- `STARTUP_GUIDE.md`: Manual startup steps, troubleshooting, test accounts
- `database/README.md`: Database initialization instructions
- `docs/`: Detailed documentation (PROJECT_SUMMARY, API_DESIGN, database-design, FRONTEND_DESIGN)
- `.github/copilot-instructions.md`: Code style, architecture summary, build/test commands (currently empty)

## Critical Notes

- **JDK Version**: Must be Java 17. Earlier versions will cause compilation errors.
- **Database Schema**: 23 tables with complex relationships. Reference `database/init_database.sql` for schema details.
- **File Uploads**: Large file support (300MB) requires appropriate server config and timeout handling.
- **Team Size Validation**: Enforced in service layer, not just frontend. Check track's `min_team_size`/`max_team_size`.
- **AI Review Integration**: Not yet implemented in backend, but tables exist (`ai_review_report`, `ai_review_detail`). Placeholder for future integration.
- **Date Handling**: Jackson configured for `yyyy-MM-dd HH:mm:ss` format, GMT+8 timezone. Frontend must match.
# AGENTS.md

This file provides guidance to AI agents when working with code in this repository.

---

## Build Commands

**Always skip GPG signing locally:**
```bash
# Build everything
mvn clean install -Dmaven.javadoc.skip=true -Dgpg.skip=true

# Build a single module (run from the module directory)
cd kernel/kernel-idgenerator-service
mvn clean install -Dgpg.skip=true

# Skip tests for faster builds
mvn clean install -Dgpg.skip=true -DskipTests=true

# Run a single test class
mvn test -Dgpg.skip=true -Dtest=UinGeneratorServiceTest

# Run with a specific Spring profile
java -Dspring.profiles.active=local -jar target/<jar-name>.jar

# Run with remote config server
java -Dspring.profiles.active=env \
  -Dspring.cloud.config.uri=<config-url> \
  -Dspring.cloud.config.label=<branch> \
  -jar target/<jar-name>.jar
```

The root `pom.xml` only contains the `kernel` module. All service modules are under `kernel/`.

---

## Repository Structure

```
commons/
├── pom.xml                     # Root POM (io.mosip:commons:1.4.0-SNAPSHOT)
├── kernel/                     # All kernel modules — one Maven reactor
│   ├── pom.xml                 # io.mosip.kernel:kernel-parent (Java 21, 39 submodules)
│   ├── kernel-bom/             # Bill of Materials — all third-party version pins
│   ├── kernel-core/            # Shared interfaces, exceptions, base DTOs, utilities
│   ├── kernel-logger-logback/  # Logback wrapper implementing kernel-core logger SPI
│   ├── kernel-dataaccess-hibernate/  # Hibernate JPA base classes
│   ├── kernel-idgenerator-*/   # ID generator libraries (VID, PRID, RID, TokenID, MachineID, etc.)
│   ├── kernel-idvalidator-*/   # Corresponding validators for each ID type
│   ├── kernel-notification-service/  # Spring Boot: SMS + email via REST (port 8083)
│   ├── kernel-ridgenerator-service/  # Spring Boot: RID generation REST service
│   ├── kernel-idgenerator-service/   # Vert.x: UIN + VID generator service (NOT Spring MVC)
│   ├── kernel-pridgenerator-service/ # Spring Boot: PRID generation REST service
│   ├── kernel-salt-generator/  # Spring Boot: cryptographic salt generation
│   └── kernel-config-server/   # Spring Cloud Config Server wrapper
├── helm/                       # Helm charts for Kubernetes deployment
│   ├── idgenerator/            # chart version: 0.0.1-develop (image: mosipqa/kernel-idgenerator-service:develop)
│   ├── notifier/               # chart version: 0.0.1-develop
│   ├── ridgenerator/           # chart version: 0.0.1-develop
│   ├── pridgenerator/          # chart version: 0.0.1-develop
│   ├── config-server/          # chart version: 0.0.2-develop
│   ├── conf-secrets/           # chart version: 0.0.1-develop
│   └── regproc-salt/           # chart version: 0.0.1-develop
├── db_scripts/                 # PostgreSQL DDL + DML scripts (mosip_kernel schema)
│   └── <db_name>/ddl/ dml/     # Tables + seed data; run via deploy.sh
└── deploy/                     # Shell-based Kubernetes install scripts
    ├── kernel/
    ├── config-server/
    └── conf-secrets/
```

---

## Architecture: Two Service Patterns

### Standard Spring Boot services
`kernel-ridgenerator-service`, `kernel-pridgenerator-service`, `kernel-notification-service`, `kernel-salt-generator`, `kernel-config-server` — follow the conventional `@SpringBootApplication` pattern with Spring MVC controllers. Their `bootstrap.properties` points to the config server for all runtime config.

### Vert.x + Spring hybrid (`kernel-idgenerator-service`)
This is the most architecturally unusual module. It does **not** use Spring MVC for its HTTP layer. Instead:
- `IDGeneratorVertxApplication.main()` first fetches config from Spring Config Server via the Vert.x config retriever, then bootstraps a `Vertx` instance manually.
- **UIN subsystem**: `UinGeneratorVerticle` + `UinTransferVerticle` — worker verticles that pre-generate a pool of UINs in PostgreSQL (`kernel.uin` table) using `SecureRandom` + Verhoeff checksum + configurable filters. UINs are produced in batches and held in the DB until consumed by ID Repository.
- **VID subsystem**: `VidPoolCheckerVerticle`, `VidPopulatorVerticle`, `VidExpiryVerticle`, `VidIsolatorVerticle` — worker verticles managing the VID pool lifecycle. VID generation reuses the `kernel-idgenerator-vid` library.
- Spring context (`HibernateDaoConfig`) is initialised only for JPA (Hibernate/PostgreSQL), not for HTTP serving.
- Verticles communicate via the Vert.x event bus (addresses defined in `UinGeneratorConstant`, `EventType`).
- HTTP health checks are handled by Vert.x `HealthCheckHandler`, not Spring Actuator routes.

---

## MOSIP ID Types (Domain Context)

Understanding these is essential when working on any generator or validator module:

| ID | Full Name | Nature | Key facts |
|---|---|---|---|
| **UIN** | Unique Identification Number | Permanent resident ID | Generated with SecureRandom + Verhoeff checksum; never reissued; filtered against pattern rules |
| **VID** | Virtual ID | Temporary alias for UIN | Expirable, revocable, privacy-friendly; used for authentication instead of exposing UIN |
| **RID / AID** | Registration / Application ID | Per-registration-event ID | Tracks each lifecycle event (enrolment, update, lost ID) at registration center |
| **PRID** | Pre-Registration ID | Pre-registration phase ID | Assigned when resident books appointment before visiting registration center |
| **Token ID (PSUT)** | Partner-Specific User Token | Per-partner pseudonym | Returned in auth response to relying party; prevents cross-partner linkage; not used for auth |

Additional IDs generated by library modules (not services): MachineID, RegistrationCenterID, PartnerID, MISPID, LicenseKey.

---

## Key Cross-Cutting Patterns

### Dependency hierarchy
All modules depend on `kernel-core` for interfaces and `kernel-bom` for version pinning. Do not declare third-party dependency versions in module POMs — they come from the BOM.

### Lombok
All entity classes and most DTOs use `@Data`, `@AllArgsConstructor`, `@NoArgsConstructor`. Lombok is declared as `<scope>compile</scope>` in `kernel-core/pom.xml` and inherited. **Important**: if a `.java` file is placed in the wrong package directory (package declaration doesn't match directory), the resulting "duplicate class" compile error will prevent Lombok annotation processing from completing, causing cascading "cannot find symbol" errors across the entire module — not just the offending file.

### Configuration
All services pull runtime configuration from the MOSIP Config Server at startup. Property files live in a separate repo: `https://github.com/mosip/mosip-config`. The relevant files are `application-default.properties` and `kernel-default.properties`. Local overrides go in `bootstrap.properties` (`spring.profiles.active=local`).

### Database schemas
Each service uses its own PostgreSQL schema (e.g., `kernel` for UIN/VID tables). DDL/DML bootstrap scripts are in `db_scripts/`. Tests use H2 in-memory with the same Hibernate configuration.

### Versioning convention
- `develop` branch: `*-SNAPSHOT` versions (currently `1.4.0-SNAPSHOT`)
- Release candidates: `1.x.x-rc.y`
- Production releases: `1.x.x`
- Helm chart versions for develop stay at `0.0.x-develop`; Docker image `tag: develop`, repo `mosipqa/...` (not `mosipid/...` which is production)

### Branch merge strategy
When merging a release tag into develop (`PROMPT.md` pattern):
- Default to release tag for all code changes
- Keep develop's SNAPSHOT versions in all `pom.xml`
- Keep develop's Helm chart versions (`0.0.x-develop`) and `mosipqa/` image repos in `values.yaml`
- Take resource limits / javaOpts from the release tag
- Version bump is done separately (`Snapshot.md` pattern): find-replace SNAPSHOT version strings in `pom.xml` files only

---

## Runtime Prerequisites (Local Dev)

1. **Config Server** running, pointed at a local clone of `mosip-config`
2. **PostgreSQL 10+** with scripts from `db_scripts/` applied  
3. **Keycloak / auth server** (for services that validate auth tokens)
4. `kernel-auth-adapter.jar` on classpath for auth-protected services

Config server local start command:
```bash
java -jar kernel-config-server-<version>.jar \
  -Dspring.profiles.active=native \
  -Dspring.cloud.config.server.native.search-locations=file:<path-to-mosip-config>/config
```

API docs (Swagger) for each service are at `http://localhost:<port>/v1/<service-name>/swagger-ui.html`.

---

## CI/CD

CI runs on push via GitHub Actions (`.github/workflows/push-trigger.yml`). The workflow builds all modules, runs tests, and publishes SNAPSHOT artifacts to `https://central.sonatype.com/repository/maven-snapshots/`. GPG signing is required for publish but skipped locally with `-Dgpg.skip=true`. Sonar analysis runs under the `sonar` Maven profile.

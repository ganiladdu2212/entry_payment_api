# Entry Payment API

Spring Boot 4.1 / Java 21 API with MySQL, stateless JWT security, OpenAPI/Swagger, validation, JPA, Actuator, and DevTools.

## Run locally

1. Install JDK 21 and Maven 3.6.3+.
2. Start MySQL on port 3306. The local profile creates the `entry_payment` database if permitted.
3. Run `mvn spring-boot:run`.
4. Open `http://localhost:8080/entry-payment/swagger-ui.html`.

Use `SPRING_PROFILES_ACTIVE=dev` or `prod` and provide `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and a strong `JWT_SECRET`. Never commit non-local secrets.

Every persistent entity should extend `BaseEntity`; this guarantees an immutable `inserted_date` populated in the Asia/Kolkata time zone.

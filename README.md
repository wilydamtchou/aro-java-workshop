# DigiBank - Workshop 3 and Workshop 4 README

## Overview

This repository contains the DigiBank implementation aligned with **Workshop 3: Dynamic Security Analysis of DigiBank at Runtime** and **Workshop 4: Securing containers, dependencies, and deployment artifacts** from the course *UCC152-2 Introduction to Security in DevOps*.

The project therefore documents and implements two complementary security stages: runtime security validation through DAST in Workshop 3, then container, dependency, and artifact hardening with pipeline automation in Workshop 4.

## Project purpose

DigiBank is used as a realistic case study for a modern banking application with REST endpoints, a database, business services, containerized delivery, and CI/CD automation. The goal is to secure the application from the source code level all the way to the packaged image and the delivery pipeline.

Workshop 3 focuses on how the application behaves when it runs, while Workshop 4 focuses on what the application imports, packages, ships, and executes inside containers and build artifacts.

## Repository structure

A recommended structure for this project is:

```text
digibank-parent/
├── common-module/
├── customer-module/
├── account-module/
├── transfer-module/
├── digibank-web/
├── dast/
│   ├── postman/
│   ├── zap/
│   └── reports/
├── container-security/
│   ├── reports/
│   ├── notes/
│   └── scripts/
├── .github/
│   └── workflows/
├── Dockerfile
├── .dockerignore
├── docker-compose.yml
├── pom.xml
└── README.md
```

The repository may also contain exported reports, screenshots, notes, and scripts used during validation and remediation.

# Workshop 3

## Objective

Workshop 3 extends the security work done previously by validating DigiBank at runtime with a DAST mindset. The aim is to detect weaknesses visible through HTTP behavior, validate the application’s response quality, remediate the issues found, and then re-run the same scenarios to prove the corrections.

## Workshop 3 scope

The Workshop 3 scope covers observable runtime weaknesses such as endpoint behavior, request validation, status codes, error handling, information leakage, and authorization or session-related issues visible from the outside. It does not primarily focus on dependency analysis, Docker hardening, or pipeline security, which belong to Workshop 4.

## Expected Workshop 3 outcomes

At the end of the DAST phase, the project should demonstrate:

- Controlled HTTP responses and generic error messages.
- Stronger validation at the API and service layers.
- Reduced exposure of sensitive data in responses.
- Reproducible testing through Postman, curl, Newman, and optionally OWASP ZAP.
- Documented evidence of before/after correction behavior.

## Workshop 3 checklist

- Start DigiBank in a stable environment.
- Verify the main endpoints are accessible.
- Explore the API using Swagger or Postman where enabled.
- Run nominal and invalid scenarios on customers, accounts, and transfers.
- Observe response codes, error messages, and data exposure.
- Fix the weaknesses in DTOs, services, and exception handlers.
- Replay the same tests to validate the fix.

## Workshop 3 test scenarios

### Customers

Typical customer tests include creating a valid customer, trying an invalid email, sending malformed phone numbers or national IDs, checking duplicate email behavior, and querying a non-existent customer.

### Accounts

Typical account tests include creating a valid account, attempting a negative balance, and querying a non-existent account.

### Transfers

Typical transfer tests include a normal transfer, transfer to the same account, zero-amount transfer, and insufficient-balance transfer.

## Workshop 3 commands

```bash
mvn clean install
mvn spring-boot:run -pl digibank-web
curl -i http://localhost:8080/api/customers/99999
newman run DigiBank-DAST.postman_collection.json -e DigiBank-Local.postman_environment.json
```

The workshop also encourages using OWASP ZAP to observe routes, headers, and response patterns directly from the running application.

## Workshop 3 evidence

For each issue identified dynamically, keep evidence of:

- The endpoint tested.
- The request executed.
- The observed response before correction.
- The vulnerability type.
- The impact on the application or data exposure.
- The fix implemented.
- The revalidation result after correction.

## Workshop 3 remediation themes

The main remediation themes expected in Workshop 3 are:

- Stronger input validation.
- Reduced data exposure in response DTOs.
- Safer centralized error handling.
- Stronger service-layer safeguards for business rules.
- Controlled Swagger exposure by profile.

# Workshop 4

## Objective

Workshop 4 extends the security work by analyzing DigiBank as a complete software artifact: code, dependencies, container images, build files, secrets handling, and CI/CD controls. The goal is to reduce attack surface, detect vulnerable dependencies, harden the container, and automate the checks in the pipeline.

## Workshop 4 scope

The Workshop 4 scope includes Maven dependencies, transitive dependencies, pom.xml configuration, Dockerfile, docker-compose.yml, .dockerignore, runtime environment variables, secrets handling, image metadata, and GitHub Actions workflows related to dependency and container security. It does not primarily repeat the runtime endpoint analysis covered in Workshop 3.

## Expected Workshop 4 outcomes

At the end of the container and dependency security phase, the project should demonstrate:

- Controlled and versioned dependency management.
- A usable Dockerfile with clear build/runtime separation.
- A reduced and better protected container image.
- Externalized secrets and safer environment-variable usage.
- Dependency and container checks automated in GitHub Actions.
- Traceable evidence through reports and image metadata.

## Workshop 4 checklist

- Reopen the existing DigiBank project cleanly in IntelliJ IDEA.
- Run a full Maven build from the parent project.
- Inspect the dependency graph with Maven.
- Run OWASP Dependency-Check.
- Build and inspect the Docker image.
- Review the Docker history and image metadata.
- Verify `.dockerignore` limits the build context.
- Verify secrets are not hard-coded in versioned files or image layers.
- Prepare and run the GitHub Actions workflow.
- Add Trivy image scanning in the pipeline.

## Recommended technical stack

The workshop uses the following tools and technologies:

- Java JDK 17.
- Maven 3.9.x.
- Spring Boot.
- Docker and Docker Compose.
- OWASP Dependency-Check.
- Trivy.
- Git and GitHub.
- GitHub Actions.

## Configuration principles

The Spring configuration should remain environment-driven and avoid hard-coded secrets. The `application.yml`, `application-dev.yml`, and `application-test.yml` files should use variables for datasource settings and keep security-sensitive defaults under control.

Swagger/OpenAPI should remain disabled by default outside the development profile, and error exposure should stay minimal in shared artifacts.

## Maven and dependency control

The parent `pom.xml` is the main entry point for dependency analysis and version centralization. It should contain consistent version properties for the build and security plugins, plus the OWASP Dependency-Check plugin configured to fail the build when needed.

Useful commands include:

```bash
mvn clean
mvn compile
mvn test
mvn clean verify
mvn dependency:tree
mvn org.owasp:dependency-check-maven:check
mvn org.owasp:dependency-check-maven:purge
```

## Dockerfile and image hardening

The Dockerfile should use a multi-stage build so that Maven is only present in the build stage and the final runtime image contains only the necessary Java runtime and packaged application. This reduces the attack surface and makes the artifact easier to inspect and maintain.

The final image should also be evaluated for:

- Base image choice.
- Layer count and image size.
- Port exposure.
- Runtime user privileges.
- Unnecessary components in the final image.

## `.dockerignore`

A proper `.dockerignore` file should exclude build outputs, IDE files, local secrets, logs, temporary notes, DAST folders, and reports that do not belong in the container build context. This reduces accidental file leakage into Docker layers and keeps the build context clean.

## Docker Compose

A working `docker-compose.yml` should start DigiBank and PostgreSQL in a reproducible environment with environment variables rather than hard-coded credentials. This helps keep the setup close to a real deployment while still remaining suitable for local analysis and automation.

## Trivy integration

Trivy is added to analyze the final Docker image for vulnerabilities in system packages, embedded libraries, secrets, and misconfigurations. In the final pipeline, Trivy complements Dependency-Check by covering the image layer rather than only Maven dependencies.

## Workshop 4 commands

```bash
mvn clean install
mvn dependency:tree
mvn org.owasp:dependency-check-maven:check
docker build -t digibank:local .
docker image inspect digibank:local
docker history digibank:local
docker compose up --build
```

## Workshop 4 evidence

For each finding or remediation, keep evidence of:

- The component or artifact analyzed.
- The file or image involved.
- The weakness detected.
- The tool used to detect it.
- The risk level and impact.
- The remediation applied.
- The verification command or rerun result.

## Workshop 4 remediation themes

The main remediation themes expected in Workshop 4 are:

- Minimize the container image.
- Use a non-root runtime user.
- Remove secrets from versioned files and image layers.
- Update or replace vulnerable dependencies.
- Limit Swagger exposure to development only.
- Integrate automated dependency and image checks into the CI/CD pipeline.

# CI/CD pipeline

## Pipeline overview

The GitHub Actions workflow is designed as a complete DevSecOps pipeline combining build, tests, static analysis, dependency scanning, Docker image creation, Trivy scanning, runtime deployment, Newman, and OWASP ZAP checks. The pipeline also archives reports and metadata as artifacts for traceability.

## Main pipeline stages

- Build and unit tests.
- Cucumber tests.
- JaCoCo coverage.
- Secret scanning with Gitleaks.
- OWASP Dependency-Check.
- SpotBugs, PMD, Checkstyle, and PIT.
- SonarQube quality gate.
- Docker image build.
- Trivy image scan.
- Runtime deployment with PostgreSQL.
- Newman API verification.
- OWASP ZAP scanning.
- Final artifact publication.

## Pipeline secrets

Secrets such as `NVD_API_KEY`, SonarQube credentials, and any registry or deployment credentials must remain outside the repository and be injected at runtime through GitHub Secrets or equivalent mechanisms.

## Final validation criteria

The project can be considered complete when all of the following are true:

- The application still builds and runs normally.
- DAST weaknesses are remediated and revalidated.
- Dependency and container findings are reduced or justified.
- The Docker image is cleaner and more controlled.
- The pipeline reruns the main security checks automatically.
- Artifacts and reports are preserved for traceability.

## Revalidation commands

```bash
mvn clean
mvn test
mvn clean verify
mvn dependency:tree
mvn org.owasp:dependency-check-maven:check -DnvdApiKey=YOUR_NVD_API_KEY
docker build -t digibank:final .
trivy image --severity HIGH,CRITICAL digibank:final
```

## Repository usage

This README is intended as the main technical and pedagogical entry point for the DigiBank project. It summarizes the security work completed in Workshop 3 and Workshop 4, and it documents the source code, runtime validation, container hardening, dependency management, and CI/CD security automation required for the course.
```
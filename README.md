# WebAuthn4J Spring Security Samples

WebAuthn4J Spring Security Samples are sample applications for WebAuthn4J Spring Security.

## Modules
* spa
  * Single Page Application sample
* lib/spa-angular-client
  * Frontend part of spa sample
* mpa
  * Multi Page Application sample

## Build

WebAuthn4J Spring Security uses a Gradle based build system.
In the instructions below, `gradlew` is invoked from the root of the source tree and serves as a cross-platform,
self-contained bootstrap mechanism for the build.

### Prerequisites

- Java17 or later
- Spring Framework 6.0 or later

### Checkout sources

```bash
git clone https://github.com/webauthn4j/webauthn4j-spring-security-samples
```

### Build all jars

```bash
./gradlew build
```

### Execute spa sample application

```
./gradlew spa:bootRun
```


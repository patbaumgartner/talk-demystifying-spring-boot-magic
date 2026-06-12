# Demo: Audit Spring Boot Starter

Two projects that demonstrate how a custom Spring Boot starter works end to end.

```
demo/
├── audit-spring-boot/          ← the starter (build this first)
│   ├── audit-spring-boot-autoconfigure/
│   └── audit-spring-boot-starter/
└── claims-service/             ← sample app that consumes the starter
```

---

## Step 1 – Build and install the starter

```bash
cd audit-spring-boot
./mvnw clean install
```

This installs the starter into your local Maven repository so the sample app can find it.

## Step 2 – Run the sample application

```bash
cd ../claims-service
./mvnw spring-boot:run
```

The app starts on **http://localhost:8080**.

---

## Try it out

### Submit a claim

```bash
curl -s -X POST http://localhost:8080/claims \
  -H "Content-Type: application/json" \
  -H "X-User-Id: user:42" \
  -d '{"type":"DENTAL","insuredId":"ins-1","description":"Annual check-up"}' | jq
```

Watch the terminal – you will see an audit log line like:

```
[AUDIT] topic=audit-events principal=user:42 action=CLAIM_SUBMITTED resource=<uuid>
```

### Approve a claim

```bash
# Use the id returned by the submit call
curl -s -X PUT http://localhost:8080/claims/<id>/approve \
  -H "X-User-Id: reviewer:7" | jq
```

### Inspect auto-configuration

```bash
curl -s http://localhost:8080/actuator/conditions | jq '.contexts[].positiveMatches.AuditAutoConfiguration'
```

You will see `OnPropertyCondition` and `OnClassCondition` both matched.

### Disable audit logging

Add `audit.enabled=false` to `application.properties` and restart.  
The `AuditEventListener` bean will not be created – verify with `/actuator/conditions`.

---

## Key points to highlight during the demo

| What you do | What Spring Boot does |
|---|---|
| Add `audit-spring-boot-starter` to `pom.xml` | Puts `AuditAutoConfiguration` on the classpath via `.imports` |
| Start the app (no config) | `@ConditionalOnProperty(matchIfMissing=true)` passes, listener registered |
| Call `publisher.publishEvent(new AuditEvent(...))` | `@EventListener` fires, log line written |
| Define your own `AuditEventListener` bean | `@ConditionalOnMissingBean` backs off, your bean wins |
| Set `audit.enabled=false` | `@ConditionalOnProperty` fails, nothing registered |

---

## Running the tests

```bash
# Autoconfigure module – uses ApplicationContextRunner (fast, no web layer)
cd audit-spring-boot
./mvnw test

# Claims service – includes @WebMvcTest slice + @SpringBootTest integration test
cd ../claims-service
./mvnw test
```

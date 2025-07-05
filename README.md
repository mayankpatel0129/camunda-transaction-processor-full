# Camunda Transaction Processor

## 📦 Project Structure

- `src/main/resources/DetermineTransactionPath.dmn` — Decision table for routing
- `src/main/resources/transactionProcessingDMNBased.bpmn` — Main process model
- `TaskRouterDelegate.java` — Executes sequential/parallel task logic
- `TaskRouterDelegateTest.java` — Unit tests for delegate behavior

## 🧰 Prerequisites

- Java 17+
- Maven 3.6+
- Camunda Spring Boot Starter
- IDE (IntelliJ or Eclipse)

## 🚀 How to Build & Run

```bash
mvn clean install
mvn spring-boot:run
```

App starts at: `http://localhost:8080`

## 🧪 Run Tests

```bash
mvn test
```

Includes tests for:
- Transaction DMN routing
- TaskRouterDelegate for sequential & parallel logic

## 📝 Usage

Start a process via REST or unit test.
The DMN will decide task execution path.

## 💡 DMN Rule Format

```
transactionType → taskFlow
---------------------------
"Purchase"        → "authorize,settle"
"Payment"         → "validate,parallel:post,reconcile"
"Adjustment"      → "review,apply,parallel:log,notify"
```

## 🔄 Customize

Update the DMN to add or modify transaction task logic.

# common

Shared event contracts between services.

## 1. Directory tree

```
common/
└── events/
```

## 2. Dependency tree

```
common/events
├── EventEnvelope.java
├── PostCreatedEvent.java
└── UserDeletedEvent.java
```

Consumers: <!-- fill: which services use these (currently inter-service calls are REST, not events) -->

## 3. Packages

### events

<!-- fill: EventEnvelope, PostCreatedEvent, UserDeletedEvent — purpose, serialization, who publishes/consumes -->

## 4. Important flows

<!-- fill: planned event-driven communication (if any) vs current REST calls -->

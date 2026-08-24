GATEWAY_PATH := backend/api-gateway
GATEWAY_PID := /tmp/blog-gateway.pid
GATEWAY_LOG := logs/gateway.log

USER_SERVICE_PATH := backend/user-service
USER_SERVICE_PID := /tmp/blog-user-service.pid
USER_SERVICE_LOG := logs/user-service.log

AUTH_SERVICE_PATH := backend/auth-service
AUTH_SERVICE_PID := /tmp/blog-auth-service.pid
AUTH_SERVICE_LOG := logs/auth-service.log

DOCKER_COMPOSE := docker


.PHONY: docker-up \
        dev-user-service stop-user-service \
        dev-gateway stop-gateway


docker-up:
	cd $(DOCKER_COMPOSE) && systemctl stop postgresql && docker compose down -v && docker compose up -d

dev-gateway:
	@mkdir -p logs
	cd $(GATEWAY_PATH) && \
	set -a && . ../../.env && set +a && \
	nohup ./mvnw spring-boot:run > ../../$(GATEWAY_LOG) 2>&1 & \
	echo $$! > $(GATEWAY_PID)


stop-gateway:
	@if [ -f $(GATEWAY_PID) ]; then \
		kill $$(cat $(GATEWAY_PID)) 2>/dev/null || true; \
		rm -f $(GATEWAY_PID); \
		echo "Gateway stopped"; \
	else \
		echo "Gateway is not running"; \
	fi

# user service
dev-user-service:
	@mkdir -p logs
	cd $(USER_SERVICE_PATH) && \
	set -a && . ../../.env && set +a && \
	nohup ./mvnw spring-boot:run > ../../$(USER_SERVICE_LOG) 2>&1 & \
	echo $$! > $(USER_SERVICE_PID)


stop-user-service:
	@if [ -f $(USER_SERVICE_PID) ]; then \
		kill $$(cat $(USER_SERVICE_PID)) 2>/dev/null || true; \
		rm -f $(USER_SERVICE_PID); \
		echo "User service stopped"; \
	else \
		echo "User service is not running"; \
	fi

# auth service
dev-auth-service:
	@mkdir -p logs
	cd $(AUTH_SERVICE_PATH) && \
	set -a && . ../../.env && set +a && \
	nohup ./mvnw spring-boot:run > ../../$(AUTH_SERVICE_LOG) 2>&1 & \
	echo $$! > $(AUTH_SERVICE_PID)


stop-auth-service:
	@if [ -f $(AUTH_SERVICE_PID) ]; then \
		kill $$(cat $(AUTH_SERVICE_PID)) 2>/dev/null || true; \
		rm -f $(AUTH_SERVICE_PID); \
		echo "Auth service stopped"; \
	else \
		echo "Auth service is not running"; \
	fi

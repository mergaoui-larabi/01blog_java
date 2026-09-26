.PHONY: \
	front-up front-down \
	docker-up \
	dev-gateway stop-gateway \
	dev-user-service stop-user-service \
	dev-auth-service stop-auth-service \
	stop start


FRONT_END_PATH := frontend/blog
FRONT_END_PID := /tmp/frontend.pid
FRONT_END_LOGS := logs/angular.log

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


# =======================
# Frontend
# =======================

front-up: front-down
	@mkdir -p logs
	@cd $(FRONT_END_PATH) && \
	nohup sh -c 'exec ng serve 2>&1 | sed -E "s/\x1B\[[0-9;]*[mK]//g"' \
		> ../../$(FRONT_END_LOGS) 2>&1 & \
	echo $$! > $(FRONT_END_PID)
	@sleep 1
	@echo "Frontend started on http://localhost:4200"

front-down:
	@fuser -k 4200/tcp 2>/dev/null || true
	@echo "Frontend stopped"


# =======================
# Docker
# =======================

docker-up:
	cd $(DOCKER_COMPOSE) && \
	systemctl stop postgresql 2>/dev/null || true && \
	docker compose down -v && \
	docker compose up -d


# =======================
# API Gateway
# =======================

dev-gateway: stop-gateway
	@mkdir -p logs
	@cd $(GATEWAY_PATH) && \
	set -a && . ../../.env && set +a && \
	nohup ./mvnw spring-boot:run > ../../$(GATEWAY_LOG) 2>&1 & \
	echo $$! > $(GATEWAY_PID)
	@echo "Gateway started"

stop-gateway:
	@if [ -f $(GATEWAY_PID) ]; then \
		kill $$(cat $(GATEWAY_PID)) 2>/dev/null || true; \
		rm -f $(GATEWAY_PID); \
		echo "Gateway stopped"; \
	else \
		echo "Gateway is not running"; \
	fi


# =======================
# User Service
# =======================

dev-user-service: stop-user-service
	@mkdir -p logs
	@cd $(USER_SERVICE_PATH) && \
	set -a && . ../../.env && set +a && \
	nohup ./mvnw spring-boot:run > ../../$(USER_SERVICE_LOG) 2>&1 & \
	echo $$! > $(USER_SERVICE_PID)
	@echo "User service started"

stop-user-service:
	@if [ -f $(USER_SERVICE_PID) ]; then \
		kill $$(cat $(USER_SERVICE_PID)) 2>/dev/null || true; \
		rm -f $(USER_SERVICE_PID); \
		echo "User service stopped"; \
	else \
		echo "User service is not running"; \
	fi


# =======================
# Auth Service
# =======================

dev-auth-service: stop-auth-service
	@mkdir -p logs
	@cd $(AUTH_SERVICE_PATH) && \
	set -a && . ../../.env && set +a && \
	nohup ./mvnw spring-boot:run > ../../$(AUTH_SERVICE_LOG) 2>&1 & \
	echo $$! > $(AUTH_SERVICE_PID)
	@echo "Auth service started"

stop-auth-service:
	@if [ -f $(AUTH_SERVICE_PID) ]; then \
		kill $$(cat $(AUTH_SERVICE_PID)) 2>/dev/null || true; \
		rm -f $(AUTH_SERVICE_PID); \
		echo "Auth service stopped"; \
	else \
		echo "Auth service is not running"; \
	fi


# =======================
# Global
# =======================

stop: stop-auth-service stop-user-service stop-gateway front-down

start: dev-gateway dev-user-service dev-auth-service
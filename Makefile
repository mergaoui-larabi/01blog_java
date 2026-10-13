.PHONY: \
	front-up front-down \
	docker-up \
	dev-gateway stop-gateway \
	dev-user-service stop-user-service \
	dev-auth-service stop-auth-service \
	dev-media-service stop-media-service \
	stop start


# =======================
# Paths
# =======================

FRONT_END_PATH := frontend/blog
FRONT_END_PID := /tmp/frontend.pid
FRONT_END_LOG := logs/angular.log

GATEWAY_PATH := backend/api-gateway
GATEWAY_PORT := 5000
GATEWAY_LOG := logs/gateway.log

USER_SERVICE_PATH := backend/user-service
USER_SERVICE_PORT := 5001
USER_SERVICE_LOG := logs/user-service.log

AUTH_SERVICE_PATH := backend/auth-service
AUTH_SERVICE_PORT := 5003
AUTH_SERVICE_LOG := logs/auth-service.log

MEDIA_SERVICE_PATH := backend/media-service
MEDIA_SERVICE_PORT := 5002
MEDIA_SERVICE_LOG := logs/media-service.log

DOCKER_COMPOSE := docker


# =======================
# Frontend
# =======================

front-up: front-down
	@mkdir -p logs
	@cd $(FRONT_END_PATH) && \
	nohup sh -c 'npm install && exec npm exec -- ng serve' \
		> ../../$(FRONT_END_LOG) 2>&1 < /dev/null & \
	echo $$! > $(FRONT_END_PID)
	@sleep 1
	@echo "Frontend starting on http://localhost:4200"

front-down:
	@fuser -k 4200/tcp 2>/dev/null || true
	@rm -f $(FRONT_END_PID)
	@echo "Frontend stopped"


# =======================
# Docker
# =======================

docker-up:
	@cd $(DOCKER_COMPOSE) && \
	set -a && . ../.env && set +a && \
	docker compose down -v && \
	docker compose up -d


# =======================
# API Gateway
# =======================

dev-gateway: stop-gateway
	@mkdir -p logs
	@cd $(GATEWAY_PATH) && \
	set -a && . ../../.env && set +a && \
	nohup sh -c './mvnw install && exec ./mvnw spring-boot:run' \
		> ../../$(GATEWAY_LOG) 2>&1 < /dev/null & \
	echo $$! > /tmp/blog-gateway.pid
	@echo "Gateway starting on port $(GATEWAY_PORT)"

stop-gateway:
	@fuser -k $(GATEWAY_PORT)/tcp 2>/dev/null || true
	@rm -f /tmp/blog-gateway.pid
	@echo "Gateway stopped"


# =======================
# User Service
# =======================

dev-user-service: stop-user-service
	@mkdir -p logs
	@cd $(USER_SERVICE_PATH) && \
	set -a && . ../../.env && set +a && \
	nohup sh -c './mvnw install && exec ./mvnw spring-boot:run' \
		> ../../$(USER_SERVICE_LOG) 2>&1 < /dev/null & \
	echo $$! > /tmp/blog-user-service.pid
	@echo "User service starting on port $(USER_SERVICE_PORT)"

stop-user-service:
	@fuser -k $(USER_SERVICE_PORT)/tcp 2>/dev/null || true
	@rm -f /tmp/blog-user-service.pid
	@echo "User service stopped"


# =======================
# Auth Service
# =======================

dev-auth-service: stop-auth-service
	@mkdir -p logs
	@cd $(AUTH_SERVICE_PATH) && \
	set -a && . ../../.env && set +a && \
	nohup sh -c './mvnw install && exec ./mvnw spring-boot:run' \
		> ../../$(AUTH_SERVICE_LOG) 2>&1 < /dev/null & \
	echo $$! > /tmp/blog-auth-service.pid
	@echo "Auth service starting on port $(AUTH_SERVICE_PORT)"

stop-auth-service:
	@fuser -k $(AUTH_SERVICE_PORT)/tcp 2>/dev/null || true
	@rm -f /tmp/blog-auth-service.pid
	@echo "Auth service stopped"


# =======================
# Media Service
# =======================

dev-media-service: stop-media-service
	@mkdir -p logs
	@cd $(MEDIA_SERVICE_PATH) && \
	set -a && . ../../.env && set +a && \
	nohup sh -c './mvnw install && exec ./mvnw spring-boot:run' \
		> ../../$(MEDIA_SERVICE_LOG) 2>&1 < /dev/null & \
	echo $$! > /tmp/blog-media-service.pid
	@echo "Media service starting on port $(MEDIA_SERVICE_PORT)"

stop-media-service:
	@fuser -k $(MEDIA_SERVICE_PORT)/tcp 2>/dev/null || true
	@rm -f /tmp/blog-media-service.pid
	@echo "Media service stopped"


# =======================
# Global
# =======================

stop: stop-media-service stop-auth-service stop-user-service stop-gateway front-down

start: dev-gateway dev-user-service dev-auth-service dev-media-service front-up
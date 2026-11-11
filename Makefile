.PHONY: \
	front-up front-down \
	docker-up docker-down \
	dev-gateway stop-gateway \
	dev-user-service stop-user-service \
	dev-auth-service stop-auth-service \
	dev-media-service stop-media-service \
	dev-post-service stop-post-service \
	dev-notification-service stop-notification-service \
	dev-moderation-service stop-moderation-service \
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
AUTH_SERVICE_PORT := 5002
AUTH_SERVICE_LOG := logs/auth-service.log

MEDIA_SERVICE_PATH := backend/media-service
MEDIA_SERVICE_PORT := 5003
MEDIA_SERVICE_LOG := logs/media-service.log

POST_SERVICE_PATH := backend/post-service
POST_SERVICE_PORT := 5004
POST_SERVICE_LOG := logs/post-service.log

NOTIFICATION_SERVICE_PATH := backend/notification-service
NOTIFICATION_SERVICE_PORT := 5005
NOTIFICATION_SERVICE_LOG := logs/notification-service.log

MODERATION_SERVICE_PATH := backend/moderation-service
MODERATION_SERVICE_PORT := 5006
MODERATION_SERVICE_LOG := logs/moderation-service.log

JWT_KEYS_DIR := backend/auth-service/keys/

LOG_DIR := logs
TIMESTAMP := $(shell date +%Y-%m-%d_%H-%M-%S)

DOCKER_COMPOSE := docker

COMPOSE_FILE := docker/docker-compose.yml

# =======================
# keys generation
# =======================

key-gen:
	@mkdir -p $(AUTH_SERVICE_PATH)/keys
	@test ! -f $(AUTH_SERVICE_PATH)/keys/private.pem || \
		(echo "Private key already exists. Aborting."; exit 1)
	@openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 \
		-out $(AUTH_SERVICE_PATH)/keys/private.pem
	@openssl pkey -in $(AUTH_SERVICE_PATH)/keys/private.pem -pubout \
		-out $(AUTH_SERVICE_PATH)/keys/public.pem
	@chmod 600 $(AUTH_SERVICE_PATH)/keys/private.pem
	@chmod 644 $(AUTH_SERVICE_PATH)/keys/public.pem
	@echo "RSA key pair generated."

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
	@mkdir -p $(LOG_DIR)
	@cd $(DOCKER_COMPOSE) && \
	set -a && . ../.env && set +a && \
	docker compose down && \
	docker compose up -d 


docker-clean-up:
	@cd $(DOCKER_COMPOSE) && \
	set -a && . ../.env && set +a && \
	docker compose  down -v && \
	docker compose  up -d

docker-down:
	@cd $(DOCKER_COMPOSE) && \
	docker compose down


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
# Post Service
# =======================

dev-post-service: stop-post-service
	@mkdir -p logs
	@cd $(POST_SERVICE_PATH) && \
	set -a && . ../../.env && set +a && \
	nohup sh -c './mvnw install && exec ./mvnw spring-boot:run' \
		> ../../$(POST_SERVICE_LOG) 2>&1 < /dev/null & \
	echo $$! > /tmp/blog-post-service.pid
	@echo "Post service starting on port $(POST_SERVICE_PORT)"

stop-post-service:
	@fuser -k $(POST_SERVICE_PORT)/tcp 2>/dev/null || true
	@rm -f /tmp/blog-post-service.pid
	@echo "Post service stopped"


# =======================
# Notification Service
# =======================

dev-notification-service: stop-notification-service
	@mkdir -p logs
	@cd $(NOTIFICATION_SERVICE_PATH) && \
	set -a && . ../../.env && set +a && \
	nohup sh -c './mvnw install && exec ./mvnw spring-boot:run' \
		> ../../$(NOTIFICATION_SERVICE_LOG) 2>&1 < /dev/null & \
	echo $$! > /tmp/blog-notification-service.pid
	@echo "Notification service starting on port $(NOTIFICATION_SERVICE_PORT)"

stop-notification-service:
	@fuser -k $(NOTIFICATION_SERVICE_PORT)/tcp 2>/dev/null || true
	@rm -f /tmp/blog-notification-service.pid
	@echo "Notification service stopped"


# =======================
# Moderation Service
# =======================

dev-moderation-service: stop-moderation-service
	@mkdir -p logs
	@cd $(MODERATION_SERVICE_PATH) && \
	set -a && . ../../.env && set +a && \
	nohup sh -c './mvnw install && exec ./mvnw spring-boot:run' \
		> ../../$(MODERATION_SERVICE_LOG) 2>&1 < /dev/null & \
	echo $$! > /tmp/blog-moderation-service.pid
	@echo "Moderation service starting on port $(MODERATION_SERVICE_PORT)"

stop-moderation-service:
	@fuser -k $(MODERATION_SERVICE_PORT)/tcp 2>/dev/null || true
	@rm -f /tmp/blog-moderation-service.pid
	@echo "Moderation service stopped"


# =======================
# Global
# =======================

stop: stop-post-service stop-notification-service stop-moderation-service stop-media-service stop-auth-service stop-user-service stop-gateway front-down

start: dev-gateway dev-user-service dev-auth-service dev-media-service dev-post-service dev-notification-service dev-moderation-service front-up

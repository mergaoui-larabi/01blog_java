# building the app
FROM node:22-alpine AS build

WORKDIR /app

COPY frontend/blog/package.json frontend/blog/package-lock.json ./
RUN npm ci

COPY frontend/blog ./
RUN npm run build

# running the app
FROM nginx:1.27-alpine

COPY --from=build /app/dist/blog/browser/ /usr/share/nginx/html

COPY frontend/blog/nginx/default.conf /etc/nginx/conf.d/default.conf


EXPOSE 80

ENTRYPOINT ["nginx", "-g", "daemon off;"]

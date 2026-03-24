# Stage 1: Build the Kotlin/Wasm app
FROM gradle:jdk21 AS builder
WORKDIR /app

RUN apt-get update && apt-get install -y \
    libatomic1 \
    ca-certificates \
    curl \
    xz-utils \
    gnupg \
 && rm -rf /var/lib/apt/lists/*

COPY . .

RUN chmod +x gradlew
RUN ./gradlew :composeApp:wasmJsBrowserDistribution

# Stage 2: Nginx
FROM nginx:stable-alpine

RUN rm /etc/nginx/conf.d/default.conf

COPY nginx.conf /etc/nginx/conf.d/default.conf

COPY --from=builder /app/composeApp/build/dist/wasmJs/productionExecutable /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
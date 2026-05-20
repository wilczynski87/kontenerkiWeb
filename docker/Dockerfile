# Build from repository root:
#   docker build -f docker/Dockerfile .
FROM gradle:8.14.4-jdk21 AS builder
WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
    libatomic1 \
    ca-certificates \
 && rm -rf /var/lib/apt/lists/*

ENV GRADLE_OPTS="-Dorg.gradle.daemon=false"
ENV GRADLE_USER_HOME=/home/gradle/.gradle

COPY gradle gradle
COPY gradlew gradlew.bat settings.gradle.kts build.gradle.kts gradle.properties ./
COPY composeApp composeApp
COPY kotlin-js-store kotlin-js-store

RUN chmod +x gradlew

RUN ./gradlew :composeApp:wasmJsBrowserDistribution -x test --no-daemon \
    -Dorg.gradle.jvmargs="-Xmx2g -XX:MaxMetaspaceSize=512m" \
    -Dkotlin.daemon.jvmargs="-Xmx1536m" \
    -Dorg.gradle.configuration-cache=false

RUN test -f composeApp/build/dist/wasmJs/productionExecutable/index.html

FROM nginx:stable-alpine

RUN rm /etc/nginx/conf.d/default.conf

COPY docker/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/composeApp/build/dist/wasmJs/productionExecutable /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]

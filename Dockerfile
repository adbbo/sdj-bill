FROM node:22-alpine AS frontend
WORKDIR /frontend
COPY frontend/package.json frontend/package-lock.json* ./
RUN npm install
COPY frontend/ ./
RUN npm run build -- --outDir /out --emptyOutDir

FROM maven:3.9.9-eclipse-temurin-21 AS backend
WORKDIR /build
COPY backend/pom.xml .
RUN mvn -q -e -DskipTests dependency:go-offline || mvn -q -DskipTests dependency:resolve
COPY backend/src ./src
COPY --from=frontend /out ./src/main/resources/static
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S sdj && adduser -S sdj -G sdj
COPY --from=backend /build/target/sdj-support-1.0.0.jar app.jar
USER sdj
EXPOSE 8080
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

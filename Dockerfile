# ─── Stage 1: Build ───────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copia somente o pom.xml primeiro para aproveitar o cache de dependências
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o código-fonte e empacota
COPY src ./src
RUN mvn package -DskipTests -B

# ─── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Cria usuário não-root para executar a aplicação
RUN addgroup -S brewer && adduser -S brewer -G brewer

COPY --from=build /app/target/brewer.jar app.jar

RUN chown brewer:brewer app.jar

USER brewer

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

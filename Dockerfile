# ─── Stage 1: Build ───────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copia somente o pom.xml primeiro para aproveitar o cache de dependências
COPY pom.xml .
# || true: ehcache:jakarta puxa deps transitivas de repos HTTP antigos (java.net),
# bloqueados pelo Maven 3.8+. O stage de package baixa o restante.
RUN mvn dependency:go-offline -B || true

# Copia o código-fonte e empacota
COPY src ./src
RUN mvn package -DskipTests -B

# ─── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre

WORKDIR /app

# Instala wget para o HEALTHCHECK e cria usuário não-root
RUN apt-get update && apt-get install -y --no-install-recommends wget \
    && rm -rf /var/lib/apt/lists/* \
  && groupadd -g 10001 brewer \
  && useradd -u 10001 -g brewer -M -s /usr/sbin/nologin brewer \
  && mkdir -p /tmp \
  && chown -R brewer:brewer /tmp

COPY --from=build /app/target/brewer.jar app.jar

RUN chown brewer:brewer app.jar && \
    chmod 644 app.jar

ENV JAVA_TOOL_OPTIONS="-Djava.io.tmpdir=/tmp"

USER brewer

EXPOSE 8080

# Usa /login pois Actuator não está configurado
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/login || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]

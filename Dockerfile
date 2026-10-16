# ---------- Étape 1 : build Maven ----------
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# Cache des dépendances : ne se relance que si le pom change
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q package -DskipTests

# ---------- Étape 2 : image d'exécution ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app
USER app

COPY --from=build /app/target/restauration-marches.jar app.jar

# Limite la mémoire JVM au conteneur (512 Mo sur l'offre gratuite Render)
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC -Xss512k"
ENV SPRING_PROFILES_ACTIVE=postgres

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]

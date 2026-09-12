# Build with Maven, ship only a JRE and the jar. The build stage is ~800 MB; keeping it
# out of the final image matters on free tiers that cap image size and pull time.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# The POM alone first, so the dependency layer stays cached until dependencies change
# rather than on every source edit.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests

# Not the -alpine variant: Temurin publishes it for amd64 only, so it can't build on an
# arm64 machine. This one is multi-arch, at the cost of roughly 100 MB.
FROM eclipse-temurin:17-jre
WORKDIR /app

RUN groupadd --system waitless && useradd --system --gid waitless waitless
USER waitless

COPY --from=build /build/target/*.jar app.jar

# Free tiers hand out about 512 MB. Without MaxRAMPercentage the JVM sizes its heap
# from the host's memory rather than the container limit and gets OOM-killed; SerialGC
# costs less overhead than G1 on a single slow core.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC -Xss512k"

# Hosts inject their own PORT; application.yml reads it.
EXPOSE 8081

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]

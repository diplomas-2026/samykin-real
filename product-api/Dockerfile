FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew
COPY src src
COPY seed-data seed-data
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
COPY --from=build /app/seed-data seed-data
COPY certs certs
RUN keytool -importcert -noprompt -trustcacerts -alias russian-trusted-root-ca \
    -file /app/certs/russian-trusted-root-ca.pem \
    -keystore "${JAVA_HOME}/lib/security/cacerts" \
    -storepass changeit \
    && keytool -importcert -noprompt -trustcacerts -alias russian-trusted-sub-ca \
    -file /app/certs/russian-trusted-sub-ca.pem \
    -keystore "${JAVA_HOME}/lib/security/cacerts" \
    -storepass changeit
ENV APP_SEED_DATA_DIR=seed-data
ENV APP_USERS_FILE=users.txt
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]

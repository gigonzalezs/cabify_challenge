FROM gradle:7.3.3-jdk11 AS builder

WORKDIR /home/gradle/src
COPY . .

RUN gradle clean build bootJar --no-daemon

FROM openjdk:11-jre-slim

WORKDIR /app
COPY --from=builder /home/gradle/src/build/libs/*.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

EXPOSE 9091


#FROM alpine:3.8

# This Dockerfile is optimized for go binaries, change it as much as necessary
# for your language of choice.

# RUN apk --no-cache add ca-certificates=20190108-r0 libc6-compat=1.1.19-r10

# EXPOSE 9091

# COPY car-pooling-challenge /
 
# ENTRYPOINT [ "/car-pooling-challenge" ]

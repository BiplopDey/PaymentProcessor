FROM openjdk:8-jre-alpine

ENV APPLICATION_USER java
RUN adduser -D -g '' $APPLICATION_USER

RUN apk update && apk add bash coreutils
RUN mkdir /app
RUN chown -R $APPLICATION_USER /app

USER $APPLICATION_USER

WORKDIR /app

CMD ["java", "-server", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-XX:InitialRAMFraction=2", "-XX:MinRAMFraction=2", "-XX:MaxRAMFraction=2", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=100", "-XX:+UseStringDeduplication", "-jar", "app.jar"]

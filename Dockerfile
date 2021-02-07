FROM openjdk:8-jdk-alpine
WORKDIR /src
COPY ./pano-1.0-all.jar .
ENTRYPOINT ["java", "-jar", "pano-1.0-all.jar"]
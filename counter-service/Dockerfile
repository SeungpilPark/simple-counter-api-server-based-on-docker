FROM openjdk:8u111-jdk-alpine

ADD target/*.jar app.jar

EXPOSE 9010
ENTRYPOINT ["java","-Xmx400M","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
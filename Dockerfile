FROM openjdk:11-jdk-slim

ADD ./target/tweet-service.jar /app/
CMD ["java", "-Xmx200m", "-jar", "/app/tweet-service.jar"]

EXPOSE 8070
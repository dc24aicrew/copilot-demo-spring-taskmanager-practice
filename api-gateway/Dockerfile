FROM amazoncorretto:17-alpine-jdk
VOLUME /tmp
COPY api-gateway/target/api-gateway-*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
EXPOSE 8080
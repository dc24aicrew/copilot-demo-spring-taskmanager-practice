FROM amazoncorretto:17-alpine-jdk
VOLUME /tmp
COPY eureka-server/target/eureka-server-*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
EXPOSE 8761
FROM amazoncorretto:17-alpine-jdk
VOLUME /tmp
COPY user-service/target/user-service-*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
EXPOSE 8081
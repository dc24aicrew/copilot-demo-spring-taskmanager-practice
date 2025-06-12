FROM amazoncorretto:17-alpine-jdk
VOLUME /tmp
COPY config-server/target/config-server-*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
EXPOSE 8888
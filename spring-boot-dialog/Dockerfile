FROM openjdk:11

COPY *.jar /app.jar

CMD ["--server.port=8095"]

EXPOSE 8095

ENTRYPOINT ["java","-jar", "/app.jar"]
FROM openjdk:17.0.2-jdk-slim

RUN apt-get update && apt-get install -qy wget gnupg

RUN groupadd -r hello && useradd --no-log-init -r -g hello hello

COPY ./docker/entrypoint.sh /usr/bin/
RUN chmod +x /usr/bin/entrypoint.sh
RUN mkdir app
RUN chown -R hello:hello app

USER hello
WORKDIR app
EXPOSE 8080

COPY ./build/libs/hello-app-0.0.1-all.jar hello-app.jar

ENTRYPOINT ["bash", "entrypoint.sh"]
CMD ["java", "-jar", "hello-app.jar"]

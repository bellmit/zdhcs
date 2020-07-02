FROM openjdk:8-jre-alpine

ENV CSMBP_SLEEP 0

ADD *.jar /app.jar

RUN sh -c 'touch /app.jar'
VOLUME /tmp
EXPOSE 8099

CMD echo "The application will start in ${CSMBP_SLEEP}s..." && \
    sleep ${CSMBP_SLEEP} && \
    java -Djava.security.egd=file:/dev/./urandom -jar /app.jar

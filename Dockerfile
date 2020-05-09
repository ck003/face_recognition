FROM java:8
MAINTAINER face
COPY ./face_recognition-0.0.1-SNAPSHOT.jar /app/face_recognition.jar
CMD java -jar /app/face_recognition.jar --spring.config.location=/app/application.yaml

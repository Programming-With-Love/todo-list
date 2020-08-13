FROM openjdk:14-jdk-alpine
LABEL author="echo" email="lizhongyue248.com" version="0.0.1-SNAPSHOT"
EXPOSE 8080
ARG JAR_FILE=build/libs/todo-0.0.1-SNAPSHOT.jar
ADD ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
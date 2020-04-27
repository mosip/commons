FROM openjdk:8

#Uncomment below and Comment above line(i.e. FROM openjdk:8) for OS specific (e.g. Alpine OS ) docker base image
#FROM openjdk:8-jdk-alpine

COPY ./target/kernel-idgenerator-service-*.jar kernel-idgenerator-service.jar

EXPOSE 8080

CMD ["java","-jar","kernel-idgenerator-service.jar"]

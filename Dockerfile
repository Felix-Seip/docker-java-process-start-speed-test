FROM mcr.microsoft.com/openjdk/jdk:17-ubuntu

RUN apt-get update
RUN apt-get install -y curl
RUN curl -sL https://deb.nodesource.com/setup_16.x
RUN apt-get install -y nodejs

ADD build/libs/nodejs-test-1.0-SNAPSHOT.jar .
COPY node ./node

ENTRYPOINT java -jar nodejs-test-1.0-SNAPSHOT.jar
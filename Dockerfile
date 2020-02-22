# Dockerfile for building artifacts
FROM gradle:5.4.1-jdk8
MAINTAINER chris@thinkdataworks.com

ENV APPDIR /app
ENV PROTOC_VERSION 3.5.1

WORKDIR $APPDIR
RUN mkdir -p $APPDIR

RUN apt-get update -yqq && \
    apt-get install -yqq build-essential

ENV CLASSPATH ".:/usr/local/lib:$CLASSPATH"

# install protoc; used for protobuff stuff
RUN curl -OL https://storage.googleapis.com/tdw-static/mirrored/protoc-${PROTOC_VERSION}-linux-x86_64.zip && \
    unzip protoc-${PROTOC_VERSION}-linux-x86_64.zip -d protoc3 && \
    mv protoc3/bin/* /usr/local/bin/ && \
    mv protoc3/include/* /usr/local/include/

COPY . $APPDIR
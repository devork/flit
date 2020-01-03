# Flit - Twirp RPC Generator Framework

This project is a generator for the [Twitch TV Twirp](https://github.com/twitchtv/twirp "Twitch TV Twirp") RPC
framework.

It supports the generation of Java based servers with the following flavours supported:

+ [Spring Boot/Spring MVC](https://spring.io/projects/spring-boot "Spring Boot")
+ [Undertow](http://undertow.io/ "Undertow")

## Building & Running

### Requirements

The build has been tested with [Oracle's JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html "JDK Downloads") (version 1.8)

The build uses gradle to generate the artifacts. No installation is required as the project uses the
[gradle wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html "gradle wrapper") setup.

For testing you will need an installation of the [protocol buffers compiler](https://github.com/google/protobuf/releases "protobuf releases").

### Modules

The project is split into the following modules:

| Module            |   Description                                         |
|:------------------|:------------------------------------------------------|
| `plugin`          | The `protoc` plugin                                   |
| `runtime:core`    | Core functionality required by generated code         |
| `runtime:spring`  | Runtime library for Spring MVC/Boot servers           |
| `runtime:undertow`| Runtime library for Undertow servers                  |


### Build

To build the various components, run the following:

    git clone git@github.com:devork/flit.git
    cd flit
    ./gradlew clean build pack
    
### Building with docker

```bash
    $ docker-compose run gen ./gradlew clean build pack 
```

### Installation

Currently, the run script only supports *nix but the run script should be fairly easy to migrate to windows.

After building:

    cp plugin/build/package/flit-plugin.zip /usr/local/bin
    cd /usr/local/bin
    unzip flit-plugin.zip
    chmod +x protoc-gen-flit

## Running

The plugin is executed as part of a protoc compilation step:

    protoc \
        --proto_path=. \
        --java_out=../java \
        --flit_out=target=server,type=undertow:../java \
        ./haberdasher.proto

### Options

The flit plugin accepts the following plugin parameters:

| Name          | Required  | Type                          | Description                                               |
|:--------------|:---------:|:------------------------------|:----------------------------------------------------------|
| `target`      | Y         | `enum[server]`                | The type of target to generate e.g. server, client etc    |
| `type`        | Y         | `enum[spring,undertow,boot]`  | Type of target to generate                                |
| `context`     | N         | `string`                      | Base context for routing, default is `/twirp`             |

# Development

All development is done in Java using JDK 8 (as mentioned above).

Remote debugging can be performed as follows:

    export FLIT_JAVA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005,quiet=y"
    protoc \
            --proto_path=. \
            --java_out=../java \
            --flit_out=target=server,type=undertow:../java \
            ./haberdasher.proto

When running with the above options, the generator will enable a remote java debug session on port 5005. This is useful
for debugging a full generation step.

## Test Fixture Generation

The test resources contains a fake plugin that simply dumps the binary request to a file called `file.bin`. This utility
can be used to generate test fixtures which can be fed to tests to drive plugin generation, for example:

    $ protoc \
        --plugin=${PWD}/protoc-gen-dump \
        --dump_out=target=server,type=undertow:../java  \
        ./helloworld.proto
    $ mv file.bin helloworld.bin

This can be run from the resources directory to generate a `CodeGeneratorRequest` protobuf file, which can then be read
by tests:

    PluginProtos.CodeGeneratorRequest request = null;
    try (InputStream is = this.getClass().getClassLoader().getResource("helloworld.bin").openStream()) {
        request = PluginProtos.CodeGeneratorRequest
            .newBuilder()
            .mergeFrom(is)
            .build();
    }

    Plugin plugin = new Plugin(request);
    plugin.process();
    
# Guides

| Platform  | Document                              |
|:----------|:--------------------------------------|
| Undertow  | [undertow.md](docs/undertow.md)       |

# Building & Installation

## Requirements

The build has been tested with [Oracle's JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html "JDK Downloads") (version 1.8)

The build uses gradle to generate the artifacts. No installation is required as the project uses the
[gradle wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html "gradle wrapper") setup.

For testing you will need an installation of the [protocol buffers compiler](https://github.com/google/protobuf/releases "protobuf releases").

## Modules

The project is split into the following modules:

| Module            |   Description                                         |
|:------------------|:------------------------------------------------------|
| `plugin`          | The `protoc` plugin                                   |
| `runtime:core`    | Core functionality required by generated code         |
| `runtime:spring`  | Runtime library for Spring MVC/Boot servers           |
| `runtime:undertow`| Runtime library for Undertow servers                  |


## Build

To build the various components, run the following:

    git clone git@github.com:devork/flit.git
    cd flit
    ./gradlew clean build pack

## Installation

Currently, the run script only supports *nix but the run script should be fairly easy to migrate to windows.

After building:

    cp plugin/build/package/flit-plugin.zip /usr/local/bin
    cd /usr/local/bin
    unzip flit-plugin.zip
    chmod +x protoc-gen-flit
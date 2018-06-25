# Undertow Server Generation

An undertow server can be generated in the following way:

The plugin is executed as part of a protoc compilation step:

    protoc \
        --proto_path=. \
        --java_out=../java \
        --flit_out=target=server,type=undertow:../java \
        ./haberdasher.proto

# Guide

## Generating Code

Assuming everything has [built and been installed](build.md), lets create a new gradle project with the following structure:


    ├── build.gradle
    ├── libs
    └── src
        └── main
            ├── java
            └── proto


The main gradle file:

```groovy
apply plugin: 'java'

group = 'com.example'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = 1.8

repositories {
    mavenCentral()
}


dependencies {
    compile 'com.google.protobuf:protobuf-java:3.5.1'
    compile 'com.google.protobuf:protobuf-java-util:3.5.1'
    compile 'io.undertow:undertow-core:2.0.9.Final'

    compileOnly 'org.projectlombok:lombok:+'
}
```

We'll use the Haberdasher proto from the Twirp site - save this to `src/main/proto/haberdasher.proto`

```proto
syntax = "proto3";

package twirp.example.haberdasher;
option go_package = "haberdasher";
option java_package = "com.example.demo.haberdasher.rpc";

// Haberdasher service makes hats for clients.
service Haberdasher {
  // MakeHat produces a hat of mysterious, randomly-selected color!
  rpc MakeHat(Size) returns (Hat);
}

// Size of a Hat, in inches.
message Size {
  int32 inches = 1; // must be > 0
}

// A Hat is a piece of headwear made by a Haberdasher.
message Hat {
  int32 inches = 1;
  string color = 2; // anything but "invisible"
  string name = 3; // i.e. "bowler"
}
```

Notice the addition of the `java_package` to the proto file: the flit generator (and protoc) will use this to specify the 
package of the code. It might be handy to add this as a task to the gradle build:

```groovy
def flitSources = [
        'haberdasher.proto'
]

task flit(type: Exec) {
    workingDir = new File(projectDir, "src/main/proto").getAbsolutePath()

    executable 'protoc'
    args = [
        '--proto_path=.',
        '--java_out=../java',
        '--flit_out=target=server,type=undertow:../java',
    ] + flitSources
}

```
And now just run with `./gradlew flit`.

The result of this is the following file structure (excluding the gradle build files etc):

    .
    ├── build.gradle
    ├── libs
    └── src
        ├── main
        │   ├── java
        │   │   └── com
        │   │       └── example
        │   │           └── demo
        │   │               └── haberdasher
        │   │                   └── rpc
        │   │                       ├── HaberdasherOuterClass.java
        │   │                       ├── RpcHaberdasherHandler.java
        │   │                       └── RpcHaberdasherService.java
        │   ├── proto
        │   │   └── haberdasher.proto
        │   └── resources
        └── test
            ├── java
            └── resources

## Wiring Up Some Services

The undertow implementation works in the following way:

+ The generated code provides handlers that map the route (protoc package + method name) to a service interface
+ Clients implement the interface to do the actual logic of the call
+ Client wire up the service and handler (via the constructor) and register the route in the runtime `FlitHandler`

The core Flit runtime provides a few simple classes which the undertow runtime uses, e.g. `FlitException` and `ErrorCode`.
Implementation classes can throw FlitException instances which are then translated and returned to remote clients as
appropriate error responses. In this way, the service implementation acts as a bridge between the service logic and the
RPC world.

Following on from the haberdasher example, here's a simple implementation of the service:

```java
package com.example.demo.service;

import com.example.demo.haberdasher.rpc.HaberdasherOuterClass;
import com.example.demo.haberdasher.rpc.RpcHaberdasherService;
import com.flit.runtime.ErrorCode;
import com.flit.runtime.FlitException;

import java.util.Random;

public class HaberDasherService implements RpcHaberdasherService {

    private static final String[] NAMES = new String[]{"bowler", "baseball cap", "top hat", "derby"};
    private static final String[] COLOURS = new String[]{"white", "black", "brown", "red", "blue"};

    private static final Random RANDOM = new Random();

    @Override
    public HaberdasherOuterClass.Hat handleMakeHat(HaberdasherOuterClass.Size in) {
        if (in.getInches() <= 0) {
            throw FlitException
                .builder()
                .withErrorCode(ErrorCode.INVALID_ARGUMENT)
                .withMeta("argument", "inches")
                .withMessage("I can't make a hat that small!")
                .build();
        }

        return HaberdasherOuterClass.Hat.newBuilder()
            .setInches(in.getInches())
            .setName(NAMES[RANDOM.nextInt(NAMES.length)])
            .setColor(COLOURS[RANDOM.nextInt(COLOURS.length)])
            .build();
    }
}
```

In addition, add the following to the libs directory:

    flit-core-runtime.jar
    flit-undertow-runtime.jar
    
And then add them as compile dependencies:

```groovy
dependencies {
    compile 'com.google.protobuf:protobuf-java:3.5.1'
    compile 'com.google.protobuf:protobuf-java-util:3.5.1'
    compile 'io.undertow:undertow-core:2.0.9.Final'
    
    compile('ch.qos.logback:logback-core:+')
    compile('ch.qos.logback:logback-classic:+')

    compile fileTree(dir: 'libs', include: '*.jar')

    compileOnly 'org.projectlombok:lombok:+'
}
```

*N.B.* We also include logback as a compile dependency - this is the only 3rd party dependency the generated code requires. 
Seeing as how ubiquitous SLF4J is in Java projects these days, this shouldn't be too bad.

And finally, wire it all up:

```java
package com.example.demo;

import com.example.demo.haberdasher.rpc.RpcHaberdasherHandler;
import com.example.demo.haberdasher.service.HaberdasherService;
import com.flit.runtime.undertow.FlitHandler;
import io.undertow.Undertow;

public class Main {
    public static void main(final String[] args) {
        Undertow server = Undertow.builder()
            .addHttpListener(8080, "0.0.0.0")
            .setHandler(new FlitHandler
                .Builder()
                .withNext(null)
                .withRoute(RpcHaberdasherHandler.ROUTE, new RpcHaberdasherHandler(new HaberdasherService()))
                .build()
            )
            .build();
        server.start();
    }
}
```

To make deployment easier, we'll add in shadow jar to package the up all the dependencies into one single JAR file. The
final Gradle file looks like this:

```groovy
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.github.jengelman.gradle.plugins:shadow:2.0.3'
    }
}

apply plugin: 'com.github.johnrengelman.shadow'
apply plugin: 'java'

group = 'com.example'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = 1.8

shadowJar {
    manifest {
        attributes(
            'Main-Class': "com.example.demo.Main"
        )
    }
}

shadowJar.dependsOn build


repositories {
    mavenCentral()
}


dependencies {
    compile 'com.google.protobuf:protobuf-java:3.5.1'
    compile 'com.google.protobuf:protobuf-java-util:3.5.1'
    compile 'io.undertow:undertow-core:2.0.9.Final'

    compile('ch.qos.logback:logback-core:1.2.3')
    compile('ch.qos.logback:logback-classic:1.2.3')
    //compile('net.logstash.logback:logstash-logback-encoder:4.9')

    compile fileTree(dir: 'libs', include: '*.jar')

    compileOnly 'org.projectlombok:lombok:+'
}

def flitSources = [
        'haberdasher.proto'
]

task flit(type: Exec) {
    workingDir = new File(projectDir, "src/main/proto").getAbsolutePath()

    executable 'protoc'
    args = [
            '--proto_path=.',
            '--java_out=../java',
            '--flit_out=target=server,type=undertow:../java',
    ] + flitSources
}
```

## Running

We can now do the build and run our application:

```bash
./gradlew clean shadowJar
 java -jar ./build/libs/flit-demo-undertow-0.0.1-SNAPSHOT-all.jar
```

If all goes well, you should see something like:

```bash
[main] DEBUG org.jboss.logging - Logging Provider: org.jboss.logging.Slf4jLoggerProvider
[main] DEBUG io.undertow - starting undertow server io.undertow.Undertow@39a054a5
[main] INFO org.xnio - XNIO version 3.3.8.Final
[main] INFO org.xnio.nio - XNIO NIO Implementation Version 3.3.8.Final
[XNIO-1 I/O-2] DEBUG org.xnio.nio - Started channel thread 'XNIO-1 I/O-2', selector sun.nio.ch.EPollSelectorImpl@42deb1d5
[XNIO-1 I/O-5] DEBUG org.xnio.nio - Started channel thread 'XNIO-1 I/O-5', selector sun.nio.ch.EPollSelectorImpl@30014259
[XNIO-1 I/O-3] DEBUG org.xnio.nio - Started channel thread 'XNIO-1 I/O-3', selector sun.nio.ch.EPollSelectorImpl@1bbf99d3
[XNIO-1 I/O-4] DEBUG org.xnio.nio - Started channel thread 'XNIO-1 I/O-4', selector sun.nio.ch.EPollSelectorImpl@4c032a21
[XNIO-1 I/O-6] DEBUG org.xnio.nio - Started channel thread 'XNIO-1 I/O-6', selector sun.nio.ch.EPollSelectorImpl@217c1dd9
[XNIO-1 I/O-8] DEBUG org.xnio.nio - Started channel thread 'XNIO-1 I/O-8', selector sun.nio.ch.EPollSelectorImpl@7d39f65a
[XNIO-1 I/O-7] DEBUG org.xnio.nio - Started channel thread 'XNIO-1 I/O-7', selector sun.nio.ch.EPollSelectorImpl@5c13e597
[XNIO-1 I/O-1] DEBUG org.xnio.nio - Started channel thread 'XNIO-1 I/O-1', selector sun.nio.ch.EPollSelectorImpl@514f5d92
[main] DEBUG io.undertow - Configuring listener with protocol HTTP for interface 0.0.0.0 and port 8080
[XNIO-1 Accept] DEBUG org.xnio.nio - Started channel thread 'XNIO-1 Accept', selector sun.nio.ch.EPollSelectorImpl@184cfffb
```

And now we can hit the endpoint!

```bash
 curl \
    --location "http://localhost:8080/twirp/twirp.example.haberdasher.Haberdasher/MakeHat" \
    --header "Content-Type:application/json" \
    --data '{"inches": 10}'

{"inches":10,"color":"blue","name":"bowler"}

echo "inches:10" \
    | protoc --proto_path=./src/main/proto \
    | --encode twirp.example.haberdasher.Size haberdasher.proto \
    | curl -s --request POST --header "Content-Type: application/protobuf" --data-binary @- --location http://localhost:8080/twirp/twirp.example.haberdasher.Haberdasher/MakeHat \
    | protoc --proto_path=./src/main/proto --decode twirp.example.haberdasher.Hat haberdasher.proto
    
inches: 10
color: "blue"
name: "bowler"

```

## Routing and Customisation

The Flit handler is just an implementation of an Undetow Handler. You can customise the request flow by adding upstream handlers
to the request. If the Flit handler is constructed with a following handler, then any time a route doesn't match it will call
next.

Internal routing occurs by first mapping the package to a handler and then the handler internally routing the method call.
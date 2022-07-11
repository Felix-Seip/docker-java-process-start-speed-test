# About this repository
This repository is meant as a demonstration to show the performance difference of starting a 
process from the JVM within a docker container & outside a docker container.

It has occurred to me / within a project that the difference between the two environments is 
incredibly different, causing a performance difference that is 10x slower within a docker container than
it is outside of one.

## How to use this repository

### Step 1: Building the jar 
In order to build this jar, you must have JDK 17 installed on your machine, since the docker image also uses 
JDK 17.

Once installed, you can build the jar using the included gradle wrapper and start the build via the following
command from the root folder:
```
./gradlew buildJar
```

This will create a jar file within the `build/libs` folder.

### Step 2: Build the docker image
In order to build the docker image, you must have the latest version of docker installed on your machine.

Once it is installed, you can build the docker image using the following command:
```
docker build -t test .
```

### Step 3: Starting the JVM process via a docker container
The JVM process requires an environment variable to be passed into the container.

The required environment variable `ITERATION_COUNT` determines how many node JVM subprocess will be spawned during 
the execution. It is best to start with 1 iteration and note the performance difference to starting the JVM process outside
of a container.

You can start the container via the following command:
```
docker run -e ITERATION_COUNT=1 test
```

### Step 4: Starting the JVM process without docker
To see the difference between the docker container, you must first set the `ITERATION_COUNT` environment variable via:
```
export ITERATION_COUNT=1
```

Afterwards you may simply start the jar file via:
```
java -jar ./build/libs/nodejs-test-1.0-SNAPSHOT.jar
```

## Comparing the output
Right before the process terminates, you should see the following output:
```
Process start speed per "n" iterations : "x" milliseconds%
```

From the tests that have been performed using this repository, it can be observed that the process start per iteration
is around 80 - 100 ms without using a docker container.

Per 100 iterations it was around 500 - 600 ms.

Within the docker container however the process start speed for one iteration is around 2000 ms.

Per 100 iterations it was around 75000 - 80000 ms.

## Environment used to test this repository 
To reproduce these performance issues, the following environment was used:
```
MacBook Pro 2021 
Chip: Apple M1 Pro
RAM: 16 GB
SSD: 512 GB
```

It should also be noted that these performance problems were also noticed on MacBook Pros with an Intel Chip.

## Extra Sidenotes
This repository was tested with various docker daemon parameters. 
The number of CPUs, RAM and SWAP were intensely tested without any big differences to the above mentioned results.
#!/bin/sh

./gradlew shadowJar
cp production.yml docker/production.yml
cp build/libs/kinesis-sandbox-1.0-all.jar docker/kinesis-sandbox.jar
cd docker && docker build -t wreulicke-sample .
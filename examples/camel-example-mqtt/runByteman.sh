#!/bin/bash
MAVEN_OPTS="-javaagent:$BYTEMAN_HOME/lib/byteman.jar=script:byteman.btm" mvn clean package exec:java -PExample

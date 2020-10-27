#!/usr/bin/env bash

# assigns empty string to JAVA_OPTS if not defined (double quotes prevent globbing and word splitting)
# e.g. "-XX:NativeMemoryTracking=summary -XX:+PrintGC -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -XX:+UnlockDiagnosticVMOptions"
: "${JAVA_OPTS:=}"

# eval JAVA_OPTS to resolve references to other environment variables, i.e. JAVA_OPTS=-XX:HeapDumpPath=/heap-dumps/${HOST_NAME}-heap-dump.hprof
JAVA_OPTS=$(eval echo $JAVA_OPTS)

echo Java Opts:             ${JAVA_OPTS}

exec java ${JAVA_OPTS} -cp "/app/resources:/app/classes:/app/libs/*" com.lgi.appstore.metadata.Application
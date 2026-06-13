#!/bin/sh
#
# Gradle start up script for UN*X
#

# Attempt to set APP_HOME
DIRNAME="$(dirname "$0")"
APP_HOME="$(cd "$DIRNAME" && pwd)"

# Java opts
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'
JAVA_OPTS=""
GRADLE_OPTS=""

# Classpath
CLASSPATH="${APP_HOME}/gradle/wrapper/gradle-wrapper.jar"

# Execute Gradle
exec "$JAVA_HOME/bin/java" \
    $DEFAULT_JVM_OPTS \
    $JAVA_OPTS \
    $GRADLE_OPTS \
    "-Dorg.gradle.appname=gradlew" \
    -classpath "$CLASSPATH" \
    org.gradle.wrapper.GradleWrapperMain \
    "$@"

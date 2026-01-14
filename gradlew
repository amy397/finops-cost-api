#!/bin/sh

#
# Gradle start up script for POSIX
#

# Resolve the app home
APP_HOME="$(cd "$(dirname "$0")" && pwd)"

# Determine the Java command to use
JAVACMD="java"
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/bin/java" ] ; then
        JAVACMD="$JAVA_HOME/bin/java"
    fi
fi

# Collect all arguments for the java command
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

exec "$JAVACMD" -Xmx64m -Xms64m -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"

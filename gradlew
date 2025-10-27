#!/usr/bin/env sh
APP_HOME=`dirname "$0"`
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar
if [ -n "$JAVA_HOME" ] ; then
  JAVA_EXE="$JAVA_HOME/bin/java"
else
  JAVA_EXE="java"
fi
exec "$JAVA_EXE" -Xms64m -Xmx1280m -cp "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"

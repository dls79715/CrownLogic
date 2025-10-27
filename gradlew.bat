@ECHO OFF
set DIR=%~dp0
set CLASSPATH=%DIR%gradle\wrapper\gradle-wrapper.jar
set JAVA_EXE=java
"%JAVA_EXE%" -Xms64m -Xmx1280m -cp "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*

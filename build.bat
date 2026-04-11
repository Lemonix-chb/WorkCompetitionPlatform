@echo off
set JAVA_HOME=C:\Users\Lemonix-chb\.jdks\ms-17.0.18
set PATH=%JAVA_HOME%\bin;%PATH%
call mvn clean package -DskipTests
pause
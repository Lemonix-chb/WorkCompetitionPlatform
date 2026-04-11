@echo off
setlocal
set JAVA_HOME=C:\Users\Lemonix-chb\.jdks\ms-17.0.18
set PATH=%JAVA_HOME%\bin;%PATH%

cd /d e:\JavaProject\WorkCompetitionPlatform
call mvnw.cmd clean package -DskipTests

if exist target\WorkCompetitionPlatform-0.0.1-SNAPSHOT.jar (
    echo Build successful!
    echo Starting application...
    java -jar target\WorkCompetitionPlatform-0.0.1-SNAPSHOT.jar
) else (
    echo Build failed!
)
endlocal
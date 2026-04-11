@echo off
echo ========================================
echo Starting Backend Build Process...
echo ========================================

set JAVA_HOME=C:\Users\Lemonix-chb\.jdks\ms-17.0.18
set PATH=%JAVA_HOME%\bin;%PATH%

echo Using JDK Version:
java -version

echo.
echo Building Spring Boot Project...
cd /d e:\JavaProject\WorkCompetitionPlatform
call mvn clean package -DskipTests

echo.
echo ========================================
echo Build Completed!
echo ========================================

if exist target\WorkCompetitionPlatform-0.0.1-SNAPSHOT.jar (
    echo Jar file created successfully!
    echo Location: target\WorkCompetitionPlatform-0.0.1-SNAPSHOT.jar
    echo.
    echo Starting Spring Boot Application...
    java -jar target\WorkCompetitionPlatform-0.0.1-SNAPSHOT.jar
) else (
    echo ERROR: Jar file not found!
    echo Build may have failed. Check the logs above.
)

pause
#!/bin/bash
cd e:/JavaProject/WorkCompetitionPlatform
export JAVA_HOME="C:/Users/Lemonix-chb/.jdks/ms-17.0.18"
export PATH="$JAVA_HOME/bin:$PATH"
echo "Using JDK 17 at $JAVA_HOME"
java -version

echo ""
echo "Cleaning and building project..."
./mvnw.cmd clean compile

echo ""
echo "Attempting to package..."
./mvnw.cmd package -DskipTests

echo ""
if [ -f "target/WorkCompetitionPlatform-0.0.1-SNAPSHOT.jar" ]; then
  echo "Build successful! Starting application..."
  java -jar target/WorkCompetitionPlatform-0.0.1-SNAPSHOT.jar
else
  echo "Build failed. Check compilation errors."
fi
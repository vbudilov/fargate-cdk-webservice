FROM openjdk:11
ENV wdir=code
ENV MY_SERVICE_PORT=8080
# Install maven
#RUN apt-get update

WORKDIR /$wdir

# Prepare by downloading dependencies
ADD build.gradle /$wdir/build.gradle
ADD gradle /$wdir/gradle
ADD gradlew /$wdir/gradlew
ADD src /$wdir/src

RUN echo "Running build"
RUN ["/code/gradlew", "build"]

EXPOSE $MY_SERVICE_PORT

# Run the service
CMD ["java", "-jar", "build/libs/code-1.0-SNAPSHOT.jar"]

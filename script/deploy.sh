#!/bin/bash

set -e

if [ "$TRAVIS_BRANCH" == "master" ]; then
  echo "pushing client artifact to Maven Central"
  cd client
  sbt compile publishSigned sonatypeRelease
  
  echo "push server image to Dockerhub"
  cd ../server
  docker login -u=$DOCKERHUB_UNAME -p=$DOCKERHUB_PASS
  docker push vidible/forecast-api:2.0.0
fi

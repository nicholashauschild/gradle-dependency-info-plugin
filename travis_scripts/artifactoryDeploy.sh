#!/bin/bash

echo "Deploying to Artifactory"
echo "pwd $(pwd)"
echo "travis dir $TRAVIS_BUILD_DIR"
./gradlew artifactoryPublish
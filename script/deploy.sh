#!/bin/bash

ls
echo `pwd`
if [ "$TRAVIS_BRANCH" == "master" ]; then
  echo "deploying"
fi

#!/bin/bash

version=$(cat '../version.txt')
echo "Read version [$version] from version.txt"

if ! [[ $version =~ ^.*SNAPSHOT$ ]];
then
    echo "Pushing tag to remote v$version"
    git push origin v$version
fi
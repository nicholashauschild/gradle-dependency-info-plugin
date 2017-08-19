#!/bin/bash

version=$(cat 'version.txt')
echo "Read version [$version] from version.txt"

if ! [[ $version =~ ^.*SNAPSHOT$ ]];
then
    echo "Updating to next snapshot!!"

    # parse parts of $version into release array
    IFS='.' read -r -a release <<< $version

    # calculate new snapshot (increment minor)
    snapshot="${release[0]}.$((${release[1]} + 1)).${release[2]}-SNAPSHOT"

    # overwrite version.txt with new snapshot version
    echo "$snapshot" > 'version.txt'

    # commit version.txt
    git add version.txt
    git commit -m "Updating to next snapshot: $snapshot"
    git push origin HEAD:master
fi
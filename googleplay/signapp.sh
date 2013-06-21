#!/bin/bash

if [ $# -lt 1 ]; then
    echo "Usage: $0 app.apk"
    exit 1
fi

jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore ~/.gnupg/wordswithcrosses.keystore "$1" wwc

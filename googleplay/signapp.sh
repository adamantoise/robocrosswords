#!/bin/bash

if [ $# -lt 1 ]; then
    echo "Usage: $0 app.apk"
    exit 1
fi

jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore ~/.gnupg/wordswithcrosses.keystore "$1" wwc

tempapk="${1%%.apk}-tmp.apk"
zipalign -f -v 4 "$1" "$tempapk" && mv "$tempapk" "$1"

#!/bin/bash
set -e

KEYSTORE=build/keystore.jks
FPFILE=certFingerprint
ALIAS=client

[ ! -e $KEYSTORE ] && echo "You need to generate keystore \"$KEYSTORE\" and insert key and cert for signing to it with alias \"$ALIAS\"" && exit
[ ! -e $FPFILE ] && echo "You need to insert your certificate SHA256 fingerprint without delimiters into \"$FPFILE\"" && exit
#change digest to yours
sed -ri "s/\"[0-9a-fA-F]{64}\";/\"$(cat $FPFILE)\";/" \
	Launcher/source/helper/SecurityHelper.java
	
#run compilation
ant -f launcher.xml

function pack {
    local input=$1
    local output=$2
    shift 2

    # $3..$n - additional files
    echo "Packing $input binary"
    zip -9 "$input" "buildnumber" $@
    pack200 -E9 -Htrue -mlatest -Uerror -r "$input"
    jarsigner -keystore $KEYSTORE -sigfile LAUNCHER "$input" $ALIAS
    [ ! -z "$output" ] && pack200 -E9 -Htrue -mlatest -Uerror "$output" "$input"

    # Return
    true
}

# Increase build number
echo -n $(($(cat buildnumber | cut -d "," -f 1)+1)), $(date +"%d.%m.%Y") > buildnumber

# Pack files
pack "Launcher.jar" "Launcher.pack.gz"
pack "LauncherAuthlib.jar" ""
pack "LauncherRuntime.jar" "runtime.pack.gz"
pack "LaunchServer.jar" "" "Launcher.pack.gz" "runtime.pack.gz"

# Cleanup
rm Launcher.pack.gz runtime.pack.gz

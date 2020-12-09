#!/bin/bash

#Input parameter is a path containing the pre_bug and bug files
#Generates a name for the output file from the path
#Runs Morpheus on the pre_bug and bug files found in the input parameter
#
#Example usage (from repo's root directory):
#bash data_scripts/run_morpheus.sh bugswarm-sandbox/bugs/traccar/traccar/64783122

PROJ_DIR="$1"
OUTPUT_FILE="$(echo $1 | cut -d "/" -f 3-5 | tr "/" "-").csv"

find "$PROJ_DIR"/pre_bug/ -name "*\.java" | xargs -I@ bash -c 'echo -n @; echo " @" | sed "s/pre_bug/bug/"' | xargs -l bash -c 'java -jar morpheus/morpheus.jar $0 $1' > "inferred/$OUTPUT_FILE"


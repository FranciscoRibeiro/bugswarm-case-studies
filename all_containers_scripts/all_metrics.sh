#!/bin/bash

BUG_INTRO_FILE="$1"
echo "TAG,DIR,BUILD_ID,ADD_BUG,DEL_BUG,FILES_CHANGED_BUG,ADD_FIX,DEL_FIX,FILES_CHANGED_FIX"

counter=1
while IFS=' ' read -r tag dir id bug_intro
do
	if [[ "$bug_intro" -ne -1 ]]
	then
		output=$(docker run -v "$(pwd)/"bugswarm-sandbox/:/bugswarm-sandbox/ bugswarm/images:$tag bash -c "sudo bash bugswarm-sandbox/metrics.sh -b $bug_intro; sudo bash bugswarm-sandbox/metrics.sh -f")
		metrics_string=""

		while IFS="\n" read -r line
		do
			IFS=" " read -r add del files_changed <<< "$line"
			metrics_string="$metrics_string,$add,$del,$files_changed"
		done <<< "$output"

		echo "$tag,$dir,$id$metrics_string"
		1>&2 echo "$counter"
		counter=$((counter+1))
	fi
done < "$BUG_INTRO_FILE"


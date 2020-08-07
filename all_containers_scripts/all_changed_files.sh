#!/bin/bash

BUG_INTRO_FILE="info/bug_intro.txt"

while IFS=' ' read -r tag dir id bug_intro
do
	if [[ "$bug_intro" -ne -1 ]]
	then
		docker run -v $(pwd)/bugswarm-sandbox:/bugswarm-sandbox bugswarm/images:$tag bash -c "sudo bash /bugswarm-sandbox/changed_files.sh -b $tag $dir $id $bug_intro"
	fi
done < "$BUG_INTRO_FILE"


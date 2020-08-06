#!/bin/bash

BUG_INTRO_FILE="../info/bug_intro.txt"
REPOS_DIR="bugswarm-sandbox/repos"
REPOS_CHANGED="$(pwd)/bugswarm-sandbox/repos_bug_changed"

while IFS=' ' read -r tag dir id bug_intro
do
	if [[ "$bug_intro" -ne -1 && -d "$REPOS_DIR"/"$dir"/"$id" ]]
	then
		cd "$REPOS_DIR"/"$dir"/"$id"
		pwd
		change_stats="$(git diff --numstat HEAD~"$((bug_intro+1))" HEAD~"$bug_intro")"
		echo "$change_stats"
		while IFS=$'\t' read -r added deleted file
		do
			mkdir -p "$REPOS_CHANGED"/"$dir"/"$id"/pre_bug/"$(dirname $file)"
			mkdir -p "$REPOS_CHANGED"/"$dir"/"$id"/bug/"$(dirname $file)"
			git show HEAD~"$((bug_intro+1))":"$file" > "$REPOS_CHANGED"/"$dir"/"$id"/pre_bug/"$file"
			git show HEAD~"$bug_intro":"$file" > "$REPOS_CHANGED"/"$dir"/"$id"/bug/"$file"
			echo "$file"
		done <<< "$change_stats"
		cd -
		pwd
	fi
done < "$BUG_INTRO_FILE"


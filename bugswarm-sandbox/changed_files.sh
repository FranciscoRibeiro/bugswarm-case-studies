#!/bin/bash

while getopts "bf" switch
do
  case $switch in
    b)
      echo "Checking BUGS"
      MAIN_DIR="bugs"
      PREVIOUS="pre_bug"
      CURRENT="bug"
      BUILD="failed"
	  BUG_INTRO=$4
      ;;
    f)
      echo "Checking FIXES"
      MAIN_DIR="fixes"
      PREVIOUS="bug"
      CURRENT="fix"
      BUILD="passed"
      ;;
    :)
      echo "-$OPTARG requires an argument"
      ;;
    \?)
      echo "Invalid option: -$OPTARG"
      ;;
  esac
done

shift $((OPTIND-1))

ROOT_DIR="/bugswarm-sandbox/$MAIN_DIR"

[[ -d "$ROOT_DIR" ]] || mkdir -p "$ROOT_DIR"

TAG=$1
DIR=$2
ID=$3
SANDBOX_PROJ="$ROOT_DIR/$DIR/$ID"
echo "$SANDBOX_PROJ"

cd home/travis/build/$BUILD/*/*;

CHANGED_FILES=($(git diff --name-only HEAD~"$((BUG_INTRO+1))" HEAD~"$((BUG_INTRO))"))

for file in ${CHANGED_FILES[@]}
do
	echo $file
done


#if [[ $NR_CHANGED -le $FILES_CHANGED  ]]
#then
#	CHANGES="$(git diff --numstat HEAD~"$((BUG_INTRO+1))" HEAD~"$((BUG_INTRO))")"
#	while IFS=$'\t' read -r added deleted file
#	do
#		if [[ $added -le $LINES_ADDED && $deleted -le $LINES_DELETED ]]
#		then
#			echo "[PINNED] $TAG"
#                       echo "files: $NR_CHANGED | added: $added | deleted: $deleted"
#			#echo "$BEFORE_DIR/$(dirname $file)"
#			#echo "$NOW_DIR/$(dirname $file)"
#			if [[ "$SAVE_FILES" = true ]]
#			then
#				#echo "Saving files"
#				#sudo mkdir -p "$BEFORE_DIR/$(dirname $file)"
#				#sudo mkdir -p "$NOW_DIR/$(dirname $file)"
#				#git show HEAD~1:$file | sudo tee "$BEFORE_DIR/$file" > /dev/null
#				#git show HEAD:$file | sudo tee "$NOW_DIR/$file" > /dev/null
#				sudo mkdir -p "$SANDBOX_PROJ"
#				sudo cp -r . "$SANDBOX_PROJ"
#			fi
#		fi
#	done <<< "$CHANGES"
#fi


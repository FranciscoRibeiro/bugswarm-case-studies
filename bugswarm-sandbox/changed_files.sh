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
BUG_INTRO=$4
SANDBOX_PROJ="$ROOT_DIR/$DIR/$ID"

cd home/travis/build/$BUILD/*/*;
echo "$TAG"
CHANGED_FILES=($(git diff --name-only HEAD~"$((BUG_INTRO+1))" HEAD~"$BUG_INTRO"))

for file in ${CHANGED_FILES[@]}
do
	directory="$(dirname $file)"

	git cat-file -e HEAD~"$((BUG_INTRO+1))":$file 2> /dev/null && \
	mkdir -p "$SANDBOX_PROJ/$PREVIOUS/$directory" && \
	git show HEAD~"$((BUG_INTRO+1))":$file > $SANDBOX_PROJ/$PREVIOUS/$file
	
	git cat-file -e HEAD~"$BUG_INTRO":$file 2> /dev/null && \
	mkdir -p "$SANDBOX_PROJ/$CURRENT/$directory" && \
	git show HEAD~"$BUG_INTRO":$file > $SANDBOX_PROJ/$CURRENT/$file
done


#!/bin/bash

while getopts "b:f" switch
do
  case $switch in
    b) [[ "$OPTARG" -eq -1 ]] && { echo "-1 -1 -1"; exit 1; }
       OFFSET="$OPTARG"
       BUILD="failed"
      ;;
    f) OFFSET=0
       BUILD="passed"
      ;;
    \?)
      1>&2 echo "Invalid option: -$OPTARG"
      exit 1
      ;;
  esac
done

#shift $((OPTIND-1))

#[[ "$4" -eq -1 ]] && exit 1
#TAG=$1
#DIR=$2
#ID=$3
#METRICS_FILE="/bugswarm-sandbox/scattered_metrics.txt"

#[[ "$BUGS" == true ]] && OFFSET="$4" || OFFSET=0
#echo "$BUGS" '$4: '"$4" "$TAG" "$DIR" "$ID"

cd home/travis/build/$BUILD/*/*;

CHANGES="$(git diff --numstat HEAD~"$((OFFSET+1))" HEAD~$OFFSET)"
tot_add=0
tot_del=0
while IFS="\n" read -r line
do
	IFS=$'\t' read -r add del file <<< "$line"
	tot_add=$((tot_add+$add))
	tot_del=$((tot_del+$del))
done <<< "$CHANGES"

#echo "$TAG $DIR $ID $tot_add $tot_del $(echo "$CHANGES" | wc -l)" 
echo "$tot_add $tot_del $(echo "$CHANGES" | wc -l)" 
#echo "$CHANGES"
#echo "$tot_add $tot_del $(echo "$CHANGES" | wc -l)"
#echo "files changed:"
#echo "$CHANGES" | wc -l
#echo "git diff --numstat HEAD~$((OFFSET+1)) HEAD~$OFFSET"


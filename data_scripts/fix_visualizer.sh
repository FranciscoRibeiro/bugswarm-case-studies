#!/bin/bash

STRATEGIES=("mc" "ml" "ms" "mr")
PROJS=("traccar" "mybatis-3" "zxing" "commons-lang" "openpnp")
LINE_NRS=("66" "291" "28" "350" "60")
CODE=("channel != null" "options != null" "modulus - 1" "classes.add(0, cls)" "new LensCalibrationParams()")

for((i=0;i<${#PROJS[@]};i++))
do
	#echo "--- i=$i; proj=${PROJS[$i]}; line=${LINES[$i]}; code=${CODE[$i]}"
	for strat in ${STRATEGIES[@]}
	do
		#echo "BEGIN LOOP: $i ${LINES[$i]}"
		#echo "$strat; ${PROJS[$i]}; ${LINES[$i]}; ${CODE[$i]}"
		patches="$(find "generated_patches/${PROJS[$i]}/$strat" -name "*\.java" | wc -l)"
		#echo "___--- i=$i; proj=${PROJS[$i]}; line=${LINES[$i]}; code=${CODE[$i]}"
		#echo "********** ${LINES[*]}"
		echo "-------- Strategy $strat generated "$patches" - ${PROJS[$i]} ${LINE_NRS[$i]} ${CODE[$i]} --------"
		bash data_scripts/find_fix.sh "generated_patches/${PROJS[$i]}/$strat" "${LINE_NRS[$i]}" "${CODE[$i]}"
		#echo "END LOOP: $i ${LINES[$i]}"
	done
done


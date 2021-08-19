#!/bin/bash

rm logs/*
rm -r generated_patches/*

AUTO_REPAIRER="auto_repairer/auto_repairer.jar"
STRATEGIES=("ms" "ml" "mc" "mr")

while IFS="," read -ra fields
do
	proj="${fields[0]}"; inferred="inferred/${fields[3]}"; src="${fields[4]}";
	for strat in ${STRATEGIES[@]}
	do
		bash data_scripts/run_auto_repairer.sh "$proj" "$strat" "$inferred" "$src"
	done
done < info/case_studies.csv


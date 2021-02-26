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
		#java -jar "$AUTO_REPAIRER" case_studies/"$proj" case_studies/"$proj"/src generated_patches/"$proj"/"$strat" -"$strat" inferred/"$inferred" &> logs/log_"$proj"_"$strat".log
	done
done < info/case_studies.csv

#for proj in ${projs[@]}
#do
#	for strat in ${STRATEGIES[@]}
#	do
#		java -jar "$AUTO_REPAIRER" case_studies/"$proj" case_studies/"$proj"/src generated_patches/"$proj"/"$strat" -"$strat" inferred/"$proj".csv &> logs/log_"$proj"_"$strat".log
#	done
#done


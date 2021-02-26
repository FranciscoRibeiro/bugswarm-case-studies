#!/bin/bash

while IFS="," read -ra
do
	proj="${fields[0]}"; repo="${fields[1]}"; commit="${fields[2]}";
	git clone "$repo" case_studies/"$proj"
	cd case_studies/"$proj";
	git checkout "$commit"
	cd -;
done < "info/case_studies.csv"


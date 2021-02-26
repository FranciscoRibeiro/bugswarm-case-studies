#!/bin/bash

AUTO_REPAIRER="auto_repairer/auto_repairer.jar"
proj="$1"
strat="$2"
inferred="$3"
src="$4"

cd case_studies/"$proj";
classpath="$(mvn dependency:build-classpath -Dmdep.outputFile=/dev/stdout -q)"
echo "$classpath"
cd -;

java -cp "$classpath:$AUTO_REPAIRER" MainKt case_studies/"$proj" case_studies/"$proj"/"$src" generated_patches/"$proj"/"$strat" -"$strat" "$inferred" &> logs/log_"$proj"_"$strat".log


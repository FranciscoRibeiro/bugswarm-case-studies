#!/bin/bash

BUGS="bugswarm-sandbox/bugs"
MORPHEUS="morpheus/morpheus.jar"
INFERRED="inferred"

for bug in $(find $BUGS -mindepth 3 -maxdepth 3)
do
	echo "$bug"
	inferred_txt="$(echo $bug | sed -e "s|$BUGS/||" | sed -e "s|/|-|g").csv"
	if [[ -d $bug/pre_bug ]]
	then
		#echo "\"Mutant ID\";\"Nr AST Transformations\";\"AST Transformations\";\"Overview Mutation Operators\";\"Inferred Mutation Operators\";\"Start End Lines\";\"Start End Columns\"" > "$INFERRED/$inferred_txt"
		java_sources=($(find $bug/pre_bug -name "*\.java" | sed -e "s|^$bug/pre_bug/||"))
		for file in ${java_sources[@]}
		do
			if [[ -f $bug/bug/$file ]]
			then
				result="$(timeout 1m java -jar "$MORPHEUS" "$bug/pre_bug/$file" "$bug/bug/$file")"
				exit_code="$?"
				case "$exit_code" in
					0)
						echo "$result" >> "$INFERRED/$inferred_txt"
						;;
					1)
                        echo "Error,$bug/pre_bug/$file,$bug/bug/$file" >> "$INFERRED/$inferred_txt"
                        ;;
                    124)
                        echo "Timeout,$bug/pre_bug/$file,$bug/bug/$file" >> "$INFERRED/$inferred_txt"
                        ;;
                esac
			fi
		done
	fi
done


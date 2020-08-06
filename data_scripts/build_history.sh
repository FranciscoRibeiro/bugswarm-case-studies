#!/bin/bash

image_tag="$1"
repo_slug="$2"
previous_build_number=$(($3+1))

curl https://api.travis-ci.org/repos/"$repo_slug"/builds?after_number="$previous_build_number" > ../build_history/"$image_tag".json

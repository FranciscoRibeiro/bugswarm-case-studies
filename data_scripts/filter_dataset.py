import json
import sys

build_ids = set()

def is_java(artifact):
  return artifact["lang"] == "Java"

def only_code(artifact):
  if("classification" in artifact):
      classification = artifact["classification"]
      return classification["code"] == "Yes" and classification["build"] == "No" and classification["test"] == "No"
  else:
      return False

def set_has_build_id(artifact):
  b_id = artifact["failed_job"]["build_id"]
  if(b_id in build_ids):
    return True
  else:
    build_ids.add(b_id)
    return False

dataset = open(sys.argv[1], "r")

for line in dataset:
  artifact = json.loads(line)
  if(is_java(artifact) and only_code(artifact) and not set_has_build_id(artifact)):
    print(line, end="")
    #print(artifact["image_tag"], artifact["repo"], artifact["failed_job"]["build_id"])

dataset.close()


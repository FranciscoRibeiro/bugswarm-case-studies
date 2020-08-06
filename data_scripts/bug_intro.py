import json
import sys

def find_bug_intro(build_history):
  if(len(build_history) < 2):
    return -1
  branch_name = build_history[0]["branch"]
  #represents how many versions back the bug was introduced
  bug_intro = 0
  for build in build_history[1:]:
    if(build["branch"] == branch_name and build["event_type"] == "push"):
      if(build["result"] == 0):
        return bug_intro
      else:
        bug_intro+=1

  return -1

dataset = open("../filtered-bugswarm.json", "r")

for line in dataset:
    artifact = json.loads(line)
    tag = artifact["image_tag"]
    build_file = open("../build_history/{}.json".format(tag), "r")
    build_history = json.load(build_file)
    bug_intro = find_bug_intro(build_history)
    print(tag, artifact["repo"], artifact["failed_job"]["build_id"], bug_intro)


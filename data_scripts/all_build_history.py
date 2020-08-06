import json
import sys
import subprocess

dataset = open(sys.argv[1], "r")
for line in dataset:
  artifact = json.loads(line)
  image_tag = artifact["image_tag"]
  repo_slug = artifact["repo"]
  failed_build_number = artifact["failed_job"]["build_job"].split(".")[0]
  cmd = "bash build_history.sh {} {} {}".format(image_tag, repo_slug, failed_build_number)
  subprocess.run(cmd.split())
  #print(image_tag, repo_slug, failed_build_number)


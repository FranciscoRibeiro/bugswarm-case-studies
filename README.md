# Bugswarm Experiments

## Repository organization
The root directory has two `json` files:

- `1.1.3-bugswarm.json`: Full bugswarm dataset as of version 1.1.3 which serves as the base for this study
- `filtered-bugswarm.json`: Obtained after filtering the full bugswarm dataset by running `data_scripts/filter_dataset.py`

The purpose of each directory in this repository:

- `bugswarm-sandbox`: Serves as the bind mount point between the host and a container (`docker run -v $(pwd)/bugswarm-sandbox:/bugswarm-sandbox` ...). As such, scripts or data files that should be made available to the container should be placed here. Same thing goes for files generated inside a container that we want to make available in the host.
- `bugswarm-sandbox/bugs`: Stores all the changed files from the buggy commits of all the bugswarm images considered in this study. Each project has a subdirectory, which lists the different possible IDs it their own directory and then splits files in `pre_bug` and `bug` directories (see [Inferring mutations with *Morpheus*](#inferring-mutations-with-morpheus))
- `all_containers_scripts`: Scripts that run an individual task (typically represented by a script in `bugswarm-sandbox`) over every bugswarm image listed in `bug_intro.txt`
- `build_history`: json files with the build history of the bugswarm images considered in this study
- `data_scripts`: Scripts that generate or manipulate data outside the scope of a running container
- `inferred`: Text files listing the mutation operators that got inferred by Morpheus for every bugswarm image considered in this study
- `info`: General information data files
- `morpheus`: Holds the Morpheus version used in this study
- `pom_files`: Holds the `pom.xml` files of each of the 5 case studies in the paper. These should be copied inside the respective container in order to run the experiments (see [Generating the SFL Report](#generating-the-sfl-report))
- `sfl_reports`: SFL reports generated for the case studies

Scripts description:

- `all_containers_scripts`:
	- `all_changed_files.sh`: Extracts all the changed files from the buggy commit for every bugswarm image
	- `all_metrics.sh`: Gathers file and line metrics from the bug and fix of every bugswarm image. Used to generate `info/metrics.csv`
- `bugswarm-sandbox`: (expected to be executed inside a container)
	- `changed_files.sh`: Extracts all the changed files from the buggy commit (`-b` switch) or the fix commit (`-f` switch)
	- `metrics.sh`: Gathers file and line metrics from the bug (`-b`) or the fix (`-f`). If `-b` is supplied then an argument specifying the number of commits to go back should be passed (`bug_intro.txt` lists these offsets). Gathered metrics are nr of files changed, nr of lines added and deleted
- `data_scripts`:
	- `all_build_history.py`: Retrieves the build history for every considered bugswarm image. Should be passed the argument `filtered-bugswarm.json`
	- `build_history.sh`: Retrieves the build history for a specific image
	- `bug_intro.py`: Analyzes each retrieved build history and looks for the commit in which the bug was introduced, i.e. when then build started failing. Used to generate `info/bug_intro.txt`
	- `filter_dataset.py`: Filters the full bugswarm dataset (`1.1.3-bugswarm.json`) and produces `filtered-bugswarm.json`. The filtering criteria is that an image should represent a project with Java source code, the fix was obtained by modifying source code (instead of changing configurations or tests) and the build ID is unique, i.e. only one image per build is considered
	- `infer_mut_ops.sh`: Uses *Morpheus* to analyze the buggy commits and produce the files in `inferred` with all the detected mutation operators for each bugswarm image


## Case Studies with Real Bugs
There are 5 case studies consisting of real bugs which we think are interesting to explore. The corresponding image IDs are (and project repositories):

- **tananaev-traccar-64783123** - [*Traccar*](https://github.com/traccar/traccar)
- **mybatis-mybatis-3-117115624** - [*MyBatis*](https://github.com/mybatis/mybatis-3)
- **zxing-zxing-139981065** - [*ZXing*](https://github.com/zxing/zxing)
- **apache-commons-lang-224267191** - [*Apache Commons Lang*](https://github.com/apache/commons-lang)
- **openpnp-openpnp-110833060** - [*OpenPnP*](https://github.com/openpnp/openpnp)

To replicate what was documented in the paper, an interactive session with a container should be created. To do this, run the following command with any of the previous `<image_id>`:

        docker run -it bugswarm/images:<image_id>

### Checking the fix

When you are interacting with a container, if you wish to see the *diff* which fixed that bug, you can go to directory `/home/travis/build/passed` and run:

        git diff HEAD~1 HEAD

This will show and highlight what changed from the previous commit (buggy) to the current commit (fixed).

### Checking the bug

Similarly, you can run the same `git diff HEAD~1 HEAD` command in directory `/home/travis/build/failed` to analyze the *diff* which **introduced** the bug. This will work for all the 5 use cases we report here. 

However, this may not be the case for every image in the Bugswarm repository. Bugswarm does a good job isolating the point in the project's version history in which the bug was fixed (`passed` directory). However, it cannot be assumed that the commit which introduced the bug is the one immediately preceding the latest commit in `failed`. The file `info/bug_intro.txt` lists (for each bugswarm image) the offset which we need to go back to in order to correctly check the point when the project's build stopped working/passing. 
As an example, the line for the first case study:

        tananaev-traccar-64783123 traccar/traccar 64783122 0

has `0` as the offset (last field). If we execute:

        git diff HEAD~<offset+1> HEAD~<offset>

we get the *diff* that highlights the changes that introduce the bug in the first place.

The 5 cases we report here all have an offset of `0`.

### Generating the SFL report

The tool [*GZoltar*](https://github.com/GZoltar/gzoltar) was used to generate the SFL report for each bug. Each of the 5 use cases has a `pom.xml` file that was properly configured in order to include the *GZoltar* plugin for this analysis. They are located in the `pom_files` directory.
To copy them inside the respective docker container, we first need the ID of the already running container. This ID can be retrieved with `docker ps`. Then, to make the file available inside the container:

        docker cp pom_files/<image_id>/pom-xml <container_id>:<path_to_failed>

As an example, let's imagine we are running a container for the *Traccar* project and that `docker ps` tells us that the container ID is `bbafb934713f`. The above command would translate to:

        docker cp pom_files/tananaev-traccar-64783123/pom-xml bbafb934713f:/home/travis/build/failed/tananaev/traccar

Then, **from inside the running container** after changing to the project's directory:

        /usr/local/maven-3.2.5/bin/mvn -Dhttps.protocols=TLSv1.2 clean test-compile
        /usr/local/maven-3.2.5/bin/mvn -Dhttps.protocols=TLSv1.2 -P sufire gzoltar:prepare-agent test
        /usr/local/maven-3.2.5/bin/mvn -Dhttps.protocols=TLSv1.2 gzoltar:fl-report

After this, the generated report will be in `target/site/gzoltar/sfl/txt/ochiai.ranking.csv`

For convenience, the generated SFL reports are available in the `sfl_reports` directory.

**Notes:** 

1. When running a container for `openpnp-openpnp-110833060`, `JAVA_HOME` needs to be set to use Java 8. As such, run `JAVA_HOME=/usr/lib/jvm/java-8-oracle/` before any of the above `mvn` commands.
2. The bug in `zxing-zxing-139981065` prevents it from compiling. As such, the SFL report cannot be generated for this case study.

### Inferring mutations with *Morpheus*

To analyze the changes for each commit inducing the bug in a more efficient way, the modified files were extracted from their containers to allow for offline analysis. The script that does this is `all_containers_scripts/all_changed_files.sh`

For each bug, the modified files can be located in the corresponding `pre_bug` and `bug` directories. As an example, for the first case study:

- `bugswarm-sandbox/bugs/traccar/traccar/64783122/pre_bug` --- contains the files **before** the bug
- `bugswarm-sandbox/bugs/traccar/traccar/64783122/bug` --- contains the **buggy version** of the files

*Morpheus* can then be used to analyze the changes made to the files and infer the mutation operators behind the modifications, like this:

        java -jar morpheus/morpheus.jar <before_file> <buggy_file>

As an example, to infer the semantics of the changes responsible for introducing the bug in the first case study:

        java -jar morpheus/morpheus.jar \
        bugswarm-sandbox/bugs/traccar/traccar/64783122/pre_bug/src/org/traccar/protocol/CastelProtocolDecoder.java \
        bugswarm-sandbox/bugs/traccar/traccar/64783122/bug/src/org/traccar/protocol/CastelProtocolDecoder.java

*Morpheus* writes to standard output. The results from these experiments were saved in the `inferred` directory. As such, the output from the previous example command can be seen in `inferred/traccar-traccar-64783122.txt`


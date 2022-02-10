#!/usr/bin/env python

# When we generate an experiment folder (FOLDER) with exp.py script on local machine,
# with K generated bash scripts, we can use this schedule.py to run all these scripts
# with N of them in parallel. The others (assuming N << K) will be started as soon as
# one current running job is completed

import random
import sys
import os
import subprocess
import time
import platform

if len(sys.argv) != 3:
    print("Usage:\nschedule.py <N> <FOLDER>")
    exit(1)

# The number of jobs to run in parallel
N = int(sys.argv[1])

if N < 1:
    print("Invalid value for N: " + str(N))
    exit(1)

# Location of experiment folder
FOLDER = sys.argv[2]

SHELL = platform.system() == 'Windows'

SCRIPTS_FOLDER = os.path.join(FOLDER, "scripts")

buffer = []

#collect name of all bash files
scripts = [f for f in os.listdir(SCRIPTS_FOLDER) if os.path.isfile(os.path.join(SCRIPTS_FOLDER, f)) and f.startswith("evomaster") and f.endswith(".sh")]

print("There are " + str(len(scripts)) + " EvoMaster script files")

random.shuffle(scripts)

k = 1

def runScript(s):
    global k
    print("Running script " + str(k)+ "/"+ str(len(scripts)) +": " + s)
    k = k + 1

    command = ["bash", os.path.join("scripts", s)]

    handler = subprocess.Popen(command, shell=SHELL, cwd=FOLDER)
    buffer.append(handler)

for s in scripts:
    if len(buffer) < N:
       runScript(s)
    else:
        while len(buffer) == N:
            for h in buffer:
                h.poll()
                if h.returncode is not None and h.returncode != 0:
                    print("Process terminated with code: " + str(h.returncode))

            # keep the ones running... those have return code not set yet
            buffer = [h for h in buffer if h.returncode is None]
            if len(buffer) == N :
                time.sleep(5)
            else:
                runScript(s)
                break

print("Waiting for last scripts to end")

for h in buffer:
    h.wait()
    if h.returncode != 0:
        print("Process terminated with code: " + str(h.returncode))

print("All jobs are completed")




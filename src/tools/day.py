from datetime import datetime
from datetime import timedelta
from datetime import timezone
import os
import re

today = datetime.now(timezone(timedelta(hours=-5)))

year = int(input(f"Year [{today.year}]: ") or today.year)
day = int(input(f"Day [{today.day}]: ") or today.day)
language = input(f"Language [java]: ") or "java"

workspace = os.environ['BUILD_WORKING_DIRECTORY']

aoc_url = f"https://adventofcode.com/{year}/day/{day}"

if language == "java":
    classname = input("Filename [Day%02d]: " % day) or "Day%02d" % day

    package = "aoc%d/day%02d" % (year, day)
    directory = os.path.join(workspace, f"src/main/java/{package}/")
    print(f"Making directory ... {directory}")
    os.makedirs(directory, exist_ok=True)

    print(f"Writing BUILD ...")
    build = os.path.join(directory, "BUILD")
    f = open(build, "w")
    f.write(f"java_binary(\n")
    f.write(f"    name = \"{classname}\",\n")
    f.write(f"    srcs = [\"{classname}.java\"],\n")
    f.write(f"    resources = glob([\"*.txt\"]),\n")
    f.write(f"    deps = [\n")
    f.write(f"      \"//src/main/java/aoccommon:aoccommon\",\n")
    f.write(f"    ],\n")
    f.write(f")\n")
    f.close()

    print(f"Writing {classname}.java ...")
    file = os.path.join(directory, f"{classname}.java")
    f = open(file, "w")
    f.write(f"package aoc%d.day%02d;\n" % (year, day))
    f.write(f"\n")
    f.write(f"/** Solution for {aoc_url}. */")
    f.write(f"class {classname} {{\n")
    f.write(f"\n")
    f.write(f"  private static final String INPUT = \"{package}/input.txt\";\n")
    f.write(f"\n")
    f.write(f"  public static void main(String [] args) throws Exception {{\n")
    f.write(f"  }}\n")
    f.write(f"}}\n")
    f.close()
else:
    print(f"Unrecognized language: {language}")

print(f"Writing input.txt ...")
txt = os.path.join(directory, "input.txt")
f = open(txt, "w")
f.close()


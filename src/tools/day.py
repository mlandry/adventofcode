from datetime import datetime
from datetime import timedelta
from datetime import timezone
import os

today = datetime.now(timezone(timedelta(hours=-5)))

year = int(input(f"Year [{today.year}]: ") or today.year)
day = int(input(f"Day [{today.day}]: ") or today.day)
language = input(f"Language [java]: ") or "java"

workspace = os.environ['BUILD_WORKING_DIRECTORY']
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
    f.write(f"    resources = [\"input.txt\"],\n")
    f.write(f")\n")
    f.close()

    print(f"Writing {classname}.java ...")
    file = os.path.join(directory, f"{classname}.java")
    f = open(file, "w")
    f.write(f"package aoc%d.day%02d;\n" % (year, day))
    f.write(f"\n")
    f.write(f"import java.io.BufferedReader;\n")
    f.write(f"import java.io.IOException;\n")
    f.write(f"import java.io.InputStreamReader;\n")
    f.write(f"\n")
    f.write(f"class {classname} {{\n")
    f.write(f"\n")
    f.write(f"  public static void main(String [] args) throws Exception {{\n")
    f.write(f"  }}\n")
    f.write(f"\n")
    f.write(f"  private static BufferedReader inputReader() throws IOException {{\n")
    f.write(f"    return new BufferedReader(\n")
    f.write(f"        new InputStreamReader(\n")
    f.write(f"            {classname}.class.getClassLoader().getResourceAsStream(\"{package}/input.txt\")));\n")
    f.write(f"  }}\n")
    f.write(f"}}\n")
    f.close()
else:
    print(f"Unrecognized language: {language}")

print(f"Writing input.txt ...")
txt = os.path.join(directory, "input.txt")
f = open(txt, "w")
f.close()


import re;
import os;
import shutil;
import subprocess;
versionre = re.compile(r"<version>(\d+\.\d+\.\d+)</version>")

version = None

with open("pom.xml", "r") as pom:
    for line in pom:
        match = versionre.search(line)
        if match:
            version = match.groups()[0]
            break

file = "target/microwave-"+version+".jar"
target = "target/microwave-"+version+".zip"

if not os.path.exists(file):
    print("Missing jar!")
    # Have to use os.system because mvn is a batch script
    os.system("mvn package")

if not os.path.exists(target):
    shutil.copyfile(file, target)

subprocess.run(["butler", "push", target, "meep3692/microwavejava:applet", "--userversion", version])
# print(["butler", "push", target, "meep3692/microwavejava:applet", "--userversion", version])
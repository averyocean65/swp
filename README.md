# SWP
SWP is an easy-to-use, simplistic, text editor written in Java. It was developed for the Hackaclub Flavortown program: https://flavortown.hackclub.com/projects/7601

# Prerequisites
In order to run SWP you need to have a Java Development Kit of version 11 or above, as well as a Java Runtime Environment, else the program simply won't run.

# How to run
## Windows
### Context Menu / Double Click
Providing that you have Java 11 (or over) installed on your system, make sure to run the provided `.jar` file with the installed `Java(TM) Platform SE Binary`.
<img width="565" height="83" alt="grafik" src="https://github.com/user-attachments/assets/20910151-e8db-46b9-a98a-caf38d461bbd" />

### run.bat File
You can use the provided batch file to run SWP, so long as the batch file is in the same directory as the .jar file.

### Terminal
Run `java.exe -jar SWP.jar` to run SWP.

## Linux
### Shell script
Ensure that the script is executable, by either checking the permissions or using `chmod +x run.sh` (or similar) to set the script to be runnable.
You can use the provided shell script file to run SWP, so long as the shell script file is in the same directory as the .jar file.

### Terminal
Run `java -jar SWP.jar` to run SWP.

# What about an .exe or app image file?
We don't provide `.exe` or Linux app image files for this project, because it uses Java. Packaging the application in a OS-specific format defeats the purpose of Java and the JVM, as `.jar` files are meant to be platform independent executables and libraries. We also tried using tools to convert the `.jar` files to executables, but found the results either extremely unstable or unusable no matter how we configured the tool.
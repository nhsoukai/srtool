#!/bin/bash
javac -target 1.6 -source 1.6 -cp src:junit-4.10.jar:antlr-3.4-complete.jar:jcommander.jar -d bin -s bin src/srt/*/*.java src/srt/*/*/*.java src/srt/*/*/*/*.java


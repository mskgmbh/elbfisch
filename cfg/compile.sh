#!/bin/sh -e
if [ -f ../src/org/jpac/plc/s7/symaddr/analysis/Analysis.java ]
then
    rm ../src/org/jpac/plc/s7/symaddr/analysis/*.java
    rm ../src/org/jpac/plc/s7/symaddr/lexer/*.java
    rm ../src/org/jpac/plc/s7/symaddr/lexer/lexer.dat
    rm ../src/org/jpac/plc/s7/symaddr/node/*.java
    rm ../src/org/jpac/plc/s7/symaddr/parser/*.java
    rm ../src/org/jpac/plc/s7/symaddr/parser/parser.dat
fi
java -jar ../lib/sablecc.jar -d ../src ./struct.bnf



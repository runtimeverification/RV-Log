@echo off

set SRC_ROOT=%~dp0..

set LIB=%SRC_ROOT%\lib

set CP=%LIB%\*;

java -cp "%CP%;%CLASSPATH%" -ea fsl.uiuc.Main %*

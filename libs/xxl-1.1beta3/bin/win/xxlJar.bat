@echo off

echo.
echo XXL: The eXtensible and fleXible Library for data processing
echo.
echo ============================================================
echo.
echo Generating jar-archive: xxl.jar
echo.
date /T
echo.

SET mode=%1
setlocal

call xxlEnv.bat

set __JAR=%XXLJAVAHOME%\bin\jar
if NOT DEFINED XXLJAVAHOME ( set __JAR=jar )

IF "%mode%"=="" GOTO normal
IF %mode%==normal GOTO normal
IF %mode%==verbose GOTO verbose
IF %mode%==help GOTO help

echo Error: Invalid parameter specified.
GOTO ende

:help
echo command usage: makejar [verbose]
GOTO ende

:verbose
echo ...
cd /D %xxloutpath%\class
%__JAR% cvfM %xxloutpath%\xxl.jar .
GOTO ende

:normal
echo ...
cd /D %xxloutpath%\class
%__JAR% cfM %xxloutpath%\xxl.jar .

:ende
echo.
echo Jar-archive creation finished.

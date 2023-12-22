@echo off
rem Probing for needed environment variables ...
if NOT DEFINED xxlrootpath ( ECHO. & ECHO. & ECHO Environment variable 'xxlrootpath' not set! Refer to INSTALL.txt! & GOTO ende) 
if NOT DEFINED xxloutpath ( ECHO. & ECHO. & ECHO Environment variable 'xxloutpath' not set! Refer to INSTALL.txt! & GOTO ende) 
if NOT DEFINED xxljavahome ( ECHO. & ECHO. & ECHO Environment variable 'xxljavahome' not set! Java executables need to be accessed via path-variable! Refer to INSTALL.txt!) 
rem ---
mkdir %xxloutpath%\class
mkdir %xxloutpath%\doc
mkdir %xxloutpath%\lib
mkdir %xxloutpath%\xxl2html
:ende

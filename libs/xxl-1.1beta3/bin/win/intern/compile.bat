@ECHO OFF
setlocal
call "%~d0%~p0\..\xxlEnv.bat"

if NOT DEFINED XXLJAVAHOME ( javac -d %xxloutpath%\class -sourcepath %xxlsourcepath% -classpath "%xxlclasspath%" %*  & GOTO ende)

%XXLJAVAHOME%\bin\javac -d %xxloutpath%\class -sourcepath %xxlsourcepath% -classpath "%xxlclasspath%" %*
GOTO ende

:ende

@ECHO OFF
rem -----------------------------------------
rem Sets local environment and calls the java vm
rem for any xxl class file given. Passes
rem all given command line parameters to java.
rem ------------------------------------------
setlocal

CALL xxlEnv.bat
if NOT DEFINED XXLJAVAHOME ( java -Xms256m -Xmx512m -classpath "%xxlclasspath%" -Dxxlrootpath=%xxlrootpath% %* & GOTO ende)

%XXLJAVAHOME%\bin\java -Xms256m -Xmx512m -classpath "%xxlclasspath%" -Dxxlrootpath=%xxlrootpath% -Dxxloutpath=%xxloutpath% %*
GOTO ende

:ende

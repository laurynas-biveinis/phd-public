@ECHO OFF
if NOT DEFINED xxlrootpath ( ECHO. & ECHO. & ECHO Environment variable 'xxlrootpath' not set! Refer to INSTALL.txt! & GOTO ende) 
if NOT DEFINED xxloutpath ( ECHO. & ECHO. & ECHO Environment variable 'xxloutpath' not set! Refer to INSTALL.txt! & GOTO ende) 
rem ---
IF NOT DEFINED xxldocclasspath SET xxldocclasspath=.;%xxloutpath%\class;%xxlrootpath%\jars\classpath\colt.jar;%xxlrootpath%\jars\classpath\jama.jar;%xxlrootpath%\jars\classpath\castor.jar;%xxlrootpath%\jars\classpath\swt.jar;%xxlrootpath%\jars\classpath\xstream.jar

IF NOT DEFINED xxlclasspath SET xxlclasspath=%classpath%;%xxldocclasspath%
IF DEFINED xxljavahome SET xxlclasspath=%xxlclasspath%;%xxljavahome%\lib\tools.jar
IF NOT DEFINED xxlsourcepath SET xxlsourcepath=%xxlrootpath%\src

rem ECHO %xxlclasspath%
rem ECHO %xxljavahome%
rem ECHO %xxlsourcepath%

@ECHO OFF
SETLOCAL

SET javafilesfile=%xxloutpath%\javafiles.lst
SET javafilesfile2=%xxloutpath%\javafiles_reg.lst

ECHO.
ECHO XXL: The eXtensible and fleXible Library for data processing
ECHO ============================================================
ECHO.
ECHO Compile script
ECHO.
ECHO given parameters: %1
ECHO.
date /T

call xxlEnv.bat

IF EXIST %javafilesfile% del %javafilesfile%
SET xxltemp=
SET xxltemp=%1
IF NOT DEFINED xxltemp GOTO xxl

IF /i %1==all GOTO all
IF /i %1==reg GOTO reg
IF /i %1==con GOTO connect
IF /i %1==xxl GOTO xxl
IF /i %1==apps GOTO apps
IF /i %1==intern GOTO intern
IF /i %1==native GOTO native


IF /i %1==help GOTO hilfe
IF /i "%1"=="-?" GOTO hilfe
IF /i "%1"=="-h" GOTO hilfe

ECHO ERROR! Unknown parameter given
GOTO hilfe

:all
ECHO xxl compiling all classes
ECHO generating file list
for /R %xxlsourcepath% %%i IN (*.java) DO @echo %%i >> %javafilesfile%
ECHO copy icons
xcopy %xxlsourcepath%\*.gif %xxloutpath%\class\*.* /y /s /q
ECHO copy properties files
xcopy %xxlsourcepath%\*.properties %xxloutpath%\class\*.* /y /s /q
GOTO compiling

:reg
ECHO compiling classes which fulfil a regular expression
ECHO generating file list
for /R %xxlsourcepath% %%i IN (*.java) DO @echo %%i >> %javafilesfile2%
grep -i "%2" %javafilesfile2% >> %javafilesfile%
del %javafilesfile2%
GOTO compiling

:xxl
ECHO compiling all classes of xxl (without connectivity, applications, ...) - pure xxl
ECHO generating file list
FOR /R %xxlsourcepath%\xxl\core\ %%i IN (*.java) DO echo %%i >> %javafilesfile%
ECHO copy icons
xcopy %xxlsourcepath%\xxl\core\*.gif %xxloutpath%\class\xxl\core\*.* /y /s /q
ECHO copy properties files
xcopy %xxlsourcepath%\xxl\core\*.properties %xxloutpath%\class\xxl\core\*.* /y /s /q
GOTO compiling

:connect
ECHO compiling all classes of connectivity (ONLY)
ECHO generating file list
FOR /R %xxlsourcepath%\xxl\connectivity\ %%i IN (*.java) DO echo %%i >> %javafilesfile%
ECHO copy icons
xcopy %xxlsourcepath%\xxl\connectivity\*.gif %xxloutpath%\class\xxl\connectivity\*.* /y /s /q
ECHO copy properties files
xcopy %xxlsourcepath%\xxl\connectivity\*.properties %xxloutpath%\class\xxl\connectivity\*.* /y /s /q
GOTO compiling

:apps
ECHO compiling all classes of applications (ONLY)
ECHO generating file list
FOR /R %xxlsourcepath%\xxl\applications\ %%i IN (*.java) DO echo %%i >> %javafilesfile%
ECHO copy icons
xcopy %xxlsourcepath%\xxl\applications\*.gif %xxloutpath%\class\xxl\applications\*.* /y /s /q
ECHO copy properties files
xcopy %xxlsourcepath%\xxl\applications\*.properties %xxloutpath%\class\xxl\applications\*.* /y /s /q
GOTO compiling

:intern
ECHO compiling intern applications (ONLY)
ECHO generating file list
FOR /R %xxlsourcepath%\intern\ %%i IN (*.java) DO echo %%i >> %javafilesfile%
ECHO copy icons
xcopy %xxlsourcepath%\intern\*.gif %xxloutpath%\class\intern\*.* /y /s /q
ECHO copy properties files
xcopy %xxlsourcepath%\intern\*.properties %xxloutpath%\class\intern\*.* /y /s /q
GOTO compiling

:native
ECHO compiling native c-sources (ONLY)
CALL %xxlrootpath%\bin\win\intern\compile_jni.bat
GOTO ende

:hilfe
ECHO "usage: xxlMake all | xxl | con | reg | apps | intern | native"
ECHO Valid parameters:
ECHO all - compiling all classes except native sources (default)
ECHO xxl - compiling only the core-packages
ECHO con - compiling only connectivity
ECHO reg - compiling classes which match a regular expression (second parameter). grep needed in PATH!
ECHO apps - compiling only applications
ECHO intern - compiling only internal applications
ECHO native - compiling native c-sources only
ECHO "help | -? | -h" - show this screen
ECHO.
GOTO :EOF
rem ----------------------------------

:compiling
ECHO ready, compiling
call %xxlrootpath%\bin\win\intern\compile @%javafilesfile%
del %javafilesfile%
ECHO Ready. All Classes compiled.

GOTO ende

:ende
echo ----end----

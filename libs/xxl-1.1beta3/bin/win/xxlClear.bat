@ECHO OFF

SETLOCAL

call xxlEnv

ECHO CAUTION: THIS SCRIPT WILL DELETE ALL FILES AND FOLDERS IN %XXLOUTPATH%!
ECHO.
ECHO USE WITH CAUTION!
ECHO.
ECHO ARE YOU SURE? IF NOT PRESS CTRL+C TO ABORT!
pause

ECHO DELETING ....
del %xxloutpath%\*.* /Q  >nul

ECHO DELETING CLASS-FILES
del %xxloutpath%\class\*.* /F /S /Q >nul
rmdir %xxloutpath%\class /S /Q

ECHO DELETING DOC-FILES
del %xxloutpath%\doc\*.* /F /S /Q >nul
rmdir %xxloutpath%\doc /S /Q

ECHO DELETING HTML-FILES
del %xxloutpath%\xxl2html\*.* /F /S /Q >nul
rmdir %xxloutpath%\xxl2html /S /Q

ECHO DELETING LIB-FILES
del %xxloutpath%\lib\*.* /F /S /Q >nul
rmdir %xxloutpath%\lib /S /Q

ECHO READY

:ende

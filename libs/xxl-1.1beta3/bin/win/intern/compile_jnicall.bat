echo create JNI-header
%XXLJAVAHOME%\bin\javah -classpath "%XXLCLASSPATH%" -d %XXLSOURCEPATH%\%native_c_path% %native_package%.%native_class%

echo compile c files
set cmd=cl /nologo /O2 /Ot /Ob2 /EHsc /MT %XXLSOURCEPATH%\%native_c_path%%native_class%Windows.c
set cmd=%cmd% /I%XXLSOURCEPATH%\%native_c_path%
set cmd=%cmd% /I%XXLJAVAHOME%\INCLUDE
set cmd=%cmd% /I%XXLJAVAHOME%\INCLUDE\win32
set cmd=%cmd% /Fe%XXLOUTPATH%\lib\%native_dll%.dll
set cmd=%cmd% /Fo%XXLOUTPATH%\lib\%native_dll%.obj
set cmd=%cmd% /LD /link /DLL
set cmd=%cmd% /libpath:"%XXLJAVAHOME%\lib"
set cmd=%cmd% user32.lib gdi32.lib oldnames.lib kernel32.lib uuid.lib

rem for debugging
rem echo on

%cmd%

rem for debugging
rem @echo off

ECHO delete .obj .lib .exp
del %XXLOUTPATH%\lib\%native_dll%.obj
del %XXLOUTPATH%\lib\%native_dll%.lib
del %XXLOUTPATH%\lib\%native_dll%.exp

rem Copying to XXLROOTPATH (rights for writing required!)
ECHO Copying dll to %XXLROOTPATH%\bin\win (rights for writing required!)
copy %XXLOUTPATH%\lib\%native_dll%.dll %XXLROOTPATH%\bin\win

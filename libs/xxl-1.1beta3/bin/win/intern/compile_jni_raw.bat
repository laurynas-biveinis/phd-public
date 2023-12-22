@ECHO OFF
ECHO.
ECHO XXL: The eXtensible and fleXible Library for data processing
ECHO ============================================================
ECHO.
ECHO Compiling C Source with Microsoft Visual Studio 7
ECHO.
ECHO Environment variables of Visual C++ have to be set correct 
ECHO before calling this batch file (PATH, LIB, INCLUDE, ...). 
ECHO Run vcvars32.bat of your Visual C++ compiler.
ECHO This script may work with different versions, too.
ECHO java-files have to be compiled before (use makeclass all 
ECHO or makeclass connectivity).
ECHO.

SETLOCAL
call "%~d0%~p0\..\xxlEnv.bat"

rem package name
set native_package=xxl.core.io.raw

rem path source
set native_java_path=xxl\core\io\raw\
set native_c_path=xxl\connectivity\jni\raw\

rem name the jni-class
set native_class=NativeRawAccess

rem name of the library (dll-file without extension)
set native_dll=RawAccess


call %xxlrootpath%\bin\win\intern\compile_jnicall

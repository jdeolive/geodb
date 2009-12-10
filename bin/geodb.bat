@echo off
setlocal enableextensions enabledelayedexpansion

if not "%JAVA_HOME%" == "" goto java_home_set

echo.
echo ERROR: JAVA_HOME not found in your environment.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation
echo.
goto error

:java_home_set

if exist "%JAVA_HOME%\bin\java.exe" goto java_home_ok

echo.
echo ERROR: JAVA_HOME is set to an invalid directory.
echo JAVA_HOME = "%JAVA_HOME%"
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation
echo.
goto error

:java_home_ok
set JAVA="%JAVA_HOME%\bin\java.exe"

@REM change directory to the lib directory
@REM set CWD=%CD%
@REM cd %~dp0../lib
@REM
@REM @REM build up the classpath
@REM set CLASSPATH=;
@REM FOR /R %%G IN (*.jar) DO (
@REM     SET CLASSPATH=!CLASSPATH!;%%G
@REM     )
@REM
@REM     cd %CWD%
@REM     %JAVA% -cp "%CLASSPATH%" geodb.Prompt %1 
@REM     goto end
@REM
@REM     :error
@REM     set ERROR_CODE=1
@REM
@REM     :end

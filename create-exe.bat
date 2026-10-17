@echo off
setlocal EnableExtensions DisableDelayedExpansion
title StudentAttendance - jpackage Standalone EXE Builder

echo ===========================================================================
echo   STUDENT ATTENDANCE SYSTEM - STANDALONE EXE PACKAGING
echo   Developer : Hani Al-khateeb  ^|  Copyright 2026 All Rights Reserved.
echo ===========================================================================
echo.

set "PROJECT_DIR=%~dp0"
if "%PROJECT_DIR:~-1%"=="\" set "PROJECT_DIR=%PROJECT_DIR:~0,-1%"
cd /d "%PROJECT_DIR%"
echo [*] Project directory : %PROJECT_DIR%
echo.

REM ---- Resolve JAVA_HOME ----------------------------------------------------
if not defined JAVA_HOME goto :DETECT_JAVA
if not exist "%JAVA_HOME%\bin\jpackage.exe" goto :DETECT_JAVA
goto :JAVA_READY

:DETECT_JAVA
echo [*] JAVA_HOME is missing or has no jpackage.exe. Auto-detecting from PATH...
set "JAVA_HOME="
for /f "delims=" %%I in ('where jpackage 2^>nul') do call :SET_JAVA_HOME "%%~dpI"
if not defined JAVA_HOME goto :NO_JAVA_HOME
goto :JAVA_READY

:SET_JAVA_HOME
if defined JAVA_HOME goto :eof
set "BINDIR=%~1"
if "%BINDIR:~-1%"=="\" set "BINDIR=%BINDIR:~0,-1%"
for %%J in ("%BINDIR%") do set "JAVA_HOME=%%~dpJ"
if "%JAVA_HOME:~-1%"=="\" set "JAVA_HOME=%JAVA_HOME:~0,-1%"
goto :eof

:JAVA_READY
set "JPACKAGE=%JAVA_HOME%\bin\jpackage.exe"
echo [OK] JAVA_HOME         : %JAVA_HOME%
echo [OK] jpackage          : %JPACKAGE%
echo.

REM ---- Validate Maven wrapper ----------------------------------------------
if not exist "%PROJECT_DIR%\mvnw.cmd" goto :NO_MVNW
echo [OK] Maven wrapper      : %PROJECT_DIR%\mvnw.cmd

REM ---- Locate application icon --------------------------------------------
set "ICON="
if exist "%PROJECT_DIR%\src\main\resources\com\example\studentattendance\images\app_icon.ico" set "ICON=%PROJECT_DIR%\src\main\resources\com\example\studentattendance\images\app_icon.ico"
if not defined ICON if exist "%PROJECT_DIR%\docs\app_icon.ico" set "ICON=%PROJECT_DIR%\docs\app_icon.ico"
if defined ICON echo [OK] Application icon  : %ICON%
if not defined ICON echo [WARN] app_icon.ico not found. Default launcher icon will be used.
echo.

REM ---- Step 1: strict offline Maven build ---------------------------------
echo [1/4] Building application JAR in strict offline mode...
call "%PROJECT_DIR%\mvnw.cmd" clean package -o -DskipTests
if errorlevel 1 goto :BUILD_FAILED

set "MAIN_JAR=%PROJECT_DIR%\target\StudentAttendance-1.0-SNAPSHOT.jar"
if not exist "%MAIN_JAR%" goto :JAR_MISSING
echo [OK] Main JAR           : %MAIN_JAR%
echo.

REM ---- Step 2: copy runtime dependencies ----------------------------------
echo [2/4] Collecting runtime dependencies in strict offline mode...
call "%PROJECT_DIR%\mvnw.cmd" dependency:copy-dependencies -o -DoutputDirectory="%PROJECT_DIR%\target\libs" -DincludeScope=runtime
if errorlevel 1 goto :DEPS_FAILED
echo.

REM ---- Step 3: staging ----------------------------------------------------
echo [3/4] Preparing staging directory...
set "STAGE=%PROJECT_DIR%\target\jpackage-staging"
if exist "%STAGE%" rmdir /s /q "%STAGE%"
mkdir "%STAGE%"
copy /y "%MAIN_JAR%" "%STAGE%\StudentAttendance-1.0-SNAPSHOT.jar" >nul
xcopy /y /q "%PROJECT_DIR%\target\libs\*.jar" "%STAGE%\" >nul
del /q "%STAGE%\*junit*.jar" 2>nul
del /q "%STAGE%\*opentest4j*.jar" 2>nul
del /q "%STAGE%\*apiguardian*.jar" 2>nul
echo [OK] Staging            : %STAGE%
echo.

REM ---- Step 4: run jpackage -----------------------------------------------
echo [4/4] Running jpackage to build the standalone app image...
set "DEST=%PROJECT_DIR%\dist"
if exist "%DEST%\StudentAttendance" rmdir /s /q "%DEST%\StudentAttendance"
if not exist "%DEST%" mkdir "%DEST%"

set "ICON_ARG="
if defined ICON set ICON_ARG=--icon "%ICON%"

"%JPACKAGE%" --type app-image --name StudentAttendance --app-version 1.0.0 --vendor "Hani Al-khateeb" --copyright "Copyright 2026 Hani Al-khateeb. All Rights Reserved." --description "Student Attendance System" --dest "%DEST%" --input "%STAGE%" --main-jar StudentAttendance-1.0-SNAPSHOT.jar --main-class com.example.studentattendance.Main --runtime-image "%JAVA_HOME%" %ICON_ARG%

if errorlevel 1 goto :JPACKAGE_FAILED

REM ---- Verify output ------------------------------------------------------
set "EXE=%DEST%\StudentAttendance\StudentAttendance.exe"
if not exist "%EXE%" goto :EXE_MISSING
echo.
echo ===========================================================================
echo   [SUCCESS] Standalone executable created.
echo   Path: %EXE%
echo ===========================================================================
echo.
if defined NOPAUSE goto :DONE
pause
:DONE
endlocal
exit /b 0

REM ---- Error handlers -----------------------------------------------------
:NO_JAVA_HOME
echo [ERROR] JAVA_HOME could not be resolved and jpackage was not found on PATH.
echo [HINT]  Install JDK 17 or later and set the JAVA_HOME environment variable.
goto :FAIL

:NO_MVNW
echo [ERROR] Maven wrapper not found: %PROJECT_DIR%\mvnw.cmd
goto :FAIL

:BUILD_FAILED
echo [ERROR] Maven build failed in offline mode.
echo [HINT]  Ensure dependencies are cached in %USERPROFILE%\.m2 and rerun once online.
goto :FAIL

:JAR_MISSING
echo [ERROR] Expected JAR was not produced: %MAIN_JAR%
goto :FAIL

:DEPS_FAILED
echo [ERROR] Copying runtime dependencies failed.
goto :FAIL

:JPACKAGE_FAILED
echo [ERROR] jpackage failed. See the jpackage output above for details.
goto :FAIL

:EXE_MISSING
echo [ERROR] jpackage finished but the EXE was not found at: %EXE%
goto :FAIL

:FAIL
echo.
echo Build script aborted. Review the messages above.
if defined NOPAUSE goto :FAIL_EXIT
pause
:FAIL_EXIT
endlocal
exit /b 1

@echo off
REM run.bat - Run RootsOfEternity on Windows
REM Usage: run.bat [rebuild]

IF "%1"=="rebuild" (
  echo Rebuilding project...
  powershell -ExecutionPolicy Bypass -File build.ps1
)

if exist out (
  echo Running from classes in out/ (classpath includes lib/*)
  java -cp ".;out;lib/*" src.main.Main
  goto :eof
)

if exist game.jar (
  echo Running game.jar (note: jar does not automatically include lib/*. If you see JDBC errors, run from out/ or run with rebuild.)
  java -jar game.jar
  goto :eof
)

echo No jar or out/ found. Building now...
powershell -ExecutionPolicy Bypass -File build.ps1
if exist game.jar (
  echo Running freshly-built game.jar...
  java -jar game.jar
  goto :eof
)

echo Failed to run the game. Build did not produce game.jar and out/ not found.
exit /b 1

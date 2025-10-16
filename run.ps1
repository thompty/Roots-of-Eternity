<#
run.ps1 - Clean, build, and run RootsOfEternity
Usage:
  .\run.ps1            # cleans, builds, and runs game
  .\run.ps1 -rebuild   # force rebuild then run
#>
param(
    [switch]$rebuild
)

# Cleanup old .class files from src
Write-Host "Cleaning old .class files from src..."
Get-ChildItem -Path .\src -Recurse -Include *.class | Remove-Item -Force

# Clean build directory
$OutDir = "build"
if (Test-Path $OutDir) { Remove-Item -Recurse -Force $OutDir }
New-Item -ItemType Directory -Path $OutDir | Out-Null

# Compile sources
$JavacSources = "javac_sources.txt"
$srcs = Get-Content $JavacSources
$cp = (Get-ChildItem -Path .\lib -Filter *.jar | Select-Object -ExpandProperty FullName) -join ';'
$cmd = 'javac -cp "' + $cp + '" -d ' + $OutDir + ' @' + $JavacSources
Write-Host "Compiling..."
& cmd /c $cmd

# Copy resources
if (Test-Path .\res) {
    Write-Host "Copying resources..."
    $dest = Join-Path $OutDir "res"
    New-Item -ItemType Directory -Force -Path $dest | Out-Null
    robocopy .\res $dest /E | Out-Null
} else {
    Write-Host "Warning: res/ directory not found. Make sure resources are available in ./res"
}

# Create jar
$jarPath = Join-Path $OutDir "game.jar"
Write-Host "Creating game.jar in $OutDir..."
& cmd /c "jar cfm $jarPath .\MANIFEST.MF -C $OutDir ."
Write-Host "Build finished. $jarPath created."

# Run game from build/game.jar if present, with sqlite-jdbc on classpath
$libJars = (Get-ChildItem -Path .\lib -Filter *.jar | Select-Object -ExpandProperty FullName) -join ';'
if (Test-Path $jarPath) {
    Write-Host "Running build\game.jar with lib/* on classpath..."
    & java -cp "$libJars;$jarPath" src.main.Main
    exit $LASTEXITCODE
}

Write-Error "Failed to run the game. Build did not produce build\game.jar."
exit 1

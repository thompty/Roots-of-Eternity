## Building and packaging RootsOfEternity (Windows)

This repository contains Java sources under `src/` and resources under `src/res/`.
Follow these steps on Windows (PowerShell) to compile, build a runnable jar, and create a native package using `jpackage`.

Prerequisites

- JDK 17+ installed (or any JDK that includes `jpackage`). Make sure `javac`, `jar`, and `jpackage` are on PATH or note the JDK installation path.
- PowerShell (Windows) — commands below assume running in PowerShell from the project root `C:\path\to\Game`

Quick automated way (recommended):

1. Open PowerShell in the project root (where `build_and_package.ps1` is located).
2. Run:

```
.\build_and_package.ps1
```

This will:

- Compile the Java sources to `out/`
- Copy `src/res/` into `out/src/res/` so resource paths remain `/src/res/...`
- Create `game.jar` using `MANIFEST.MF` (Main-Class = `src.main.Main`)
- Create a default `config.txt` next to the jar if it doesn't exist

Manual steps (if you prefer):

1. Compile (from project root):

```
mkdir .\out
javac -d .\out (Get-ChildItem -Path .\src -Recurse -Filter *.java | ForEach-Object { $_.FullName }) -encoding UTF8
```

2. Copy resources into the classpath location expected by the code:

```
mkdir .\out\src\res -Force
robocopy .\src\res .\out\src\res /E
```

3. Create jar (uses `MANIFEST.MF` in project root):

```
jar cfm .\game.jar .\MANIFEST.MF -C .\out .
```

4. Run the jar:

```
java -jar .\game.jar
```

Packaging with jpackage (recommended for distributing to friends)

- Option A: app-image (portable folder you can zip for itch.io):

```
jpackage --type app-image --input . --main-jar game.jar --name RootsOfEternity --dest .\image
```

Then zip the resulting `.\image` folder and upload the zip to itch.io.

- Option B: Windows installer (EXE) — bundles runtime and produces an installer:

```
jpackage --type exe --input . --main-jar game.jar --name RootsOfEternity --dest .\installer --win-shortcut --win-dir-chooser --icon path\to\icon.ico
```

If you have the WiX Toolset installed (candle.exe and light.exe on PATH) jpackage will be able to produce a native Windows installer (EXE/MSI). On your machine those tools may already be available; an example installer command that also creates shortcuts, a Start Menu entry, and a destination folder chooser is:

```
jpackage --type exe --input . --main-jar game.jar --name RootsOfEternity --dest .\installer --resource-dir .\pack-resources --win-shortcut --win-dir-chooser --win-menu --app-version 1.0 --vendor "thompty" --description "Roots of Eternity - small action RPG" --verbose
```

If jpackage fails with "Cannot find WiX tools" even though WiX is installed, make sure the WiX `bin` folder (for example `C:\Program Files (x86)\WiX Toolset v3.14\bin`) is on your PATH in the same shell where you run `jpackage`.

Notes and gotchas

- `config.txt` is read/written as `new FileReader("config.txt")` (current working directory). Make sure a `config.txt` exists next to the jar/executable or change the code to store in `%APPDATA%` (I can change that for you).
- The code expects resource paths like `/src/res/...` inside the jar. The build script copies `src/res/` into the jar accordingly.
- If `jpackage` complains, point it to the full JDK path: `C:\Path\To\jdk\bin\jpackage.exe`.

If you want, I can:

- Add an automated call to `jpackage` in the script (you must supply JDK path and icon path), or
- Change `Config` to read/write config in `%APPDATA%` so the installed app doesn't need write access to Program Files.

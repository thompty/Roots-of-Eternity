Contributing / Build Instructions

This file describes how to build and test the project locally.

Requirements

- JDK 17+ (javac, jar)
- PowerShell (Windows)
- Optional: WiX Toolset for jpackage installer

Quick build (PowerShell)

1. Ensure third-party jars are in `lib/`.
2. Place runtime assets under top-level `res/` (images, fonts, sounds, maps).
3. Run the build script from the repo root:

```powershell
.\build.ps1
```

This compiles sources to `out/`, copies `res/` into `out/src/res/`, and produces `game.jar`.

Cleaning

Use the cleanup script to remove build artifacts:

```powershell
.\cleanup.ps1
```

Notes

- `configData.db` and `hitbox_tuner.cfg` are runtime artifacts and should not be committed (they're in .gitignore).
- If you move or rename resources, update resource paths in code (they should use `/res/...` inside the JAR).
- For packaging to a Windows installer, see `README-packaging.md`.

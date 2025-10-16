Repository layout and purpose

Root files

- build_and_package.ps1 - Windows packaging/build script (uses jpackage)
- cleanup.ps1 - helper to clean up build artifacts
- configData.db - runtime SQLite DB (do NOT commit; it's in .gitignore)
- game.jar - packaged jar (generated)
- MANIFEST.MF - jar manifest (Main-Class = src.main.Main)
- README-packaging.md - packaging instructions
- \_\_files.txt - helper listing (not important)

Directories

- src/ - Java source tree
  - main/ - core engine and game entry points
  - entity/ - base entity classes and game objects
  - object/ - in-world items / objects
  - monster/ - enemy implementations
  - ai/ - AI utilities
  - tiles/ - tile classes and tile manager
  - res/ - runtime resources (images, fonts, sound)
- bin/ - compiled classes produced by javac
- out/ - alternate compiled output used by packaging
- lib/ - third-party jars (sqlite, gson, etc.)
- installer/ - optional packaged installers (jpackage output)
- pack-resources/ - resources for jpackage

Recommended cleanup / organization

1. Move build outputs to `out/` only and ensure `bin/` is removed from repo (it's compiled output).
2. Keep `lib/` for third-party JARs but consider committing them to a package manager or documenting pin versions in README.
3. Runtime files that should not be committed (configData.db, hitbox_tuner.cfg) are added to `.gitignore`.
4. Add a small `docs/` folder if you plan to expand documentation or changelogs.

Quick housekeeping commands (PowerShell)

```powershell
# Remove compiled output (safe if you can rebuild)
Remove-Item -Recurse -Force .\bin, .\out -ErrorAction SilentlyContinue

# Rebuild project (in repo root)
& cmd /c 'javac -cp "lib/*" -d out @javac_sources.txt'

# Package jar
jar cfm .\game.jar .\MANIFEST.MF -C .\out .
```

Run scripts

- `run.ps1` - PowerShell helper to build (optional) and run the game. Use `./run.ps1` or `./run.ps1 -rebuild`.
- `run.bat` - Windows batch equivalent: `run.bat` or `run.bat rebuild`.

If you want, I can:

- Remove `bin/` or `game.jar` from the repository (make the changes and run cleanup), or
- Move `src/res/` into a top-level `res/` and adjust build scripts (requires code changes), or
- Add a small `Makefile` or `build.ps1` wrapper that always cleans and builds predictably.

Tell me which cleanup or organizational change you want me to apply and I'll do it.

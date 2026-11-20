# boardgame-kotlin-carcassonne

Kotlin Carcassonne module and its Vue front.

## Layout

```
back/    :back    Kotlin game plugin (rules, scoring, tiles, bot actions)
front/            Vue Module Federation remote (copied from boardgame-web-carcassonne/front)
```

## Status

Implemented: the base game — starting tile, 71-tile deck, edge-matched placement with rotation,
follower deployment (knight/thief/farmer/monk), completed road/city/cloister scoring with pennants
and majorities, final scoring (incomplete features, farms, majority), and winner detection. Bots are
not implemented (`nextBotAction` returns null; the front disables "Add bot").

Verified: `:back:test` (30 tests ported from the old `tiles`, `scoring` and service suites), the
release asset, the front build against generated Kotlin types, and a two-player launcher smoke test.

## Build / test

```bash
gradle :back:test
gradle :back:jar        # -> back/build/libs/back-<version>.jar
```

## Development build

```bash
gradle devBuild         # :back:generateTs + npm run build-front + :back:jar
gradle devFront         # front only (-> front/dist)
```

The launcher's `DEV=true` watcher reloads the module when `back/build/libs/back-<version>.jar`
changes and copies `front/dist`. Requires `npm` on `PATH`.

IntelliJ IDEA: the shared **Build Carcassonne (dev)** run configuration runs `devBuild`.

## Version

1. `-PcarcassonneVersion=…`
2. `RELEASE_VERSION`
3. the latest reachable git tag (`v0.2.0` → `0.2.0`)
4. default `0.1.0`

Tag the release before building; the **Build Carcassonne release** configuration then produces
`carcassonne-<version>.zip` and `back-<version>.jar` with `version.properties` matching the tag.

## Release packaging

```bash
gradle releaseBuild            # -> back/build/dist/carcassonne-<version>.zip
gradle :back:releaseZip        # same, but assumes front/dist is already built
```

The asset contains `back/carcassonne.jar` (classes + `classes.dex`) and `front/**`. The core installs
it as `games-modules/carcassonne/<version>/`.

IntelliJ IDEA: the shared **Build Carcassonne release** run configuration runs `releaseBuild`.

## Publishing a release

```bash
gradle publishRelease                 # latest release
gradle publishRelease -PghDraft=true  # draft for review
```

Runs `releaseBuild`, pushes the local `v<version>` tag and creates the GitHub release with generated
notes and the module zip as its only asset. Requires `gh` installed/authenticated, a clean tree at the
tagged commit, and an `origin` remote.

## TypeScript model codegen

```bash
gradle :back:generateTs   # -> front/src/domain/generated/types.ts
```

Models/enums are discovered via `@GenerateTs` on the Kotlin definitions. Carcassonne uses
`@GenerateTs(asUnion = true)` for its string enums, so the generated front types keep the wire values
as literal unions (`'city' | 'road' | ...`) exactly like the old TS module.

The front domain combines the generated wire types with front-only data:

- `front/src/domain/templates.ts` — the tile table (mirrors
  `back/src/main/kotlin/boardgame/carcassonne/Tiles.kt`; keep both in sync);
- `front/src/domain/types.ts` — `CarcassonneFieldSide` and `CarcassonneTileTemplate`;
- `front/src/domain/tiles.ts` — rotation, placement and feature-group helpers;
- `front/src/domain/scoring.ts` — the feature graph used to grey out occupied features.

After changing Kotlin models:

```bash
gradle :back:generateTs
git diff --exit-code front/src/domain/generated/types.ts   # CI guard
npm run build-front
```

The front depends on `boardgame-web-common` via `file:../../core/common`.

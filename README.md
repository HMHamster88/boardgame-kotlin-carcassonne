# boardgame-kotlin-carcassonne

Kotlin Carcassonne module and its Vue front.

## Layout

```
back/    :back    Kotlin game plugin (rules, state, bot actions)
front/            Vue Module Federation remote (copied from boardgame-web-carcassonne/front)
```

## Status

Scaffold: `front/` copied; the Kotlin module is not created yet and the front does not compile until
the domain extraction below is done.

## Front copy TODO

The front imports `CarcassonneTileTemplate`, feature/follower types and the runtime
`getTileTemplate` from the old TS `carcassonne-back` package. Copy `back/src/types` and the tile
templates into `front/src/domain/` and repoint the imports before deleting the dependency.

`front/package.json` now depends on `boardgame-web-common` via `file:../../core/common`.

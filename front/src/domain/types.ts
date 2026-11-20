import type { CarcassonneEdgeType } from "./generated/types.js";

export * from "./generated/types.js";

export type CarcassonneFieldSide = 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7;

/**
 * Front-only tile design. The rules engine keeps the same table in Kotlin; edits must be mirrored
 * in `back/src/main/kotlin/boardgame/carcassonne/Tiles.kt`.
 */
export interface CarcassonneTileTemplate {
    id: string;
    edges: [CarcassonneEdgeType, CarcassonneEdgeType, CarcassonneEdgeType, CarcassonneEdgeType];
    cityGroups: number[][];
    cityPennants?: number[];
    roadGroups: number[][];
    fieldSides: CarcassonneFieldSide[][];
    cloister?: boolean;
    count: number;
    starting?: boolean;
}

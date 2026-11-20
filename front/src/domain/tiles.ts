import type {
    CarcassonneEdgeType,
    CarcassonneFeatureType,
    CarcassonneFieldSide,
    CarcassonneFollowerType,
    CarcassonneTile,
    CarcassonneTileTemplate
} from "./types.js";
import { getTileTemplate } from "./templates.js";

export const edgeDirections = [
    { x: 0, y: -1 },
    { x: 1, y: 0 },
    { x: 0, y: 1 },
    { x: -1, y: 0 }
] as const;

export const surroundingOffsets = [
    { x: -1, y: -1 }, { x: 0, y: -1 }, { x: 1, y: -1 },
    { x: -1, y: 0 }, { x: 1, y: 0 },
    { x: -1, y: 1 }, { x: 0, y: 1 }, { x: 1, y: 1 }
] as const;

export const fieldSidesByEdge: [CarcassonneFieldSide, CarcassonneFieldSide][] = [
    [0, 1],
    [2, 3],
    [4, 5],
    [6, 7]
];

export const oppositeFieldSide: CarcassonneFieldSide[] = [5, 4, 7, 6, 1, 0, 3, 2];

export const followerTypeByFeature: Record<CarcassonneFeatureType, CarcassonneFollowerType> = {
    city: 'knight',
    road: 'thief',
    field: 'farmer',
    cloister: 'monk'
};

export function getPlacedEdgeType(
    template: CarcassonneTileTemplate,
    rotation: number,
    direction: number
): CarcassonneEdgeType {
    return template.edges[(direction - rotation + 4) % 4]!;
}

export function getPlacedFieldSide(side: CarcassonneFieldSide, rotation: number): CarcassonneFieldSide {
    return ((side + rotation * 2) % 8) as CarcassonneFieldSide;
}

export function getOriginalFieldSide(side: CarcassonneFieldSide, rotation: number): CarcassonneFieldSide {
    return ((side - rotation * 2 + 8) % 8) as CarcassonneFieldSide;
}

export function getOriginalEdgeIndex(direction: number, rotation: number): number {
    return (direction - rotation + 4) % 4;
}

export function getPlacedEdgeDirection(originalEdgeIndex: number, rotation: number): number {
    return (originalEdgeIndex + rotation) % 4;
}

export function getOppositeDirection(direction: number): number {
    return (direction + 2) % 4;
}

export function positionKey(x: number, y: number): string {
    return `${x},${y}`;
}

export function getTilesByPosition(board: CarcassonneTile[]): Map<string, CarcassonneTile> {
    return new Map(board.map(tile => [positionKey(tile.x, tile.y), tile]));
}

export function canPlaceTile(
    board: CarcassonneTile[],
    templateId: string,
    x: number,
    y: number,
    rotation: number
): boolean {
    const template = getTileTemplate(templateId);
    if (!template) {
        return false;
    }
    if (!Number.isInteger(x) || !Number.isInteger(y) || !Number.isInteger(rotation) || rotation < 0 || rotation > 3) {
        return false;
    }

    const tilesByPosition = getTilesByPosition(board);
    if (tilesByPosition.has(positionKey(x, y))) {
        return false;
    }

    let hasNeighbor = false;

    for (let direction = 0; direction < edgeDirections.length; direction++) {
        const offset = edgeDirections[direction]!;
        const neighbor = tilesByPosition.get(positionKey(x + offset.x, y + offset.y));
        if (!neighbor) {
            continue;
        }
        hasNeighbor = true;

        const neighborTemplate = getTileTemplate(neighbor.templateId);
        if (!neighborTemplate) {
            return false;
        }

        const ownEdge = getPlacedEdgeType(template, rotation, direction);
        const neighborEdge = getPlacedEdgeType(neighborTemplate, neighbor.rotation, getOppositeDirection(direction));
        if (ownEdge !== neighborEdge) {
            return false;
        }
    }

    return hasNeighbor;
}

export function getEmptyAdjacentCells(board: CarcassonneTile[]): { x: number, y: number }[] {
    const occupied = new Set(board.map(tile => positionKey(tile.x, tile.y)));
    const cells = new Map<string, { x: number, y: number }>();

    for (const tile of board) {
        for (const offset of edgeDirections) {
            const x = tile.x + offset.x;
            const y = tile.y + offset.y;
            const key = positionKey(x, y);
            if (occupied.has(key)) {
                continue;
            }
            cells.set(key, { x, y });
        }
    }

    return [...cells.values()];
}

export function hasLegalPlacement(board: CarcassonneTile[], templateId: string): boolean {
    for (const cell of getEmptyAdjacentCells(board)) {
        for (let rotation = 0; rotation < 4; rotation++) {
            if (canPlaceTile(board, templateId, cell.x, cell.y, rotation)) {
                return true;
            }
        }
    }
    return false;
}

export function getFeatureGroupsForTemplate(
    template: CarcassonneTileTemplate,
    featureType: CarcassonneFeatureType
): number[][] {
    if (featureType === 'city') {
        return template.cityGroups;
    }
    if (featureType === 'road') {
        return template.roadGroups;
    }
    if (featureType === 'field') {
        return template.fieldSides;
    }
    return template.cloister ? [[0]] : [];
}

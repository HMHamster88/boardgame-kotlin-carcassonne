import type { CarcassonneTileTemplate } from "./types.js";

// Mirrors back/src/main/kotlin/boardgame/carcassonne/Tiles.kt; keep both tables in sync.
export const tileTemplates: CarcassonneTileTemplate[] = [
    {
        id: 'starting-tile',
        edges: ['city', 'road', 'field', 'road'],
        cityGroups: [[0]],
        roadGroups: [[1, 3]],
        fieldSides: [[2, 7], [3, 4, 5, 6]],
        count: 1,
        starting: true
    },
    {
        id: 'city-cap',
        edges: ['city', 'field', 'field', 'field'],
        cityGroups: [[0]],
        roadGroups: [],
        fieldSides: [[2, 3, 4, 5, 6, 7]],
        count: 5
    },
    {
        id: 'city-cap-road-through',
        edges: ['city', 'road', 'field', 'road'],
        cityGroups: [[0]],
        roadGroups: [[1, 3]],
        fieldSides: [[2, 7], [3, 4, 5, 6]],
        count: 3
    },
    {
        id: 'city-cap-road-curve',
        edges: ['city', 'field', 'road', 'road'],
        cityGroups: [[0]],
        roadGroups: [[2, 3]],
        fieldSides: [[5, 6], [2, 3, 4, 7]],
        count: 3
    },
    {
        id: 'city-cap-road-curve-east',
        edges: ['city', 'road', 'road', 'field'],
        cityGroups: [[0]],
        roadGroups: [[1, 2]],
        fieldSides: [[3, 4], [2, 5, 6, 7]],
        count: 3
    },
    {
        id: 'city-cap-road-t',
        edges: ['city', 'road', 'road', 'road'],
        cityGroups: [[0]],
        roadGroups: [[1], [2], [3]],
        fieldSides: [[3, 4], [5, 6], [2, 7]],
        count: 3
    },
    {
        id: 'city-two-adjacent',
        edges: ['city', 'city', 'field', 'field'],
        cityGroups: [[0, 1]],
        roadGroups: [],
        fieldSides: [[4, 5, 6, 7]],
        count: 3
    },
    {
        id: 'city-two-adjacent-pennant',
        edges: ['city', 'city', 'field', 'field'],
        cityGroups: [[0, 1]],
        cityPennants: [1],
        roadGroups: [],
        fieldSides: [[4, 5, 6, 7]],
        count: 2
    },
    {
        id: 'city-two-adjacent-road',
        edges: ['city', 'city', 'road', 'road'],
        cityGroups: [[0, 1]],
        roadGroups: [[2, 3]],
        fieldSides: [[5, 6], [4, 7]],
        count: 3
    },
    {
        id: 'city-two-adjacent-road-pennant',
        edges: ['city', 'city', 'road', 'road'],
        cityGroups: [[0, 1]],
        cityPennants: [1],
        roadGroups: [[2, 3]],
        fieldSides: [[5, 6], [4, 7]],
        count: 2
    },
    {
        id: 'two-cities-adjacent',
        edges: ['city', 'city', 'field', 'field'],
        cityGroups: [[0], [1]],
        roadGroups: [],
        fieldSides: [[4, 5, 6, 7]],
        count: 2
    },
    {
        id: 'two-cities-opposite',
        edges: ['city', 'field', 'city', 'field'],
        cityGroups: [[0], [2]],
        roadGroups: [],
        fieldSides: [[2, 3], [6, 7]],
        count: 3
    },
    {
        id: 'city-opposite-connected',
        edges: ['city', 'field', 'city', 'field'],
        cityGroups: [[0, 2]],
        roadGroups: [],
        fieldSides: [[2, 3], [6, 7]],
        count: 1
    },
    {
        id: 'city-opposite-connected-pennant',
        edges: ['city', 'field', 'city', 'field'],
        cityGroups: [[0, 2]],
        cityPennants: [1],
        roadGroups: [],
        fieldSides: [[2, 3], [6, 7]],
        count: 2
    },
    {
        id: 'city-three',
        edges: ['city', 'city', 'field', 'city'],
        cityGroups: [[0, 1, 3]],
        roadGroups: [],
        fieldSides: [[4, 5]],
        count: 3
    },
    {
        id: 'city-three-pennant',
        edges: ['city', 'city', 'field', 'city'],
        cityGroups: [[0, 1, 3]],
        cityPennants: [1],
        roadGroups: [],
        fieldSides: [[4, 5]],
        count: 1
    },
    {
        id: 'city-three-road',
        edges: ['city', 'city', 'road', 'city'],
        cityGroups: [[0, 1, 3]],
        roadGroups: [[2]],
        fieldSides: [[4], [5]],
        count: 1
    },
    {
        id: 'city-three-road-pennant',
        edges: ['city', 'city', 'road', 'city'],
        cityGroups: [[0, 1, 3]],
        cityPennants: [1],
        roadGroups: [[2]],
        fieldSides: [[4], [5]],
        count: 2
    },
    {
        id: 'city-four',
        edges: ['city', 'city', 'city', 'city'],
        cityGroups: [[0, 1, 2, 3]],
        cityPennants: [1],
        roadGroups: [],
        fieldSides: [],
        count: 1
    },
    {
        id: 'cloister',
        edges: ['field', 'field', 'field', 'field'],
        cityGroups: [],
        roadGroups: [],
        fieldSides: [[0, 1, 2, 3, 4, 5, 6, 7]],
        cloister: true,
        count: 4
    },
    {
        id: 'cloister-road',
        edges: ['road', 'field', 'field', 'field'],
        cityGroups: [],
        roadGroups: [[0]],
        fieldSides: [[0, 1, 2, 3, 4, 5, 6, 7]],
        cloister: true,
        count: 2
    },
    {
        id: 'road-straight',
        edges: ['road', 'field', 'road', 'field'],
        cityGroups: [],
        roadGroups: [[0, 2]],
        fieldSides: [[1, 2, 3, 4], [0, 5, 6, 7]],
        count: 8
    },
    {
        id: 'road-curve',
        edges: ['road', 'field', 'field', 'road'],
        cityGroups: [],
        roadGroups: [[0, 3]],
        fieldSides: [[1, 2, 3, 4, 5, 6], [0, 7]],
        count: 9
    },
    {
        id: 'road-t',
        edges: ['road', 'road', 'field', 'road'],
        cityGroups: [],
        roadGroups: [[0], [1], [3]],
        fieldSides: [[3, 4, 5, 6], [0, 7], [1, 2]],
        count: 4
    },
    {
        id: 'road-cross',
        edges: ['road', 'road', 'road', 'road'],
        cityGroups: [],
        roadGroups: [[0], [1], [2], [3]],
        fieldSides: [[1, 2], [3, 4], [5, 6], [0, 7]],
        count: 1
    }
];

const templatesById = new Map(tileTemplates.map(template => [template.id, template]));

export function getTileTemplate(templateId: string): CarcassonneTileTemplate | undefined {
    return templatesById.get(templateId);
}
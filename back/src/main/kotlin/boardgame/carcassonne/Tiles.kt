package boardgame.carcassonne

data class TileOffset(val x: Int, val y: Int)

/**
 * Static tile design. Edges are ordered north, east, south, west; field sides are ordered clockwise:
 * 0/1 = north edge (west/east half), 2/3 = east edge (north/south half), 4/5 = south edge
 * (east/west half), 6/7 = west edge (south/north half).
 */
data class CarcassonneTileTemplate(
    val id: String,
    val edges: List<CarcassonneEdgeType>,
    val cityGroups: List<List<Int>> = emptyList(),
    val cityPennants: List<Int>? = null,
    val roadGroups: List<List<Int>> = emptyList(),
    val fieldSides: List<List<Int>> = emptyList(),
    val cloister: Boolean = false,
    val count: Int = 1,
    val starting: Boolean = false
)

val edgeDirections: List<TileOffset> = listOf(
    TileOffset(0, -1),
    TileOffset(1, 0),
    TileOffset(0, 1),
    TileOffset(-1, 0)
)

val surroundingOffsets: List<TileOffset> = listOf(
    TileOffset(-1, -1), TileOffset(0, -1), TileOffset(1, -1),
    TileOffset(-1, 0), TileOffset(1, 0),
    TileOffset(-1, 1), TileOffset(0, 1), TileOffset(1, 1)
)

val fieldSidesByEdge: List<List<Int>> = listOf(
    listOf(0, 1),
    listOf(2, 3),
    listOf(4, 5),
    listOf(6, 7)
)

val oppositeFieldSide: List<Int> = listOf(5, 4, 7, 6, 1, 0, 3, 2)

val followerTypeByFeature: Map<CarcassonneFeatureType, CarcassonneFollowerType> = mapOf(
    CarcassonneFeatureType.city to CarcassonneFollowerType.knight,
    CarcassonneFeatureType.road to CarcassonneFollowerType.thief,
    CarcassonneFeatureType.field to CarcassonneFollowerType.farmer,
    CarcassonneFeatureType.cloister to CarcassonneFollowerType.monk
)

/**
 * Base game tile set - 24 designs, 72 tiles, following the official Carcassonne distribution (the
 * starting tile is one of the four "city + straight road" tiles).
 */
val tileTemplates: List<CarcassonneTileTemplate> = listOf(
    CarcassonneTileTemplate(
        id = "starting-tile",
        edges = listOf(
            CarcassonneEdgeType.city,
            CarcassonneEdgeType.road,
            CarcassonneEdgeType.field,
            CarcassonneEdgeType.road
        ),
        cityGroups = listOf(listOf(0)),
        roadGroups = listOf(listOf(1, 3)),
        fieldSides = listOf(listOf(2, 7), listOf(3, 4, 5, 6)),
        count = 1,
        starting = true
    ),
    CarcassonneTileTemplate(
        id = "city-cap",
        edges = listOf(
            CarcassonneEdgeType.city,
            CarcassonneEdgeType.field,
            CarcassonneEdgeType.field,
            CarcassonneEdgeType.field
        ),
        cityGroups = listOf(listOf(0)),
        fieldSides = listOf(listOf(2, 3, 4, 5, 6, 7)),
        count = 5
    ),
    CarcassonneTileTemplate(
        id = "city-cap-road-through",
        edges = listOf(
            CarcassonneEdgeType.city,
            CarcassonneEdgeType.road,
            CarcassonneEdgeType.field,
            CarcassonneEdgeType.road
        ),
        cityGroups = listOf(listOf(0)),
        roadGroups = listOf(listOf(1, 3)),
        fieldSides = listOf(listOf(2, 7), listOf(3, 4, 5, 6)),
        count = 3
    ),
    CarcassonneTileTemplate(
        id = "city-cap-road-curve",
        edges = listOf(
            CarcassonneEdgeType.city,
            CarcassonneEdgeType.field,
            CarcassonneEdgeType.road,
            CarcassonneEdgeType.road
        ),
        cityGroups = listOf(listOf(0)),
        roadGroups = listOf(listOf(2, 3)),
        fieldSides = listOf(listOf(5, 6), listOf(2, 3, 4, 7)),
        count = 3
    ),
    CarcassonneTileTemplate(
        id = "city-cap-road-curve-east",
        edges = listOf(
            CarcassonneEdgeType.city,
            CarcassonneEdgeType.road,
            CarcassonneEdgeType.road,
            CarcassonneEdgeType.field
        ),
        cityGroups = listOf(listOf(0)),
        roadGroups = listOf(listOf(1, 2)),
        fieldSides = listOf(listOf(3, 4), listOf(2, 5, 6, 7)),
        count = 3
    ),
    CarcassonneTileTemplate(
        id = "city-cap-road-t",
        edges = listOf(
            CarcassonneEdgeType.city,
            CarcassonneEdgeType.road,
            CarcassonneEdgeType.road,
            CarcassonneEdgeType.road
        ),
        cityGroups = listOf(listOf(0)),
        roadGroups = listOf(listOf(1), listOf(2), listOf(3)),
        fieldSides = listOf(listOf(3, 4), listOf(5, 6), listOf(2, 7)),
        count = 3
    ),
    CarcassonneTileTemplate(
        id = "city-two-adjacent",
        edges = listOf(
            CarcassonneEdgeType.city,
            CarcassonneEdgeType.city,
            CarcassonneEdgeType.field,
            CarcassonneEdgeType.field
        ),
        cityGroups = listOf(listOf(0, 1)),
        fieldSides = listOf(listOf(4, 5, 6, 7)),
        count = 3
    ),
    CarcassonneTileTemplate(
        id = "city-two-adjacent-pennant",
        edges = listOf(
            CarcassonneEdgeType.city,
            CarcassonneEdgeType.city,
            CarcassonneEdgeType.field,
            CarcassonneEdgeType.field
        ),
        cityGroups = listOf(listOf(0, 1)),
        cityPennants = listOf(1),
        fieldSides = listOf(listOf(4, 5, 6, 7)),
        count = 2
    ),
    CarcassonneTileTemplate(
        id = "city-two-adjacent-road",
        edges = listOf(
            CarcassonneEdgeType.city,
            CarcassonneEdgeType.city,
            CarcassonneEdgeType.road,
            CarcassonneEdgeType.road
        ),
        cityGroups = listOf(listOf(0, 1)),
        roadGroups = listOf(listOf(2, 3)),
        fieldSides = listOf(listOf(5, 6), listOf(4, 7)),
        count = 3
    ),
    CarcassonneTileTemplate(
        id = "city-two-adjacent-road-pennant",
        edges = listOf(
            CarcassonneEdgeType.city,
            CarcassonneEdgeType.city,
            CarcassonneEdgeType.road,
            CarcassonneEdgeType.road
        ),
        cityGroups = listOf(listOf(0, 1)),
        cityPennants = listOf(1),
        roadGroups = listOf(listOf(2, 3)),
        fieldSides = listOf(listOf(5, 6), listOf(4, 7)),
        count = 2
    ),
    CarcassonneTileTemplate(
        id = "two-cities-adjacent",
        edges = listOf(
            CarcassonneEdgeType.city,
            CarcassonneEdgeType.city,
            CarcassonneEdgeType.field,
            CarcassonneEdgeType.field
        ),
        cityGroups = listOf(listOf(0), listOf(1)),
        fieldSides = listOf(listOf(4, 5, 6, 7)),
        count = 2
    ),
    CarcassonneTileTemplate(
        id = "two-cities-opposite",
        edges = listOf(
            CarcassonneEdgeType.city,
            CarcassonneEdgeType.field,
            CarcassonneEdgeType.city,
            CarcassonneEdgeType.field
        ),
        cityGroups = listOf(listOf(0), listOf(2)),
        fieldSides = listOf(listOf(2, 3), listOf(6, 7)),
        count = 3
    ),
    CarcassonneTileTemplate(
        id = "city-opposite-connected",
        edges = listOf(
            CarcassonneEdgeType.city,
            CarcassonneEdgeType.field,
            CarcassonneEdgeType.city,
            CarcassonneEdgeType.field
        ),
        cityGroups = listOf(listOf(0, 2)),
        fieldSides = listOf(listOf(2, 3), listOf(6, 7)),
        count = 1
    ),
    CarcassonneTileTemplate(
        id = "city-opposite-connected-pennant",
        edges = listOf(
            CarcassonneEdgeType.city,
            CarcassonneEdgeType.field,
            CarcassonneEdgeType.city,
            CarcassonneEdgeType.field
        ),
        cityGroups = listOf(listOf(0, 2)),
        cityPennants = listOf(1),
        fieldSides = listOf(listOf(2, 3), listOf(6, 7)),
        count = 2
    ),
    CarcassonneTileTemplate(
        id = "city-three",
        edges = listOf(
            CarcassonneEdgeType.city,
            CarcassonneEdgeType.city,
            CarcassonneEdgeType.field,
            CarcassonneEdgeType.city
        ),
        cityGroups = listOf(listOf(0, 1, 3)),
        fieldSides = listOf(listOf(4, 5)),
        count = 3
    ),
    CarcassonneTileTemplate(
        id = "city-three-pennant",
        edges = listOf(
            CarcassonneEdgeType.city,
            CarcassonneEdgeType.city,
            CarcassonneEdgeType.field,
            CarcassonneEdgeType.city
        ),
        cityGroups = listOf(listOf(0, 1, 3)),
        cityPennants = listOf(1),
        fieldSides = listOf(listOf(4, 5)),
        count = 1
    ),
    CarcassonneTileTemplate(
        id = "city-three-road",
        edges = listOf(
            CarcassonneEdgeType.city,
            CarcassonneEdgeType.city,
            CarcassonneEdgeType.road,
            CarcassonneEdgeType.city
        ),
        cityGroups = listOf(listOf(0, 1, 3)),
        roadGroups = listOf(listOf(2)),
        fieldSides = listOf(listOf(4), listOf(5)),
        count = 1
    ),
    CarcassonneTileTemplate(
        id = "city-three-road-pennant",
        edges = listOf(
            CarcassonneEdgeType.city,
            CarcassonneEdgeType.city,
            CarcassonneEdgeType.road,
            CarcassonneEdgeType.city
        ),
        cityGroups = listOf(listOf(0, 1, 3)),
        cityPennants = listOf(1),
        roadGroups = listOf(listOf(2)),
        fieldSides = listOf(listOf(4), listOf(5)),
        count = 2
    ),
    CarcassonneTileTemplate(
        id = "city-four",
        edges = listOf(
            CarcassonneEdgeType.city,
            CarcassonneEdgeType.city,
            CarcassonneEdgeType.city,
            CarcassonneEdgeType.city
        ),
        cityGroups = listOf(listOf(0, 1, 2, 3)),
        cityPennants = listOf(1),
        count = 1
    ),
    CarcassonneTileTemplate(
        id = "cloister",
        edges = listOf(
            CarcassonneEdgeType.field,
            CarcassonneEdgeType.field,
            CarcassonneEdgeType.field,
            CarcassonneEdgeType.field
        ),
        fieldSides = listOf(listOf(0, 1, 2, 3, 4, 5, 6, 7)),
        cloister = true,
        count = 4
    ),
    CarcassonneTileTemplate(
        id = "cloister-road",
        edges = listOf(
            CarcassonneEdgeType.road,
            CarcassonneEdgeType.field,
            CarcassonneEdgeType.field,
            CarcassonneEdgeType.field
        ),
        roadGroups = listOf(listOf(0)),
        fieldSides = listOf(listOf(0, 1, 2, 3, 4, 5, 6, 7)),
        cloister = true,
        count = 2
    ),
    CarcassonneTileTemplate(
        id = "road-straight",
        edges = listOf(
            CarcassonneEdgeType.road,
            CarcassonneEdgeType.field,
            CarcassonneEdgeType.road,
            CarcassonneEdgeType.field
        ),
        roadGroups = listOf(listOf(0, 2)),
        fieldSides = listOf(listOf(1, 2, 3, 4), listOf(0, 5, 6, 7)),
        count = 8
    ),
    CarcassonneTileTemplate(
        id = "road-curve",
        edges = listOf(
            CarcassonneEdgeType.road,
            CarcassonneEdgeType.field,
            CarcassonneEdgeType.field,
            CarcassonneEdgeType.road
        ),
        roadGroups = listOf(listOf(0, 3)),
        fieldSides = listOf(listOf(1, 2, 3, 4, 5, 6), listOf(0, 7)),
        count = 9
    ),
    CarcassonneTileTemplate(
        id = "road-t",
        edges = listOf(
            CarcassonneEdgeType.road,
            CarcassonneEdgeType.road,
            CarcassonneEdgeType.field,
            CarcassonneEdgeType.road
        ),
        roadGroups = listOf(listOf(0), listOf(1), listOf(3)),
        fieldSides = listOf(listOf(3, 4, 5, 6), listOf(0, 7), listOf(1, 2)),
        count = 4
    ),
    CarcassonneTileTemplate(
        id = "road-cross",
        edges = listOf(
            CarcassonneEdgeType.road,
            CarcassonneEdgeType.road,
            CarcassonneEdgeType.road,
            CarcassonneEdgeType.road
        ),
        roadGroups = listOf(listOf(0), listOf(1), listOf(2), listOf(3)),
        fieldSides = listOf(listOf(1, 2), listOf(3, 4), listOf(5, 6), listOf(0, 7)),
        count = 1
    )
)

private val templatesById: Map<String, CarcassonneTileTemplate> =
    tileTemplates.associateBy { it.id }

fun getTileTemplate(templateId: String): CarcassonneTileTemplate? = templatesById[templateId]

fun getStartingTileTemplate(): CarcassonneTileTemplate =
    tileTemplates.firstOrNull { it.starting } ?: error("Starting tile template is missing")

fun createTileDeck(): List<String> = buildList {
    tileTemplates.forEach { template ->
        if (!template.starting) repeat(template.count) { add(template.id) }
    }
}

fun getPlacedEdgeType(
    template: CarcassonneTileTemplate,
    rotation: Int,
    direction: Int
): CarcassonneEdgeType = template.edges[(direction - rotation + 4) % 4]

fun getPlacedFieldSide(side: Int, rotation: Int): Int = (side + rotation * 2) % 8

fun getOriginalFieldSide(side: Int, rotation: Int): Int = (side - rotation * 2 + 8) % 8

fun getOriginalEdgeIndex(direction: Int, rotation: Int): Int = (direction - rotation + 4) % 4

fun getPlacedEdgeDirection(originalEdgeIndex: Int, rotation: Int): Int =
    (originalEdgeIndex + rotation) % 4

fun getOppositeDirection(direction: Int): Int = (direction + 2) % 4

fun positionKey(x: Int, y: Int): String = "$x,$y"

fun getTilesByPosition(board: List<CarcassonneTile>): Map<String, CarcassonneTile> =
    board.associateBy { positionKey(it.x, it.y) }

fun canPlaceTile(
    board: List<CarcassonneTile>,
    templateId: String,
    x: Int,
    y: Int,
    rotation: Int
): Boolean {
    val template = getTileTemplate(templateId) ?: return false
    if (rotation < 0 || rotation > 3) return false

    val tilesByPosition = getTilesByPosition(board)
    if (tilesByPosition.containsKey(positionKey(x, y))) return false

    var hasNeighbor = false
    edgeDirections.forEachIndexed { direction, offset ->
        val neighbor = tilesByPosition[positionKey(x + offset.x, y + offset.y)] ?: return@forEachIndexed
        hasNeighbor = true

        val neighborTemplate = getTileTemplate(neighbor.templateId) ?: return false
        val ownEdge = getPlacedEdgeType(template, rotation, direction)
        val neighborEdge =
            getPlacedEdgeType(neighborTemplate, neighbor.rotation, getOppositeDirection(direction))
        if (ownEdge != neighborEdge) return false
    }

    return hasNeighbor
}

fun getEmptyAdjacentCells(board: List<CarcassonneTile>): List<TileOffset> {
    val occupied = board.mapTo(mutableSetOf()) { positionKey(it.x, it.y) }
    val cells = mutableMapOf<String, TileOffset>()

    board.forEach { tile ->
        edgeDirections.forEach { offset ->
            val x = tile.x + offset.x
            val y = tile.y + offset.y
            val key = positionKey(x, y)
            if (!occupied.contains(key)) cells[key] = TileOffset(x, y)
        }
    }

    return cells.values.toList()
}

fun hasLegalPlacement(board: List<CarcassonneTile>, templateId: String): Boolean {
    for (cell in getEmptyAdjacentCells(board)) {
        for (rotation in 0 until 4) {
            if (canPlaceTile(board, templateId, cell.x, cell.y, rotation)) return true
        }
    }
    return false
}

fun getFeatureGroupsForTemplate(
    template: CarcassonneTileTemplate,
    featureType: CarcassonneFeatureType
): List<List<Int>> = when (featureType) {
    CarcassonneFeatureType.city -> template.cityGroups
    CarcassonneFeatureType.road -> template.roadGroups
    CarcassonneFeatureType.field -> template.fieldSides
    CarcassonneFeatureType.cloister -> if (template.cloister) listOf(listOf(0)) else emptyList()
}

fun validateTileTemplates(): List<String> {
    val errors = mutableListOf<String>()
    val seenIds = mutableSetOf<String>()

    tileTemplates.forEach { template ->
        if (!seenIds.add(template.id)) errors += "Duplicate tile id: ${template.id}"
        if (template.count < 1) errors += "Tile ${template.id} must have a positive count"

        val coverage = mapOf(
            CarcassonneEdgeType.city to mutableListOf<Int>(),
            CarcassonneEdgeType.road to mutableListOf<Int>()
        )
        listOf(
            CarcassonneEdgeType.city to template.cityGroups,
            CarcassonneEdgeType.road to template.roadGroups
        ).forEach { (type, groups) ->
            groups.forEach { group ->
                group.forEach { edge ->
                    if (edge < 0 || edge > 3) {
                        errors += "Tile ${template.id} has invalid $type edge $edge"
                    } else {
                        coverage.getValue(type).add(edge)
                    }
                }
            }
        }

        listOf(CarcassonneEdgeType.city, CarcassonneEdgeType.road).forEach { type ->
            val expectedEdges = template.edges.mapIndexedNotNull { index, edge ->
                if (edge == type) index else null
            }
            val actualEdges = coverage.getValue(type).sorted()
            if (expectedEdges.joinToString(",") != actualEdges.joinToString(",")) {
                errors += "Tile ${template.id} has mismatched $type edges"
            }
        }

        val fieldSideCounts = mutableMapOf<Int, Int>()
        template.fieldSides.forEachIndexed { groupIndex, group ->
            if (group.isEmpty()) errors += "Tile ${template.id} has an empty field group $groupIndex"
            group.forEach { side ->
                if (side < 0 || side > 7) {
                    errors += "Tile ${template.id} has invalid field side $side"
                } else {
                    fieldSideCounts[side] = (fieldSideCounts[side] ?: 0) + 1
                }
            }
        }

        val expectedFieldSides = mutableSetOf<Int>()
        template.edges.forEachIndexed { edgeIndex, edgeType ->
            if (edgeType != CarcassonneEdgeType.city) {
                expectedFieldSides.addAll(fieldSidesByEdge[edgeIndex])
            }
        }

        expectedFieldSides.forEach { side ->
            val count = fieldSideCounts[side] ?: 0
            if (count != 1) {
                errors += "Tile ${template.id} field side $side must be covered exactly once (found $count)"
            }
        }
        fieldSideCounts.keys.forEach { side ->
            if (!expectedFieldSides.contains(side)) {
                errors += "Tile ${template.id} field side $side lies on a city edge"
            }
        }

        template.edges.forEachIndexed { edgeIndex, edgeType ->
            if (edgeType == CarcassonneEdgeType.field) {
                val (firstSide, secondSide) = fieldSidesByEdge[edgeIndex]
                val firstGroup = template.fieldSides.indexOfFirst { it.contains(firstSide) }
                val secondGroup = template.fieldSides.indexOfFirst { it.contains(secondSide) }
                if (firstGroup < 0 || secondGroup < 0 || firstGroup != secondGroup) {
                    errors += "Tile ${template.id} field edge $edgeIndex must belong to a single field group"
                }
            }
        }

        val pennants = template.cityPennants
        if (pennants != null && pennants.size != template.cityGroups.size) {
            errors += "Tile ${template.id} has mismatched city pennants"
        }
    }

    return errors
}

package boardgame.carcassonne

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TilesTest {

    private fun placedTile(
        id: String,
        templateId: String,
        x: Int,
        y: Int,
        rotation: Int = 0
    ): CarcassonneTile = CarcassonneTile(id = id, templateId = templateId, x = x, y = y, rotation = rotation)

    @Test
    fun tileTemplatesCoverEveryEdgeExactlyOnceAndUseValidCounts() {
        assertEquals(emptyList(), validateTileTemplates())
    }

    @Test
    fun buildASeventyOneTileDeckPlusTheStartingTile() {
        val deck = createTileDeck()
        assertEquals(71, deck.size)
        assertEquals(1, tileTemplates.count { it.starting })
    }

    @Test
    fun defineAPennantCountForEveryCityGroupWhenPennantsAreUsed() {
        tileTemplates.forEach { template ->
            val pennants = template.cityPennants
            if (pennants != null) {
                assertEquals(template.cityGroups.size, pennants.size)
            }
        }
    }

    @Test
    fun splitFieldsAtRoadsAndCities() {
        fun fieldGroupCount(templateId: String): Int {
            val template = getTileTemplate(templateId)
            assertNotNull(template)
            return getFeatureGroupsForTemplate(template, CarcassonneFeatureType.field).size
        }

        assertEquals(2, fieldGroupCount("road-straight"))
        assertEquals(2, fieldGroupCount("road-curve"))
        assertEquals(3, fieldGroupCount("road-t"))
        assertEquals(4, fieldGroupCount("road-cross"))
        assertEquals(1, fieldGroupCount("cloister-road"))
        assertEquals(2, fieldGroupCount("city-three-road"))
        assertEquals(3, fieldGroupCount("city-cap-road-t"))
        assertEquals(0, fieldGroupCount("city-four"))
    }

    @Test
    fun matchTheOfficialBaseGameDistribution() {
        val total = tileTemplates.sumOf { it.count }
        assertEquals(72, total)
        assertEquals(25, tileTemplates.size)

        // The starting tile is one of the four "city cap + straight road" tiles, so it is kept as
        // its own template and the remaining three are in city-cap-road-through.
        val startTiles = tileTemplates.filter { it.starting }.sumOf { it.count }
        val designCounts = tileTemplates
            .filterNot { it.starting }
            .map { it.count }
            .sorted()

        assertEquals(1, startTiles)
        assertEquals(
            listOf(1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 5, 8, 9),
            designCounts
        )
    }

    @Test
    fun rotatesEdgesClockwise() {
        val template = getTileTemplate("starting-tile")!!

        assertEquals(CarcassonneEdgeType.city, getPlacedEdgeType(template, 0, 0))
        assertEquals(CarcassonneEdgeType.road, getPlacedEdgeType(template, 0, 1))
        assertEquals(CarcassonneEdgeType.field, getPlacedEdgeType(template, 0, 2))
        assertEquals(CarcassonneEdgeType.road, getPlacedEdgeType(template, 1, 0))
        assertEquals(CarcassonneEdgeType.city, getPlacedEdgeType(template, 1, 1))
        assertEquals(CarcassonneEdgeType.road, getPlacedEdgeType(template, 1, 2))
    }

    @Test
    fun acceptsAMatchingRoadEdge() {
        // starting tile: city north, roads east and west, field south
        val board = listOf(placedTile("start", "starting-tile", 0, 0))
        assertTrue(canPlaceTile(board, "road-straight", 1, 0, 1))
    }

    @Test
    fun rejectsAMismatchedCityEdge() {
        val board = listOf(placedTile("start", "starting-tile", 0, 0))
        assertFalse(canPlaceTile(board, "city-cap", 0, 1, 0))
    }

    @Test
    fun acceptsARotatedCityEdge() {
        val board = listOf(placedTile("start", "starting-tile", 0, 0))
        assertTrue(canPlaceTile(board, "city-cap", 0, -1, 2))
    }

    @Test
    fun rejectsTilesThatOnlyTouchDiagonally() {
        val board = listOf(placedTile("start", "starting-tile", 0, 0))
        assertFalse(canPlaceTile(board, "cloister", 1, 1, 0))
    }

    @Test
    fun findsAtLeastOneLegalPlacementForEveryTemplate() {
        val board = listOf(placedTile("start", "starting-tile", 0, 0))
        tileTemplates.forEach { template ->
            if (!template.starting) {
                assertTrue(hasLegalPlacement(board, template.id), "No legal placement for ${template.id}")
            }
        }
    }
}

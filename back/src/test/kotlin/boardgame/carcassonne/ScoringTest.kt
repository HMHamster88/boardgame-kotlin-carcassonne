package boardgame.carcassonne

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ScoringTest {

    private fun placedTile(
        id: String,
        templateId: String,
        x: Int,
        y: Int,
        rotation: Int = 0
    ): CarcassonneTile = CarcassonneTile(id = id, templateId = templateId, x = x, y = y, rotation = rotation)

    private fun follower(
        playerId: String,
        type: CarcassonneFollowerType,
        tileId: String,
        featureType: CarcassonneFeatureType,
        segmentIndex: Int
    ): CarcassonneFollower = CarcassonneFollower(
        playerId = playerId,
        type = type,
        tileId = tileId,
        featureType = featureType,
        segmentIndex = segmentIndex
    )

    private fun makePublicState(
        board: List<CarcassonneTile>,
        followers: List<CarcassonneFollower> = emptyList()
    ): CarcassonnePublicGameState = CarcassonnePublicGameState(
        mode = CarcassonneGameMode.base,
        board = board.toMutableList(),
        followers = followers.toMutableList(),
        activePlayerIndex = 0,
        playersStates = mutableListOf(
            CarcassonnePlayerPublicState("p1", 0),
            CarcassonnePlayerPublicState("p2", 0)
        ),
        winnersIds = mutableListOf(),
        statistics = CarcassonneGameStatistics(0),
        tilesRemaining = 0
    )

    private fun makePrivateState(): CarcassonnePrivateGameState = CarcassonnePrivateGameState(
        deck = mutableListOf(),
        playersStates = mutableListOf(
            CarcassonnePlayerPrivateState("p1", 7),
            CarcassonnePlayerPrivateState("p2", 7)
        )
    )

    private fun pointsFor(publicState: CarcassonnePublicGameState, playerId: String): Int =
        publicState.playersStates.firstOrNull { it.playerId == playerId }?.points ?: 0

    private fun supplyFor(privateState: CarcassonnePrivateGameState, playerId: String): Int =
        privateState.playersStates.firstOrNull { it.playerId == playerId }?.followersInSupply ?: 0

    @Test
    fun scoresARoadClosedByACloisterAndACity() {
        val board = listOf(
            placedTile("monastery", "cloister-road", 0, 0),
            placedTile("city", "city-three-road", 0, -1)
        )
        val publicState = makePublicState(
            board,
            listOf(follower("p1", CarcassonneFollowerType.thief, "monastery", CarcassonneFeatureType.road, 0))
        )
        val privateState = makePrivateState()

        scoreCompletedFeatures(privateState, publicState)

        assertEquals(2, pointsFor(publicState, "p1"))
        assertTrue(publicState.followers.isEmpty())
        assertEquals(8, supplyFor(privateState, "p1"))
    }

    @Test
    fun scoresACompletedCityAndEveryPennantInIt() {
        val board = listOf(
            placedTile("city-center", "city-three-pennant", 0, 0),
            placedTile("city-north", "city-cap", 0, -1, 2),
            placedTile("city-east", "city-cap", 1, 0, 3),
            placedTile("city-west", "city-cap", -1, 0, 1)
        )
        val publicState = makePublicState(
            board,
            listOf(follower("p1", CarcassonneFollowerType.knight, "city-center", CarcassonneFeatureType.city, 0))
        )
        val privateState = makePrivateState()

        scoreCompletedFeatures(privateState, publicState)

        assertEquals(10, pointsFor(publicState, "p1"))
        assertTrue(publicState.followers.isEmpty())
        assertEquals(8, supplyFor(privateState, "p1"))
    }

    @Test
    fun awardsTheFullCityScoreToTiedPlayers() {
        val board = listOf(
            placedTile("city-a", "city-cap", 0, 0),
            placedTile("city-b", "city-cap", 0, -1, 2)
        )
        val publicState = makePublicState(
            board,
            listOf(
                follower("p1", CarcassonneFollowerType.knight, "city-a", CarcassonneFeatureType.city, 0),
                follower("p2", CarcassonneFollowerType.knight, "city-b", CarcassonneFeatureType.city, 0)
            )
        )
        val privateState = makePrivateState()

        scoreCompletedFeatures(privateState, publicState)

        assertEquals(4, pointsFor(publicState, "p1"))
        assertEquals(4, pointsFor(publicState, "p2"))
        assertEquals(8, supplyFor(privateState, "p1"))
        assertEquals(8, supplyFor(privateState, "p2"))
    }

    @Test
    fun awardsAllPointsToThePlayerWithTheMostFollowers() {
        val board = listOf(
            placedTile("city-a", "city-cap", 0, 0),
            placedTile("city-b", "city-cap", 0, -1, 2)
        )
        val publicState = makePublicState(
            board,
            listOf(
                follower("p1", CarcassonneFollowerType.knight, "city-a", CarcassonneFeatureType.city, 0),
                follower("p1", CarcassonneFollowerType.knight, "city-b", CarcassonneFeatureType.city, 0),
                follower("p2", CarcassonneFollowerType.knight, "city-b", CarcassonneFeatureType.city, 0)
            )
        )
        val privateState = makePrivateState()

        scoreCompletedFeatures(privateState, publicState)

        assertEquals(4, pointsFor(publicState, "p1"))
        assertEquals(0, pointsFor(publicState, "p2"))
        assertEquals(9, supplyFor(privateState, "p1"))
        assertEquals(8, supplyFor(privateState, "p2"))
    }

    @Test
    fun scoresACompletedCloisterForNinePointsAndReturnsTheMonk() {
        val board = listOf(
            placedTile("cloister--1--1", "cloister", -1, -1),
            placedTile("cloister-0--1", "cloister", 0, -1),
            placedTile("cloister-1--1", "cloister", 1, -1),
            placedTile("cloister--1-0", "cloister", -1, 0),
            placedTile("cloister-0-0", "cloister", 0, 0),
            placedTile("cloister-1-0", "cloister", 1, 0),
            placedTile("cloister--1-1", "cloister", -1, 1),
            placedTile("cloister-0-1", "cloister", 0, 1),
            placedTile("cloister-1-1", "cloister", 1, 1)
        )
        val publicState = makePublicState(
            board,
            listOf(follower("p1", CarcassonneFollowerType.monk, "cloister-0-0", CarcassonneFeatureType.cloister, 0))
        )
        val privateState = makePrivateState()

        scoreCompletedFeatures(privateState, publicState)

        assertEquals(9, pointsFor(publicState, "p1"))
        assertTrue(publicState.followers.isEmpty())
        assertEquals(8, supplyFor(privateState, "p1"))
    }

    @Test
    fun scoresIncompleteRoadsCitiesAndCloisters() {
        val board = listOf(
            placedTile("road", "road-straight", 0, 0),
            placedTile("city", "city-opposite-connected", 5, 0),
            placedTile("cloister", "cloister", 10, 0),
            placedTile("cloister-left", "cloister", 9, 0),
            placedTile("cloister-right", "cloister", 11, 0),
            placedTile("cloister-below", "cloister", 10, 1)
        )
        val publicState = makePublicState(
            board,
            listOf(
                follower("p1", CarcassonneFollowerType.thief, "road", CarcassonneFeatureType.road, 0),
                follower("p1", CarcassonneFollowerType.knight, "city", CarcassonneFeatureType.city, 0),
                follower("p1", CarcassonneFollowerType.monk, "cloister", CarcassonneFeatureType.cloister, 0)
            )
        )

        scoreFinalFeatures(publicState)

        assertEquals(6, pointsFor(publicState, "p1"))
    }

    @Test
    fun countsEveryCitySegmentForAnIncompleteCity() {
        val board = listOf(
            placedTile("city-a", "city-opposite-connected", 0, 0),
            placedTile("city-b", "city-cap", 0, -1, 2)
        )
        val publicState = makePublicState(
            board,
            listOf(follower("p1", CarcassonneFollowerType.knight, "city-a", CarcassonneFeatureType.city, 0))
        )

        scoreFinalFeatures(publicState)

        assertEquals(2, pointsFor(publicState, "p1"))
    }

    @Test
    fun awardsThreePointsForEachFarmSupplyingACompletedCity() {
        val board = listOf(
            placedTile("city-a", "city-cap", 0, 0),
            placedTile("city-b", "city-cap", 0, -1, 2)
        )
        val publicState = makePublicState(
            board,
            listOf(
                follower("p1", CarcassonneFollowerType.farmer, "city-a", CarcassonneFeatureType.field, 0),
                follower("p2", CarcassonneFollowerType.farmer, "city-b", CarcassonneFeatureType.field, 0)
            )
        )

        scoreFinalFeatures(publicState)

        assertEquals(3, pointsFor(publicState, "p1"))
        assertEquals(3, pointsFor(publicState, "p2"))
    }

    @Test
    fun awardsFarmPointsToThePlayerWithTheMostFarmersInAConnectedField() {
        val board = listOf(
            placedTile("city-a", "city-cap", 0, 0),
            placedTile("city-b", "city-cap", 0, -1, 2),
            placedTile("field-a", "cloister", 1, 0),
            placedTile("field-b", "cloister", 2, 0)
        )
        val publicState = makePublicState(
            board,
            listOf(
                follower("p1", CarcassonneFollowerType.farmer, "city-a", CarcassonneFeatureType.field, 0),
                follower("p1", CarcassonneFollowerType.farmer, "field-a", CarcassonneFeatureType.field, 0),
                follower("p2", CarcassonneFollowerType.farmer, "field-b", CarcassonneFeatureType.field, 0)
            )
        )

        scoreFinalFeatures(publicState)

        assertEquals(3, pointsFor(publicState, "p1"))
        assertEquals(0, pointsFor(publicState, "p2"))
    }

    @Test
    fun connectsFieldsOnTheSameSideOfARoadAcrossARoadEdge() {
        val board = listOf(
            placedTile("top", "road-straight", 0, 0),
            placedTile("bottom", "road-straight", 0, 1)
        )
        val graph = buildFeatureGraph(board)

        val topEast = graph.nodeToFeature[featureNodeKey("top", CarcassonneFeatureType.field, 0)]
        val topWest = graph.nodeToFeature[featureNodeKey("top", CarcassonneFeatureType.field, 1)]
        val bottomEast = graph.nodeToFeature[featureNodeKey("bottom", CarcassonneFeatureType.field, 0)]
        val bottomWest = graph.nodeToFeature[featureNodeKey("bottom", CarcassonneFeatureType.field, 1)]

        assertNotNull(topEast)
        assertNotNull(topWest)
        assertNotNull(bottomEast)
        assertNotNull(bottomWest)
        assertEquals(topEast, bottomEast)
        assertEquals(topWest, bottomWest)
        assertNotEquals(topEast, topWest)
    }

    @Test
    fun connectsFieldsCorrectlyWhenAdjacentTilesHaveDifferentRotations() {
        val board = listOf(
            placedTile("bottom", "road-straight", 0, 0, 0),
            placedTile("top", "road-straight", 0, -1, 2)
        )
        val graph = buildFeatureGraph(board)

        val bottomEast = graph.nodeToFeature[featureNodeKey("bottom", CarcassonneFeatureType.field, 0)]
        val bottomWest = graph.nodeToFeature[featureNodeKey("bottom", CarcassonneFeatureType.field, 1)]
        val topEast = graph.nodeToFeature[featureNodeKey("top", CarcassonneFeatureType.field, 0)]
        val topWest = graph.nodeToFeature[featureNodeKey("top", CarcassonneFeatureType.field, 1)]

        assertNotNull(bottomEast)
        assertNotNull(bottomWest)
        assertNotNull(topEast)
        assertNotNull(topWest)
        assertEquals(bottomEast, topWest)
        assertEquals(bottomWest, topEast)
        assertNotEquals(bottomEast, bottomWest)
    }

    @Test
    fun doesNotConnectFieldsThatOnlyTouchAtACorner() {
        val board = listOf(
            placedTile("first", "cloister", 0, 0),
            placedTile("second", "cloister", 1, 1)
        )
        val graph = buildFeatureGraph(board)

        val firstField = graph.nodeToFeature[featureNodeKey("first", CarcassonneFeatureType.field, 0)]
        val secondField = graph.nodeToFeature[featureNodeKey("second", CarcassonneFeatureType.field, 0)]

        assertNotNull(firstField)
        assertNotNull(secondField)
        assertNotEquals(firstField, secondField)
    }

    @Test
    fun returnsEveryPlayerTiedForTheHighestScore() {
        val publicState = makePublicState(emptyList())
        publicState.playersStates = mutableListOf(
            CarcassonnePlayerPublicState("p1", 10),
            CarcassonnePlayerPublicState("p2", 10),
            CarcassonnePlayerPublicState("p3", 4)
        )

        assertEquals(listOf("p1", "p2"), getWinnerIds(publicState))
    }
}

package boardgame.carcassonne

import boardgame.api.Game
import boardgame.api.GameContext
import boardgame.api.GameStatePayload
import boardgame.api.GameStatus
import boardgame.api.Player
import boardgame.api.decodeTree
import boardgame.api.encodeTree
import boardgame.api.nowIso
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CarcassonneServiceTest {

    private val service = CarcassonneGameBackService()

    private data class Placement(val x: Int, val y: Int, val rotation: Int)

    private class FakeContext(
        override val game: Game,
        initialSettings: JsonObject,
        initialPublic: JsonElement,
        initialPrivate: JsonElement?
    ) : GameContext {
        private var settingsValue = initialSettings
        private var publicValue = initialPublic
        private var privateValue = initialPrivate

        override val settings: JsonObject get() = settingsValue
        override val publicState: JsonElement get() = publicValue
        override val privateState: JsonElement? get() = privateValue

        override fun updatePublicState(value: JsonElement) {
            publicValue = value
        }

        override fun updatePrivateState(value: JsonElement?) {
            privateValue = value
        }

        override fun updateSettings(value: JsonElement) {
            settingsValue = value.jsonObject
        }

        override fun notify(peerId: String?, message: String, params: JsonElement?) = Unit
    }

    private fun createGame(): Game = Game(
        id = "game-1",
        owner = "p1",
        name = "Test game",
        type = CARCASSONNE_TYPE,
        players = mutableListOf(
            Player("p1", "Player 1", "#ff0000", online = true, isBot = false),
            Player("p2", "Player 2", "#0000ff", online = true, isBot = false)
        ),
        created = nowIso()
    )

    private fun createContext(game: Game, state: GameStatePayload): FakeContext =
        FakeContext(game, service.getDefaultSettings(), state.publicState, state.privateState)

    private fun publicState(ctx: FakeContext): CarcassonnePublicGameState =
        decodeTree(ctx.publicState)

    private fun privateState(ctx: FakeContext): CarcassonnePrivateGameState =
        decodeTree(ctx.privateState!!)

    private fun findPlacement(board: List<CarcassonneTile>, templateId: String): Placement {
        for (cell in getEmptyAdjacentCells(board)) {
            for (rotation in 0 until 4) {
                if (canPlaceTile(board, templateId, cell.x, cell.y, rotation)) {
                    return Placement(cell.x, cell.y, rotation)
                }
            }
        }
        error("No legal placement for $templateId")
    }

    private var nextTileId = 0

    private fun placeCurrentTile(ctx: FakeContext, playerId: String) = runBlocking {
        val public = publicState(ctx)
        val currentTile = public.currentTile ?: error("No current tile")
        val placement = findPlacement(public.board, currentTile)
        service.performAction(
            ctx,
            encodeTree(
                CarcassonnePlaceLandTileAction(
                    tileId = "tile-${nextTileId++}",
                    x = placement.x,
                    y = placement.y,
                    rotation = placement.rotation
                )
            ),
            playerId
        )
    }

    @Test
    fun startsAGameWithAStartingTileAShuffledDeckAndTheFirstTileInHand() {
        val game = createGame()
        val ctx = createContext(game, service.startGame(game, service.getDefaultSettings()))
        val public = publicState(ctx)
        val private = privateState(ctx)

        assertEquals(GameStatus.STARTED, game.status)
        assertEquals(1, public.board.size)
        assertEquals("starting-tile", public.board[0].templateId)
        assertNotNull(public.currentTile)
        assertEquals(71, public.tilesRemaining)
        assertTrue(public.followers.isEmpty())
        assertEquals(70, private.deck.size)
        assertEquals(listOf(7, 7), private.playersStates.map { it.followersInSupply })
    }

    @Test
    fun rejectsPlacementsFromAPlayerWhoIsNotActive() = runBlocking {
        val game = createGame()
        val ctx = createContext(game, service.startGame(game, service.getDefaultSettings()))
        val public = publicState(ctx)
        val placement = findPlacement(public.board, public.currentTile!!)

        val result = service.performAction(
            ctx,
            encodeTree(
                CarcassonnePlaceLandTileAction(
                    tileId = "wrong-player",
                    x = placement.x,
                    y = placement.y,
                    rotation = placement.rotation
                )
            ),
            "p2"
        )

        assertNull(result)
        assertEquals(1, publicState(ctx).board.size)
    }

    @Test
    fun placesTheCurrentTileAndExposesAPendingTileForFollowerDeployment() {
        val game = createGame()
        val ctx = createContext(game, service.startGame(game, service.getDefaultSettings()))

        placeCurrentTile(ctx, "p1")

        val public = publicState(ctx)
        assertEquals(2, public.board.size)
        assertNull(public.currentTile)
        assertNotNull(public.pendingTileId)
        assertEquals(70, public.tilesRemaining)
    }

    @Test
    fun deploysAFollowerAdvancesTheTurnAndKeepsTheFollowerTotalAtSeven() {
        val game = createGame()
        val ctx = createContext(game, service.startGame(game, service.getDefaultSettings()))

        placeCurrentTile(ctx, "p1")

        val public = publicState(ctx)
        val pendingTile = public.board.first { it.id == public.pendingTileId }
        val template = getTileTemplate(pendingTile.templateId)!!
        val candidates = listOf(
            CarcassonneFeatureType.field,
            CarcassonneFeatureType.city,
            CarcassonneFeatureType.road
        )
        var featureType = candidates.firstOrNull { getFeatureGroupsForTemplate(template, it).isNotEmpty() }
        var segmentIndex = 0
        if (featureType == null) {
            featureType = CarcassonneFeatureType.cloister
            segmentIndex = 0
        }

        runBlocking {
            service.performAction(
                ctx,
                encodeTree(
                    CarcassonneDeployFollowerAction(
                        tileId = pendingTile.id,
                        featureType = featureType,
                        segmentIndex = segmentIndex,
                        followerType = followerTypeByFeature.getValue(featureType)
                    )
                ),
                "p1"
            )
        }

        val after = publicState(ctx)
        assertNull(after.pendingTileId)
        assertNotNull(after.currentTile)
        assertEquals(1, after.activePlayerIndex)
        assertEquals(1, after.statistics.turnCount)

        val playerState = privateState(ctx).playersStates.first { it.playerId == "p1" }
        assertEquals(7, playerState.followersInSupply + after.followers.size)
    }

    @Test
    fun endsATurnWithoutDeployingAFollower() {
        val game = createGame()
        val ctx = createContext(game, service.startGame(game, service.getDefaultSettings()))

        placeCurrentTile(ctx, "p1")
        runBlocking {
            service.performAction(ctx, encodeTree(CarcassonneEndTurnAction()), "p1")
        }

        var public = publicState(ctx)
        assertNull(public.pendingTileId)
        assertNotNull(public.currentTile)
        assertEquals(1, public.activePlayerIndex)
        assertEquals(1, public.statistics.turnCount)

        placeCurrentTile(ctx, "p2")
        runBlocking {
            service.performAction(ctx, encodeTree(CarcassonneEndTurnAction()), "p2")
        }

        public = publicState(ctx)
        assertEquals(0, public.activePlayerIndex)
        assertEquals(2, public.statistics.turnCount)
    }

    @Test
    fun playsThroughTheDeckAndFinishesTheGame() {
        val game = createGame()
        val ctx = createContext(game, service.startGame(game, service.getDefaultSettings()))

        var guard = 0
        while (game.status != GameStatus.FINISHED && guard < 200) {
            val public = publicState(ctx)
            val playerId = game.players[public.activePlayerIndex].userId
            if (public.pendingTileId != null) {
                runBlocking {
                    service.performAction(ctx, encodeTree(CarcassonneEndTurnAction()), playerId)
                }
            } else {
                placeCurrentTile(ctx, playerId)
            }
            guard++
        }

        val public = publicState(ctx)
        assertEquals(GameStatus.FINISHED, game.status)
        assertNull(public.pendingTileId)
        assertNull(public.currentTile)
        assertEquals(0, public.tilesRemaining)
        assertTrue(public.winnersIds.isNotEmpty())
        assertTrue(guard < 200)
    }
}

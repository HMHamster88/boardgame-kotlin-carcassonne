package boardgame.carcassonne

import boardgame.api.BotAction
import boardgame.api.BotGameContext
import boardgame.api.Game
import boardgame.api.GameBackModule
import boardgame.api.GameBackService
import boardgame.api.GameContext
import boardgame.api.GameStatePayload
import boardgame.api.GameStaticSettings
import boardgame.api.GameStatus
import boardgame.api.decodeTree
import boardgame.api.encodeTree
import boardgame.api.shuffledBy
import boardgame.api.str
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

class CarcassonneGameModule : GameBackModule {
    override fun createService(): GameBackService = CarcassonneGameBackService()
}

class CarcassonneGameBackService : GameBackService {

    override val type: String = CARCASSONNE_TYPE
    override val version: String = BuildInfo.version
    override val homepage: String = "https://github.com/HMHamster88/boardgame-kotlin-carcassonne"
    override val gameStaticSettings: GameStaticSettings = GameStaticSettings(minPlayers = 2, maxPlayers = 5)

    override val localizedName: JsonObject = buildJsonObject {
        putJsonObject("en") { put(CARCASSONNE_TYPE, "Carcassonne") }
        putJsonObject("ru") { put(CARCASSONNE_TYPE, "Каркассон") }
    }

    override fun getDefaultSettings(): JsonObject =
        encodeTree(CarcassonneGameSettings(id = "", mode = CarcassonneGameMode.base)).jsonObject

    override fun startGame(game: Game, settings: JsonObject): GameStatePayload {
        val gameSettings = decodeTree<CarcassonneGameSettings>(settings)
        val startingTemplate = getStartingTileTemplate()

        val publicState = CarcassonnePublicGameState(
            mode = gameSettings.mode,
            board = mutableListOf(
                CarcassonneTile(
                    id = startingTemplate.id,
                    templateId = startingTemplate.id,
                    x = 0,
                    y = 0,
                    rotation = 0
                )
            ),
            followers = mutableListOf(),
            activePlayerIndex = 0,
            playersStates = game.players
                .map { CarcassonnePlayerPublicState(playerId = it.userId) }
                .toMutableList(),
            winnersIds = mutableListOf(),
            statistics = CarcassonneGameStatistics(turnCount = 0),
            tilesRemaining = 0
        )

        val privateState = CarcassonnePrivateGameState(
            deck = createTileDeck().shuffledBy().toMutableList(),
            playersStates = game.players
                .map { CarcassonnePlayerPrivateState(it.userId, initialFollowersPerPlayer) }
                .toMutableList()
        )

        val firstTile = drawPlayableTile(publicState, privateState)
        publicState.currentTile = firstTile
        publicState.tilesRemaining = privateState.deck.size + if (firstTile != null) 1 else 0

        if (firstTile == null) {
            scoreFinalFeatures(publicState)
            publicState.winnersIds = getWinnerIds(publicState).toMutableList()
            game.status = GameStatus.FINISHED
        } else {
            game.status = GameStatus.STARTED
        }

        return GameStatePayload(
            id = game.id,
            publicState = encodeTree(publicState),
            privateState = encodeTree(privateState)
        )
    }

    override suspend fun performAction(
        ctx: GameContext,
        action: JsonElement,
        playerId: String
    ): JsonElement? {
        if (ctx.publicState is JsonNull || ctx.privateState == null) return null

        val actionType = action.jsonObject.str("type") ?: return null
        val publicState = decodeTree<CarcassonnePublicGameState>(ctx.publicState)
        val privateState = decodeTree<CarcassonnePrivateGameState>(ctx.privateState!!)
        val activePlayer = ctx.game.players.getOrNull(publicState.activePlayerIndex)
        val isActivePlayerAction = playerId == activePlayer?.userId

        when (actionType) {
            "PlaceLandTileAction" -> {
                if (!isActivePlayerAction || publicState.pendingTileId != null ||
                    publicState.currentTile == null
                ) {
                    return null
                }

                val place = decodeTree<CarcassonnePlaceLandTileAction>(action)
                if (place.tileId.isBlank() || publicState.board.any { it.id == place.tileId }) {
                    return null
                }
                if (!canPlaceTile(publicState.board, publicState.currentTile!!, place.x, place.y, place.rotation)) {
                    return null
                }

                publicState.board += CarcassonneTile(
                    id = place.tileId,
                    templateId = publicState.currentTile!!,
                    x = place.x,
                    y = place.y,
                    rotation = place.rotation
                )
                publicState.currentTile = null
                publicState.pendingTileId = place.tileId
                publicState.tilesRemaining = privateState.deck.size
                persist(ctx, publicState, privateState)
                return null
            }

            "DeployFollowerAction" -> {
                if (!isActivePlayerAction || publicState.pendingTileId == null) return null

                val deploy = decodeTree<CarcassonneDeployFollowerAction>(action)
                val tile = publicState.board.firstOrNull { it.id == publicState.pendingTileId }
                    ?: return null
                if (deploy.tileId != tile.id) return null

                val template = getTileTemplate(tile.templateId) ?: return null
                if (!isValidSegment(template, deploy.featureType, deploy.segmentIndex)) return null
                if (followerTypeByFeature[deploy.featureType] != deploy.followerType) return null
                if (hasFollowerOnFeature(publicState, tile.id, deploy.featureType, deploy.segmentIndex)) {
                    return null
                }

                val playerState = privateState.playersStates.firstOrNull { it.playerId == playerId }
                    ?: return null
                if (playerState.followersInSupply <= 0) return null

                playerState.followersInSupply--
                publicState.followers += CarcassonneFollower(
                    playerId = playerId,
                    type = deploy.followerType,
                    tileId = tile.id,
                    featureType = deploy.featureType,
                    segmentIndex = deploy.segmentIndex
                )

                finishTurn(ctx, privateState, publicState)
                return null
            }

            "EndTurnAction" -> {
                if (!isActivePlayerAction || publicState.pendingTileId == null) return null
                finishTurn(ctx, privateState, publicState)
                return null
            }
        }

        return null
    }

    private fun persist(
        ctx: GameContext,
        publicState: CarcassonnePublicGameState,
        privateState: CarcassonnePrivateGameState
    ) {
        ctx.updatePublicState(encodeTree(publicState))
        ctx.updatePrivateState(encodeTree(privateState))
    }

    private fun isValidSegment(
        template: CarcassonneTileTemplate,
        featureType: CarcassonneFeatureType,
        segmentIndex: Int
    ): Boolean {
        if (segmentIndex < 0) return false
        return when (featureType) {
            CarcassonneFeatureType.cloister -> template.cloister && segmentIndex == 0
            CarcassonneFeatureType.city -> segmentIndex < template.cityGroups.size
            CarcassonneFeatureType.road -> segmentIndex < template.roadGroups.size
            CarcassonneFeatureType.field -> segmentIndex < template.fieldSides.size
        }
    }

    private fun hasFollowerOnFeature(
        publicState: CarcassonnePublicGameState,
        tileId: String,
        featureType: CarcassonneFeatureType,
        segmentIndex: Int
    ): Boolean {
        val featureGraph = buildFeatureGraph(publicState.board)
        val featureRoot = featureGraph.nodeToFeature[featureNodeKey(tileId, featureType, segmentIndex)]
            ?: return false
        val feature = featureGraph.features[featureRoot] ?: return false
        val nodeKeys = feature.nodes.mapTo(mutableSetOf()) { it.key }
        return publicState.followers.any { nodeKeys.contains(followerNodeKey(it)) }
    }

    private fun drawPlayableTile(
        publicState: CarcassonnePublicGameState,
        privateState: CarcassonnePrivateGameState
    ): String? {
        while (privateState.deck.isNotEmpty()) {
            val templateId = privateState.deck.removeAt(0)
            if (hasLegalPlacement(publicState.board, templateId)) return templateId
        }
        return null
    }

    private fun finishTurn(
        ctx: GameContext,
        privateState: CarcassonnePrivateGameState,
        publicState: CarcassonnePublicGameState
    ) {
        scoreCompletedFeatures(privateState, publicState)
        publicState.pendingTileId = null

        val nextTile = drawPlayableTile(publicState, privateState)
        if (nextTile == null) {
            scoreFinalFeatures(publicState)
            publicState.winnersIds = getWinnerIds(publicState).toMutableList()
            publicState.currentTile = null
            publicState.tilesRemaining = 0
            ctx.game.status = GameStatus.FINISHED
            persist(ctx, publicState, privateState)
            return
        }

        publicState.currentTile = nextTile
        publicState.tilesRemaining = privateState.deck.size + 1
        publicState.activePlayerIndex =
            (publicState.activePlayerIndex + 1) % ctx.game.players.size
        publicState.statistics.turnCount++
        persist(ctx, publicState, privateState)
    }

    override suspend fun nextBotAction(ctx: BotGameContext): BotAction? = null

    companion object {
        private const val initialFollowersPerPlayer = 7
    }
}

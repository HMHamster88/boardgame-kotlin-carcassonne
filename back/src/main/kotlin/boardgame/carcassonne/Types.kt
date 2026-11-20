package boardgame.carcassonne

import boardgame.api.codegen.GenerateTs
import kotlinx.serialization.Serializable

const val CARCASSONNE_TYPE = "CARCASSONNE"

@GenerateTs(asUnion = true)
@Serializable
enum class CarcassonneGameMode {
    base,
    river
}

@GenerateTs(asUnion = true)
@Serializable
enum class CarcassonneEdgeType {
    city,
    road,
    field
}

@GenerateTs(asUnion = true)
@Serializable
enum class CarcassonneFeatureType {
    city,
    road,
    field,
    cloister
}

@GenerateTs(asUnion = true)
@Serializable
enum class CarcassonneFollowerType {
    knight,
    thief,
    farmer,
    monk
}

@GenerateTs
@Serializable
data class CarcassonneGameSettings(
    val id: String = "",
    var mode: CarcassonneGameMode = CarcassonneGameMode.base
)

@GenerateTs
@Serializable
data class CarcassonnePlayerPublicState(
    val playerId: String = "",
    var points: Int = 0
)

@GenerateTs
@Serializable
data class CarcassonnePlayerPrivateState(
    val playerId: String = "",
    var followersInSupply: Int = 0
)

@GenerateTs
@Serializable
data class CarcassonneGameStatistics(
    var turnCount: Int = 0
)

@GenerateTs
@Serializable
data class CarcassonneTile(
    val id: String = "",
    val templateId: String = "",
    val x: Int = 0,
    val y: Int = 0,
    val rotation: Int = 0
)

@GenerateTs
@Serializable
data class CarcassonneFollower(
    val playerId: String = "",
    val type: CarcassonneFollowerType = CarcassonneFollowerType.farmer,
    val tileId: String = "",
    val featureType: CarcassonneFeatureType = CarcassonneFeatureType.field,
    val segmentIndex: Int = 0
)

@GenerateTs
@Serializable
data class CarcassonnePublicGameState(
    var mode: CarcassonneGameMode = CarcassonneGameMode.base,
    var board: MutableList<CarcassonneTile> = mutableListOf(),
    var followers: MutableList<CarcassonneFollower> = mutableListOf(),
    var currentTile: String? = null,
    var pendingTileId: String? = null,
    var tilesRemaining: Int = 0,
    var activePlayerIndex: Int = 0,
    var playersStates: MutableList<CarcassonnePlayerPublicState> = mutableListOf(),
    var winnersIds: MutableList<String> = mutableListOf(),
    var statistics: CarcassonneGameStatistics = CarcassonneGameStatistics()
)

@GenerateTs
@Serializable
data class CarcassonnePrivateGameState(
    var deck: MutableList<String> = mutableListOf(),
    var playersStates: MutableList<CarcassonnePlayerPrivateState> = mutableListOf()
)

// ---------------------------------------------------------------- actions

@GenerateTs(name = "CarcassonnePlaceLandTileAction", typeLiteral = "PlaceLandTileAction")
@Serializable
data class CarcassonnePlaceLandTileAction(
    val type: String = "PlaceLandTileAction",
    val tileId: String = "",
    val x: Int = 0,
    val y: Int = 0,
    val rotation: Int = 0
)

@GenerateTs(name = "CarcassonneDeployFollowerAction", typeLiteral = "DeployFollowerAction")
@Serializable
data class CarcassonneDeployFollowerAction(
    val type: String = "DeployFollowerAction",
    val tileId: String = "",
    val featureType: CarcassonneFeatureType = CarcassonneFeatureType.field,
    val segmentIndex: Int = 0,
    val followerType: CarcassonneFollowerType = CarcassonneFollowerType.farmer
)

@GenerateTs(name = "CarcassonneEndTurnAction", typeLiteral = "EndTurnAction")
@Serializable
data class CarcassonneEndTurnAction(val type: String = "EndTurnAction")

package boardgame.carcassonne

data class CarcassonneFeatureNode(
    val key: String,
    val type: CarcassonneFeatureType,
    val tile: CarcassonneTile,
    val segmentIndex: Int,
    val edges: List<Int>,
    val pennants: Int
)

data class CarcassonneFeatureGroup(
    val root: String,
    val type: CarcassonneFeatureType,
    val nodes: List<CarcassonneFeatureNode>,
    val complete: Boolean
)

data class CarcassonneFeatureGraph(
    val nodesByKey: Map<String, CarcassonneFeatureNode>,
    val features: Map<String, CarcassonneFeatureGroup>,
    val nodeToFeature: Map<String, String>,
    val fieldFeatureRootsByCityRoot: Map<String, Set<String>>
)

private class DisjointSet {
    private val parents = mutableMapOf<String, String>()

    fun add(key: String) {
        if (!parents.containsKey(key)) parents[key] = key
    }

    fun find(key: String): String {
        val parent = parents[key]
        if (parent == null || parent == key) return key
        val root = find(parent)
        parents[key] = root
        return root
    }

    fun union(first: String, second: String) {
        val firstRoot = find(first)
        val secondRoot = find(second)
        if (firstRoot != secondRoot) parents[secondRoot] = firstRoot
    }
}

fun featureNodeKey(
    tileId: String,
    featureType: CarcassonneFeatureType,
    segmentIndex: Int
): String = "$tileId:$featureType:$segmentIndex"

fun followerNodeKey(follower: CarcassonneFollower): String =
    featureNodeKey(follower.tileId, follower.featureType, follower.segmentIndex)

private fun getCityPennants(feature: CarcassonneFeatureGroup): Int =
    feature.nodes.sumOf { it.pennants }

private fun getDistinctTileCount(feature: CarcassonneFeatureGroup): Int =
    feature.nodes.mapTo(mutableSetOf()) { it.tile.id }.size

private fun isFeatureComplete(
    type: CarcassonneFeatureType,
    nodes: List<CarcassonneFeatureNode>,
    tilesByPosition: Map<String, CarcassonneTile>
): Boolean {
    if (type == CarcassonneFeatureType.field) return false

    if (type == CarcassonneFeatureType.cloister) {
        val tile = nodes.firstOrNull()?.tile ?: return false
        return surroundingOffsets.all { offset ->
            tilesByPosition.containsKey(positionKey(tile.x + offset.x, tile.y + offset.y))
        }
    }

    return nodes.all { node ->
        val template = getTileTemplate(node.tile.templateId) ?: return@all false
        node.edges.all { originalEdge ->
            val direction = getPlacedEdgeDirection(originalEdge, node.tile.rotation)
            val offset = edgeDirections[direction]
            val neighbor = tilesByPosition[positionKey(node.tile.x + offset.x, node.tile.y + offset.y)]
                ?: return@all false
            val neighborTemplate = getTileTemplate(neighbor.templateId) ?: return@all false
            val neighborEdge =
                getPlacedEdgeType(neighborTemplate, neighbor.rotation, getOppositeDirection(direction))
            neighborEdge.name == type.name
        }
    }
}

fun buildFeatureGraph(board: List<CarcassonneTile>): CarcassonneFeatureGraph {
    val nodesByKey = mutableMapOf<String, CarcassonneFeatureNode>()
    val edgeNodeKeys = mutableMapOf<String, String>()
    val fieldSideNodeKeys = mutableMapOf<String, String>()
    val disjointSet = DisjointSet()

    fun addNode(
        tile: CarcassonneTile,
        type: CarcassonneFeatureType,
        segmentIndex: Int,
        edges: List<Int>,
        pennants: Int
    ) {
        val key = featureNodeKey(tile.id, type, segmentIndex)
        nodesByKey[key] = CarcassonneFeatureNode(key, type, tile, segmentIndex, edges, pennants)
        disjointSet.add(key)
        if (type == CarcassonneFeatureType.city || type == CarcassonneFeatureType.road) {
            edges.forEach { edge -> edgeNodeKeys["${tile.id}:$type:$edge"] = key }
        }
    }

    board.forEach { tile ->
        val template = getTileTemplate(tile.templateId) ?: return@forEach

        template.cityGroups.forEachIndexed { index, edges ->
            addNode(tile, CarcassonneFeatureType.city, index, edges, template.cityPennants?.getOrNull(index) ?: 0)
        }
        template.roadGroups.forEachIndexed { index, edges ->
            addNode(tile, CarcassonneFeatureType.road, index, edges, 0)
        }
        template.fieldSides.forEachIndexed { index, sides ->
            val edges = sides.map { it / 2 }.distinct()
            addNode(tile, CarcassonneFeatureType.field, index, edges, 0)
            val nodeKey = featureNodeKey(tile.id, CarcassonneFeatureType.field, index)
            sides.forEach { side -> fieldSideNodeKeys["${tile.id}:$side"] = nodeKey }
        }
        if (template.cloister) addNode(tile, CarcassonneFeatureType.cloister, 0, emptyList(), 0)
    }

    val tilesByPosition = getTilesByPosition(board)

    board.forEach { tile ->
        val template = getTileTemplate(tile.templateId) ?: return@forEach

        for (direction in 0 until edgeDirections.size) {
            val offset = edgeDirections[direction]
            val neighbor = tilesByPosition[positionKey(tile.x + offset.x, tile.y + offset.y)]
                ?: continue
            val neighborTemplate = getTileTemplate(neighbor.templateId) ?: continue

            val type = getPlacedEdgeType(template, tile.rotation, direction)
            val neighborDirection = getOppositeDirection(direction)
            val neighborType = getPlacedEdgeType(neighborTemplate, neighbor.rotation, neighborDirection)
            if (type != neighborType) continue

            if (type == CarcassonneEdgeType.city || type == CarcassonneEdgeType.road) {
                val ownNodeKey = edgeNodeKeys[
                    "${tile.id}:$type:${getOriginalEdgeIndex(direction, tile.rotation)}"
                ]
                val neighborNodeKey = edgeNodeKeys[
                    "${neighbor.id}:$type:${getOriginalEdgeIndex(neighborDirection, neighbor.rotation)}"
                ]
                if (ownNodeKey != null && neighborNodeKey != null) {
                    disjointSet.union(ownNodeKey, neighborNodeKey)
                }
            }

            if (type == CarcassonneEdgeType.field || type == CarcassonneEdgeType.road) {
                val originalEdge = getOriginalEdgeIndex(direction, tile.rotation)
                fieldSidesByEdge[originalEdge].forEach { side ->
                    val placedSide = getPlacedFieldSide(side, tile.rotation)
                    val neighborSide = getOriginalFieldSide(oppositeFieldSide[placedSide], neighbor.rotation)
                    val ownFieldNodeKey = fieldSideNodeKeys["${tile.id}:$side"]
                    val neighborFieldNodeKey = fieldSideNodeKeys["${neighbor.id}:$neighborSide"]
                    if (ownFieldNodeKey != null && neighborFieldNodeKey != null) {
                        disjointSet.union(ownFieldNodeKey, neighborFieldNodeKey)
                    }
                }
            }
        }
    }

    val nodesByRoot = mutableMapOf<String, MutableList<CarcassonneFeatureNode>>()
    nodesByKey.values.forEach { node ->
        val root = disjointSet.find(node.key)
        nodesByRoot.getOrPut(root) { mutableListOf() }.add(node)
    }

    val features = mutableMapOf<String, CarcassonneFeatureGroup>()
    val nodeToFeature = mutableMapOf<String, String>()

    nodesByRoot.forEach { (root, nodes) ->
        val type = nodes.first().type
        features[root] = CarcassonneFeatureGroup(
            root = root,
            type = type,
            nodes = nodes,
            complete = isFeatureComplete(type, nodes, tilesByPosition)
        )
        nodes.forEach { node -> nodeToFeature[node.key] = root }
    }

    val fieldFeatureRootsByCityRoot = mutableMapOf<String, MutableSet<String>>()

    board.forEach { tile ->
        val template = getTileTemplate(tile.templateId) ?: return@forEach
        if (template.cityGroups.isEmpty() || template.fieldSides.isEmpty()) return@forEach

        template.fieldSides.indices.forEach { fieldIndex ->
            val fieldRoot =
                disjointSet.find(featureNodeKey(tile.id, CarcassonneFeatureType.field, fieldIndex))
            template.cityGroups.indices.forEach { cityIndex ->
                val cityRoot =
                    disjointSet.find(featureNodeKey(tile.id, CarcassonneFeatureType.city, cityIndex))
                fieldFeatureRootsByCityRoot.getOrPut(cityRoot) { mutableSetOf() }.add(fieldRoot)
            }
        }
    }

    return CarcassonneFeatureGraph(nodesByKey, features, nodeToFeature, fieldFeatureRootsByCityRoot)
}

private fun getFollowersOnFeature(
    followers: List<CarcassonneFollower>,
    feature: CarcassonneFeatureGroup,
    followerType: CarcassonneFollowerType
): List<CarcassonneFollower> {
    val nodeKeys = feature.nodes.mapTo(mutableSetOf()) { it.key }
    return followers.filter { follower ->
        follower.type == followerType && nodeKeys.contains(followerNodeKey(follower))
    }
}

private fun getMajorityPlayers(followers: List<CarcassonneFollower>): List<String> {
    val counts = mutableMapOf<String, Int>()
    followers.forEach { follower ->
        counts[follower.playerId] = (counts[follower.playerId] ?: 0) + 1
    }
    if (counts.isEmpty()) return emptyList()
    val maxCount = counts.values.max()
    return counts.entries.filter { it.value == maxCount }.map { it.key }
}

private fun awardPoints(
    publicState: CarcassonnePublicGameState,
    playerIds: List<String>,
    points: Int
) {
    playerIds.forEach { playerId ->
        publicState.playersStates.firstOrNull { it.playerId == playerId }?.let { it.points += points }
    }
}

private fun returnFollowersToSupply(
    privateState: CarcassonnePrivateGameState,
    publicState: CarcassonnePublicGameState,
    followers: List<CarcassonneFollower>
) {
    if (followers.isEmpty()) return

    publicState.followers.removeAll(followers.toSet())
    followers.forEach { follower ->
        privateState.playersStates
            .firstOrNull { it.playerId == follower.playerId }
            ?.let { it.followersInSupply++ }
    }
}

private fun getCompletedFeaturePoints(feature: CarcassonneFeatureGroup): Int = when (feature.type) {
    CarcassonneFeatureType.cloister -> 9
    CarcassonneFeatureType.city -> getDistinctTileCount(feature) * 2 + getCityPennants(feature) * 2
    CarcassonneFeatureType.road -> getDistinctTileCount(feature)
    else -> 0
}

private fun countSurroundingPlacedTiles(
    tile: CarcassonneTile,
    tilesByPosition: Map<String, CarcassonneTile>
): Int = surroundingOffsets.count { offset ->
    tilesByPosition.containsKey(positionKey(tile.x + offset.x, tile.y + offset.y))
}

fun scoreCompletedFeatures(
    privateState: CarcassonnePrivateGameState,
    publicState: CarcassonnePublicGameState
) {
    val featureGraph = buildFeatureGraph(publicState.board)

    for (feature in featureGraph.features.values) {
        if (!feature.complete || feature.type == CarcassonneFeatureType.field) continue

        val followerType = followerTypeByFeature.getValue(feature.type)
        val followers = getFollowersOnFeature(publicState.followers, feature, followerType)
        if (followers.isEmpty()) continue

        awardPoints(publicState, getMajorityPlayers(followers), getCompletedFeaturePoints(feature))
        returnFollowersToSupply(privateState, publicState, followers)
    }
}

fun scoreFinalFeatures(publicState: CarcassonnePublicGameState) {
    val featureGraph = buildFeatureGraph(publicState.board)
    val tilesByPosition = getTilesByPosition(publicState.board)

    for (feature in featureGraph.features.values) {
        if (feature.type == CarcassonneFeatureType.field) continue

        val followerType = followerTypeByFeature.getValue(feature.type)
        val followers = getFollowersOnFeature(publicState.followers, feature, followerType)
        if (followers.isEmpty()) continue

        val points = when (feature.type) {
            CarcassonneFeatureType.city -> {
                if (feature.complete) continue
                feature.nodes.size + getCityPennants(feature)
            }

            CarcassonneFeatureType.road -> {
                if (feature.complete) continue
                getDistinctTileCount(feature)
            }

            CarcassonneFeatureType.cloister -> {
                if (feature.complete) continue
                val tile = feature.nodes.firstOrNull()?.tile ?: continue
                1 + countSurroundingPlacedTiles(tile, tilesByPosition)
            }

            else -> 0
        }

        awardPoints(publicState, getMajorityPlayers(followers), points)
    }

    featureGraph.fieldFeatureRootsByCityRoot.forEach { (cityRoot, fieldRoots) ->
        val city = featureGraph.features[cityRoot]
        if (city == null || city.type != CarcassonneFeatureType.city || !city.complete) {
            return@forEach
        }

        fieldRoots.forEach { fieldRoot ->
            val field = featureGraph.features[fieldRoot]
            if (field == null || field.type != CarcassonneFeatureType.field) return@forEach

            val farmers = getFollowersOnFeature(publicState.followers, field, CarcassonneFollowerType.farmer)
            if (farmers.isEmpty()) return@forEach

            awardPoints(publicState, getMajorityPlayers(farmers), 3)
        }
    }
}

fun getWinnerIds(publicState: CarcassonnePublicGameState): List<String> {
    val playersStates = publicState.playersStates
    if (playersStates.isEmpty()) return emptyList()

    val maxPoints = playersStates.maxOf { it.points }
    return playersStates.filter { it.points == maxPoints }.map { it.playerId }
}

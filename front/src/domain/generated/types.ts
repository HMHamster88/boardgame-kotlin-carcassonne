// Generated from Kotlin models by the module's generateTs task. Do not edit.
// Only model types shared with the back-end are generated; front-only helpers stay hand-written.

export interface CarcassonneDeployFollowerAction {
    type: 'DeployFollowerAction'
    tileId: string
    featureType: CarcassonneFeatureType
    segmentIndex: number
    followerType: CarcassonneFollowerType
}

export type CarcassonneEdgeType = 'city' | 'road' | 'field'

export interface CarcassonneEndTurnAction {
    type: 'EndTurnAction'
}

export type CarcassonneFeatureType = 'city' | 'road' | 'field' | 'cloister'

export interface CarcassonneFollower {
    playerId: string
    type: CarcassonneFollowerType
    tileId: string
    featureType: CarcassonneFeatureType
    segmentIndex: number
}

export type CarcassonneFollowerType = 'knight' | 'thief' | 'farmer' | 'monk'

export type CarcassonneGameMode = 'base' | 'river'

export interface CarcassonneGameSettings {
    id: string
    mode: CarcassonneGameMode
}

export interface CarcassonneGameStatistics {
    turnCount: number
}

export interface CarcassonnePlaceLandTileAction {
    type: 'PlaceLandTileAction'
    tileId: string
    x: number
    y: number
    rotation: number
}

export interface CarcassonnePlayerPrivateState {
    playerId: string
    followersInSupply: number
}

export interface CarcassonnePlayerPublicState {
    playerId: string
    points: number
}

export interface CarcassonnePrivateGameState {
    deck: string[]
    playersStates: CarcassonnePlayerPrivateState[]
}

export interface CarcassonnePublicGameState {
    mode: CarcassonneGameMode
    board: CarcassonneTile[]
    followers: CarcassonneFollower[]
    currentTile?: string
    pendingTileId?: string
    tilesRemaining: number
    activePlayerIndex: number
    playersStates: CarcassonnePlayerPublicState[]
    winnersIds: string[]
    statistics: CarcassonneGameStatistics
}

export interface CarcassonneTile {
    id: string
    templateId: string
    x: number
    y: number
    rotation: number
}


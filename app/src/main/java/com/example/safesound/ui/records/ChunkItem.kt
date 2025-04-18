package com.example.safesound.ui.records

import com.example.safesound.data.records.Chunk

sealed class ChunkItem {
    data class Regular(val chunk: Chunk) : ChunkItem()
    data class SilentGroup(val start: String, val end: String) : ChunkItem()
}

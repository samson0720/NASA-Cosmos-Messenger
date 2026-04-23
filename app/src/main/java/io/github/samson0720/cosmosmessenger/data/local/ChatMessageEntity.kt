package io.github.samson0720.cosmosmessenger.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_message")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val sortOrder: Int,
    val sender: String,
    val contentType: String,
    val text: String?,
    val apodDate: String?,
    val apodTitle: String?,
    val apodExplanation: String?,
    val apodMediaType: String?,
    val apodUrl: String?,
    val apodHdUrl: String?,
    val apodSource: String?,
)

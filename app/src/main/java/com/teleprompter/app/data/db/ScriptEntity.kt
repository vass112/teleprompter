package com.teleprompter.app.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scripts")
data class ScriptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "Untitled Script",
    val content: String = "",
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,
    @ColumnInfo(name = "last_scroll_position")
    val lastScrollPosition: Float = 0f,
    @ColumnInfo(name = "last_speed")
    val lastSpeed: Float = 1.0f,
    @ColumnInfo(name = "font_size")
    val fontSize: Float = 18f,
    @ColumnInfo(name = "line_spacing")
    val lineSpacing: Float = 1.5f,
    @ColumnInfo(name = "text_color")
    val textColor: Int = 0xFFFFFFFF.toInt(),
    @ColumnInfo(name = "is_bold")
    val isBold: Boolean = false
)

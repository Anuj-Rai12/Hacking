package com.uptodd.uptoddapp.database.activitypodcast

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "activity_podcast_table")
data class ActivityPodcast(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "description")
    val description: String,
    @ColumnInfo(name = "kit_content")
    val kitContent: String,
    @ColumnInfo(name = "video")
    val video: String
)
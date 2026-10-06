package com.example.data.db

import androidx.room.TypeConverter
import com.example.domain.model.ActionItem
import com.example.domain.model.SourceChunk
import com.example.domain.model.TranscriptEntry
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class VerbaConverters {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val stringListType = Types.newParameterizedType(List::class.java, String::class.java)
    private val stringListAdapter = moshi.adapter<List<String>>(stringListType)

    private val actionItemListType = Types.newParameterizedType(List::class.java, ActionItem::class.java)
    private val actionItemListAdapter = moshi.adapter<List<ActionItem>>(actionItemListType)

    private val transcriptListType = Types.newParameterizedType(List::class.java, TranscriptEntry::class.java)
    private val transcriptListAdapter = moshi.adapter<List<TranscriptEntry>>(transcriptListType)

    private val sourceListType = Types.newParameterizedType(List::class.java, SourceChunk::class.java)
    private val sourceListAdapter = moshi.adapter<List<SourceChunk>>(sourceListType)

    @TypeConverter
    fun fromStringList(list: List<String>?): String? = list?.let { stringListAdapter.toJson(it) }

    @TypeConverter
    fun toStringList(json: String?): List<String>? = json?.let {
        try { stringListAdapter.fromJson(it) } catch (e: Exception) { emptyList() }
    }

    @TypeConverter
    fun fromActionItemList(list: List<ActionItem>?): String? = list?.let { actionItemListAdapter.toJson(it) }

    @TypeConverter
    fun toActionItemList(json: String?): List<ActionItem>? = json?.let {
        try { actionItemListAdapter.fromJson(it) } catch (e: Exception) { emptyList() }
    }

    @TypeConverter
    fun fromTranscriptList(list: List<TranscriptEntry>?): String? = list?.let { transcriptListAdapter.toJson(it) }

    @TypeConverter
    fun toTranscriptList(json: String?): List<TranscriptEntry>? = json?.let {
        try { transcriptListAdapter.fromJson(it) } catch (e: Exception) { emptyList() }
    }

    @TypeConverter
    fun fromSourceList(list: List<SourceChunk>?): String? = list?.let { sourceListAdapter.toJson(it) }

    @TypeConverter
    fun toSourceList(json: String?): List<SourceChunk>? = json?.let {
        try { sourceListAdapter.fromJson(it) } catch (e: Exception) { emptyList() }
    }
}

package com.example.khadra.data.model

import android.net.Uri
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import android.util.Log

@Serializable
data class Tree(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val location: String = "",
    val status: String = "",
    @Serializable(with = DateSerializer::class)
    @SerialName("created_at")
    val createdAt: Date = Date(),
    @SerialName("coordinates_lat")
    val coordinatesLat: Double = 0.0,
    @SerialName("coordinates_lng")
    val coordinatesLng: Double = 0.0,
    @SerialName("url_image")
    val imageUrl: String? = null,
    @SerialName("last_irrigation_action")
    @Serializable(with = DateSerializer::class)
    val lastIrrigationAction: Date = Date(),
    @SerialName("updated_at")
    @Serializable(with = DateSerializer::class)
    val updatedAt: Date = Date()
) {
    val coordinates: Pair<Double, Double>
        get() = Pair(coordinatesLat, coordinatesLng)

    // Convert imageUrl string to Uri when needed
    val imageUri: Uri?
        get() = try {
            imageUrl?.let { Uri.parse(it) }
        } catch (e: Exception) {
            Log.e("Tree", "Failed to parse URI: $imageUrl", e)
            null
        }

    companion object {
        fun fromCoordinates(
            id: String,
            name: String,
            type: String,
            status: String,
            coordinates: Pair<Double, Double>,
            location: String,
            imageUrl: String? = null,
            lastIrrigationAction: Date = Date(),
            createdAt: Date = Date(),
            updatedAt: Date = Date()
        ): Tree {
            return Tree(
                id = id,
                name = name,
                type = type,
                status = status,
                coordinatesLat = coordinates.first,
                coordinatesLng = coordinates.second,
                location = location,
                imageUrl = imageUrl,
                lastIrrigationAction = lastIrrigationAction,
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        }
    }
}

object DateSerializer : KSerializer<Date> {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Date", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Date) {
        encoder.encodeString(dateFormat.format(value))
    }

    override fun deserialize(decoder: Decoder): Date {
        val dateStr = decoder.decodeString()
        return try {
            dateFormat.parse(dateStr) ?: throw IllegalArgumentException("Invalid date format: $dateStr")
        } catch (e: Exception) {
            Log.e("DateSerializer", "Failed to parse date: $dateStr", e)
            try {
                // Try parsing without milliseconds
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                    .apply { timeZone = TimeZone.getTimeZone("UTC") }
                    .parse(dateStr) ?: Date()
            } catch (e2: Exception) {
                Log.e("DateSerializer", "Failed to parse date without milliseconds: $dateStr", e2)
                Date() // Return current date as fallback
            }
        }
    }
}

data class TreeViewModel(
    val name: String,
    val type: String,
    val imageUri: String? = null,
    val location: String,
    val coordinates: Pair<Double, Double>? = null
)

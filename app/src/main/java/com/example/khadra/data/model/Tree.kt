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

@Serializable
data class Tree(
    val id: String,
    val name: String,
    val type: String,
    val status: String,
    @SerialName("coordinates_lat") val coordinatesLat: Double,
    @SerialName("coordinates_lng") val coordinatesLng: Double,
    val location: String,
    @SerialName("url_image") val urlImage: String = "",
    @SerialName("image_uri") val imageUri: String = "",
    @SerialName("last_irrigation_action") @Serializable(with = DateSerializer::class) val lastIrrigationAction: Date,
    @SerialName("created_at") @Serializable(with = DateSerializer::class) val createdAt: Date,
    @SerialName("updated_at") @Serializable(with = DateSerializer::class) val updatedAt: Date
) {
    val coordinates: Pair<Double, Double>
        get() = Pair(coordinatesLat, coordinatesLng)

    companion object {
        fun fromCoordinates(
            id: String,
            name: String,
            type: String,
            status: String,
            coordinates: Pair<Double, Double>,
            location: String,
            urlImage: String = "",
            imageUri: String = "",
            lastIrrigationAction: Date = Date(),
            createdAt: Date = Date(),
            updatedAt: Date = Date()
        ): Tree = Tree(
            id = id,
            name = name,
            type = type,
            status = status,
            coordinatesLat = coordinates.first,
            coordinatesLng = coordinates.second,
            location = location,
            urlImage = urlImage,
            imageUri = imageUri,
            lastIrrigationAction = lastIrrigationAction,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}

object DateSerializer : KSerializer<Date> {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Date", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Date) {
        encoder.encodeString(dateFormat.format(value))
    }

    override fun deserialize(decoder: Decoder): Date {
        return dateFormat.parse(decoder.decodeString()) ?: Date(0)
    }
}

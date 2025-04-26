package com.example.khadra.data.remote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.khadra.data.model.Tree
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.postgrest.exception.PostgrestRestException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.ktor.client.utils.EmptyContent.contentType
import java.io.ByteArrayOutputStream
import java.util.UUID
import io.ktor.http.ContentType

class SupabaseTreeDataSource(
    private val client: SupabaseClient,
    private val context: Context
) {
    init {
        client.storage
    }

    companion object {
        private const val TABLE_NAME = "trees"
        private const val BUCKET_NAME = "tree-images"
        private const val TAG = "SupabaseTreeDataSource"
    }

    suspend fun getTrees(): List<Tree> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching trees from Supabase table: $TABLE_NAME")
            val result = client.postgrest[TABLE_NAME].select().decodeList<Tree>()
            Log.d(TAG, "Successfully fetched ${result.size} trees from Supabase")
            Log.d(TAG, "Sample tree data: ${result.firstOrNull()}")
            result
        } catch (e: PostgrestRestException) {
            if (e.message?.contains("row-level security policy") == true) {
                Log.e(TAG, "RLS is enabled. Please disable RLS for the trees table in Supabase dashboard.", e)
            }
            Log.e(TAG, "PostgrestRestException: ${e.message}", e)
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching trees from Supabase", e)
            Log.e(TAG, "Error details: ${e.message}")
            emptyList()
        }
    }

    private suspend fun uploadImage(imageUri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()
            
            val targetSize = 1024 // Target width/height
            val sampleSize = calculateSampleSize(options.outWidth, options.outHeight, targetSize)
            
            val decodingOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }
            val newInputStream = context.contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(newInputStream, null, decodingOptions)
                ?: throw IllegalStateException("Failed to decode image")
            newInputStream?.close()
            
            val width = bitmap.width
            val height = bitmap.height
            val ratio = width.toFloat() / height
            val (newWidth, newHeight) = if (width > height) {
                targetSize to (targetSize / ratio).toInt()
            } else {
                (targetSize * ratio).toInt() to targetSize
            }
            
            val compressedBitmap = Bitmap.createScaledBitmap(
                bitmap,
                newWidth,
                newHeight,
                true
            )
            
            val outputStream = ByteArrayOutputStream()
            compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            val compressedBytes = outputStream.toByteArray()
            
            bitmap.recycle()
            compressedBitmap.recycle()
            outputStream.close()
            
            val fileName = "${UUID.randomUUID()}.jpg"
            
            client.storage[BUCKET_NAME].upload(
                path = fileName,
                data = compressedBytes,
                options = {
                    contentType = ContentType.Image.JPEG
                }
            )

            // Return the public URL
            client.storage[BUCKET_NAME].publicUrl(fileName)
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading image", e)
            null
        }
    }

    private fun calculateSampleSize(width: Int, height: Int, targetSize: Int): Int {
        var sampleSize = 1
        while ((width / sampleSize) > targetSize && (height / sampleSize) > targetSize) {
            sampleSize *= 2
        }
        return sampleSize
    }

    suspend fun addTree(tree: Tree, imageUri: Uri?): Tree = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting tree addition to Supabase")
            
            val imageUrl = imageUri?.let {
                Log.d(TAG, "Uploading image for tree")
                uploadImage(it).also { url ->
                    Log.d(TAG, "Image uploaded, URL: $url")
                }
            }
            
            val treeToInsert = tree.copy(
                imageUrl = imageUrl
            )
            
            Log.d(TAG, "Tree data to insert: $treeToInsert")
            
            val insertedTree = client.postgrest[TABLE_NAME]
                .insert(treeToInsert) {
                    select()
                }
                .decodeSingle<Tree>()
            
            Log.d(TAG, "Tree successfully added to Supabase")
            insertedTree
        } catch (e: PostgrestRestException) {
            Log.e(TAG, "PostgrestRestException: ${e.message}", e)
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error adding tree to Supabase", e)
            throw e
        }
    }

    suspend fun updateTree(tree: Tree): Tree = withContext(Dispatchers.IO) {
        try {
            val response = client.postgrest[TABLE_NAME]
                .update(mapOf(
                    "name" to tree.name,
                    "type" to tree.type,
                    "status" to tree.status,
                    "location" to tree.location,
                    "coordinates_lat" to tree.coordinatesLat,
                    "coordinates_lng" to tree.coordinatesLng,
                    "last_irrigation_action" to tree.lastIrrigationAction,
                    "image_url" to tree.imageUrl
                )) {
                    filter {
                        eq("id", tree.id)
                    }
                }

            tree
        } catch (e: Exception) {
            Log.e(TAG, "Error updating tree", e)
            throw e
        }
    }

    suspend fun deleteTree(treeId: String) {
        // Implementation pending
    }
}
package com.example.khadra.data.remote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.khadra.data.model.Tree
import com.example.khadra.data.model.IrrigationHistory
import com.example.khadra.data.model.Location
import com.example.khadra.data.source.TreeDataSource
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.postgrest.exception.PostgrestRestException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.postgrest.query.Order
import io.ktor.client.utils.EmptyContent.contentType
import java.io.ByteArrayOutputStream
import java.util.UUID
import io.ktor.http.ContentType
import javax.inject.Inject

class SupabaseTreeDataSource @Inject constructor(
    private val client: SupabaseClient,
    private val context: Context
) : TreeDataSource {
    init {
        client.storage
    }

    companion object {
        private const val TABLE_NAME = "trees"
        private const val BUCKET_NAME = "tree-images"
        private const val TAG = "SupabaseTreeDataSource"
    }

    override suspend fun getTrees(): List<Tree> = withContext(Dispatchers.IO) {
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
            throw e
        }
    }

    override suspend fun addTree(tree: Tree, imageUri: Uri?): Tree = withContext(Dispatchers.IO) {
        try {
            val treeToAdd = if (imageUri != null) {
                val imageUrl = uploadImage(imageUri)
                tree.copy(imageUrl = imageUrl)
            } else {
                tree
            }
            
            client.postgrest[TABLE_NAME].insert(treeToAdd).decodeSingle()
        } catch (e: Exception) {
            Log.e(TAG, "Error adding tree to Supabase", e)
            throw e
        }
    }

    override suspend fun updateTree(tree: Tree): Tree = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Updating tree in Supabase: $tree")
            val updatedTree = client.postgrest[TABLE_NAME]
                .update(value = tree) {
                    filter {
                        eq("id", tree.id)
                    }
                }
                .decodeSingle<Tree>()
            Log.d(TAG, "Tree updated successfully: $updatedTree")
            updatedTree
        } catch (e: PostgrestRestException) {
            Log.e(TAG, "PostgrestRestException while updating tree: ${e.message}", e)
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error updating tree in Supabase: ${e.message}", e)
            throw e
        }
    }

    override suspend fun deleteTree(treeId: String): Unit = withContext(Dispatchers.IO) {
        try {
            client.postgrest[TABLE_NAME]
                .delete {
                    filter {
                        eq("id", treeId)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting tree from Supabase", e)
            throw e
        }
    }

    override suspend fun getCurrentLocation(): Location {
        throw NotImplementedError("getCurrentLocation should be implemented in LocationDataSource")
    }

    override suspend fun addIrrigationHistory(history: IrrigationHistory): IrrigationHistory = withContext(Dispatchers.IO) {
        try {
            client.postgrest["irrigation_history"]
                .insert(history)
                .decodeSingle()
        } catch (e: Exception) {
            Log.e(TAG, "Error adding irrigation history to Supabase", e)
            throw e
        }
    }

    override suspend fun getIrrigationHistory(treeId: String): List<IrrigationHistory> = withContext(Dispatchers.IO) {
        try {
            client.postgrest["irrigation_history"]
                .select {
                    filter {
                        eq("tree_id", treeId)
                    }
                    order("irrigation_date", Order.DESCENDING)
                }
                .decodeList()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting irrigation history from Supabase", e)
            throw e
        }
    }

    private suspend fun uploadImage(imageUri: Uri): String = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            val imageBytes = outputStream.toByteArray()

            val fileName = "${UUID.randomUUID()}.jpg"
            client.storage[BUCKET_NAME].upload(
                path = fileName,
                data = imageBytes
            ) {
                contentType = ContentType.Image.JPEG
            }

            val publicUrl = client.storage[BUCKET_NAME].publicUrl(fileName)
            Log.d(TAG, "Image uploaded successfully: $publicUrl")
            publicUrl
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading image to Supabase storage", e)
            throw e
        }
    }
}
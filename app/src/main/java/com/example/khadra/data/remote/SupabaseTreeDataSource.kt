package com.example.khadra.data.remote

import android.content.Context
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
import java.io.ByteArrayOutputStream
import java.util.UUID
import io.ktor.http.ContentType

class SupabaseTreeDataSource(
    private val client: SupabaseClient,
    private val context: Context
) {
    init {
        client.storage // Initialize storage plugin
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
            val bytes = inputStream?.use { input ->
                ByteArrayOutputStream().use { output ->
                    input.copyTo(output)
                    output.toByteArray()
                }
            } ?: throw IllegalStateException("Could not read image file")

            val fileName = "${UUID.randomUUID()}.jpg"
            
            // Upload the image bytes
            client.storage[BUCKET_NAME].upload(
                path = fileName,
                data = bytes,
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

    suspend fun addTree(tree: Tree, imageUri: Uri?): Tree = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting tree addition to Supabase")
            
            // First upload the image if provided
            val imageUrl = imageUri?.let { 
                Log.d(TAG, "Uploading image for tree")
                uploadImage(it).also { url ->
                    Log.d(TAG, "Image uploaded, URL: $url")
                }
            }
            
            // Create tree with image URL
            val treeToInsert = tree.copy(
                imageUrl = imageUrl // This will be mapped to url_image in Supabase due to @SerialName
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

    // TODO: Implement updateTree and deleteTree functions using Supabase client
    suspend fun updateTree(tree: Tree) {
        // Implementation pending
    }

    suspend fun deleteTree(treeId: String) {
        // Implementation pending
    }
}
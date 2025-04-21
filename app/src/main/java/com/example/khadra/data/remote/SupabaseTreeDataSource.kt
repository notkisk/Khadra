package com.example.khadra.data.remote

import com.example.khadra.data.model.Tree
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Returning
import io.github.jan.supabase.postgrest.exception.PostgrestRestException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

class SupabaseTreeDataSource(private val client: SupabaseClient) {

    companion object {
        private const val TABLE_NAME = "trees"
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
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching trees from Supabase", e)
            Log.e(TAG, "Error details: ${e.message}")
            emptyList()
        }
    }

    suspend fun addTree(tree: Tree): Tree = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting tree addition to Supabase")
            Log.d(TAG, "Tree data to insert: $tree")
            Log.d(TAG, "Coordinates: (${tree.coordinatesLat}, ${tree.coordinatesLng})")
            
            val insertedTree = client.postgrest[TABLE_NAME]
                .insert(tree) {
                    select() // Get the inserted row back
                }
                .decodeSingle<Tree>()
            
            Log.d(TAG, "Tree successfully added to Supabase")
            Log.d(TAG, "Inserted tree details: $insertedTree")
            Log.d(TAG, "Inserted tree ID: ${insertedTree.id}")
            insertedTree
        } catch (e: PostgrestRestException) {
            if (e.message?.contains("row-level security policy") == true) {
                Log.e(TAG, "RLS is enabled and blocking operations. To fix this:", e)
                Log.e(TAG, "1. Go to Supabase dashboard")
                Log.e(TAG, "2. Navigate to Database -> Tables -> trees")
                Log.e(TAG, "3. Click on 'Disable RLS' to allow all operations")
                throw RuntimeException("Please disable RLS for the trees table in Supabase dashboard")
            }
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add tree to Supabase", e)
            Log.e(TAG, "Error details: ${e.message}")
            throw e
        }
    }

    // TODO: Implement updateTree and deleteTree functions using Supabase client
    suspend fun updateTree(tree: Tree) {
        Log.w(TAG, "updateTree not implemented yet.")
        // client.postgrest[TABLE_NAME].update({ /* update fields */ }) { eq("id", tree.id) }
    }

    suspend fun deleteTree(treeId: String) {
        Log.w(TAG, "deleteTree not implemented yet.")
        // client.postgrest[TABLE_NAME].delete { eq("id", treeId) }
    }
}
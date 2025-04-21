package com.example.khadra.data.repository

import android.content.Context
import com.example.khadra.data.model.Tree
import com.example.khadra.data.model.TreeType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Date
import javax.inject.Inject
import android.util.Log
import com.example.khadra.data.remote.SupabaseTreeDataSource

class TreeRepositoryImpl @Inject constructor(
    private val context: Context,
    private val supabaseDataSource: SupabaseTreeDataSource // Add data source to constructor
) : TreeRepository {

    // Use the data source to get trees
    override fun getTrees(): Flow<List<Tree>> = flow {
        try {
            Log.d("TreeRepositoryImpl", "Fetching trees via SupabaseDataSource...") // Added logging
            val trees = supabaseDataSource.getTrees()
            Log.d("TreeRepositoryImpl", "Successfully fetched ${trees.size} trees.") // Added logging
            emit(trees)
        } catch (e: Exception) {
            Log.e("TreeRepositoryImpl", "Error fetching trees from Supabase: ${e.message}", e)
        }
    }

    override suspend fun addTree(tree: Tree) {
        try {
            Log.d("TreeRepositoryImpl", "Adding tree via SupabaseDataSource: $tree") // Added logging
            supabaseDataSource.addTree(tree)
            Log.i("TreeRepositoryImpl", "Tree added successfully via Supabase")
        } catch (e: Exception) {
            Log.e("TreeRepositoryImpl", "Error adding tree via Supabase: ${e.message}", e)
            // Optionally, rethrow or handle the error further
            throw e // Re-throw exception so ViewModel can catch it
        }
    }

     fun getTreeById(id: String): Flow<Tree?> = flow { // Assuming ID is String based on Tree model
        // TODO: Implement getTreeById using Supabase if needed, for now return null or dummy
        Log.w("TreeRepositoryImpl", "getTreeById($id) not implemented with Supabase yet")
        emit(null) // Placeholder
    }

     suspend fun updateTree(tree: Tree) {
        // TODO: Implement updateTree using Supabase
        Log.w("TreeRepositoryImpl", "updateTree not implemented with Supabase yet")
    }

     suspend fun deleteTree(treeId: String) { // Assuming ID is String
        // TODO: Implement deleteTree using Supabase
        Log.w("TreeRepositoryImpl", "deleteTree($treeId) not implemented with Supabase yet")
    }
}

package com.example.khadra.data.repository

import android.net.Uri
import com.example.khadra.data.model.Tree
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import android.util.Log
import com.example.khadra.data.remote.SupabaseTreeDataSource

class TreeRepositoryImpl @Inject constructor(
    private val supabaseDataSource: SupabaseTreeDataSource
) : TreeRepository {

    override suspend fun getTrees(): Flow<List<Tree>> = flow {
        try {
            Log.d("TreeRepositoryImpl", "Fetching trees via SupabaseDataSource...")
            val trees = supabaseDataSource.getTrees()
            Log.d("TreeRepositoryImpl", "Successfully fetched ${trees.size} trees.")
            emit(trees)
        } catch (e: Exception) {
            Log.e("TreeRepositoryImpl", "Error fetching trees from Supabase: ${e.message}", e)
            emit(emptyList())
        }
    }

    override suspend fun addTree(tree: Tree, imageUri: Uri?): Tree {
        try {
            Log.d("TreeRepositoryImpl", "Adding tree via SupabaseDataSource: $tree")
            val addedTree = supabaseDataSource.addTree(tree, imageUri)
            Log.i("TreeRepositoryImpl", "Tree added successfully via Supabase")
            return addedTree
        } catch (e: Exception) {
            Log.e("TreeRepositoryImpl", "Error adding tree via Supabase: ${e.message}", e)
            throw e
        }
    }

    override suspend fun updateTree(tree: Tree): Tree {
        // TODO: Implement updateTree using Supabase
        Log.w("TreeRepositoryImpl", "updateTree not implemented with Supabase yet")
        return tree
    }

    override suspend fun deleteTree(treeId: String) {
        // TODO: Implement deleteTree using Supabase
        Log.w("TreeRepositoryImpl", "deleteTree($treeId) not implemented with Supabase yet")
    }
}

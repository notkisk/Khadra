package com.example.khadra.data.repository

import android.net.Uri
import com.example.khadra.data.model.Tree
import kotlinx.coroutines.flow.Flow

interface TreeRepository {
    suspend fun addTree(tree: Tree, imageUri: Uri? = null): Tree
    suspend fun getTrees(): List<Tree>
    suspend fun updateTree(tree: Tree): Tree
    suspend fun deleteTree(treeId: String)
}

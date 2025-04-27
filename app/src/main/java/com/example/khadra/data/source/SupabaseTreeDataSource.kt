package com.example.khadra.data.source

import android.net.Uri
import com.example.khadra.SupabaseClientProvider
import com.example.khadra.data.model.IrrigationHistory
import com.example.khadra.data.model.Location
import com.example.khadra.data.model.Tree
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject

class SupabaseTreeDataSource @Inject constructor() : TreeDataSource {
    private val client = SupabaseClientProvider.client

    override suspend fun getTrees(): List<Tree> {
        return client.postgrest["trees"].select().decodeList()
    }

    override suspend fun addTree(tree: Tree, imageUri: Uri?): Tree {
        return client.postgrest["trees"].insert(tree).decodeSingle()
    }

    override suspend fun updateTree(tree: Tree): Tree {
        return client.postgrest["trees"]
            .update(value = tree) {
                filter {
                    eq("id", tree.id)
                }
            }

            .decodeSingle()
    }

    override suspend fun deleteTree(treeId: String) {
        client.postgrest["trees"]
            .delete {
               filter {
                   eq("id", treeId)
               }
            }
    }

    override suspend fun getCurrentLocation(): Location {
        // TODO: Implement using device location services
        throw NotImplementedError()
    }

    override suspend fun addIrrigationHistory(history: IrrigationHistory): IrrigationHistory {
        return client.postgrest["irrigation_history"]
            .insert(history)
            .decodeSingle()
    }

    override suspend fun getIrrigationHistory(treeId: String): List<IrrigationHistory> {
        return client.postgrest["irrigation_history"]
            .select {
                filter {
                    eq("tree_id", treeId)
                }
                order("irrigation_date", Order.DESCENDING)
            }
            .decodeList()
    }
}

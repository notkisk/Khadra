package com.example.khadra.domain.usecase

import com.example.khadra.data.model.Tree
import com.example.khadra.data.repository.TreeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTreesUseCase @Inject constructor(
    private val repository: TreeRepository
) {
    suspend operator fun invoke(): Flow<List<Tree>> {
        return repository.getTrees()
    }
}
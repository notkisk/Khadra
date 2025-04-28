package com.example.khadra.data.manager

import android.util.Log
import com.example.khadra.data.model.Tree
import com.example.khadra.data.repository.TreeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

@Singleton
class TreeStatusManager @Inject constructor(
    private val repository: TreeRepository
) {
    private val _statusUpdateFlow = MutableStateFlow<Tree?>(null)
    val statusUpdateFlow = _statusUpdateFlow.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Default)
    private val CHECK_INTERVAL = TimeUnit.MINUTES.toMillis(15)

    private data class HeatZone(
        val lat: Double,
        val lng: Double,
        val factor: Double,
        val radius: Double
    )

    private val heatZones = listOf(
        HeatZone(33.3678, 6.8516, 0.7, 5.0),   // el oued city center - hottest
        HeatZone(33.3589, 6.8450, 0.75, 3.0),  // university area
        HeatZone(33.2833, 6.9000, 0.8, 4.0),   // bayadha area
        HeatZone(33.3500, 6.8667, 0.77, 3.5)   // industrial zone
    )

    init {
        startStatusCheck()
    }

    private fun startStatusCheck() {
        scope.launch {
            while (true) {
                checkAndUpdateTreeStatuses()
                delay(CHECK_INTERVAL)
            }
        }
    }

    private suspend fun checkAndUpdateTreeStatuses() {
        try {
            val trees = repository.getTrees()
            val now = Date()

            trees.forEach { tree ->
                try {
                    val newStatus = calculateTreeStatus(tree, now)
                    if (newStatus != tree.status) {
                        val updatedTree = tree.copy(
                            status = newStatus,
                            updatedAt = now
                        )
                        val syncedTree = repository.updateTree(updatedTree)
                        _statusUpdateFlow.emit(syncedTree)
                        Log.d(TAG, "Tree ${tree.id} status updated to $newStatus and synced with cloud")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating tree ${tree.id} status in cloud", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching trees from cloud", e)
        }
    }

    private fun calculateTreeStatus(tree: Tree, now: Date): String {
        val daysSinceLastIrrigation = TimeUnit.MILLISECONDS.toDays(
            now.time - tree.lastIrrigationAction.time
        )

        val (criticalDays, lowDays, moderateDays) = when (tree.type.lowercase()) {
            "palm" -> Triple(10, 7, 5)
            "evergreen" -> Triple(8, 6, 4)
            "fruit" -> Triple(5, 4, 2)
            else -> Triple(7, 5, 3)
        }

        val locationFactor = calculateLocationFactor(tree.coordinatesLat, tree.coordinatesLng)

        val adjustedCriticalDays = (criticalDays * locationFactor).toInt()
        val adjustedLowDays = (lowDays * locationFactor).toInt()
        val adjustedModerateDays = (moderateDays * locationFactor).toInt()

        return when {
            daysSinceLastIrrigation >= adjustedCriticalDays -> "Critical"
            daysSinceLastIrrigation >= adjustedLowDays -> "Low"
            daysSinceLastIrrigation >= adjustedModerateDays -> "Moderate"
            else -> "Healthy"
        }
    }

    private fun calculateLocationFactor(lat: Double, lng: Double): Double {
        var minDistance = Double.MAX_VALUE
        var closestZoneFactor = 0.85

        for (zone in heatZones) {
            val distance = calculateDistance(lat, lng, zone.lat, zone.lng)
            if (distance <= zone.radius) {
                val influence = 1 - (distance / zone.radius)
                val factor = zone.factor * influence + 0.85 * (1 - influence)
                if (distance < minDistance) {
                    minDistance = distance
                    closestZoneFactor = factor
                }
            }
        }

        return closestZoneFactor
    }

    private fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val latDiff = abs(lat1 - lat2) * 111.0
        val lngDiff = abs(lng1 - lng2) * 111.0 * kotlin.math.cos(Math.toRadians(lat1))
        return sqrt(latDiff.pow(2) + lngDiff.pow(2))
    }

    companion object {
        private const val TAG = "TreeStatusManager"
    }
}

package com.example.domain.model

import kotlin.random.Random

/**
 * Representation of a DNS SRV record for a container instance inside the Yanga Market superapp ecosystem.
 * Formatted as: _service._proto.name. TTL IN SRV priority weight port target
 */
data class DnsSrvRecord(
    val service: String,        // e.g. "hospital", "food", "wallet", "vibes"
    val protocol: String = "tcp", // e.g. "tcp", "http"
    val domain: String = "yanga.market",
    val priority: Int,          // Lower value is preferred
    val weight: Int,            // Relative weight for records with the same priority (higher is preferred)
    val port: Int,              // well-known or dynamic container port
    val target: String,         // host/container address e.g. "hospital-pod-a.yanga.local"
    var isHealthy: Boolean = true // dynamically mutable health status for high availability load balancing
) {
    /**
     * Formats the SRV record into standard DNS zone representation.
     */
    fun toDnsString(): String {
        return "_$service._$protocol.$domain. 3600 IN SRV $priority $weight $port $target"
    }

    /**
     * Returns the full URL string to reach this container instance.
     */
    fun getAddressUrl(): String {
        val scheme = if (protocol.contains("http", ignoreCase = true)) protocol else "http"
        return "$scheme://$target:$port"
    }
}

/**
 * Service Discovery Registry that manages registration of microservice container endpoints
 * and dynamically resolves requests modeled after standard DNS SRV record selection.
 */
class ServiceDiscoveryRegistry {

    private val records = mutableListOf<DnsSrvRecord>()

    /**
     * Registers a container instance's SRV record in the registry.
     */
    fun register(record: DnsSrvRecord) {
        // Avoid duplicates by target and port
        records.removeAll { it.target.equals(record.target, ignoreCase = true) && it.port == record.port }
        records.add(record)
    }

    /**
     * Unregisters a container record.
     */
    fun unregister(target: String, port: Int) {
        records.removeAll { it.target.equals(target, ignoreCase = true) && it.port == port }
    }

    /**
     * Helper to find all registered records.
     */
    fun getAllRecords(): List<DnsSrvRecord> = records.toList()

    /**
     * Updates the health status of a target container.
     */
    fun updateHealth(target: String, port: Int, isHealthy: Boolean) {
        records.find { it.target.equals(target, ignoreCase = true) && it.port == port }?.isHealthy = isHealthy
    }

    /**
     * Clears all records (useful for fresh boots or tests).
     */
    fun clear() {
        records.clear()
    }

    /**
     * Resolves and returns healthy SRV records matched by service, protocol, and domain.
     * Returned list is sorted according to standard RFC 2782 rules:
     * 1. Priority (lowest first).
     * 2. Within same priority, weight (higher first, or dynamically randomized).
     */
    fun resolveSrv(service: String, protocol: String = "tcp", domain: String = "yanga.market"): List<DnsSrvRecord> {
        return records.filter {
            it.service.equals(service, ignoreCase = true) &&
            it.protocol.equals(protocol, ignoreCase = true) &&
            it.domain.equals(domain, ignoreCase = true) &&
            it.isHealthy
        }.sortedWith(
            compareBy<DnsSrvRecord> { it.priority }
                .thenByDescending { it.weight }
        )
    }

    /**
     * Resolves a single optimal container connection address using standard dynamic weighted Selection.
     * If multiple endpoints have the same top priority, selects one probabilistically weighted by its SRV weight field.
     */
    fun discoverEndpoint(service: String, protocol: String = "tcp", domain: String = "yanga.market"): DnsSrvRecord? {
        val healthyMatches = records.filter {
            it.service.equals(service, ignoreCase = true) &&
            it.protocol.equals(protocol, ignoreCase = true) &&
            it.domain.equals(domain, ignoreCase = true) &&
            it.isHealthy
        }

        if (healthyMatches.isEmpty()) return null

        // Group by lowest priority number (highest actual priority)
        val lowestPriorityVal = healthyMatches.minOf { it.priority }
        val topPriorityGroup = healthyMatches.filter { it.priority == lowestPriorityVal }

        if (topPriorityGroup.size == 1) {
            return topPriorityGroup.first()
        }

        // Weighted random selection algorithm among the lowest-priority candidates
        val totalWeight = topPriorityGroup.sumOf { it.weight }
        if (totalWeight <= 0) {
            // Fallback to random uniform item if weight is 0
            return topPriorityGroup.randomOrNull()
        }

        val randomThreshold = Random.nextInt(totalWeight)
        var runningSum = 0
        for (item in topPriorityGroup) {
            runningSum += item.weight
            if (randomThreshold < runningSum) {
                return item
            }
        }

        return topPriorityGroup.lastOrNull()
    }
}

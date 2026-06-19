package com.example.domain.model

import java.util.UUID

/**
 * Represents a specific section/compartment within a level/floor of the Yanga commercial facility mall.
 * Holds rent pricing, availability indicators, and layout coordinates.
 */
data class FacilitySection(
    val floorIndex: Int,       // 0-indexed floor
    val sectionIndex: Int,     // 0-indexed section
    val locationCode: String,  // e.g. "F1-A", "F2-B"
    val rentAmt: Double,       // Monthly rent price in Naira or relative credit units
    val isAvailable: Boolean,  // True if open for booking/unoccupied
    val servicesSubtype: String // e.g. "Retail Outlet", "Food court kiosk", "Medical Suite"
) {
    /**
     * Formats helper output.
     */
    fun fetchCoordinateString(): String = "Floor ${floorIndex + 1}, Section ${('A' + sectionIndex)}"
}

/**
 * Manages the two-dimensional array representing facility floor rooms and
 * provides capabilities to query section details by literal codes or floor integers.
 */
class FacilityRentDirectory {

    // A two-dimensional array (Grid) containing facility section details.
    // 4 Floors (levels 1-4) x 3 sections per floor (Sections A, B, C)
    val layoutGrid: Array<Array<FacilitySection>> = Array(4) { floor ->
        Array(3) { section ->
            val floorLabel = floor + 1
            val sectionLetter = ('A' + section).toString()
            val code = "F$floorLabel-$sectionLetter"
            val rentPrice = 120000.0 * floorLabel + (section * 45000.0)
            
            // availability has mixed indicators (e.g., floor 1 is semi-vacant, higher floors have mixed states)
            val isAvailable = (floor + section) % 2 != 0 
            
            val subtitleType = when (floorLabel) {
                1 -> "Ground Level Retail & Grocers (High Traffic)"
                2 -> "Level 2 Boutique Outlets & Electronics"
                3 -> "Level 3 Wellness, Diagnostic Labs & Clinic Spaces"
                4 -> "Penthouse Hub, Workspace & Suya Grill Lounge"
                else -> "Standard Facility Space"
            }

            FacilitySection(
                floorIndex = floor,
                sectionIndex = section,
                locationCode = code,
                rentAmt = rentPrice,
                isAvailable = isAvailable,
                servicesSubtype = subtitleType
            )
        }
    }

    /**
     * Returns the raw 2D array representation.
     */
    fun fetchRawGrid(): Array<Array<FacilitySection>> = layoutGrid

    /**
     * Looks up details of a section dynamically by matching a 'location code' or floor.
     * Returns a matching section if the code matches "F1-A", "F2-B" etc.
     */
    fun findByLocationCode(code: String): FacilitySection? {
        val normalized = code.trim().uppercase().replace(" ", "")
        for (floor in layoutGrid) {
            for (section in floor) {
                // Support both exact e.g., "F1-A" or compact "F1A"
                if (section.locationCode == normalized || section.locationCode.replace("-", "") == normalized) {
                    return section
                }
            }
        }
        return null
    }

    /**
     * Looks up all section details on a given floor by 1-based floor number.
     * Returns list of matching sections.
     */
    fun findByFloorNumber(floorNum: Int): List<FacilitySection> {
        val floorIdx = floorNum - 1
        if (floorIdx in layoutGrid.indices) {
            return layoutGrid[floorIdx].toList()
        }
        return emptyList()
    }
}

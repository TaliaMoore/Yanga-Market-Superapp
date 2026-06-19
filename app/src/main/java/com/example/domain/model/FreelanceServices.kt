package com.example.domain.model

import java.util.UUID

enum class ServiceBookingStatus {
    ESCROW_HELD,     // Funds securely locked in the in-app Escrow Vault
    IN_PROGRESS,     // Freelancer actively working on the campaign/application
    MILESTONE_APPROVED, // At least one milestone verified by client
    FINISHED_RELEASED,  // Project fully accepted and escrow funds released to wallet
    CANCELLED_REFUNDED  // Cancelled work with full fallback refund
}

enum class MilestoneStatus {
    PENDING,
    SUBMITTED_FOR_REVIEW,
    APPROVED_AND_PAID
}

data class FreelancerReview(
    val id: String = UUID.randomUUID().toString(),
    val reviewerName: String,
    val rating: Int, // scale of 1-5
    val comment: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class ProjectMilestone(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val costAmount: Double,
    var status: MilestoneStatus = MilestoneStatus.PENDING
) {
    fun approve() {
        this.status = MilestoneStatus.APPROVED_AND_PAID
    }
    fun submit() {
        this.status = MilestoneStatus.SUBMITTED_FOR_REVIEW
    }
}

data class FreelancerProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val title: String,
    val avatarEmoji: String = "👨‍💻",
    val rating: Double,
    val basePrice: Double,
    val bio: String,
    val category: String, // e.g. "Software", "Event Prep", "Creative Arts", "Catering"
    val portfolioGallery: List<String>, // List of beautiful conceptual descriptors or mock images
    val serviceListings: List<String>,  // Specific customized package line-items
    val reviews: List<FreelancerReview> = emptyList()
) {
    fun calculateAverageRating(): Double {
        if (reviews.isEmpty()) return rating
        return reviews.map { it.rating }.average()
    }
}

data class EscrowProjectBooking(
    val id: String = "ESCR-${UUID.randomUUID().toString().take(6).uppercase()}",
    val freelancerId: String,
    val freelancerName: String,
    val selectedService: String,
    val totalAmountPaidToEscrow: Double,
    var currentStatus: ServiceBookingStatus = ServiceBookingStatus.ESCROW_HELD,
    val milestones: List<ProjectMilestone>,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun releaseEscrow(): Boolean {
        if (currentStatus == ServiceBookingStatus.FINISHED_RELEASED) return false
        milestones.forEach { it.approve() }
        currentStatus = ServiceBookingStatus.FINISHED_RELEASED
        return true
    }

    fun approveMilestone(milestoneId: String): Boolean {
        val found = milestones.find { it.id == milestoneId } ?: return false
        found.approve()
        
        // If all approved, auto release or transition state
        if (milestones.all { it.status == MilestoneStatus.APPROVED_AND_PAID }) {
            currentStatus = ServiceBookingStatus.FINISHED_RELEASED
        } else {
            currentStatus = ServiceBookingStatus.MILESTONE_APPROVED
        }
        return true
    }

    fun submitMilestone(milestoneId: String): Boolean {
        val found = milestones.find { it.id == milestoneId } ?: return false
        found.submit()
        return true
    }
}

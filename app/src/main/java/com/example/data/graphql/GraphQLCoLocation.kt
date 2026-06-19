package com.example.data.graphql

/**
 * Lead Software Architect: Dynamic Co-Location architecture pattern for Yanga Market superapp.
 * Since Jetpack Compose represents components as composable functions rather than React classes/objects,
 * this system provides clean component co-location of GraphQL fragments to keep component data requirements
 * modularized, separated, and co-located alongside the views that render them.
 */
interface GraphQLFragmentComponent {
    /**
     * The name or identifier of the UI component.
     */
    val componentName: String

    /**
     * The GraphQL fragment definition representing this specific module's co-located data requirements.
     */
    val graphQLFragment: String
}

/**
 * A central registry to manage co-located component fragments, enabling modular composition
 * similar to React's native component-level query fragment co-location model.
 */
object GraphQLCoLocationRegistry {
    private val registry = mutableMapOf<String, GraphQLFragmentComponent>()

    init {
        // Register default UI module components with their co-located data dependencies
        register(DashboardComponentFragment)
        register(FoodCatalogComponentFragment)
        register(FruitsCatalogComponentFragment)
        register(RetailCatalogsComponentFragment)
        register(HospitalServicesComponentFragment)
        register(EventsEngagementComponentFragment)
        register(WalletTransactionHistoryFragment)
    }

    fun register(component: GraphQLFragmentComponent) {
        registry[component.componentName] = component
    }

    fun getFragment(componentName: String): String? {
        return registry[componentName]?.graphQLFragment
    }

    fun buildQueryForScreen(screenName: String, rootQueryPattern: String): String {
        val rootFragment = getFragment(screenName) ?: ""
        return """
            $rootQueryPattern
            
            # Co-located fragments injected for $screenName
            $rootFragment
        """.trimIndent()
    }
}

// --- CO-LOCATED MODULE FRAGMENTS (PRECISELY DEFINING EACH COMPONENT'S GRAPHQL SCHEMA DEMANDS) ---

object DashboardComponentFragment : GraphQLFragmentComponent {
    override val componentName = "DashboardComponent"
    override val graphQLFragment = """
        fragment DashboardFields on UserProfile {
            id
            username
            walletBalance
            isVendor
            liveQuote {
                quote
                category
            }
        }
    """.trimIndent()
}

object FoodCatalogComponentFragment : GraphQLFragmentComponent {
    override val componentName = "FoodCatalogComponent"
    override val graphQLFragment = """
        fragment FoodCatalogFields on Food {
            name
            price
            category
            description
            isFruit
        }
    """.trimIndent()
}

object FruitsCatalogComponentFragment : GraphQLFragmentComponent {
    override val componentName = "FruitsCatalogComponent"
    override val graphQLFragment = """
        fragment FruitsCatalogFields on Food {
            name
            price
            category
            description
            isFruit
        }
    """.trimIndent()
}

object RetailCatalogsComponentFragment : GraphQLFragmentComponent {
    override val componentName = "RetailCatalogsComponent"
    override val graphQLFragment = """
        fragment RetailFields on RetailShop {
            name
            specialty
            distanceKm
            items {
                name
                price
                category
            }
        }
    """.trimIndent()
}

object HospitalServicesComponentFragment : GraphQLFragmentComponent {
    override val componentName = "HospitalServicesComponent"
    override val graphQLFragment = """
        fragment HospitalFields on Hospital {
            name
            location
            distanceKm
            specialties
        }
    """.trimIndent()
}

object EventsEngagementComponentFragment : GraphQLFragmentComponent {
    override val componentName = "EventsEngagementComponent"
    override val graphQLFragment = """
        fragment EventFields on Event {
            title
            host
            date
            time
            venue
            price
            rsvpCount
        }
    """.trimIndent()
}

object WalletTransactionHistoryFragment : GraphQLFragmentComponent {
    override val componentName = "WalletTransactionHistoryComponent"
    override val graphQLFragment = """
        fragment WalletTransactionFields on WalletTransaction {
            id
            amount
            type
            timestamp
            securityHash
            note
        }
    """.trimIndent()
}

package com.example.domain.model

import java.util.Locale

/**
 * Lead Software Architect: Domain business logic for Yanga Market superapp billing module.
 * Provides overloaded methods to handle multiple checkout/billing calculation scenarios.
 */
class Billing {

    /**
     * Scenario 1: Computes bill for a single item price.
     * @param price The cost of the single item.
     * @return Fully rounded price to 2 decimal places.
     */
    fun computeBill(price: Double): Double {
        return formatToTwoDecimals(price)
    }

    /**
     * Scenario 2: Computes bill for an item price with a given quantity.
     * @param price The unit cost of the item.
     * @param quantity The quantity of the item purchased.
     * @return Fully rounded total to 2 decimal places.
     */
    fun computeBill(price: Double, quantity: Int): Double {
        val total = price * quantity
        return formatToTwoDecimals(total)
    }

    /**
     * Scenario 3: Computes bill for an item price with a given quantity minus a coupon value.
     * @param price The unit cost of the item.
     * @param quantity The quantity of the item purchased.
     * @param couponValue The deduction value from coupon usage.
     * @return Fully rounded net total to 2 decimal places (guaranteed >= 0.0).
     */
    fun computeBill(price: Double, quantity: Int, couponValue: Double): Double {
        val total = (price * quantity) - couponValue
        val netTotal = if (total < 0.0) 0.0 else total
        return formatToTwoDecimals(netTotal)
    }

    /**
     * Rounds a double balance value to the second decimal place to ensure users
     * see a precise and human-readable balance format.
     */
    private fun formatToTwoDecimals(value: Double): Double {
        return String.format(Locale.US, "%.2f", value).toDouble()
    }
}

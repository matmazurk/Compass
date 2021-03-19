package com.mat.compass

import android.location.Location
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class UtilsTest {

    @Test
    fun `test angle between two locations`() {

        val firstLocation = mockk<Location>(relaxed = true)
        every { firstLocation.latitude } returnsMany listOf(0.0, 1.0, 0.0, 1.0)
        every { firstLocation.longitude } returnsMany listOf(0.0, 1.0, 0.0, 1.0)
        val secondLocation = mockk<Location>(relaxed = true)
        every { secondLocation.latitude } returnsMany listOf(1.0, 0.0, 0.0, 1.0)
        every { secondLocation.longitude } returnsMany listOf(1.0, 0.0, 1.0, -1.0)

        firstLocation.angleBetween(secondLocation).assertWithPercentageMargin(
            360.0 - 45, 10.0
        )
        firstLocation.angleBetween(secondLocation).assertWithPercentageMargin(
            45.0, 10.0
        )
        firstLocation.angleBetween(secondLocation).assertWithPercentageMargin(
            360.0 - 180, 10.0
        )
        firstLocation.angleBetween(secondLocation).assertWithPercentageMargin(
            360.0 - 270, 10.0
        )
    }

    private fun Double.assertWithPercentageMargin(expected: Double, margin: Double) {
        val upperBound = this + this * margin
        val lowerBound = this - this * margin
        assert(expected <= upperBound)
        assert(expected >= lowerBound)
    }
}

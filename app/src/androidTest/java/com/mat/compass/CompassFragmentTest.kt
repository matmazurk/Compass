package com.mat.compass

import android.Manifest
import android.location.Location
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.idling.CountingIdlingResource
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import com.mat.compass.fragments.CompassFragment
import io.mockk.every
import io.mockk.mockk
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

@RunWith(AndroidJUnit4::class)
class CompassFragmentTest {

    @get:Rule
    val permissionRule = GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION)
    private val mockedViewModel: CompassViewModel = mockk(relaxed = true)
    private lateinit var fragmentScenario: FragmentScenario<CompassFragment>
    private lateinit var distance: MutableLiveData<Int?>
    private lateinit var azimuth: MutableLiveData<Float>
    private var destination: Location? = null
    private val anglesToTest = listOf(0f, 47f, 99f, 138f, 193f, 245f, 312f)
    private var destinationPointerAngle: Float = 0f
    private lateinit var compassAnimationIdlingResource: CountingIdlingResource
    private lateinit var destinationPointerIdlingResource: CountingIdlingResource

    @Before
    fun setup() {
        val koinModule = module(true, true) {
            single { mockedViewModel }
        }
        loadKoinModules(koinModule)
        distance = MutableLiveData()
        azimuth = MutableLiveData(0f)
        destination = Location("")
        every { mockedViewModel.distance } returns distance
        every { mockedViewModel.azimuth } returns azimuth
        every { mockedViewModel.destination } returns destination
        every { mockedViewModel.destinationPointerAngle } returns destinationPointerAngle
        fragmentScenario = launchFragmentInContainer(
            themeResId = R.style.Theme_Compass
        )
        fragmentScenario.onFragment { fragment ->
            compassAnimationIdlingResource = fragment.compassAnimationIdlingResource
            destinationPointerIdlingResource = fragment.destinationPointerIdlingResource
        }
        IdlingRegistry.getInstance().register(compassAnimationIdlingResource)
        IdlingRegistry.getInstance().register(destinationPointerIdlingResource)
    }

    @After
    fun teardown() {
        IdlingRegistry.getInstance().unregister(compassAnimationIdlingResource)
        IdlingRegistry.getInstance().unregister(destinationPointerIdlingResource)
    }

    @Test
    fun test_enable_gps_button() {
        val distanceMatchingText = InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.distance_unknown)
        distance.postValue(null)
        fragmentScenario.onFragment {
            it.gpsEnabled = false
            it.setupViews()
        }
        onView(withId(R.id.bt_enable_gps))
            .check(matches(isDisplayed()))

        fragmentScenario.onFragment {
            it.gpsEnabled = true
        }

        onView(withId(R.id.bt_enable_gps))
            .perform(click())

        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val allowGpsBtn = device.findObject(
            By.res("android:id/button1")
        )
        allowGpsBtn?.click()
        onView(isRoot()).perform(waitFor(10))

        onView(withId(R.id.bt_enable_gps))
            .check(matches(not(isDisplayed())))
        onView(withId(R.id.layout_destination))
            .check(matches(isDisplayed()))
        onView(withId(R.id.mtv))
            .check(matches(withText(distanceMatchingText)))
    }

    @Test
    fun test_compass_rotation() {
        val initialCompassRotation = 45f
        anglesToTest.forEach { angle ->
            azimuth.postValue(angle)
            onView(withId(R.id.iv_compass))
                    .check(matches(isDisplayed()))
                    .check(matches(rotated(360 + initialCompassRotation - angle)))
        }
    }

    @Test
    fun test_destination_pointer_and_destination_layout_visible_when_permissions_and_gps_enabled() {
        val distanceValue = 321
        val matchingText = InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.distance_meters, distanceValue)
        assertLocationTurnedOn()
        distance.postValue(321)
        azimuth.postValue(0f)
        onView(withId(R.id.layout_destination))
            .check(matches(isDisplayed()))
        onView(withId(R.id.arrow))
            .check(matches(isDisplayed()))
        onView(withId(R.id.mtv))
            .check(matches(withText(matchingText)))
    }

    @Test
    fun test_destination_pointer_transition() {
        assertLocationTurnedOn()
        distance.postValue(43)
        anglesToTest.forEach { angle ->
            azimuth.postValue(angle)
            onView(withId(R.id.arrow))
                    .check(matches(isDisplayed()))
                    .check(matches(rotated(-angle)))
        }
    }

    private fun assertLocationTurnedOn() {
        if (!ApplicationProvider.getApplicationContext<App>().isGpsEnabled()) {
            onView(withId(R.id.bt_enable_gps))
                    .check(matches(isDisplayed()))
                    .perform(click())

            val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            val allowGpsBtn = device.findObject(
                    By.res("android:id/button1")
            )
            allowGpsBtn?.click()
            onView(isRoot()).perform(waitFor(10))
        }
    }
}
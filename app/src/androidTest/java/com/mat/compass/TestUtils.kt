package com.mat.compass

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

fun waitFor(delay: Long): ViewAction {
    return object : ViewAction {
        override fun perform(uiController: UiController?, view: View?) {
            uiController?.loopMainThreadForAtLeast(delay)
        }

        override fun getConstraints(): Matcher<View> {
            return ViewMatchers.isRoot()
        }

        override fun getDescription(): String {
            return "wait for " + delay + "milliseconds"
        }
    }
}

fun rotated(rotation: Float) = RotationMatcher(rotation)

class RotationMatcher(private val rotation: Float) : TypeSafeMatcher<View>() {
    override fun describeTo(description: Description) {
        description.appendText("is rotated for given angle")
    }

    override fun matchesSafely(view: View): Boolean {
        return view.rotation == rotation
    }
}

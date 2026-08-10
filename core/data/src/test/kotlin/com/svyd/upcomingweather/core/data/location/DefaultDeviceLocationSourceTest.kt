package com.svyd.upcomingweather.core.data.location

import com.svyd.upcomingweather.core.data.location.geocoder.ReverseGeocoder
import com.svyd.upcomingweather.core.data.location.permission.LocationPermission
import com.svyd.upcomingweather.core.data.location.position.PositionProvider
import com.svyd.upcomingweather.core.data.localsource.LocationPromptLocalSource
import com.svyd.upcomingweather.core.domain.failure.WeatherFailure
import com.svyd.upcomingweather.core.domain.model.Coordinates
import com.svyd.upcomingweather.core.domain.model.PlaceLabel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultDeviceLocationSourceTest {

    @Test
    fun `without permission nothing is asked of the platform`() = runTest {
        val positions = RecordingPositions()

        val thrown = runCatching { source(granted = false, positions = positions).currentPlace() }

        assertTrue(thrown.exceptionOrNull() is WeatherFailure.LocationPermissionMissing)
        assertFalse("no position should have been requested", positions.asked)
    }

    /** The half of the picture this layer owns; the rationale flag is the caller's. */
    @Test
    fun `a refusal reports whether the prompt has been shown before`() = runTest {
        val asked = source(granted = false, everAsked = true).failure()
        val never = source(granted = false, everAsked = false).failure()

        assertTrue((asked as WeatherFailure.LocationPermissionMissing).askedBefore)
        assertFalse((never as WeatherFailure.LocationPermissionMissing).askedBefore)
    }

    @Test
    fun `no position at all is reported as unavailable`() = runTest {
        val thrown = source(position = null).failure()

        assertTrue(thrown is WeatherFailure.LocationUnavailable)
    }

    @Test
    fun `a named position carries its name`() = runTest {
        val place = source(position = reading, name = "Budapest").currentPlace()

        assertEquals(PlaceLabel.Named("Budapest"), place.label)
    }

    /** A forecast for an unnamed position is still correct; only its title is vaguer. */
    @Test
    fun `a position nothing can name is still a place`() = runTest {
        val place = source(position = reading, name = null).currentPlace()

        assertEquals(PlaceLabel.NamelessCurrentLocation, place.label)
        assertEquals(Coordinates(47.5, 19.04), place.coordinates)
    }

    /** A coarse reading is good to about a kilometre, so the extra digits are not information. */
    @Test
    fun `the reading is cut to the precision it has`() = runTest {
        val place = source(position = reading).currentPlace()

        assertEquals(Coordinates(47.5, 19.04), place.coordinates)
    }

    @Test
    fun `southern and western readings round towards the right side of zero`() = runTest {
        val place = source(position = Coordinates(-33.86882, -151.20929)).currentPlace()

        assertEquals(Coordinates(-33.87, -151.21), place.coordinates)
    }

    /** Rounding after naming, so a kilometre of slack cannot move the answer to another district. */
    @Test
    fun `the name is looked up from the reading, not from the rounded one`() = runTest {
        val geocoder = RecordingGeocoder(name = "Budapest")

        source(position = reading, geocoder = geocoder).currentPlace()

        assertEquals(reading, geocoder.asked)
    }

    @Test
    fun `a granted permission never consults the prompt flag`() = runTest {
        val prompts = RecordingPrompts()

        source(position = reading, prompts = prompts).currentPlace()

        assertNull("the flag is only for explaining a refusal", prompts.read)
    }

    private suspend fun DeviceLocationSource.failure(): Throwable? =
        runCatching { currentPlace() }.exceptionOrNull()

    private fun source(
        granted: Boolean = true,
        position: Coordinates? = reading,
        name: String? = "Budapest",
        everAsked: Boolean = false,
        geocoder: ReverseGeocoder = RecordingGeocoder(name),
        positions: PositionProvider = RecordingPositions(position),
        prompts: LocationPromptLocalSource = RecordingPrompts(everAsked),
    ): DeviceLocationSource = DefaultDeviceLocationSource(
        permission = { granted },
        positions = positions,
        geocoder = geocoder,
        prompts = prompts,
    )

    private class RecordingPositions(private val position: Coordinates? = null) : PositionProvider {
        var asked = false
            private set

        override suspend fun currentPosition(): Coordinates? {
            asked = true
            return position
        }
    }

    private class RecordingGeocoder(private val name: String?) : ReverseGeocoder {
        var asked: Coordinates? = null
            private set

        override suspend fun name(at: Coordinates): String? {
            asked = at
            return name
        }
    }

    private class RecordingPrompts(private val everAsked: Boolean = false) :
        LocationPromptLocalSource {
        var read: Boolean? = null
            private set

        override suspend fun everAsked(): Boolean = everAsked.also { read = it }

        override suspend fun recordAsked() = Unit
    }

    private companion object {
        val reading = Coordinates(47.49791, 19.04023)
    }
}

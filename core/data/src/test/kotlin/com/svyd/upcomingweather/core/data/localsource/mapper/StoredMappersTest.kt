package com.svyd.upcomingweather.core.data.localsource.mapper

import com.svyd.upcomingweather.core.data.localsource.dto.StoredLabel
import com.svyd.upcomingweather.core.domain.model.Coordinates
import com.svyd.upcomingweather.core.domain.model.Place
import com.svyd.upcomingweather.core.domain.model.PlaceLabel
import com.svyd.upcomingweather.core.domain.model.SelectedPlace
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Round trips, because a field dropped on the way down is invisible everywhere else: the sources
 * are tested against stored types directly, so only these mappers can lose one.
 */
class StoredMappersTest {

    @Test
    fun `a place survives being written and read`() {
        val place = Place(
            id = "4517009",
            name = "London",
            region = "Ohio",
            country = "United States",
            coordinates = Coordinates(latitude = 39.8865, longitude = -83.4483),
        )

        assertEquals(place, place.toStored().toPlace())
    }

    /** Most places outside the United States have no region, and the subtitle reads without one. */
    @Test
    fun `a place with no region survives too`() {
        val place = Place(
            id = "2267057",
            name = "Lisbon",
            region = null,
            country = "Portugal",
            coordinates = Coordinates(latitude = 38.71667, longitude = -9.13333),
        )

        assertEquals(place, place.toStored().toPlace())
    }

    @Test
    fun `a named selection survives`() {
        val selection = SelectedPlace(
            label = PlaceLabel.Named("Budapest"),
            coordinates = Coordinates(latitude = 47.5, longitude = 19.04),
        )

        assertEquals(selection, selection.toStored().toSelectedPlace())
    }

    /** The case the label type exists for: a position nothing could name. */
    @Test
    fun `an unnamed position survives as unnamed`() {
        val selection = SelectedPlace(
            label = PlaceLabel.NamelessCurrentLocation,
            coordinates = Coordinates(latitude = 47.5, longitude = 19.04),
        )

        val read = selection.toStored().toSelectedPlace()

        assertEquals(selection, read)
        assertEquals(PlaceLabel.NamelessCurrentLocation, read.label)
    }

    @Test
    fun `a name is what separates the two labels on the way down`() {
        assertEquals(StoredLabel(name = "Budapest"), PlaceLabel.Named("Budapest").toStored())
        assertEquals(StoredLabel(name = null), PlaceLabel.NamelessCurrentLocation.toStored())
    }

    /** A place named "null" is still a named place; only an absent name is the other case. */
    @Test
    fun `a stored label reads back as the case it was written from`() {
        assertEquals(PlaceLabel.Named("Budapest"), StoredLabel(name = "Budapest").toLabel())
        assertEquals(PlaceLabel.NamelessCurrentLocation, StoredLabel(name = null).toLabel())
    }
}

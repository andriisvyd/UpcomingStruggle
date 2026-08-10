package com.svyd.upcomingweather.core.data.mapper

import com.svyd.upcomingweather.core.data.cloud.dto.SearchResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaceMapperTest {

    @Test
    fun `search results become places, region and all`() {
        val response: SearchResponse =
            Fixtures.json.decodeFromString(Fixtures.read("search-budapest.json"))

        val places = response.toPlaces()

        val first = places.first()
        assertEquals("3054643", first.id)
        assertEquals("Budapest", first.name)
        assertEquals("Budapest", first.region)
        assertEquals("Hungary", first.country)
        assertEquals(47.49835, first.coordinates.latitude, 0.00001)
        assertEquals(19.04045, first.coordinates.longitude, 0.00001)
    }

    /** Five places called Budapest, told apart by where they are, not by what they are called. */
    @Test
    fun `places sharing a name keep their own ids`() {
        val response: SearchResponse =
            Fixtures.json.decodeFromString(Fixtures.read("search-budapest.json"))

        val budapests = response.toPlaces().filter { it.name == "Budapest" }

        assertTrue("the fixture holds more than one Budapest", budapests.size > 1)
        assertEquals(budapests.size, budapests.map { it.id }.toSet().size)
    }

    @Test
    fun `no results at all is an absent field, not an empty list`() {
        val response: SearchResponse = Fixtures.json.decodeFromString("""{"generationtime_ms":0.1}""")

        assertEquals(emptyList<Any>(), response.toPlaces())
    }
}

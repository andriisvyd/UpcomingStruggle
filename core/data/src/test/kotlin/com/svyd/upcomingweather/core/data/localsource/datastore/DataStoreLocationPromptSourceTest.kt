package com.svyd.upcomingweather.core.data.localsource.datastore

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DataStoreLocationPromptSourceTest {

    @get:Rule
    val folder = TemporaryFolder()

    @Test
    fun `location has not been asked for until it has`() = runTest {
        val source = DataStoreLocationPromptSource(folder.preferences(this))

        assertFalse(source.everAsked())
        source.recordAsked()
        assertTrue(source.everAsked())
    }
}

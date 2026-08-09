package com.svyd.upcomingweather.core.data.localsource.datastore

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.svyd.upcomingweather.core.data.localsource.dto.StoredLabel
import com.svyd.upcomingweather.core.data.localsource.dto.StoredSelection
import com.svyd.upcomingweather.core.data.mapper.Fixtures
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DataStoreSelectionSourceTest {

    @get:Rule
    val folder = TemporaryFolder()

    @Test
    fun `nothing is selected until something is`() = runTest {
        assertNull(source().selection.first())
    }

    @Test
    fun `a selection survives being written and read`() = runTest {
        val source = source()
        val selection = StoredSelection(StoredLabel("Budapest"), 47.49835, 19.04045)

        source.save(selection)

        assertEquals(selection, source.selection.first())
    }

    /** A store written by an older build is not a reason to fail to start. */
    @Test
    fun `a value that no longer decodes reads as absent`() = runTest {
        val store = folder.preferences(this)
        store.edit { it[stringPreferencesKey("selection")] = """{"unexpected":"shape"}""" }

        assertNull(DataStoreSelectionSource(store, Fixtures.json).selection.first())
    }

    private fun TestScope.source() = DataStoreSelectionSource(folder.preferences(this), Fixtures.json)
}

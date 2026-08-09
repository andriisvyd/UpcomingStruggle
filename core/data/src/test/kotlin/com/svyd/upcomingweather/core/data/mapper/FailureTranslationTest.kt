package com.svyd.upcomingweather.core.data.mapper

import com.svyd.upcomingweather.core.domain.failure.WeatherFailure
import kotlinx.serialization.SerializationException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class FailureTranslationTest {

    @Test
    fun `a dead network is reported as having no connection`() {
        listOf(
            UnknownHostException("api.open-meteo.com"),
            SocketTimeoutException("timeout"),
            IOException("socket closed"),
        ).forEach { thrown ->
            val failure = assertThrows(WeatherFailure.NoConnection::class.java) {
                translateFailures<Unit> { throw thrown }
            }
            assertSame(thrown, failure.cause)
        }
    }

    @Test
    fun `a provider that refuses is reported as unavailable`() {
        val thrown = HttpException(
            Response.error<Unit>(503, "".toResponseBody("application/json".toMediaType())),
        )

        val failure = assertThrows(WeatherFailure.ServiceUnavailable::class.java) {
            translateFailures<Unit> { throw thrown }
        }
        assertSame(thrown, failure.cause)
    }

    /**
     * A payload this app cannot read is this app's defect. Dressing it as a service failure would
     * offer a retry that can never work, and hide the bug behind it.
     */
    @Test
    fun `a payload that will not decode travels untouched`() {
        val thrown = SerializationException("missing field 'timezone'")

        val failure = assertThrows(SerializationException::class.java) {
            translateFailures<Unit> { throw thrown }
        }

        assertSame(thrown, failure)
        assertTrue(failure !is WeatherFailure)
    }

    @Test
    fun `nothing is translated when nothing goes wrong`() {
        assertEquals(7, translateFailures { 7 })
    }
}

package com.bird.StarrySkyTeaHouse.media

import org.junit.Assert.assertEquals
import org.junit.Test

class TeaMusicContractTest {
    @Test
    fun teaMusicUsesGentleBackgroundAndButtonVolumes() {
        assertEquals(0.16f, TeaMusicContract.BGM_VOLUME, 0.001f)
        assertEquals(0.4f, TeaMusicContract.BUTTON_TAP_VOLUME, 0.001f)
    }
}

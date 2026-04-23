package io.github.samson0720.cosmosmessenger.feature.favorites.collage

import org.junit.Assert.assertEquals
import org.junit.Test

class MemoryCollageTemplateTest {

    @Test
    fun entries_exposeThreeSelectableTemplates() {
        assertEquals(
            listOf(
                MemoryCollageTemplate.PolaroidOrbit,
                MemoryCollageTemplate.MissionBoard,
                MemoryCollageTemplate.CelestialJournal,
            ),
            MemoryCollageTemplate.entries.toList(),
        )
    }

    @Test
    fun entries_keepStableRendererFileNameFragments() {
        assertEquals("polaroidorbit", MemoryCollageTemplate.PolaroidOrbit.name.lowercase())
        assertEquals("missionboard", MemoryCollageTemplate.MissionBoard.name.lowercase())
        assertEquals("celestialjournal", MemoryCollageTemplate.CelestialJournal.name.lowercase())
    }
}

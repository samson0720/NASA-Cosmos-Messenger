package io.github.samson0720.cosmosmessenger.feature.chat.starcard.frame

import io.github.samson0720.cosmosmessenger.feature.chat.starcard.ZodiacSign
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ZodiacFrameSpecsTest {

    @Test
    fun allSigns_haveAFrameSpec() {
        val specs = ZodiacFrameSpecs.all

        assertEquals(ZodiacSign.entries.size, specs.size)
        assertEquals(ZodiacSign.entries.toSet(), specs.map { it.sign }.toSet())
    }

    @Test
    fun allSigns_haveATemplateResource() {
        val resourceIds = ZodiacSign.entries.map(ZodiacTemplateResources::resourceIdFor)

        assertEquals(ZodiacSign.entries.size, resourceIds.size)
        assertEquals(ZodiacSign.entries.size, resourceIds.toSet().size)
        resourceIds.forEach { resourceId ->
            assertTrue("template resource id should be generated", resourceId != 0)
        }
    }

    @Test
    fun allSpecs_haveConstellationGeometry() {
        ZodiacFrameSpecs.all.forEach { spec ->
            assertTrue("${spec.sign} should have stars", spec.stars.isNotEmpty())
            assertTrue("${spec.sign} should have lines", spec.lines.isNotEmpty())
            spec.lines.forEach { (start, end) ->
                assertTrue("${spec.sign} line start is out of range", start in spec.stars.indices)
                assertTrue("${spec.sign} line end is out of range", end in spec.stars.indices)
            }
        }
    }

    @Test
    fun allTemplateSpecs_fitWithinExportCanvas() {
        ZodiacFrameSpecs.all.forEach { spec ->
            with(spec.template) {
                assertTrue("${spec.sign} image should fit horizontally", imageCenterX - imageRadius >= 0f)
                assertTrue("${spec.sign} image should fit horizontally", imageCenterX + imageRadius <= CARD_WIDTH)
                assertTrue("${spec.sign} image should fit vertically", imageCenterY - imageRadius >= 0f)
                assertTrue("${spec.sign} image should fit vertically", imageCenterY + imageRadius < titleY)
                assertTrue("${spec.sign} title should appear before date", titleY < dateY)
                assertTrue("${spec.sign} date should appear before brand area", dateY < brandY)
                assertTrue("${spec.sign} brand area should fit card", brandY < CARD_HEIGHT)
            }
        }
    }

    private companion object {
        const val CARD_WIDTH = 1024f
        const val CARD_HEIGHT = 1536f
    }
}

package ch.stephgit.windescalator.alert.detail.direction

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DirectionTest {
    @Test
    fun `getByDegree returns matching cardinal and intercardinal directions`() {
        assertEquals(Direction.N, Direction.getByDegree(0))
        assertEquals(Direction.NE, Direction.getByDegree(45))
        assertEquals(Direction.E, Direction.getByDegree(90))
        assertEquals(Direction.SE, Direction.getByDegree(135))
        assertEquals(Direction.S, Direction.getByDegree(180))
        assertEquals(Direction.SW, Direction.getByDegree(225))
        assertEquals(Direction.W, Direction.getByDegree(270))
        assertEquals(Direction.NW, Direction.getByDegree(315))
    }

    @Test
    fun `getByDegree uses the next direction at an exclusive boundary`() {
        assertEquals(Direction.N, Direction.getByDegree(22))
        assertEquals(Direction.NE, Direction.getByDegree(23))
        assertEquals(Direction.N, Direction.getByDegree(360))
    }

    @Test
    fun `getByFullName returns matching direction or null`() {
        assertEquals(Direction.NW, Direction.getByFullName("Nordwest"))
        assertNull(Direction.getByFullName("Unbekannt"))
    }
}

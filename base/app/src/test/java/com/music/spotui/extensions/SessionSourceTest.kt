package com.music.spotui.extensions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** The audio source picked in the player for the session: first, then the user's order; gone with the source. */
class SessionSourceTest {
    private val a = "io.github.a.hifi"
    private val b = "io.github.b.tidal"
    private val c = "io.github.c.below"
    private val saved = listOf(a, b, SPOTIFY_SOURCE, c)
    private val on = { _: String -> true }

    @Test fun thePickedSourceComesFirstThenTheUsersOrder() {
        assertEquals(listOf(b, a, SPOTIFY_SOURCE, c), SessionSource.effective(saved, b))
        // One under Spotify, not asked in the user's order: it is, first.
        assertEquals(listOf(c, a, b, SPOTIFY_SOURCE), SessionSource.effective(saved, c))
        // Spotify first: no extension is asked before it.
        assertEquals(listOf(SPOTIFY_SOURCE, a, b, c), SessionSource.effective(saved, SPOTIFY_SOURCE))
    }

    @Test fun withoutOneTheOrderIsTheUsers() {
        assertEquals(saved, SessionSource.effective(saved, null))
        // Back to my order: nothing picked any more.
        assertEquals(saved, SessionSource.effective(saved, SessionSource.valid(null, saved, on)))
    }

    @Test fun aSourceTurnedOffOrRemovedLeavesIt() {
        // Turned off: no longer a lead, the user's order is back.
        assertNull(SessionSource.valid(b, saved, { it != b }))
        assertEquals(saved, SessionSource.effective(saved, SessionSource.valid(b, saved, { it != b })))
        // Removed (or no longer an audio source): not in the order, not a lead.
        val without = saved - b
        assertNull(SessionSource.valid(b, without, on))
        assertEquals(without, SessionSource.effective(without, b))
        // Spotify is always a source.
        assertEquals(SPOTIFY_SOURCE, SessionSource.valid(SPOTIFY_SOURCE, saved, { false }))
    }

    @Test fun pickingTheSourceTheOrderAsksFirstIsTheUsersOrder() {
        assertNull(SessionSource.pick(a, saved, on))
        assertEquals(b, SessionSource.pick(b, saved, on))
        assertEquals(SPOTIFY_SOURCE, SessionSource.pick(SPOTIFY_SOURCE, saved, on))
        // The first one turned off: Spotify is asked first after it, picking it changes nothing.
        assertNull(SessionSource.pick(SPOTIFY_SOURCE, listOf(a, SPOTIFY_SOURCE), { it != a }))
        // A source turned off cannot be picked.
        assertNull(SessionSource.pick(b, saved, { it != b }))
    }

    @Test fun theSheetOffersSpotifyAndTheSourcesOnInTheOrderAskedNow() {
        assertEquals(listOf(a, b, SPOTIFY_SOURCE, c), SessionSource.choices(saved, null, on))
        assertEquals(listOf(c, a, SPOTIFY_SOURCE), SessionSource.choices(saved, c, { it != b }))
        // The pick turned off meanwhile: the user's order, without it.
        assertEquals(listOf(a, SPOTIFY_SOURCE, c), SessionSource.choices(saved, b, { it != b }))
    }
}

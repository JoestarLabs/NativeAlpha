package com.cylonid.nativealpha.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SandboxManagerTest {
    private lateinit var sandboxManager: SandboxManager

    @Before
    fun setUp() {
        sandboxManager = SandboxManager.getInstance()
        sandboxManager.nextContainer = 0
        DataManager.getInstance().websites.clear()
        for (i in 0 until 8) {
            sandboxManager.unregisterWebAppFromSandbox(i)
        }
    }

    @Test
    fun testRegisterAndUnregisterSandboxTracksOwnership() {
        val appA =
            WebApp("https://a.example", 0, 0).apply {
                isUseContainer = true
                containerId = 2
            }
        val appB =
            WebApp("https://b.example", 1, 1).apply {
                isUseContainer = true
                containerId = 2
            }

        assertFalse(sandboxManager.isSandboxUsedByAnotherApp(appA))
        sandboxManager.registerWebAppToSandbox(appA)
        assertFalse(sandboxManager.isSandboxUsedByAnotherApp(appA))
        assertTrue(sandboxManager.isSandboxUsedByAnotherApp(appB))

        // Unregistering needs DataManager.websites populated for logging
        DataManager.getInstance().websites.clear()
        DataManager.getInstance().websites.addAll(listOf(appA, appB))
        sandboxManager.unregisterWebAppFromSandbox(2)
        assertFalse(sandboxManager.isSandboxUsedByAnotherApp(appB))
        DataManager.getInstance().websites.clear()
    }

    @Test
    fun testContainerIdAllocationCyclesAcrossAllEightSlots() {
        val allocated = (0 until 10).map { sandboxManager.calculateNextFreeContainerId() }
        assertEquals(listOf(0, 1, 2, 3, 4, 5, 6, 7, 0, 1), allocated)
    }

    @Test
    fun testOutOfBoundsContainerIdsAreHandledSafely() {
        sandboxManager.nextContainer = 99
        assertEquals(0, sandboxManager.calculateNextFreeContainerId())

        val invalidApp =
            WebApp("https://invalid.example", 0, 0).apply {
                containerId = -1
            }
        sandboxManager.registerWebAppToSandbox(invalidApp)
        assertFalse(sandboxManager.isSandboxUsedByAnotherApp(invalidApp))
        sandboxManager.unregisterWebAppFromSandbox(99)
    }

    @Test
    fun testUnregisterHandlesMissingWebAppWithoutNpe() {
        val orphan =
            WebApp("https://orphan.example", 42, 42).apply {
                isUseContainer = true
                containerId = 3
            }
        sandboxManager.registerWebAppToSandbox(orphan)
        DataManager.getInstance().websites.clear()
        sandboxManager.unregisterWebAppFromSandbox(3)
        assertFalse(sandboxManager.isSandboxUsedByAnotherApp(orphan))
    }

    @Test
    fun testCalculateNextFreeContainerIdSkipsAlreadyAssignedContainersEvenIfCounterResets() {
        val appA =
            WebApp("https://amazon.example", 0, 0).apply {
                isUseContainer = true
                containerId = 0
            }
        val appB =
            WebApp("https://github.example", 1, 1).apply {
                isUseContainer = true
                containerId = 2
            }
        DataManager.getInstance().websites.addAll(listOf(appA, appB))

        sandboxManager.nextContainer = 0
        assertEquals(1, sandboxManager.calculateNextFreeContainerId())
        assertEquals(3, sandboxManager.calculateNextFreeContainerId())
    }

    @Test
    fun testLoadAppDataDeduplicatesCollidingSandboxContainers() {
        val dm = DataManager.getInstance()
        dm.websites.clear()
        dm.websites.add(
            WebApp("https://amazon.example", 0, 0).apply {
                isUseContainer = true
                containerId = 0
            },
        )
        dm.websites.add(
            WebApp("https://github.example", 1, 1).apply {
                isUseContainer = true
                containerId = 0
            },
        )
        dm.saveWebAppData()

        dm.loadAppData()

        assertEquals(0, dm.websites[0].containerId)
        assertEquals(1, dm.websites[1].containerId)
    }
}

package com.cylonid.nativealpha.ui.pdf

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.cylonid.nativealpha.R
import com.cylonid.nativealpha.ui.theme.NativeAlphaTheme
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

class FakePdfRendererEngine(
    override val pageCount: Int = 3,
) : PdfRendererEngine {
    override suspend fun getPageDimensions(pageIndex: Int): Pair<Int, Int> = Pair(100, 140)

    override suspend fun renderPage(
        pageIndex: Int,
        targetWidth: Int,
    ): Bitmap = Bitmap.createBitmap(100, 140, Bitmap.Config.ARGB_8888)

    override fun close() {}
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PdfViewerScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testLoadingStateDisplaysProgressIndicatorAndText() {
        val snackbarHostState = SnackbarHostState()

        composeTestRule.setContent {
            NativeAlphaTheme {
                PdfViewerScreen(
                    fileName = "sample_test.pdf",
                    uiState = PdfViewerUiState.Loading,
                    onNavigateBack = {},
                    onSaveToDownloads = {},
                    onShare = {},
                    onOpenWith = {},
                    onRetry = {},
                    snackbarHostState = snackbarHostState,
                )
            }
        }

        composeTestRule.onNodeWithText("sample_test.pdf").assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.pdf_viewer_loading)).assertIsDisplayed()
    }

    @Test
    fun testErrorStateDisplaysMessageAndRetryButton() {
        val snackbarHostState = SnackbarHostState()
        var retryClicked = false

        composeTestRule.setContent {
            NativeAlphaTheme {
                PdfViewerScreen(
                    fileName = "failed_doc.pdf",
                    uiState = PdfViewerUiState.Error("Network connection timed out"),
                    onNavigateBack = {},
                    onSaveToDownloads = {},
                    onShare = {},
                    onOpenWith = {},
                    onRetry = { retryClicked = true },
                    snackbarHostState = snackbarHostState,
                )
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.pdf_viewer_error_loading)).assertIsDisplayed()
        composeTestRule.onNodeWithText("Network connection timed out").assertIsDisplayed()

        val retryBtn = composeTestRule.onNodeWithText(context.getString(R.string.pdf_viewer_retry))
        retryBtn.assertIsDisplayed()
        retryBtn.performClick()
        assertTrue("Retry callback should be triggered", retryClicked)
    }

    @Test
    fun testNavigateBackClick() {
        val snackbarHostState = SnackbarHostState()
        var backClicked = false

        composeTestRule.setContent {
            NativeAlphaTheme {
                PdfViewerScreen(
                    fileName = "doc.pdf",
                    uiState = PdfViewerUiState.Loading,
                    onNavigateBack = { backClicked = true },
                    onSaveToDownloads = {},
                    onShare = {},
                    onOpenWith = {},
                    onRetry = {},
                    snackbarHostState = snackbarHostState,
                )
            }
        }

        val backNode = composeTestRule.onNodeWithContentDescription(context.getString(R.string.back))
        backNode.assertIsDisplayed()
        backNode.performClick()
        assertTrue("Back callback should be triggered", backClicked)
    }

    @Test
    fun testReadyStateDisplaysActions() {
        val snackbarHostState = SnackbarHostState()
        var downloadClicked = false
        var shareClicked = false
        var openWithClicked = false

        val testFile = File(context.cacheDir, "test_ready.pdf")
        val helper = FakePdfRendererEngine(pageCount = 3)

        composeTestRule.setContent {
            NativeAlphaTheme {
                PdfViewerScreen(
                    fileName = "test_ready.pdf",
                    uiState = PdfViewerUiState.Ready(testFile, helper),
                    onNavigateBack = {},
                    onSaveToDownloads = { downloadClicked = true },
                    onShare = { shareClicked = true },
                    onOpenWith = { openWithClicked = true },
                    onRetry = {},
                    snackbarHostState = snackbarHostState,
                )
            }
        }

        composeTestRule.onNodeWithText("test_ready.pdf").assertIsDisplayed()

        val downloadBtn = composeTestRule.onNodeWithContentDescription(context.getString(R.string.pdf_viewer_save_download))
        downloadBtn.assertIsDisplayed()
        downloadBtn.performClick()
        assertTrue(downloadClicked)

        val shareBtn = composeTestRule.onNodeWithContentDescription(context.getString(R.string.pdf_viewer_share))
        shareBtn.assertIsDisplayed()
        shareBtn.performClick()
        assertTrue(shareClicked)

        val openWithBtn = composeTestRule.onNodeWithContentDescription(context.getString(R.string.pdf_viewer_open_external))
        openWithBtn.assertIsDisplayed()
        openWithBtn.performClick()
        assertTrue(openWithClicked)

        helper.close()
    }
}

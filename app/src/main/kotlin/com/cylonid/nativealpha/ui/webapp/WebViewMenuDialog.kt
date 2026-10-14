package com.cylonid.nativealpha.ui.webapp

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.cylonid.nativealpha.model.DataManager
import com.cylonid.nativealpha.model.WebApp
import com.cylonid.nativealpha.ui.theme.NativeAlphaTheme

class WebViewMenuDialogFragment : DialogFragment() {
    private var title: String = ""
    private var url: String = ""
    private var canGoBack: Boolean = true
    private var canGoForward: Boolean = false
    private var webAppId: Int = -1
    private var listener: WebViewMenuListener? = null

    companion object {
        private const val TAG = "WebViewMenuDialogFragment"
        private const val ARG_TITLE = "arg_title"
        private const val ARG_URL = "arg_url"
        private const val ARG_CAN_GO_BACK = "arg_can_go_back"
        private const val ARG_CAN_GO_FORWARD = "arg_can_go_forward"
        private const val ARG_WEBAPP_ID = "arg_webapp_id"

        @JvmStatic
        fun show(
            fragmentManager: FragmentManager,
            title: String,
            url: String,
            canGoBack: Boolean,
            canGoForward: Boolean,
            webApp: WebApp?,
            listener: WebViewMenuListener,
        ): WebViewMenuDialogFragment? {
            if (fragmentManager.isStateSaved) return null
            val existing = fragmentManager.findFragmentByTag(TAG) as? WebViewMenuDialogFragment
            if (existing != null) {
                existing.dismissAllowingStateLoss()
            }
            val dialog =
                WebViewMenuDialogFragment().apply {
                    this.listener = listener
                    arguments =
                        Bundle().apply {
                            putString(ARG_TITLE, title)
                            putString(ARG_URL, url)
                            putBoolean(ARG_CAN_GO_BACK, canGoBack)
                            putBoolean(ARG_CAN_GO_FORWARD, canGoForward)
                            putInt(ARG_WEBAPP_ID, webApp?.ID ?: -1)
                        }
                }
            dialog.show(fragmentManager, TAG)
            return dialog
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (listener == null && context is WebViewMenuListener) {
            listener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            title = args.getString(ARG_TITLE, "")
            url = args.getString(ARG_URL, "")
            canGoBack = args.getBoolean(ARG_CAN_GO_BACK, true)
            canGoForward = args.getBoolean(ARG_CAN_GO_FORWARD, false)
            webAppId = args.getInt(ARG_WEBAPP_ID, -1)
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            window.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.setDimAmount(0.35f)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val webApp = if (webAppId != -1) DataManager.getInstance().getWebApp(webAppId) else null
        return androidx.compose.ui.platform.ComposeView(requireContext()).apply {
            setViewCompositionStrategy(androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                NativeAlphaTheme {
                    WebViewMenuDialogContent(
                        title = title,
                        url = url,
                        canGoBack = canGoBack,
                        canGoForward = canGoForward,
                        webApp = webApp,
                        onBack = { listener?.onBack() },
                        onForward = { listener?.onForward() },
                        onReload = { listener?.onReload() },
                        onShare = { listener?.onShare() },
                        onCopyUrl = { listener?.onCopyUrl() },
                        onMainMenu = { listener?.onMainMenu() },
                        onFallbackContextMenu = { listener?.onFallbackContextMenu() },
                        onCloseWebApp = { listener?.onCloseWebApp() },
                        onDismiss = { dismiss() },
                    )
                }
            }
        }
    }
}

@Composable
fun WebViewMenuDialogContent(
    title: String,
    url: String,
    canGoBack: Boolean,
    canGoForward: Boolean,
    webApp: WebApp?,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onReload: () -> Unit,
    onShare: () -> Unit,
    onCopyUrl: () -> Unit,
    onMainMenu: () -> Unit,
    onFallbackContextMenu: () -> Unit,
    onCloseWebApp: () -> Unit,
    onDismiss: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .widthIn(max = 380.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
            border =
                BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                ),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 14.dp),
            ) {
                WebViewMenuHeader(
                    title = title,
                    url = url,
                    webApp = webApp,
                    onDismiss = onDismiss,
                )

                WebViewMenuQuickBar(
                    canGoBack = canGoBack,
                    canGoForward = canGoForward,
                    onBack = onBack,
                    onReload = onReload,
                    onForward = onForward,
                    onShare = onShare,
                    onCopyUrl = onCopyUrl,
                    onDismiss = onDismiss,
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                )

                WebViewMenuActionList(
                    onMainMenu = onMainMenu,
                    onFallbackContextMenu = onFallbackContextMenu,
                    onCloseWebApp = onCloseWebApp,
                    onDismiss = onDismiss,
                )
            }
        }
    }
}

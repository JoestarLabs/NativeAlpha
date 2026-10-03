package com.cylonid.nativealpha

import android.app.Activity
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.AppShortcut
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.fragment.app.DialogFragment
import com.cylonid.nativealpha.model.DataManager
import com.cylonid.nativealpha.model.WebApp
import com.cylonid.nativealpha.ui.theme.NativeAlphaTheme
import com.cylonid.nativealpha.util.App
import com.cylonid.nativealpha.util.Const
import com.cylonid.nativealpha.util.NotificationUtils
import com.cylonid.nativealpha.util.ShortcutIconUtils
import com.cylonid.nativealpha.util.WebViewLauncher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONException
import org.json.JSONObject
import org.jsoup.Jsoup
import java.net.HttpURLConnection
import java.net.URL
import java.util.TreeMap
import java.util.regex.Pattern

enum class IconFetchResult(
    @JvmField val index: Int,
) {
    FAVICON(0),
    TITLE(1),
    NEW_BASEURL(2),
}

class ShortcutDialogFragment : DialogFragment() {
    private var webapp: WebApp? = null
    var baseUrl: String = ""

    companion object {
        private const val ARG_WEBAPP_ID = "arg_webapp_id"

        @JvmStatic
        fun newInstance(webapp: WebApp): ShortcutDialogFragment =
            ShortcutDialogFragment().apply {
                this.webapp = webapp
                this.baseUrl = webapp.baseUrl
                runCatching {
                    arguments =
                        Bundle().apply {
                            putInt(ARG_WEBAPP_ID, webapp.ID)
                        }
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (webapp == null) {
            val id = arguments?.getInt(ARG_WEBAPP_ID, -1) ?: -1
            if (id != -1) {
                runCatching {
                    webapp = DataManager.getInstance().getWebApp(id)
                    baseUrl = webapp?.baseUrl.orEmpty()
                }
            }
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
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                NativeAlphaTheme {
                    ShortcutDialogContent(
                        webapp = webapp,
                        baseUrl = baseUrl,
                        onDismiss = { dismiss() },
                        onConfirm = { finalTitle, bitmap ->
                            addShortcutToHomeScreen(finalTitle, bitmap)
                            dismiss()
                        },
                        fetchWebappData = { fetchWebappData() },
                        loadBitmap = { url -> loadBitmap(url) },
                    )
                }
            }
        }

    private fun loadBitmap(strUrl: String?): Bitmap? {
        if (strUrl.isNullOrBlank()) return null
        var con: HttpURLConnection? = null
        return try {
            val url = URL(strUrl)
            con =
                (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 3000
                    readTimeout = 3000
                    instanceFollowRedirects = true
                    setRequestProperty("User-Agent", Const.DESKTOP_USER_AGENT)
                }
            val loaded =
                con.inputStream.use { stream ->
                    BitmapFactory.decodeStream(stream)
                } ?: return null
            val downscaled = ShortcutIconUtils.downscaleIfNecessary(loaded, ShortcutIconUtils.MAX_ICON_DIMENSION)
            if (downscaled.width < Const.FAVICON_MIN_WIDTH) null else downscaled
        } catch (_: Exception) {
            null
        } finally {
            con?.disconnect()
        }
    }

    private fun buildIconMap(): TreeMap<Int, String> {
        val foundIcons = TreeMap<Int, String>()
        if (baseUrl.isEmpty()) return foundIcons
        val hostPart =
            baseUrl
                .replace("http://", "")
                .replace("https://", "")
                .replace("www.", "")

        if (hostPart.startsWith("amazon.")) {
            foundIcons[300] = "https://upload.wikimedia.org/wikipedia/commons/d/de/Amazon_icon.png"
        }
        if (hostPart.startsWith("paypal.")) {
            foundIcons[196] = "https://www.paypalobjects.com/webstatic/icon/pp196.png"
        }
        if (hostPart.startsWith("google.")) {
            foundIcons[240] = "https://www.gstatic.com/images/branding/googleg/2x/googleg_standard_color_120dp.png"
        }
        if (hostPart.startsWith("anchor.fm")) {
            foundIcons[Int.MAX_VALUE] = "https://d12xoj7p9moygp.cloudfront.net/favicon/apple-touch-icon-wave-152x152.png"
        }
        if (hostPart.startsWith("oebb.at")) {
            foundIcons[Int.MAX_VALUE] = "https://www.oebb.at/.resources/pv-2017/themes/images/favicons/android-chrome-192x192.png"
        }
        if (hostPart.startsWith("oe3.orf.at")) {
            foundIcons[Int.MAX_VALUE] = "https://tubestatic.orf.at/mojo/1_3/storyserver//tube/common/images/apple-icons/oe3.png"
        }
        return foundIcons
    }

    fun fetchWebappData(): Array<String?> {
        val result = arrayOfNulls<String>(3)
        val foundIcons = buildIconMap()

        try {
            var doc =
                Jsoup
                    .connect(baseUrl)
                    .ignoreHttpErrors(true)
                    .userAgent(Const.DESKTOP_USER_AGENT)
                    .followRedirects(true)
                    .get()

            // Step 1: Check for META Redirect
            val metaTags = doc.select("meta[http-equiv=refresh]")
            if (!metaTags.isEmpty()) {
                val metaTag = metaTags.first()
                val content = metaTag?.attr("content").orEmpty()
                val pattern = Pattern.compile(".*URL='?(.*)$", Pattern.CASE_INSENSITIVE)
                val m = pattern.matcher(content)
                val redirectUrl = if (m.matches()) m.group(1) else null
                if (!redirectUrl.isNullOrEmpty()) {
                    baseUrl = redirectUrl
                    doc = Jsoup.connect(baseUrl).followRedirects(true).get()
                }
            }

            // Step 2: Check PWA manifest
            val manifest = doc.select("link[rel=manifest]")
            if (!manifest.isEmpty()) {
                val mf = manifest.first()
                if (mf != null) {
                    val data =
                        Jsoup
                            .connect(mf.absUrl("href"))
                            .ignoreContentType(true)
                            .execute()
                            .body()
                    val json = JSONObject(data)

                    try {
                        result[IconFetchResult.TITLE.index] = json.getString("name")
                        val startUrl = json.getString("start_url")
                        if (startUrl.isNotEmpty()) {
                            val manifestBaseUrl = URL(mf.absUrl("href"))
                            val fullUrl = URL(manifestBaseUrl, startUrl)
                            result[IconFetchResult.NEW_BASEURL.index] = fullUrl.toString()
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                    try {
                        val manifestIcons = json.getJSONArray("icons")
                        for (i in 0 until manifestIcons.length()) {
                            val iconObj = manifestIcons.getJSONObject(i)
                            val iconHref = iconObj.getString("src")
                            val sizes = iconObj.getString("sizes")
                            val width = ShortcutIconUtils.getWidthFromIcon(sizes)
                            val manifestBaseUrl = URL(mf.absUrl("href"))
                            val fullUrl = URL(manifestBaseUrl, iconHref)
                            foundIcons[width] = fullUrl.toString()
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }

            // Step 3: Fallback to PNG icons
            if (foundIcons.isEmpty()) {
                val htmlTitle = doc.select("title")
                if (!htmlTitle.isEmpty()) {
                    result[IconFetchResult.TITLE.index] = htmlTitle.first()?.text()
                }

                val icons = doc.select("link[rel=icon]")
                icons.addAll(doc.select("link[rel=shortcut icon]"))
                if (icons.size < 3) {
                    val appleIcons = doc.select("link[rel=apple-touch-icon]")
                    val appleIconsPrec = doc.select("link[rel=apple-touch-icon-precomposed]")
                    icons.addAll(appleIcons)
                    icons.addAll(appleIconsPrec)
                }

                for (icon in icons) {
                    val iconHref = icon.absUrl("href")
                    val sizes = icon.attr("sizes")
                    if (sizes.isNotEmpty()) {
                        val width = ShortcutIconUtils.getWidthFromIcon(sizes)
                        foundIcons[width] = iconHref
                    } else {
                        foundIcons[1] = iconHref
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (foundIcons.isNotEmpty()) {
            val bestFit = foundIcons.lastEntry()
            if (bestFit != null) {
                result[IconFetchResult.FAVICON.index] = bestFit.value
            }
        }

        return result
    }

    private fun addShortcutToHomeScreen(
        finalTitleInput: String,
        bitmap: Bitmap?,
    ) {
        val currentActivity = activity ?: return
        val currentWebapp = webapp ?: return
        var finalTitle = finalTitleInput.trim()
        if (finalTitle.isEmpty()) {
            finalTitle = currentWebapp.title
        }
        if (finalTitle.isEmpty()) {
            finalTitle = "Unknown"
        }
        currentWebapp.title = finalTitle
        DataManager.getInstance().saveWebAppData()

        if (bitmap != null) {
            ShortcutIconUtils.saveIcon(currentActivity, currentWebapp.ID, bitmap)
        }

        val intent = WebViewLauncher.createWebViewIntent(currentWebapp, currentActivity) ?: return

        val icon: IconCompat =
            if (bitmap != null) {
                IconCompat.createWithBitmap(bitmap)
            } else {
                IconCompat.createWithResource(currentActivity, R.mipmap.native_alpha_shortcut)
            }

        if (ShortcutManagerCompat.isRequestPinShortcutSupported(currentActivity)) {
            val pinShortcutInfo =
                ShortcutInfoCompat
                    .Builder(currentActivity, finalTitle)
                    .setIcon(icon)
                    .setShortLabel(finalTitle)
                    .setLongLabel(finalTitle)
                    .setIntent(intent)
                    .build()
            val newScId = pinShortcutInfo.id
            val scManager = App.getAppContext().getSystemService(ShortcutManager::class.java)
            if (scManager?.pinnedShortcuts?.none { it.id == newScId } == true) {
                ShortcutManagerCompat.requestPinShortcut(currentActivity, pinShortcutInfo, null)
            } else {
                NotificationUtils.showToast(currentActivity, getString(R.string.shortcut_already_exists))
            }
        }
    }
}

@Composable
private fun ShortcutDialogContent(
    webapp: WebApp?,
    baseUrl: String,
    onDismiss: () -> Unit,
    onConfirm: (title: String, bitmap: Bitmap?) -> Unit,
    fetchWebappData: () -> Array<String?>,
    loadBitmap: (String?) -> Bitmap?,
) {
    val context = LocalContext.current
    val activity = context as? Activity
    var titleText by remember { mutableStateOf(webapp?.title.orEmpty()) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val iconPickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
        ) { uri: Uri? ->
            if (uri != null) {
                val customIcon =
                    ShortcutIconUtils.decodeSampledBitmapFromUri(
                        context,
                        uri,
                        ShortcutIconUtils.MAX_ICON_DIMENSION,
                        ShortcutIconUtils.MAX_ICON_DIMENSION,
                    )
                if (customIcon != null) {
                    bitmap = customIcon
                    isLoading = false
                } else {
                    if (activity != null) {
                        NotificationUtils.showToast(
                            activity,
                            context.getString(R.string.icon_not_found),
                            Toast.LENGTH_SHORT,
                        )
                    } else {
                        Toast.makeText(context, R.string.icon_not_found, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    LaunchedEffect(Unit) {
        val webAppData =
            withContext(Dispatchers.IO) {
                withTimeoutOrNull(5000L) {
                    fetchWebappData()
                }
            }

        var fetchedBitmap: Bitmap? = null
        val iconUrl = webAppData?.getOrNull(IconFetchResult.FAVICON.index)
        if (!iconUrl.isNullOrBlank()) {
            fetchedBitmap =
                withContext(Dispatchers.IO) {
                    loadBitmap(iconUrl)
                }
        }

        // Fallback to well-known favicon endpoints
        if (fetchedBitmap == null) {
            fetchedBitmap =
                withContext(Dispatchers.IO) {
                    val fallbacks = ShortcutIconUtils.getFallbackIconUrls(baseUrl)
                    for (fallbackUrl in fallbacks) {
                        val b = loadBitmap(fallbackUrl)
                        if (b != null) return@withContext b
                    }
                    null
                }
        }

        // Fallback to monogram if none found
        if (fetchedBitmap == null) {
            val fallbackTitle =
                webAppData?.getOrNull(IconFetchResult.TITLE.index)?.takeIf { it.isNotBlank() }
                    ?: webapp?.title?.takeIf { it.isNotBlank() }
                    ?: "Web App"
            fetchedBitmap = ShortcutIconUtils.createMonogramIcon(fallbackTitle, 192)
        }

        // Apply new base url if manifest had start_url
        val newBaseUrl = webAppData?.getOrNull(IconFetchResult.NEW_BASEURL.index)
        if (!newBaseUrl.isNullOrBlank() && webapp != null) {
            webapp.baseUrl = newBaseUrl
            DataManager.getInstance().saveWebAppData()
        }

        // Apply title if fetched
        val fetchedTitle = webAppData?.getOrNull(IconFetchResult.TITLE.index)
        if (!fetchedTitle.isNullOrBlank() &&
            (titleText.isBlank() || titleText == baseUrl.removePrefix("http://").removePrefix("https://").removePrefix("www."))
        ) {
            titleText = fetchedTitle
        }

        bitmap = fetchedBitmap
        isLoading = false
    }

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.create_shortcut_on_home_screen),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier =
                        Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(20.dp),
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(36.dp),
                            strokeWidth = 3.5.dp,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    } else if (bitmap != null) {
                        Image(
                            bitmap = bitmap!!.asImageBitmap(),
                            contentDescription = stringResource(R.string.shortcut_icon),
                            modifier = Modifier.size(64.dp),
                            contentScale = ContentScale.Fit,
                        )
                    } else {
                        Image(
                            painter = painterResource(R.mipmap.native_alpha_shortcut),
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            contentScale = ContentScale.Fit,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                FilledTonalButton(
                    onClick = { iconPickerLauncher.launch("image/*") },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AddPhotoAlternate,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.custom_icon),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = titleText,
                    onValueChange = { titleText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.shortcut_title)) },
                    placeholder = { Text(stringResource(R.string.loading_icon_and_website_title)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.AppShortcut,
                            contentDescription = null,
                        )
                    },
                    trailingIcon = {
                        if (titleText.isNotEmpty()) {
                            IconButton(onClick = { titleText = "" }) {
                                Icon(
                                    imageVector = Icons.Rounded.Clear,
                                    contentDescription = stringResource(R.string.cancel),
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done,
                        ),
                    keyboardActions =
                        KeyboardActions(
                            onDone = {
                                onConfirm(titleText, bitmap)
                            },
                        ),
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = stringResource(android.R.string.cancel),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(titleText, bitmap) },
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(
                            text = stringResource(android.R.string.ok),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }
    }
}

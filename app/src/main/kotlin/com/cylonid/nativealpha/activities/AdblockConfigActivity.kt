package com.cylonid.nativealpha.activities

import android.app.ActivityManager
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.cylonid.nativealpha.R
import com.cylonid.nativealpha.databinding.AdblockConfigActivityBinding
import com.cylonid.nativealpha.databinding.AddAdblockConfigDialogBinding
import com.cylonid.nativealpha.fragments.adblocklist.AdblockListFragment
import com.cylonid.nativealpha.model.AdblockConfig
import com.cylonid.nativealpha.model.DataManager
import com.cylonid.nativealpha.util.Const
import com.cylonid.nativealpha.util.NotificationUtils
import com.cylonid.nativealpha.util.ProcessUtils

class AdblockConfigActivity : ToolbarBaseActivity<AdblockConfigActivityBinding>() {
    private lateinit var adblockListFragment: AdblockListFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.adblockFab.setOnClickListener { showAddAdblockDialog() }

        binding.btnRestoreDefault.setOnClickListener {
            DataManager.getInstance().apply {
                settings.globalWebApp.adBlockSettings = Const.getDefaultAdBlockConfig()
                saveGlobalSettings()
            }
            ProcessUtils.closeAllWebAppsAndProcesses(getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
            updateAdblockList()
        }

        setToolbarTitle(getString(R.string.adblock_config))

        adblockListFragment =
            supportFragmentManager.findFragmentById(R.id.adblock_fragment_container_view) as AdblockListFragment
    }

    override fun inflateBinding(layoutInflater: LayoutInflater): AdblockConfigActivityBinding =
        AdblockConfigActivityBinding.inflate(layoutInflater)

    private fun updateAdblockList() {
        adblockListFragment.updateAdblockList()
    }

    private fun showAddAdblockDialog() {
        val localBinding = AddAdblockConfigDialogBinding.inflate(layoutInflater)
        val dialog =
            AlertDialog
                .Builder(this)
                .setView(localBinding.root)
                .setTitle(getString(R.string.add_a_new_adblock_provider))
                .setPositiveButton(android.R.string.ok) { _: DialogInterface, _: Int ->

                    val url =
                        localBinding.addAdblockUrl.text
                            .toString()
                            .trim()

                    val formattedUrl =
                        if (url.startsWith("https://") || url.startsWith("http://")) url else "https://$url"

                    val urlAlreadyExists =
                        DataManager
                            .getInstance()
                            .settings.globalWebApp.adBlockSettings
                            .any { it.value == formattedUrl }
                    if (urlAlreadyExists) {
                        NotificationUtils.showToast(this, getString(R.string.entry_already_exists))
                        return@setPositiveButton
                    }

                    DataManager.getInstance().apply {
                        val label =
                            if (localBinding.addAdblockLabel.text.isNotEmpty()) localBinding.addAdblockLabel.text.toString() else url
                        settings.globalWebApp.adBlockSettings += AdblockConfig(label, formattedUrl)
                        saveGlobalSettings()
                    }
                    updateAdblockList()
                }.setNegativeButton(android.R.string.cancel, null)
                .create()
        dialog.show()

        val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        okButton.isEnabled = false
        localBinding.addAdblockUrl.addTextChangedListener(
            object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    okButton.isEnabled = !s.isNullOrBlank()
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int,
                ) {}

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int,
                ) {}
            },
        )
    }
}

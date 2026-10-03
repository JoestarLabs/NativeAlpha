package com.cylonid.nativealpha.ui.webapplist.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cylonid.nativealpha.R

@Composable
fun AddWebAppDialog(
    onConfirm: (url: String, createShortcut: Boolean) -> Unit,
    onDismiss: () -> Unit,
    initialUrl: String = "",
) {
    var url by remember { mutableStateOf(initialUrl) }
    var createShortcut by remember { mutableStateOf(true) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val isValid = url.trim().isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.add_webapp),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                    label = { Text(stringResource(R.string.url)) },
                    placeholder = { Text("https://example.com") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Link,
                            contentDescription = null,
                        )
                    },
                    trailingIcon = {
                        if (url.isNotEmpty()) {
                            IconButton(onClick = { url = "" }) {
                                Icon(
                                    imageVector = Icons.Rounded.Clear,
                                    contentDescription = stringResource(R.string.cancel),
                                )
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Done,
                        ),
                    keyboardActions =
                        KeyboardActions(
                            onDone = {
                                if (isValid) {
                                    onConfirm(url.trim(), createShortcut)
                                }
                            },
                        ),
                )

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable { createShortcut = !createShortcut }
                            .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(R.string.create_shortcut_on_home_screen),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    Switch(
                        checked = createShortcut,
                        onCheckedChange = { createShortcut = it },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (isValid) {
                        onConfirm(url.trim(), createShortcut)
                    }
                },
                enabled = isValid,
            ) {
                Text(
                    text = stringResource(R.string.ok),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        },
    )
}

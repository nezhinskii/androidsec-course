package com.example.inventory.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inventory.InventoryTopAppBar
import com.example.inventory.R
import com.example.inventory.data.settings.AppSettings
import com.example.inventory.ui.AppViewModelProvider
import com.example.inventory.ui.navigation.NavigationDestination
import com.example.inventory.ui.theme.InventoryTheme

object SettingsDestination : NavigationDestination {
    override val route = "settings"
    override val titleRes = R.string.settings_title
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val settings by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            InventoryTopAppBar(
                title = stringResource(SettingsDestination.titleRes),
                canNavigateBack = true,
                navigateUp = navigateBack
            )
        },
        modifier = modifier
    ) { innerPadding ->
        SettingsBody(
            settings = settings,
            onHideSensitiveChanged = viewModel::onHideSensitiveDataChanged,
            onDisableSharingChanged = viewModel::onDisableSharingChanged,
            onUseDefaultQuantityChanged = viewModel::onUseDefaultQuantityChanged,
            onDefaultQuantityChanged = viewModel::onDefaultQuantityChanged,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        )
    }
}

@Composable
private fun SettingsBody(
    settings: AppSettings,
    onHideSensitiveChanged: (Boolean) -> Unit,
    onDisableSharingChanged: (Boolean) -> Unit,
    onUseDefaultQuantityChanged: (Boolean) -> Unit,
    onDefaultQuantityChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingsSwitch(
            label = stringResource(R.string.hide_sensitive_data),
            checked = settings.hideSensitiveData,
            onCheckedChange = onHideSensitiveChanged
        )

        SettingsSwitch(
            label = stringResource(R.string.disable_sharing),
            checked = settings.disableSharing,
            onCheckedChange = onDisableSharingChanged
        )

        SettingsSwitch(
            label = stringResource(R.string.use_default_quantity),
            checked = settings.useDefaultQuantity,
            onCheckedChange = onUseDefaultQuantityChanged
        )

        if (settings.useDefaultQuantity) {
            var text by remember { mutableStateOf(settings.defaultQuantity.toString()) }

            OutlinedTextField(
                value = text,
                onValueChange = { newText ->
                    text = newText.filter { it.isDigit() }
                    onDefaultQuantityChanged(text)
                },
                label = { Text(stringResource(R.string.default_quantity)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SettingsSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    InventoryTheme {
        SettingsBody(
            settings = AppSettings(),
            onDefaultQuantityChanged = {},
            onHideSensitiveChanged = {},
            onDisableSharingChanged = {},
            onUseDefaultQuantityChanged = {}
        )
    }
}
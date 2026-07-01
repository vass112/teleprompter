package com.teleprompter.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Overlay Defaults",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            SettingsCard {
                SettingsSliderItem(
                    title = "Default Font Size",
                    subtitle = "Adjust text size in overlay",
                    value = 18f,
                    onValueChange = {},
                    valueRange = 12f..32f,
                    suffix = "sp"
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsSliderItem(
                    title = "Default Scroll Speed",
                    subtitle = "Speed of auto-scroll",
                    value = 1f,
                    onValueChange = {},
                    valueRange = 0.25f..5f,
                    suffix = "x"
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsSliderItem(
                    title = "Overlay Opacity",
                    subtitle = "Transparency of floating overlay",
                    value = 0.8f,
                    onValueChange = {},
                    valueRange = 0.2f..1f,
                    suffix = "%"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Behavior",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            SettingsCard {
                SettingsSwitchItem(
                    title = "Dark Mode",
                    subtitle = "Use AMOLED black theme",
                    checked = true,
                    onCheckedChange = {},
                    icon = Icons.Default.DarkMode
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsSwitchItem(
                    title = "Remember Scroll Position",
                    subtitle = "Resume from where you left off",
                    checked = true,
                    onCheckedChange = {},
                    icon = Icons.Default.Bookmark
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsSwitchItem(
                    title = "Keep Screen On",
                    subtitle = "Prevent screen from turning off",
                    checked = true,
                    onCheckedChange = {},
                    icon = Icons.Default.ScreenLockPortrait
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Controls",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            SettingsCard {
                SettingsSwitchItem(
                    title = "Volume Button Control",
                    subtitle = "Use volume keys to control speed",
                    checked = false,
                    onCheckedChange = {},
                    icon = Icons.Default.VolumeUp
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsSwitchItem(
                    title = "Gesture Control",
                    subtitle = "Double tap to play/pause",
                    checked = true,
                    onCheckedChange = {},
                    icon = Icons.Default.TouchApp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Reading Aid",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            SettingsCard {
                SettingsSwitchItem(
                    title = "Reading Line Indicator",
                    subtitle = "Highlight current reading line",
                    checked = true,
                    onCheckedChange = {},
                    icon = Icons.Default.HorizontalRule
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsSwitchItem(
                    title = "Mirror Mode",
                    subtitle = "Flip text horizontally",
                    checked = false,
                    onCheckedChange = {},
                    icon = Icons.Default.Flip
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsSwitchItem(
                    title = "Countdown Before Scroll",
                    subtitle = "3-2-1 countdown before auto-scroll starts",
                    checked = false,
                    onCheckedChange = {},
                    icon = Icons.Default.Timer
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "About",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            SettingsCard {
                SettingsInfoItem(
                    title = "Version",
                    subtitle = "1.0.0"
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsInfoItem(
                    title = "Made for content creators",
                    subtitle = "Record with any camera app"
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(content = content)
    }
}

@Composable
fun SettingsSliderItem(
    title: String,
    subtitle: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    suffix: String,
    modifier: Modifier = Modifier
) {
    var sliderValue by remember { mutableStateOf(value) }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            Text(
                "${if (suffix == "%") (sliderValue * 100).toInt().toString() else String.format("%.1f", sliderValue)}$suffix",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it; onValueChange(it) },
            valueRange = valueRange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SettingsSwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SettingsInfoItem(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

package com.rinchhen.vezzremote.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rinchhen.vezzremote.data.AppCategory
import com.rinchhen.vezzremote.data.TvApp
import com.rinchhen.vezzremote.ui.components.AppTile
import com.rinchhen.vezzremote.ui.components.PageTitle
import com.rinchhen.vezzremote.ui.theme.Acid
import com.rinchhen.vezzremote.ui.theme.Line
import com.rinchhen.vezzremote.ui.theme.Paper
import com.rinchhen.vezzremote.ui.theme.PaperMuted
import kotlinx.coroutines.delay

private enum class AppFilter(val title: String) {
    All("All"),
    Favorites("Favorites"),
    Recent("Recent"),
    Media("Media"),
    Tools("Tools"),
}

@Composable
fun AppsContent(
    apps: List<TvApp>,
    currentApp: String,
    recentPackages: List<String>,
    onLaunchApp: (TvApp) -> Unit,
    onToggleFavorite: (TvApp) -> Unit,
    onRefresh: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf(AppFilter.All) }
    var launching by rememberSaveable { mutableStateOf<String?>(null) }

    val recentRank = recentPackages.withIndex().associate { it.value to it.index }
    val filtered = apps
        .filter { app ->
            when (filter) {
                AppFilter.All -> true
                AppFilter.Favorites -> app.favorite
                AppFilter.Recent -> app.packageName in recentRank
                AppFilter.Media -> app.category == AppCategory.Media
                AppFilter.Tools -> app.category == AppCategory.Tools
            }
        }
        .filter { app ->
            query.isBlank() || app.label.contains(query.trim(), ignoreCase = true)
        }
        .let { list ->
            if (filter == AppFilter.Recent) {
                list.sortedBy { recentRank[it.packageName] ?: Int.MAX_VALUE }
            } else {
                list
            }
        }

    LaunchedEffect(launching) {
        if (launching != null) {
            delay(1_500)
            launching = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        PageTitle(
            title = "Apps",
            subtitle = "${apps.size} apps",
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                placeholder = { Text("SEARCH APPS", style = MaterialTheme.typography.labelSmall) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Acid,
                    unfocusedBorderColor = Line,
                    cursorColor = Acid,
                    focusedTextColor = Paper,
                    unfocusedTextColor = Paper,
                ),
            )
            IconButton(onClick = onRefresh) {
                Icon(Icons.Outlined.Refresh, contentDescription = "Refresh apps from TV", tint = PaperMuted)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            AppFilter.entries.forEach { item ->
                FilterTab(
                    title = item.title,
                    selected = filter == item,
                    onClick = { filter = item },
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp),
        ) {
            launching?.let { name ->
                Text(
                    "LAUNCHING ${name.uppercase()}…",
                    style = MaterialTheme.typography.labelSmall,
                    color = Acid,
                    maxLines = 1,
                )
            }
        }

        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.TopCenter) {
                Text(
                    "NO APPS MATCH",
                    style = MaterialTheme.typography.labelSmall,
                    color = PaperMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 28.dp),
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(76.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 28.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(filtered, key = { it.packageName }) { app ->
                    AppTile(
                        app = app,
                        running = app.packageName == currentApp,
                        onClick = {
                            launching = app.label
                            onLaunchApp(app)
                        },
                        onLongClick = { onToggleFavorite(app) },
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterTab(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .height(48.dp)
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) Acid else PaperMuted,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(if (selected) Acid else Color.Transparent),
        )
    }
}

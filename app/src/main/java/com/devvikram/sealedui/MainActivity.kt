package com.devvikram.sealedui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.devvikram.alogger.Alogger
import com.devvikram.alogger.FileLogAdapter
import com.devvikram.alogger.LogLevel
import com.devvikram.alogger.PrettyFormatter
import com.devvikram.alogger.logD
import com.devvikram.alogger.logE
import com.devvikram.alogger.logI
import com.devvikram.alogger.logV
import com.devvikram.alogger.logW
import com.devvikram.sealedui.ui.theme.ShimmyAppTheme
import java.io.File

class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ============ Initialize Logger ============
        setupLogger()

        logI { "MainActivity created" }
        enableEdgeToEdge()

        setContent {
            ShimmyAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        val state by viewModel.uiState.collectAsState()

                        logD { "UI State: ${state::class.simpleName}" }
                        Column(
                            modifier = Modifier.fillMaxSize().padding(innerPadding),
                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(
                                16.dp,
                                androidx.compose.ui.Alignment.CenterVertically
                            )
                        ) {
                            LoggerTestScreen()
                            StateLayout(
                                state = state,
                                modifier = Modifier.padding(16.dp),
                                onRetry = {
                                    logI { "Retrying data load" }
                                    viewModel.load()
                                },
                                errorContent = { throwable, message, retry ->
                                    logE({ "Error in StateLayout: $message" }, throwable)
                                    Text(
                                        text = "Error occurred: $message",
                                        modifier = Modifier.padding(16.dp)
                                    )
                                },
                                emptyContent = { message ->
                                    logI { "Empty state: $message" }
                                    Text(
                                        text = "No data available: $message",
                                        modifier = Modifier.padding(16.dp)
                                    )
                                },
                            ) { items ->
                                logD { "Rendering ${items.size} items" }
                                LazyColumn {
                                    items(items.size) { index ->
                                        Text("Item: ${items[index]}")
                                        logV { "Item rendered: ${items[index]}" }
                                        Divider()
                                    }
                                }
                            }
                        }


                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        logD { "MainActivity started" }
    }

    override fun onResume() {
        super.onResume()
        logV { "MainActivity resumed" }
    }

    override fun onPause() {
        logV { "MainActivity paused" }
        super.onPause()
    }

    override fun onDestroy() {
        logI { "MainActivity destroyed" }
        super.onDestroy()
    }

    // ============ Logger Setup ============

    private fun setupLogger() {
        // Initialize Alogger singleton
        Alogger.init(
            tag = "ShimmyApp",
            level = LogLevel.DEBUG,
            formatter = PrettyFormatter()
        )

        // Add file logging for persistence
        val logDir = File(cacheDir, "logs")
        val fileAdapter = FileLogAdapter(
            logDir = logDir,
            maxFileSizeBytes = 2 * 1024 * 1024, // 2MB
            maxFiles = 5
        )
        Alogger.get().addAdapter(fileAdapter)

        logI { "Logger initialized | Tag: ShimmyApp | Level: DEBUG" }
        logI { "Log files saved to: ${logDir.absolutePath}" }
    }
}

/**
 * Composable for testing all logger features
 */
@Composable
fun LoggerTestScreen(modifier: Modifier = Modifier) {
    var clickCount by remember { mutableStateOf(0) }
    var logHistoryCount by remember { mutableStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Alogger Integration Test",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ============ Basic Logging ============
        Text(
            text = "Basic Logging",
            style = MaterialTheme.typography.titleMedium
        )

        Button(
            onClick = {
                clickCount++
                logD { "Debug button clicked $clickCount times" }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Log Debug (Click: $clickCount)")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { logI { "Info button clicked" } },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Log Info")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { logW { "Warning button clicked" } },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Log Warning")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { logV { "Verbose button clicked" } },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Log Verbose")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ============ Error Logging ============
        Text(
            text = "Error Logging",
            style = MaterialTheme.typography.titleMedium
        )

        Button(
            onClick = {
                try {
                    throw Exception("Test exception from UI button")
                } catch (e: Exception) {
                    logE({ "An error occurred" }, e)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Log Error with Exception")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ============ History Management ============
        Text(
            text = "Log History",
            style = MaterialTheme.typography.titleMedium
        )

        Button(
            onClick = {
                val history = Alogger.get().getHistory()
                logHistoryCount = history.size
                logI { "Total logs in history: ${history.size}" }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Get History Count: $logHistoryCount")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                Alogger.get().clearHistory()
                logHistoryCount = 0
                logI { "History cleared" }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Clear History")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ============ Info ============
        Text(
            text = "📝 All logs are saved to cache/logs directory",
            style = MaterialTheme.typography.bodySmall
        )

        Text(
            text = "🔍 Check Logcat filter: 'ShimmyApp'",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ShimmyAppTheme {
        Greeting("Android")
    }
}
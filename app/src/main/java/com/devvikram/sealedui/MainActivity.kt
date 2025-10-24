package com.devvikram.sealedui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.devvikram.sealedui.ui.theme.ShimmyAppTheme

class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by lazy {
        AppViewModel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShimmyAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        val state by viewModel.uiState.collectAsState()
                        StateLayout(
                            state = state,
                            modifier = Modifier.padding(innerPadding),
                            onRetry = { viewModel.load() },
                            errorContent = {
                                throwable, message, retry ->
                                Log.e("StateLayout", "Error: $message", throwable)
                                Text("Error occurred: $message", modifier = Modifier.padding(16.dp))
                            },
                            emptyContent = { message ->
                                Text("No data available: $message", modifier = Modifier.padding(16.dp))
                            },
                        ) { items ->
                            LazyColumn {
                                items(items.size) { index ->
                                    Text("Item: ${items[index]}")
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
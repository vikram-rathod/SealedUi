package com.devvikram.shimmyy

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun <T> StateLayout(
    state: UiState<T>,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
    errorContent: (@Composable (throwable: Throwable?, message: String?, onRetry: (() -> Unit)?) -> Unit)? = null,
    emptyContent: (@Composable (message: String?) -> Unit)? = null,
    loadingContent: (@Composable () -> Unit)? = null,
    content: @Composable (T) -> Unit
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (state) {
            is UiState.Loading -> {
                (loadingContent ?: { DefaultLoading() })()
            }

            is UiState.Empty -> {
                (emptyContent ?: { DefaultEmpty(state.message) })(state.message)
            }

            is UiState.Error -> {
                (errorContent ?: { throwable, message, retry ->
                    DefaultError(message ?: throwable?.localizedMessage, retry)
                })(state.throwable, state.message, onRetry)
            }

            is UiState.Success -> {
                content(state.data)
            }
        }
    }
}

// ✅ Default composables
@Composable
private fun DefaultLoading() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Loading...", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun DefaultEmpty(message: String?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Inbox, contentDescription = null, modifier = Modifier.size(56.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = message ?: "Nothing to show", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun DefaultError(message: String?, onRetry: (() -> Unit)?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.ErrorOutline, contentDescription = null, modifier = Modifier.size(56.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = message ?: "Something went wrong", style = MaterialTheme.typography.bodyMedium)
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

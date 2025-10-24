# Sealed UI - Kotlin UI State Management Library

**Latest Version:** `v1.0.7`

**Sealed UI** is a lightweight Kotlin library that provides a **type-safe and flexible way to manage UI states** in Android and Kotlin projects using **sealed classes**. It helps developers handle **common UI states** such as Loading, Success, Empty, and Error in a clean and consistent way.

---

## Features

- **Type-safe UI states** using Kotlin sealed classes
- Handles **Loading, Success, Empty, and Error** states out of the box
- **Easy integration** with Jetpack Compose, ViewModel, or any reactive UI
- **Minimal and lightweight**, no unnecessary dependencies
- Improves **readability and maintainability** of your UI code
- Helps enforce **clean architecture principles** in UI layers

---

## Dependency

Add the **JitPack repository** in your project `build.gradle` (project-level):

```gradle
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
````

Add the **library dependency** in your module `build.gradle` (app-level):

```gradle
dependencies {
    implementation 'com.github.vikram-rathod:SealedUI:1.0.7'
}
```

---

## Usage

### 1. Define UI State in ViewModel

```kotlin
// File: MyViewModel.kt
import androidx.lifecycle.ViewModel
import com.devvikram.sealedui.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MyViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<List<String>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<String>>> = _uiState

    fun loadData() {
        try {
            val data = listOf("Item1", "Item2", "Item3")
            if (data.isEmpty()) {
                _uiState.value = UiState.Empty("No items found")
            } else {
                _uiState.value = UiState.Success(data)
            }
        } catch (e: Exception) {
            _uiState.value = UiState.Error(message = "Failed to load data")
        }
    }
}
```

### 2. Display UI State in Compose with `StateLayout`

```kotlin
// File: MyScreen.kt
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devvikram.sealedui.StateLayout
import com.devvikram.sealedui.UiState

@Composable
fun MyScreen(viewModel: MyViewModel = viewModel()) {
    val uiState = viewModel.uiState.collectAsState()

    StateLayout(state = uiState.value, onRetry = { viewModel.loadData() }) { data ->
        LazyColumn {
            items(data) { item ->
                Text(text = item)
            }
        }
    }
}
```

### Optional Customization

You can override the default Loading, Empty, or Error UI:

```kotlin
StateLayout(
    state = uiState.value,
    onRetry = { viewModel.loadData() },
    loadingContent = { Text("Please wait...") },
    emptyContent = { Text("Nothing here yet!") },
    errorContent = { throwable, message, retry ->
        Column {
            Text("Oops: ${message ?: throwable?.localizedMessage}")
            retry?.let { Button(onClick = it) { Text("Try again") } }
        }
    }
) { data ->
    LazyColumn {
        items(data) { item -> Text(item) }
    }
}
```

---

## Benefits

* **Simplifies UI state handling**: Avoids repetitive `if/else` logic in your UI
* **Improves code readability**: Each state is self-descriptive
* **Works with reactive flows**: Compatible with `StateFlow`, `LiveData`, or any reactive architecture
* **Clean separation of concerns**: UI only reacts to state changes

---

## Contributing

Contributions are welcome! Open issues or submit pull requests for improvements.

---

## License

MIT License © 2025 [Vikram Rathod](https://github.com/vikram-rathod)

```

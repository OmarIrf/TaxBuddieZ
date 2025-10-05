[versions]
compose-bom = "2024.06.00"
material3 = "1.2.1"
foundation = "1.6.8"

[libraries]
androidx.compose.bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
androidx.ui = { group = "androidx.compose.ui", name = "ui" }
androidx.ui.graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx.ui.tooling.preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx.ui.tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx.material3 = { group = "androidx.compose.material3", name = "material3", version.ref = "material3" }
androidx.foundation = { group = "androidx.compose.foundation", name = "foundation", version.ref = "foundation" }

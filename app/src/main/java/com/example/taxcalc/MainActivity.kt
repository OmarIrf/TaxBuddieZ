package com.example.taxcalc

// Compose foundation / layout

// Material 3

// Compose runtime

// UI utils

// Formatting / time
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AppRoot() }
    }
}

/* -------------------- Tiny navigation enum -------------------- */
private enum class Screen { WELCOME, CALC }

/* -------------------- App Root -------------------- */
@Composable
private fun AppRoot() {
    var screen by remember { mutableStateOf(Screen.WELCOME) }
    MaterialTheme {
        Surface(Modifier.fillMaxSize()) {
            when (screen) {
                Screen.WELCOME -> WelcomeScreen(onStart = { screen = Screen.CALC })
                Screen.CALC    -> TaxCalcScreen(onBack = { screen = Screen.WELCOME })
            }
        }
    }
}

/* -------------------- WELCOME -------------------- */
@Composable
private fun WelcomeScreen(onStart: () -> Unit) {
    val suggestions = remember { buddiezSuggestionsForToday() }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF6A1B9A) // purple background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("Welcome to", style = MaterialTheme.typography.titleMedium, color = Color.White)
            Text("TaxBuddieZ", style = MaterialTheme.typography.displaySmall, color = Color.White)

            Text(
                "Your friendly buddy for quick federal tax checks.\nTap Start to jump into the calculator.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )

            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF6A1B9A) // purple text on white button
                )
            ) { Text("Start") }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF7B1FA2), // darker purple
                    contentColor = Color.White
                )
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("BuddieZ Suggestion Box", style = MaterialTheme.typography.titleMedium)
                    suggestions.forEach { line -> Text("• $line") }
                }
            }

            Spacer(Modifier.height(16.dp))
            Image(
                painter = painterResource(id = R.drawable.emoji_buddiez),
                contentDescription = "TaxBuddieZ Mascot",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .padding(16.dp)
            )
        }
    }
}

private fun buddiezSuggestionsForToday(): List<String> {
    val today = LocalDate.now().dayOfWeek
    val base = mutableListOf<String>()

    // Light, friendly suggestions – tweak as you like
    when (today) {
        DayOfWeek.MONDAY -> {
            base += "Skim your pay-stubs and note federal withholding."
            base += "Set a weekly 10-minute money check-in."
        }
        DayOfWeek.TUESDAY -> {
            base += "Enter last month’s income into TaxBuddieZ and compare to now."
            base += "Collect receipts you might itemize later."
        }
        DayOfWeek.WEDNESDAY -> {
            base += "Review standard vs. itemized deduction quickly."
            base += "Check all documents needed for filing."
        }
        DayOfWeek.THURSDAY -> {
            base += "Run a quick estimate with ‘gross’ toggle on/off."
            base += "Note any side-gig income you earned this week."
        }
        DayOfWeek.FRIDAY -> {
            base += "Do a 2-minute ‘savings sweep’ from checking."
            base += "Plan one small learning: credits vs. deductions."
        }
        DayOfWeek.SATURDAY -> {
            base += "File your important docs into a single tax folder."
            base += "Back up your tax docs to cloud or drive."
        }
        DayOfWeek.SUNDAY -> {
            base += "Happy Sunday FunDaY."
            base += "Schedule a reminder for next Sunday’s check-in."
        }
        else -> {

            base += "Stay tax-smart today: review your finances briefly."
        }
    }

    // Always add two general, evergreen nudges
    base += "If income changed, rerun an estimate!"
    base += "Snap screenshots of any tax-related docs and store them safely!"
    base += "Check-in with your TaxBuddieZ and stay efficient!"

    return base
}

/* -------------------- CALCULATOR -------------------- */
@Composable
fun TaxCalcScreen(onBack: () -> Unit) {
    val scroll = rememberScrollState()

    var taxYear by remember { mutableIntStateOf(2025) }
    var filing by remember { mutableStateOf(FilingStatus.SINGLE) }
    var incomeText by remember { mutableStateOf("") }
    var useGross by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    var totalTax by remember { mutableStateOf<Double?>(null) }
    var effRate by remember { mutableStateOf<Double?>(null) }
    var marginal by remember { mutableStateOf<Double?>(null) }

    // Purple background (remove color if you're using light theme)
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF6A1B9A)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(scroll),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onBack) { Text("Back") }
                Text(
                    "Calculator",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )
            }

            // Year selection
            Text("Tax Year", style = MaterialTheme.typography.labelLarge, color = Color.White)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row {
                    RadioButton(selected = taxYear == 2024, onClick = { taxYear = 2024 })
                    Spacer(Modifier.width(6.dp)); Text("2024", color = Color.White)
                }
                Row {
                    RadioButton(selected = taxYear == 2025, onClick = { taxYear = 2025 })
                    Spacer(Modifier.width(6.dp)); Text("2025", color = Color.White)
                }
            }

            // Filing status (STACKED with HoH)
            Text("Filing Status", style = MaterialTheme.typography.labelLarge, color = Color.White)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Single
                Row {
                    RadioButton(
                        selected = filing == FilingStatus.SINGLE,
                        onClick = { filing = FilingStatus.SINGLE },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color.White,
                            unselectedColor = Color.White
                        )
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Single", color = Color.White)
                }

                // Married / Joint
                Row {
                    RadioButton(
                        selected = filing == FilingStatus.MARRIED_JOINT,
                        onClick = { filing = FilingStatus.MARRIED_JOINT },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color.White,
                            unselectedColor = Color.White
                        )
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Married / Joint", color = Color.White)
                }

                // Head of Household
                Row {
                    RadioButton(
                        selected = filing == FilingStatus.HEAD_OF_HOUSEHOLD,
                        onClick = { filing = FilingStatus.HEAD_OF_HOUSEHOLD },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color.White,
                            unselectedColor = Color.White
                        )
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Head of Household", color = Color.White)
                }
            }


            OutlinedTextField(
                value = incomeText,
                onValueChange = { txt -> incomeText = txt.filter { it.isDigit() || it == '.' } },
                label = {
                    Text(
                        if (useGross) "Gross Income (USD)" else "Taxable Income (USD)",
                        color = Color.White
                    )
                },
                textStyle = TextStyle(color = Color.White),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    // text + cursor
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White,
                    // label
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White,
                    // outline (indicator)
                    focusedIndicatorColor = Color.White,
                    unfocusedIndicatorColor = Color.White,
                    // background
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    errorContainerColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )


            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Switch(checked = useGross, onCheckedChange = { useGross = it })
                Text("Use gross (subtract standard deduction)", color = Color.White)
            }

            if (errorMsg != null) {
                Text(errorMsg!!, color = MaterialTheme.colorScheme.error)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        errorMsg = null
                        val income = incomeText.toDoubleOrNull()
                        if (income == null || income < 0) {
                            errorMsg = "Enter a valid non-negative number."
                            return@Button
                        }
                        try {
                            val taxable = if (useGross)
                                TaxCalculator.taxableFromGross(taxYear, filing, income)
                            else income

                            val res = TaxCalculator.compute(
                                taxYear = taxYear,
                                filingStatus = filing,
                                taxableIncome = taxable
                            )
                            totalTax = res.totalTax
                            effRate = res.effectiveRate
                            marginal = res.marginalRate
                        } catch (e: Exception) {
                            errorMsg = e.message ?: "Calculation error."
                        }
                    }
                ) { Text("Calculate") }

                OutlinedButton(
                    onClick = {
                        incomeText = ""
                        totalTax = null; effRate = null; marginal = null
                        errorMsg = null
                    }
                ) { Text("Clear") }
            }

            ResultCard(totalTax, effRate, marginal)

        }

            Text(
                "Approximate federal calculation for educational use. Uses IRS brackets for 2024–2025.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White
            )
        }
    }

    @Composable
    private fun ResultCard(total: Double?, eff: Double?, marg: Double?) {
        if (total == null || eff == null || marg == null) return
        val currency = NumberFormat.getCurrencyInstance(Locale.US)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Results", style = MaterialTheme.typography.titleMedium)
                Text("Total Tax: ${currency.format(total)}")
                Text("Effective Rate: ${String.format(Locale.US, "%.2f%%", eff * 100)}")
                Text("Marginal Rate: ${String.format(Locale.US, "%.2f%%", marg * 100)}")
            }
        }
    }

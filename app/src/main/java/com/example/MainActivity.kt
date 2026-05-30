package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.CalcButtonAccent
import com.example.ui.theme.CalcButtonBg
import com.example.ui.theme.CalcOperator
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CalculatorApp()
            }
        }
    }
}

@Composable
fun CalculatorApp(viewModel: CalculatorViewModel = viewModel()) {
    val appMode by viewModel.appMode.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(modifier = Modifier.statusBarsPadding()) {
                TopSegmentedControl(appMode, viewModel::setAppMode)
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize().navigationBarsPadding()) {
            Crossfade(targetState = appMode, label = "AppModeTransition") { mode ->
                when (mode) {
                    AppMode.CALCULATOR -> CalculatorScreen(viewModel)
                    AppMode.CONVERTER -> ConverterScreen(viewModel)
                    AppMode.HISTORY -> HistoryScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun TopSegmentedControl(currentMode: AppMode, onModeSelected: (AppMode) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(48.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppMode.values().forEach { mode ->
            val isSelected = currentMode == mode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onModeSelected(mode) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mode.title,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun CalculatorScreen(viewModel: CalculatorViewModel) {
    val expression by viewModel.expression.collectAsState()
    val result by viewModel.result.collectAsState()
    val calcMode by viewModel.calcMode.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(24.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = expression,
                fontSize = 48.sp,
                color = MaterialTheme.colorScheme.onBackground,
                lineHeight = 52.sp,
                textAlign = TextAlign.End,
                fontWeight = FontWeight.Light
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = result,
                fontSize = 28.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                maxLines = 1,
                textAlign = TextAlign.End
            )
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = MaterialTheme.colorScheme.surface)

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { viewModel.toggleCalcMode() }) {
                Text(
                    if (calcMode == CalculatorMode.BASIC) "Sci" else "Basic",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp
                )
            }
        }
        
        CalcKeypad(calcMode, viewModel)
    }
}

@Composable
fun CalcKeypad(calcMode: CalculatorMode, viewModel: CalculatorViewModel) {
    val keys = if (calcMode == CalculatorMode.BASIC) {
        listOf(
            "C", "(", ")", "÷",
            "7", "8", "9", "×",
            "4", "5", "6", "-",
            "1", "2", "3", "+",
            "0", ".", "⌫", "="
        )
    } else {
        listOf(
            "sin", "cos", "tan", "÷",
            "log10", "ln", "sqrt", "×",
            "π", "e", "^", "-",
            "7", "8", "9", "+",
            "4", "5", "6", "=",
            "1", "2", "3", "",
            "0", ".", "C", "⌫"
        )
    }
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(keys) { key ->
            if (key.isEmpty()) {
                Spacer(modifier = Modifier.size(72.dp))
            } else {
                CalcButton(key, onClick = {
                    when(key) {
                        "C" -> viewModel.onCalcClear()
                        "⌫" -> viewModel.onCalcDelete()
                        "=" -> viewModel.onCalcEquals()
                        else -> viewModel.onCalcInput(key)
                    }
                })
            }
        }
    }
}

@Composable
fun CalcButton(symbol: String, onClick: () -> Unit) {
    val isOp = symbol in listOf("÷", "×", "-", "+", "=")
    val isAction = symbol in listOf("C", "⌫", "(", ")")

    val bgColor = when {
        isOp -> CalcOperator
        isAction -> CalcButtonAccent
        else -> CalcButtonBg
    }
    val fgColor = when {
        isOp -> Color.White
        isAction -> Color.White
        else -> MaterialTheme.colorScheme.onBackground
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(bgColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(symbol, fontSize = if (symbol.length > 2) 18.sp else 28.sp, color = fgColor, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun HistoryScreen(viewModel: CalculatorViewModel) {
    val history by viewModel.history.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("History Log", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            TextButton(onClick = { viewModel.clearHistory() }) {
                Text("Clear", color = MaterialTheme.colorScheme.primary)
            }
        }
        
        if (history.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No recent calculations", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(history) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.End) {
                            Text(item.expression, fontSize = 20.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                            Spacer(Modifier.height(4.dp))
                            Text(item.result, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterScreen(viewModel: CalculatorViewModel) {
    val category by viewModel.converterCategory.collectAsState()
    val sourceUnit by viewModel.sourceUnit.collectAsState()
    val targetUnit by viewModel.targetUnit.collectAsState()
    val input by viewModel.converterInput.collectAsState()
    val output by viewModel.converterOutput.collectAsState()
    
    val units = viewModel.getUnitsForCategory(category)
    
    Column(modifier = Modifier.fillMaxSize().padding(top = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ConverterCategory.values().forEach { cat ->
                FilterChip(
                    selected = category == cat,
                    onClick = { viewModel.setConverterCategory(cat) },
                    label = { Text(cat.title) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
           UnitSelectorBox(unit = sourceUnit, value = input, units = units, onUnitSelected = viewModel::setSourceUnit)
           
           Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
               IconButton(onClick = { viewModel.swapUnits() }) {
                   Icon(Icons.Default.SwapVert, contentDescription = "Swap", tint = MaterialTheme.colorScheme.primary)
               }
           }
           
           UnitSelectorBox(unit = targetUnit, value = output, units = units, onUnitSelected = viewModel::setTargetUnit)
        }
        
        val keys = listOf(
            "7", "8", "9", "C",
            "4", "5", "6", "⌫",
            "1", "2", "3", ".",
            "",  "0", "",  ""
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(keys) { key ->
                if (key.isNotEmpty()) {
                    CalcButton(symbol = key, onClick = { viewModel.onConverterInput(key) })
                } else {
                    Spacer(Modifier.size(72.dp))
                }
            }
        }
    }
}

@Composable
fun UnitSelectorBox(
    unit: String,
    value: String,
    units: List<String>,
    onUnitSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier.fillMaxWidth().clickable { expanded = true },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(unit + " ▼", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(value.ifEmpty { "0" }, fontSize = 32.sp, color = MaterialTheme.colorScheme.onBackground, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            units.forEach { u ->
                DropdownMenuItem(
                    text = { Text(u) },
                    onClick = { 
                        onUnitSelected(u)
                        expanded = false 
                    }
                )
            }
        }
    }
}

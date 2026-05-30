package com.example

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import net.objecthunter.exp4j.ExpressionBuilder

enum class AppMode(val title: String) {
    CALCULATOR("Calculator"), CONVERTER("Converter"), HISTORY("History")
}

enum class CalculatorMode {
    BASIC, SCIENTIFIC
}

enum class ConverterCategory(val title: String) {
    LENGTH("Length"), WEIGHT("Weight"), TEMPERATURE("Temp")
}

data class HistoryItem(val expression: String, val result: String)

class CalculatorViewModel : ViewModel() {

    private val _appMode = MutableStateFlow(AppMode.CALCULATOR)
    val appMode: StateFlow<AppMode> = _appMode.asStateFlow()

    private val _calcMode = MutableStateFlow(CalculatorMode.BASIC)
    val calcMode: StateFlow<CalculatorMode> = _calcMode.asStateFlow()

    private val _expression = MutableStateFlow("")
    val expression: StateFlow<String> = _expression.asStateFlow()

    private val _result = MutableStateFlow("")
    val result: StateFlow<String> = _result.asStateFlow()

    private val _history = MutableStateFlow<List<HistoryItem>>(emptyList())
    val history: StateFlow<List<HistoryItem>> = _history.asStateFlow()

    private val _converterCategory = MutableStateFlow(ConverterCategory.LENGTH)
    val converterCategory: StateFlow<ConverterCategory> = _converterCategory.asStateFlow()

    private val _converterInput = MutableStateFlow("")
    val converterInput: StateFlow<String> = _converterInput.asStateFlow()

    private val _sourceUnit = MutableStateFlow("Meters")
    val sourceUnit: StateFlow<String> = _sourceUnit.asStateFlow()
    
    private val _targetUnit = MutableStateFlow("Feet")
    val targetUnit: StateFlow<String> = _targetUnit.asStateFlow()
    
    private val _converterOutput = MutableStateFlow("")
    val converterOutput: StateFlow<String> = _converterOutput.asStateFlow()

    fun setAppMode(mode: AppMode) {
        _appMode.value = mode
    }

    fun toggleCalcMode() {
        _calcMode.value = if (_calcMode.value == CalculatorMode.BASIC) CalculatorMode.SCIENTIFIC else CalculatorMode.BASIC
    }

    fun onCalcInput(input: String) {
        if (_expression.value == "Error") {
            _expression.value = ""
        }
        val appended = when (input) {
            "sin", "cos", "tan", "sqrt", "log10" -> "$input("
            "ln" -> "log("
            else -> input
        }
        _expression.value += appended
        evaluateExpression(live = true)
    }

    fun onCalcClear() {
        _expression.value = ""
        _result.value = ""
    }

    fun onCalcDelete() {
        if (_expression.value.isNotEmpty() && _expression.value != "Error") {
            _expression.value = _expression.value.dropLast(1)
            evaluateExpression(live = true)
        }
    }

    fun onCalcEquals() {
        evaluateExpression(live = false)
        if (_result.value.isNotEmpty() && _result.value != "Error") {
            val item = HistoryItem(_expression.value, _result.value)
            _history.value = listOf(item) + _history.value
            _expression.value = _result.value
            _result.value = ""
        }
    }
    
    private fun evaluateExpression(live: Boolean) {
        if (_expression.value.isEmpty()) {
            _result.value = ""
            return
        }
        try {
            var evalStr = _expression.value
                .replace("×", "*")
                .replace("÷", "/")
                .replace("π", "pi")
                .replace("e", "e")
            
            val exp = ExpressionBuilder(evalStr).build()
            val res = exp.evaluate()
            
            _result.value = if (res == res.toLong().toDouble()) {
                res.toLong().toString()
            } else {
                res.toString()
            }
        } catch (e: Exception) {
            if (!live) {
                _result.value = "Error"
            } else {
                _result.value = "" // Clear live preview if invalid
            }
        }
    }

    fun setConverterCategory(category: ConverterCategory) {
        _converterCategory.value = category
        when (category) {
            ConverterCategory.LENGTH -> {
                _sourceUnit.value = "Meters"
                _targetUnit.value = "Feet"
            }
            ConverterCategory.WEIGHT -> {
                _sourceUnit.value = "Kilograms"
                _targetUnit.value = "Pounds"
            }
            ConverterCategory.TEMPERATURE -> {
                _sourceUnit.value = "Celsius"
                _targetUnit.value = "Fahrenheit"
            }
        }
        calculateConversion()
    }
    
    fun setSourceUnit(unit: String) {
        _sourceUnit.value = unit
        calculateConversion()
    }

    fun setTargetUnit(unit: String) {
        _targetUnit.value = unit
        calculateConversion()
    }
    
    fun swapUnits() {
        val temp = _sourceUnit.value
        _sourceUnit.value = _targetUnit.value
        _targetUnit.value = temp
        calculateConversion()
    }

    fun onConverterInput(input: String) {
        if (input == "C") {
            _converterInput.value = ""
        } else if (input == "⌫") {
            if (_converterInput.value.isNotEmpty()) {
                _converterInput.value = _converterInput.value.dropLast(1)
            }
        } else {
            _converterInput.value += input
        }
        calculateConversion()
    }

    private fun calculateConversion() {
        val inputValue = _converterInput.value.toDoubleOrNull()
        if (inputValue == null) {
            _converterOutput.value = ""
            return
        }
        
        val source = _sourceUnit.value
        val target = _targetUnit.value
        val cat = _converterCategory.value
        
        try {
            val res = when (cat) {
                ConverterCategory.LENGTH -> convertLength(inputValue, source, target)
                ConverterCategory.WEIGHT -> convertWeight(inputValue, source, target)
                ConverterCategory.TEMPERATURE -> convertTemperature(inputValue, source, target)
            }
            _converterOutput.value = String.format("%.4f", res).trimEnd('0').trimEnd('.')
        } catch (e: Exception) {
            _converterOutput.value = "Error"
        }
    }
    
    private fun convertLength(value: Double, from: String, to: String): Double {
        val inMeters = when (from) {
            "Meters" -> value
            "Centimeters" -> value / 100.0
            "Kilometers" -> value * 1000.0
            "Inches" -> value * 0.0254
            "Feet" -> value * 0.3048
            "Yards" -> value * 0.9144
            "Miles" -> value * 1609.34
            else -> value
        }
        return when (to) {
            "Meters" -> inMeters
            "Centimeters" -> inMeters * 100.0
            "Kilometers" -> inMeters / 1000.0
            "Inches" -> inMeters / 0.0254
            "Feet" -> inMeters / 0.3048
            "Yards" -> inMeters / 0.9144
            "Miles" -> inMeters / 1609.34
            else -> inMeters
        }
    }
    
    private fun convertWeight(value: Double, from: String, to: String): Double {
         val inKgs = when (from) {
            "Kilograms" -> value
            "Grams" -> value / 1000.0
            "Milligrams" -> value / 1000000.0
            "Pounds" -> value * 0.453592
            "Ounces" -> value * 0.0283495
            else -> value
        }
        return when (to) {
            "Kilograms" -> inKgs
            "Grams" -> inKgs * 1000.0
            "Milligrams" -> inKgs * 1000000.0
            "Pounds" -> inKgs / 0.453592
            "Ounces" -> inKgs / 0.0283495
            else -> inKgs
        }
    }
    
    private fun convertTemperature(value: Double, from: String, to: String): Double {
        if (from == to) return value
        val inCelsius = when (from) {
            "Fahrenheit" -> (value - 32) * 5/9
            "Kelvin" -> value - 273.15
            else -> value 
        }
        return when (to) {
            "Fahrenheit" -> (inCelsius * 9/5) + 32
            "Kelvin" -> inCelsius + 273.15
            else -> inCelsius
        }
    }
    
    fun getUnitsForCategory(category: ConverterCategory): List<String> {
        return when (category) {
            ConverterCategory.LENGTH -> listOf("Meters", "Centimeters", "Kilometers", "Inches", "Feet", "Yards", "Miles")
            ConverterCategory.WEIGHT -> listOf("Kilograms", "Grams", "Milligrams", "Pounds", "Ounces")
            ConverterCategory.TEMPERATURE -> listOf("Celsius", "Fahrenheit", "Kelvin")
        }
    }
    
    fun clearHistory() {
        _history.value = emptyList()
    }
}

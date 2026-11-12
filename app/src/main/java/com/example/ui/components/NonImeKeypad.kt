package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Amber300
import com.example.ui.theme.Amber500
import com.example.ui.theme.Amber950
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Cyan500
import com.example.ui.theme.Navy950
import com.example.ui.theme.OnDark
import com.example.ui.theme.Rose400
import com.example.ui.theme.Rose500
import com.example.ui.theme.Rose950
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

/**
 * Ensures any text OutlinedTextField immediately dismisses the in-app numeric keypad
 * when gaining focus so the Android IME and app keypad are never shown together.
 */
@Composable
fun Modifier.dismissKeypadOnTextFocus(onDismissKeypad: () -> Unit): Modifier {
    return this.onFocusChanged { state ->
        if (state.isFocused) {
            onDismissKeypad()
        }
    }
}

/**
 * Phone-friendly OutlinedTextField with instant clear button (✕),
 * optional auto-clear of placeholder defaults on focus, and high-contrast Navy/Ice styling.
 */
@Composable
fun ClearableOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else 4,
    autoClearValues: Set<String> = emptySet(),
    onDismissKeypad: (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    readOnly: Boolean = false
) {
    var didAutoClearThisFocus by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = if (!readOnly && value.isNotEmpty()) {
            {
                IconButton(
                    onClick = { onValueChange("") },
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Effacer le texte",
                        tint = Rose500,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        } else null,
        singleLine = singleLine,
        maxLines = maxLines,
        readOnly = readOnly,
        keyboardOptions = keyboardOptions,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = Slate100,
            fontWeight = FontWeight.Medium
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Cyan500,
            unfocusedBorderColor = Slate700,
            focusedContainerColor = Slate900,
            unfocusedContainerColor = Slate900,
            focusedTextColor = Slate100,
            unfocusedTextColor = Slate100,
            focusedLabelColor = Cyan400,
            unfocusedLabelColor = Slate400,
            cursorColor = Amber500
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.onFocusChanged { state ->
            if (state.isFocused) {
                onDismissKeypad?.invoke()
                if (!didAutoClearThisFocus && autoClearValues.any { it.equals(value.trim(), ignoreCase = true) }) {
                    didAutoClearThisFocus = true
                    onValueChange("")
                }
            } else {
                didAutoClearThisFocus = false
            }
        }
    )
}

/**
 * Non-IME clickable numeric display field.
 * Uses Surface + Row + Text (never TextField/BasicTextField), so Android's system
 * soft keyboard NEVER pops up when tapping a numeric field.
 */
@Composable
fun NumericDisplayField(
    label: String,
    value: String,
    isActive: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
    suffix: String = "",
    placeholder: String = "0",
    accentColor: Color = Cyan500,
    onClear: (() -> Unit)? = null,
    testTag: String = "numeric_field"
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Surface(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(14.dp))
            .clickable {
                focusManager.clearFocus(force = true)
                keyboardController?.hide()
                onSelect()
            }
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        color = if (isActive) Slate900 else Slate850,
        border = BorderStroke(
            width = if (isActive) 2.5.dp else 1.dp,
            color = if (isActive) accentColor else Slate700
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isActive) accentColor else Slate400,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (isActive && value.isNotEmpty() && onClear != null) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Effacer",
                        tint = Rose500,
                        modifier = Modifier
                            .size(18.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onClear() }
                    )
                }
            }
            Spacer(modifier = Modifier.height(3.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = value.ifEmpty { placeholder },
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    ),
                    color = if (value.isEmpty()) Slate400 else Slate100,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (suffix.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = suffix,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isActive) accentColor else Slate400,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Pure helper to edit a numeric input buffer via the in-app keypad.
 * Uses overwrite-on-first-digit when pristine == true so replacing a default value is instant.
 */
fun applyKeypadInput(
    current: String,
    key: String,
    allowDecimal: Boolean,
    isPristine: Boolean
): Pair<String, Boolean> {
    return when (key) {
        "C" -> "" to false
        "⌫" -> {
            if (isPristine || current.isEmpty()) "" to false
            else current.dropLast(1) to false
        }
        "." -> {
            if (!allowDecimal) return current to false
            if (isPristine) "0." to false
            else if (current.contains('.')) current to false
            else if (current.isEmpty()) "0." to false
            else "$current." to false
        }
        else -> {
            val base = if (isPristine) "" else current
            if (base == "0" && key == "0") "0" to false
            else if (base == "0" && key != ".") key to false
            else {
                val candidate = "$base$key"
                val parts = candidate.split('.')
                if (parts.size == 2 && parts[1].length > 2) current to false
                else if (candidate.length > 10) current to false
                else candidate to false
            }
        }
    }
}

fun applyKeypadQuickDelta(
    current: String,
    delta: Double,
    allowDecimal: Boolean
): String {
    val parsed = current.toDoubleOrNull() ?: 0.0
    val next = (parsed + delta).coerceAtLeast(0.0)
    return if (!allowDecimal) {
        next.toInt().toString()
    } else {
        val rounded = kotlin.math.round(next * 100.0) / 100.0
        if (rounded % 1.0 == 0.0) rounded.toInt().toString() else rounded.toString()
    }
}

@Composable
fun NonImeNumericKeypad(
    onKeyPress: (String) -> Unit,
    onValidate: (() -> Unit)? = null,
    allowDecimal: Boolean = true,
    quickChips: List<Pair<String, Double>> = emptyList(),
    onQuickDelta: ((Double) -> Unit)? = null,
    validateLabel: String = "Valider",
    accentColor: Color = Cyan500,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("in_app_numeric_keypad"),
        shape = RoundedCornerShape(18.dp),
        color = Navy950,
        border = BorderStroke(1.5.dp, Slate700),
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (quickChips.isNotEmpty() && onQuickDelta != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickChips.forEach { (label, delta) ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onQuickDelta(delta) }
                                .testTag("keypad_chip_${label.replace("+", "plus_")}"),
                            shape = RoundedCornerShape(10.dp),
                            color = Amber950,
                            border = BorderStroke(1.dp, Amber500)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Amber300,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }

            val rows = listOf(
                listOf("7", "8", "9", "⌫"),
                listOf("4", "5", "6", "C"),
                listOf("1", "2", "3", if (allowDecimal) "." else "00"),
                listOf("0", if (onValidate != null) "VALIDATE" else "00")
            )

            rows.forEachIndexed { rowIdx, keys ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (rowIdx < 3) {
                        keys.forEach { key ->
                            val isAction = key == "⌫" || key == "C"
                            KeypadKeyButton(
                                key = key,
                                isAction = isAction,
                                onClick = { onKeyPress(key) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        KeypadKeyButton(
                            key = "0",
                            isAction = false,
                            onClick = { onKeyPress("0") },
                            modifier = Modifier.weight(1f)
                        )
                        if (onValidate != null) {
                            Button(
                                onClick = onValidate,
                                modifier = Modifier
                                    .weight(3f)
                                    .height(54.dp)
                                    .testTag("keypad_validate_btn"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = accentColor,
                                    contentColor = OnDark
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = validateLabel,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = validateLabel,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        } else {
                            KeypadKeyButton(
                                key = "00",
                                isAction = false,
                                onClick = {
                                    onKeyPress("0")
                                    onKeyPress("0")
                                },
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.weight(2f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeypadKeyButton(
    key: String,
    isAction: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = when (key) {
        "C" -> Rose950
        "⌫" -> Amber950
        else -> Slate900
    }
    val fg = when (key) {
        "C" -> Rose400
        "⌫" -> Amber300
        else -> Slate100
    }
    val borderColor = when (key) {
        "C" -> Rose500
        "⌫" -> Amber500
        else -> Slate700
    }

    Surface(
        modifier = modifier
            .height(54.dp)
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("keypad_key_$key"),
        shape = RoundedCornerShape(14.dp),
        color = bg,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (key == "⌫") {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Effacer",
                    tint = fg,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Text(
                    text = key,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = if (isAction) 17.sp else 21.sp,
                        fontWeight = FontWeight.Black
                    ),
                    color = fg
                )
            }
        }
    }
}

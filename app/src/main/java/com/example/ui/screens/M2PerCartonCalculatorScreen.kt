package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.FreeLineItem
import com.example.data.model.TileBoxCalculationResult
import com.example.data.model.TileCalculatorMath
import com.example.data.model.TileDimensionUnit
import com.example.data.model.formatM2
import com.example.ui.theme.Amber300
import com.example.ui.theme.Amber400
import com.example.ui.theme.Amber500
import com.example.ui.theme.Amber950
import com.example.ui.theme.Cyan300
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Cyan500
import com.example.ui.theme.Cyan950
import com.example.ui.theme.Emerald300
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Emerald500
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
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import java.util.UUID

private val preciseM2Format = DecimalFormat("0.####", DecimalFormatSymbols(Locale.US))

private fun parseLocalizedDecimal(raw: String): Double? {
    val cleaned = raw.trim().replace(',', '.')
    if (cleaned.isEmpty()) return null
    return cleaned.toDoubleOrNull()
}

private fun formatCleanNumber(value: Double): String {
    val clean = (value * 10000.0).let { kotlin.math.round(it) / 10000.0 }
    return preciseM2Format.format(clean)
}

@Composable
fun M2PerCartonCalculatorScreen(
    modifier: Modifier = Modifier,
    onAddCalculatedLineToCart: ((FreeLineItem) -> Unit)? = null
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var unitName by rememberSaveable { mutableStateOf(TileDimensionUnit.CM.name) }
    val unit = remember(unitName) { TileDimensionUnit.valueOf(unitName) }

    var lengthInput by rememberSaveable { mutableStateOf("60") }
    var widthInput by rememberSaveable { mutableStateOf("60") }
    var piecesInput by rememberSaveable { mutableStateOf("4") }
    var requiredAreaInput by rememberSaveable { mutableStateOf("25") }
    var marginPercent by rememberSaveable { mutableIntStateOf(0) }

    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var calculatedResult by remember {
        mutableStateOf<TileBoxCalculationResult?>(
            TileCalculatorMath.calculateM2PerCartonAndCartons(
                tileLength = 60.0,
                tileWidth = 60.0,
                unit = TileDimensionUnit.CM,
                piecesPerCarton = 4,
                requiredAreaM2 = 25.0,
                marginPercent = 0
            )
        )
    }
    var hasPendingChanges by rememberSaveable { mutableStateOf(false) }

    val invalidInputsErrorText = stringResource(R.string.m2_calc_error_invalid_inputs)

    val liveM2PerCartonPreview = remember(lengthInput, widthInput, unit, piecesInput) {
        val l = parseLocalizedDecimal(lengthInput)
        val w = parseLocalizedDecimal(widthInput)
        val pcs = piecesInput.trim().toIntOrNull()
        if (l != null && w != null && pcs != null && l > 0.0 && w > 0.0 && pcs > 0) {
            val lenM = if (unit == TileDimensionUnit.CM) l / 100.0 else l
            val widM = if (unit == TileDimensionUnit.CM) w / 100.0 else w
            lenM * widM * pcs
        } else {
            null
        }
    }

    fun performCalculation() {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()

        val l = parseLocalizedDecimal(lengthInput)
        val w = parseLocalizedDecimal(widthInput)
        val pcs = piecesInput.trim().toIntOrNull()
        val area = parseLocalizedDecimal(requiredAreaInput)

        if (l == null || w == null || pcs == null || area == null ||
            l <= 0.0 || w <= 0.0 || pcs <= 0 || area <= 0.0
        ) {
            errorMessage = invalidInputsErrorText
            return
        }

        errorMessage = null
        hasPendingChanges = false
        calculatedResult = TileCalculatorMath.calculateM2PerCartonAndCartons(
            tileLength = l,
            tileWidth = w,
            unit = unit,
            piecesPerCarton = pcs,
            requiredAreaM2 = area,
            marginPercent = marginPercent
        )
    }

    fun switchUnit(targetUnit: TileDimensionUnit) {
        if (targetUnit == unit) return
        val factor = if (targetUnit == TileDimensionUnit.METER) 0.01 else 100.0
        parseLocalizedDecimal(lengthInput)?.let { lengthInput = formatCleanNumber(it * factor) }
        parseLocalizedDecimal(widthInput)?.let { widthInput = formatCleanNumber(it * factor) }
        unitName = targetUnit.name
        hasPendingChanges = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 720.dp)
                .align(Alignment.TopCenter)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .testTag("m2_per_carton_calculator_screen"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Deep Navy Header Card
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Navy950,
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = Amber500
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = stringResource(R.string.m2_calc_screen_title),
                                tint = OnDark,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.m2_calc_screen_title),
                            style = MaterialTheme.typography.titleLarge,
                            color = OnDark,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = stringResource(R.string.m2_calc_screen_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = Amber500,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Quick Tile Format Presets + Unit Selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.m2_calc_tile_dimensions_section),
                        style = MaterialTheme.typography.labelLarge,
                        color = Slate400,
                        fontWeight = FontWeight.Black
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TileDimensionUnit.entries.forEach { option ->
                            val selected = option == unit
                            Surface(
                                modifier = Modifier
                                    .height(40.dp)
                                    .minimumInteractiveComponentSize()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { switchUnit(option) }
                                    .testTag("unit_toggle_${option.suffix}"),
                                shape = RoundedCornerShape(10.dp),
                                color = if (selected) Cyan500 else Slate900,
                                border = BorderStroke(1.5.dp, if (selected) Cyan500 else Slate700)
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = option.suffix.uppercase(),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (selected) OnDark else Slate100,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TileCalculatorMath.TILE_FORMAT_PRESETS.forEach { preset ->
                        val targetL = if (unit == TileDimensionUnit.CM) preset.lengthCm else preset.lengthCm / 100.0
                        val targetW = if (unit == TileDimensionUnit.CM) preset.widthCm else preset.widthCm / 100.0
                        val curL = parseLocalizedDecimal(lengthInput)
                        val curW = parseLocalizedDecimal(widthInput)
                        val curPcs = piecesInput.trim().toIntOrNull()
                        val isPresetSelected = curL != null && curW != null && curPcs == preset.piecesPerCarton &&
                            kotlin.math.abs(curL - targetL) < 0.001 &&
                            kotlin.math.abs(curW - targetW) < 0.001

                        FilterChip(
                            selected = isPresetSelected,
                            onClick = {
                                lengthInput = formatCleanNumber(targetL)
                                widthInput = formatCleanNumber(targetW)
                                piecesInput = preset.piecesPerCarton.toString()
                                errorMessage = null
                                hasPendingChanges = true
                            },
                            label = {
                                Text(
                                    text = "${preset.label} • ${preset.piecesPerCarton} pcs",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Amber500,
                                selectedLabelColor = OnDark,
                                containerColor = Slate900,
                                labelColor = Slate100
                            ),
                            modifier = Modifier.testTag("preset_${preset.label}")
                        )
                    }
                }
            }

            // Inputs Card
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Slate900,
                border = BorderStroke(1.5.dp, Slate800),
                tonalElevation = 2.dp
            ) {
                BoxWithConstraints(modifier = Modifier.padding(16.dp)) {
                    val isWide = maxWidth >= 520.dp
                    val fieldColors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Cyan500,
                        unfocusedBorderColor = Slate700,
                        focusedLabelColor = Cyan400,
                        unfocusedLabelColor = Slate400,
                        focusedTextColor = Slate100,
                        unfocusedTextColor = Slate100,
                        cursorColor = Amber500,
                        focusedContainerColor = Slate950,
                        unfocusedContainerColor = Slate950
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = lengthInput,
                                onValueChange = {
                                    lengthInput = it
                                    errorMessage = null
                                    hasPendingChanges = true
                                },
                                label = {
                                    Text(
                                        text = "${stringResource(R.string.m2_calc_length_label)} (${unit.suffix})",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Straighten, contentDescription = null, tint = Cyan400)
                                },
                                trailingIcon = if (lengthInput.isNotEmpty()) {
                                    {
                                        IconButton(onClick = { lengthInput = ""; hasPendingChanges = true }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Effacer", tint = Rose500)
                                        }
                                    }
                                } else null,
                                suffix = {
                                    Text(unit.suffix, color = Cyan400, fontWeight = FontWeight.Bold)
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                    imeAction = ImeAction.Next
                                ),
                                shape = RoundedCornerShape(14.dp),
                                colors = fieldColors,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("tile_length_input")
                            )

                            OutlinedTextField(
                                value = widthInput,
                                onValueChange = {
                                    widthInput = it
                                    errorMessage = null
                                    hasPendingChanges = true
                                },
                                label = {
                                    Text(
                                        text = "${stringResource(R.string.m2_calc_width_label)} (${unit.suffix})",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Straighten, contentDescription = null, tint = Cyan400)
                                },
                                trailingIcon = if (widthInput.isNotEmpty()) {
                                    {
                                        IconButton(onClick = { widthInput = ""; hasPendingChanges = true }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Effacer", tint = Rose500)
                                        }
                                    }
                                } else null,
                                suffix = {
                                    Text(unit.suffix, color = Cyan400, fontWeight = FontWeight.Bold)
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                    imeAction = ImeAction.Next
                                ),
                                shape = RoundedCornerShape(14.dp),
                                colors = fieldColors,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("tile_width_input")
                            )
                        }

                        if (isWide) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                PiecesPerCartonField(
                                    value = piecesInput,
                                    onValueChange = {
                                        piecesInput = it.filter { ch -> ch.isDigit() }
                                        errorMessage = null
                                        hasPendingChanges = true
                                    },
                                    colors = fieldColors,
                                    modifier = Modifier.weight(1f)
                                )
                                RequiredAreaField(
                                    value = requiredAreaInput,
                                    onValueChange = {
                                        requiredAreaInput = it
                                        errorMessage = null
                                        hasPendingChanges = true
                                    },
                                    onDone = { performCalculation() },
                                    colors = fieldColors,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        } else {
                            PiecesPerCartonField(
                                value = piecesInput,
                                onValueChange = {
                                    piecesInput = it.filter { ch -> ch.isDigit() }
                                    errorMessage = null
                                    hasPendingChanges = true
                                },
                                colors = fieldColors,
                                modifier = Modifier.fillMaxWidth()
                            )
                            RequiredAreaField(
                                value = requiredAreaInput,
                                onValueChange = {
                                    requiredAreaInput = it
                                    errorMessage = null
                                    hasPendingChanges = true
                                },
                                onDone = { performCalculation() },
                                colors = fieldColors,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Marge :",
                                style = MaterialTheme.typography.labelLarge,
                                color = Slate400,
                                fontWeight = FontWeight.Bold
                            )
                            TileCalculatorMath.MARGIN_PRESETS.forEach { pct ->
                                val selected = marginPercent == pct
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .minimumInteractiveComponentSize()
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            marginPercent = pct
                                            hasPendingChanges = true
                                        }
                                        .testTag("margin_chip_$pct"),
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (selected) Amber500 else Slate850,
                                    border = BorderStroke(1.dp, if (selected) Amber500 else Slate700)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = if (pct == 0) "Exact 0%" else "+$pct%",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (selected) OnDark else Slate100,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        }

                        if (liveM2PerCartonPreview != null) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Cyan950,
                                border = BorderStroke(1.dp, Cyan500.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Colisage estimé :",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Cyan300,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${formatCleanNumber(liveM2PerCartonPreview)} m² / carton",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Cyan400,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }

                        AnimatedVisibility(visible = errorMessage != null) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Rose950,
                                border = BorderStroke(1.dp, Rose500)
                            ) {
                                Text(
                                    text = errorMessage.orEmpty(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Rose400,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp)
                                        .testTag("calc_error_banner")
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    focusManager.clearFocus(force = true)
                                    keyboardController?.hide()
                                    lengthInput = ""
                                    widthInput = ""
                                    piecesInput = ""
                                    requiredAreaInput = ""
                                    marginPercent = 0
                                    errorMessage = null
                                    calculatedResult = null
                                    hasPendingChanges = false
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .testTag("calc_screen_reset_btn"),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.5.dp, Rose500)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = stringResource(R.string.m2_calc_reset_btn),
                                    tint = Rose400,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.m2_calc_reset_btn),
                                    color = Rose400,
                                    fontWeight = FontWeight.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Button(
                                onClick = { performCalculation() },
                                modifier = Modifier
                                    .weight(1.7f)
                                    .height(52.dp)
                                    .testTag("calculate_cartons_btn"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Amber500,
                                    contentColor = OnDark
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.m2_calc_calculate_btn),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = calculatedResult != null,
                enter = fadeIn() + expandVertically(
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                ),
                exit = fadeOut() + shrinkVertically()
            ) {
                calculatedResult?.let { res ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("calc_result_card"),
                        shape = RoundedCornerShape(18.dp),
                        color = Slate900,
                        border = BorderStroke(
                            width = 2.dp,
                            color = if (hasPendingChanges) Amber500 else Emerald500
                        ),
                        tonalElevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (hasPendingChanges) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Amber950,
                                    border = BorderStroke(1.dp, Amber500)
                                ) {
                                    Text(
                                        text = "Valeurs modifiées — appuyez sur « Calculer les Cartons » pour actualiser",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Amber300,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Navy950
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = stringResource(R.string.m2_calc_result_cartons_title),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = OnDark.copy(alpha = 0.8f)
                                        )
                                        Text(
                                            text = "${res.cartonsNeeded} Carton${if (res.cartonsNeeded > 1) "s" else ""}",
                                            style = MaterialTheme.typography.headlineMedium,
                                            color = Amber500,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.testTag("calc_result_cartons")
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Cyan500
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            horizontalAlignment = Alignment.End
                                        ) {
                                            Text(
                                                text = stringResource(R.string.m2_calc_result_m2_per_carton_title),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = OnDark.copy(alpha = 0.9f)
                                            )
                                            Text(
                                                text = "${formatCleanNumber(res.m2PerCarton)} m²",
                                                style = MaterialTheme.typography.titleLarge,
                                                color = OnDark,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier.testTag("calc_result_m2_per_carton")
                                            )
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(color = Slate800)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                ResultMetricTile(
                                    icon = Icons.Default.GridOn,
                                    label = "1 Carreau",
                                    value = "${formatCleanNumber(res.tileAreaM2)} m²",
                                    accent = Cyan400,
                                    modifier = Modifier.weight(1f)
                                )
                                ResultMetricTile(
                                    icon = Icons.Default.Layers,
                                    label = "Total Pièces",
                                    value = "${res.totalPieces} pcs",
                                    accent = Amber400,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                ResultMetricTile(
                                    icon = Icons.Default.SquareFoot,
                                    label = "Surface Livrée",
                                    value = "${formatM2(res.deliveredAreaM2)} m²",
                                    accent = Emerald400,
                                    modifier = Modifier.weight(1f)
                                )
                                ResultMetricTile(
                                    icon = Icons.Default.Inventory2,
                                    label = if (res.marginPercent > 0) "Surplus (+${res.marginPercent}% coupe)" else "Surplus",
                                    value = "+${formatM2(res.surplusAreaM2)} m²",
                                    accent = Emerald300,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            if (onAddCalculatedLineToCart != null && res.cartonsNeeded > 0 && res.deliveredAreaM2 > 0.0) {
                                Button(
                                    onClick = {
                                        onAddCalculatedLineToCart(
                                            FreeLineItem(
                                                id = "calc_${UUID.randomUUID().toString().take(8)}",
                                                designation = "Carrelage ${lengthInput.trim()}×${widthInput.trim()}${unit.suffix} (${formatCleanNumber(res.m2PerCarton)} m²/ctn • ${res.cartonsNeeded} ctn)",
                                                quantite = res.deliveredAreaM2,
                                                unite = "m²",
                                                prixUnitaire = 0.0,
                                                sousTotal = 0.0
                                            )
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp)
                                        .testTag("calc_send_to_pos_btn"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Emerald500,
                                        contentColor = OnDark
                                    )
                                ) {
                                    Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.m2_calc_send_to_pos),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (calculatedResult == null) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Slate900,
                    border = BorderStroke(1.dp, Slate800)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = Slate400,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.m2_calc_empty_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate400
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun PiecesPerCartonField(
    value: String,
    onValueChange: (String) -> Unit,
    colors: androidx.compose.material3.TextFieldColors,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = stringResource(R.string.m2_calc_pieces_label),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingIcon = {
            Icon(Icons.Default.GridOn, contentDescription = null, tint = Amber400)
        },
        trailingIcon = if (value.isNotEmpty()) {
            {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Effacer", tint = Rose500)
                }
            }
        } else null,
        suffix = {
            Text("pcs", color = Amber400, fontWeight = FontWeight.Bold)
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next
        ),
        shape = RoundedCornerShape(14.dp),
        colors = colors,
        modifier = modifier.testTag("pieces_per_carton_input")
    )
}

@Composable
private fun RequiredAreaField(
    value: String,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit,
    colors: androidx.compose.material3.TextFieldColors,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = stringResource(R.string.m2_calc_required_area_label),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingIcon = {
            Icon(Icons.Default.SquareFoot, contentDescription = null, tint = Emerald400)
        },
        trailingIcon = if (value.isNotEmpty()) {
            {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Effacer", tint = Rose500)
                }
            }
        } else null,
        suffix = {
            Text("m²", color = Emerald400, fontWeight = FontWeight.Bold)
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        shape = RoundedCornerShape(14.dp),
        colors = colors,
        modifier = modifier.testTag("required_area_input")
    )
}

@Composable
private fun ResultMetricTile(
    icon: ImageVector,
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Slate950,
        border = BorderStroke(1.dp, Slate800)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = Slate400,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = Slate100,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

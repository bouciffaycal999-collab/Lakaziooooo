package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CalcInputMode
import com.example.data.model.CartItem
import com.example.data.model.FreeLineItem
import com.example.data.model.TileCalculatorMath
import com.example.data.model.TileDimensionUnit
import com.example.data.model.TileProduct
import com.example.data.model.formatDa
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
import com.example.ui.theme.Emerald950
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy950
import com.example.ui.theme.OnDark
import com.example.ui.theme.Rose400
import com.example.ui.theme.Rose500
import com.example.ui.theme.Rose950
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import java.util.UUID
import kotlin.math.round

private enum class CalcFieldTarget(val allowDecimal: Boolean) {
    SURFACE(true),
    CARTONS(false),
    PIECES(false),
    TILE_LENGTH(true),
    TILE_WIDTH(true),
    COLISAGE_M2(true),
    PCS_PER_CARTON(false),
    PRICE_PER_M2(true),
    PRICE_PER_PIECE(true)
}

private val cleanDecimalFormat = DecimalFormat("0.####", DecimalFormatSymbols(Locale.US))

private fun formatDimNum(v: Double): String {
    val clean = (v * 10000.0).let { round(it) / 10000.0 }
    return cleanDecimalFormat.format(clean)
}

private fun formatMoneyInput(v: Double): String {
    return if (v % 1.0 == 0.0) v.toInt().toString() else v.toString()
}

private fun parseInitialTileDims(dimension: String?): Pair<String, String> {
    if (dimension.isNullOrBlank()) return "60" to "60"
    val nums = Regex("""\d+(?:[.,]\d+)?""").findAll(dimension).map { it.value.replace(',', '.') }.toList()
    return if (nums.size >= 2) nums[0] to nums[1] else "60" to "60"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartTileCalculatorSheet(
    product: TileProduct?,
    editingCartItem: CartItem?,
    isOneTimeProduct: Boolean = false,
    onDismiss: () -> Unit,
    onConfirmProductCart: (
        product: TileProduct,
        cartons: Int,
        pieces: Int,
        m2Total: Double,
        prixPerM2: Double,
        bain: String,
        calibre: String,
        editingItemId: String?,
        prixPiece: Double
    ) -> Unit,
    onConfirmStandaloneFreeLine: (FreeLineItem) -> Unit
) {
    BackHandler { onDismiss() }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val isCatalogBound = product != null && !isOneTimeProduct
    val isProductCartMode = isCatalogBound || isOneTimeProduct || editingCartItem != null

    var customProductName by remember {
        mutableStateOf(
            when {
                editingCartItem != null -> editingCartItem.name
                isOneTimeProduct -> ""
                else -> ""
            }
        )
    }

    val (initTileL, initTileW) = remember(product?.dimension, editingCartItem?.dimension) {
        parseInitialTileDims(editingCartItem?.dimension ?: product?.dimension)
    }
    var tileUnit by remember { mutableStateOf(TileDimensionUnit.CM) }
    var tileLengthStr by remember { mutableStateOf(initTileL) }
    var tileWidthStr by remember { mutableStateOf(initTileW) }
    var showColisageBuilder by remember { mutableStateOf(!isCatalogBound) }

    var mode by remember {
        mutableStateOf(if (editingCartItem != null) CalcInputMode.CARTONS_PIECES else CalcInputMode.SURFACE)
    }
    var marginPercent by remember { mutableIntStateOf(0) }

    var surfaceStr by remember {
        mutableStateOf(
            if (editingCartItem != null) formatM2(editingCartItem.m2Total)
            else "20"
        )
    }
    var cartonsStr by remember {
        mutableStateOf((editingCartItem?.cartons ?: 1).toString())
    }
    var piecesStr by remember {
        mutableStateOf((editingCartItem?.pieces ?: 0).toString())
    }
    var colisageM2Str by remember {
        mutableStateOf(
            formatDimNum(editingCartItem?.m2Carton ?: product?.m2Carton ?: 1.44)
        )
    }
    var pcsPerCartonStr by remember {
        mutableStateOf((editingCartItem?.pcs ?: product?.pcs ?: 4).toString())
    }

    val initM2Price = editingCartItem?.prixUnitaire ?: product?.prix ?: 1200.0
    val initM2Ctn = (editingCartItem?.m2Carton ?: product?.m2Carton ?: 1.44).coerceAtLeast(0.01)
    val initPcs = (editingCartItem?.pcs ?: product?.pcs ?: 4).coerceAtLeast(1)

    var pricePerM2Str by remember { mutableStateOf(formatMoneyInput(initM2Price)) }
    var pricePerPieceStr by remember {
        val initPiecePrice = editingCartItem?.prixPiece?.takeIf { it > 0.0 }
            ?: round(initM2Price * (initM2Ctn / initPcs))
        mutableStateOf(formatMoneyInput(initPiecePrice))
    }
    var hasCustomPiecePrice by remember {
        mutableStateOf((editingCartItem?.prixPiece ?: 0.0) > 0.0)
    }

    var bainNote by remember { mutableStateOf(editingCartItem?.bain ?: "") }
    var calibreNote by remember { mutableStateOf(editingCartItem?.calibre ?: "") }

    var activeField by remember {
        mutableStateOf(
            if (editingCartItem != null) CalcFieldTarget.CARTONS else CalcFieldTarget.SURFACE
        )
    }
    var isPristine by remember { mutableStateOf(true) }
    var showKeypad by remember { mutableStateOf(true) }

    val m2PerCartonVal = (colisageM2Str.toDoubleOrNull() ?: product?.m2Carton ?: 1.44).coerceAtLeast(0.01)
    val pcsPerCartonVal = (pcsPerCartonStr.toIntOrNull() ?: product?.pcs ?: 4).coerceAtLeast(1)
    val prixVal = (pricePerM2Str.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
    val prixPieceVal = (pricePerPieceStr.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
    val loosePiecesCount = piecesStr.toIntOrNull() ?: 0

    val singleTileAreaPreview = remember(tileLengthStr, tileWidthStr, tileUnit) {
        val l = tileLengthStr.toDoubleOrNull() ?: 0.0
        val w = tileWidthStr.toDoubleOrNull() ?: 0.0
        if (l > 0.0 && w > 0.0) {
            val lm = if (tileUnit == TileDimensionUnit.CM) l / 100.0 else l
            val wm = if (tileUnit == TileDimensionUnit.CM) w / 100.0 else w
            lm * wm
        } else 0.0
    }

    fun recomputeColisageFromTileDims(
        nextL: String = tileLengthStr,
        nextW: String = tileWidthStr,
        nextPcs: String = pcsPerCartonStr,
        unit: TileDimensionUnit = tileUnit
    ) {
        val l = nextL.toDoubleOrNull() ?: 0.0
        val w = nextW.toDoubleOrNull() ?: 0.0
        val pcs = nextPcs.toIntOrNull() ?: 0
        if (l > 0.0 && w > 0.0 && pcs > 0) {
            val boxRes = TileCalculatorMath.calculateM2PerCartonAndCartons(
                tileLength = l,
                tileWidth = w,
                unit = unit,
                piecesPerCarton = pcs,
                requiredAreaM2 = 1.0,
                marginPercent = 0
            )
            if (boxRes.m2PerCarton > 0.0) {
                colisageM2Str = formatDimNum(boxRes.m2PerCarton)
                if (!hasCustomPiecePrice) {
                    val pM2 = pricePerM2Str.toDoubleOrNull() ?: 0.0
                    pricePerPieceStr = formatMoneyInput(round(pM2 * (boxRes.m2PerCarton / pcs.coerceAtLeast(1))))
                }
            }
        }
    }

    val result = remember(
        mode, surfaceStr, cartonsStr, piecesStr,
        marginPercent, m2PerCartonVal, pcsPerCartonVal, prixVal, prixPieceVal
    ) {
        when (mode) {
            CalcInputMode.CARTONS_PIECES -> TileCalculatorMath.calculateFromCartonsAndPieces(
                cartons = cartonsStr.toIntOrNull() ?: 0,
                loosePieces = piecesStr.toIntOrNull() ?: 0,
                m2PerCarton = m2PerCartonVal,
                pcsPerCarton = pcsPerCartonVal,
                prixPerM2 = prixVal,
                prixParPiece = prixPieceVal
            )
            else -> TileCalculatorMath.calculateFromSurface(
                surfaceM2 = surfaceStr.toDoubleOrNull() ?: 0.0,
                marginPercent = marginPercent,
                m2PerCarton = m2PerCartonVal,
                pcsPerCarton = pcsPerCartonVal,
                prixPerM2 = prixVal
            )
        }
    }

    fun selectNumericField(target: CalcFieldTarget) {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
        activeField = target
        isPristine = true
        showKeypad = true
    }

    fun currentFieldValue(): String = when (activeField) {
        CalcFieldTarget.SURFACE -> surfaceStr
        CalcFieldTarget.CARTONS -> cartonsStr
        CalcFieldTarget.PIECES -> piecesStr
        CalcFieldTarget.TILE_LENGTH -> tileLengthStr
        CalcFieldTarget.TILE_WIDTH -> tileWidthStr
        CalcFieldTarget.COLISAGE_M2 -> colisageM2Str
        CalcFieldTarget.PCS_PER_CARTON -> pcsPerCartonStr
        CalcFieldTarget.PRICE_PER_M2 -> pricePerM2Str
        CalcFieldTarget.PRICE_PER_PIECE -> pricePerPieceStr
    }

    fun setFieldValue(newVal: String) {
        when (activeField) {
            CalcFieldTarget.SURFACE -> surfaceStr = newVal
            CalcFieldTarget.CARTONS -> cartonsStr = newVal
            CalcFieldTarget.PIECES -> piecesStr = newVal
            CalcFieldTarget.TILE_LENGTH -> {
                tileLengthStr = newVal
                recomputeColisageFromTileDims(nextL = newVal)
            }
            CalcFieldTarget.TILE_WIDTH -> {
                tileWidthStr = newVal
                recomputeColisageFromTileDims(nextW = newVal)
            }
            CalcFieldTarget.COLISAGE_M2 -> {
                colisageM2Str = newVal
                if (!hasCustomPiecePrice) {
                    val m2c = (newVal.toDoubleOrNull() ?: m2PerCartonVal).coerceAtLeast(0.01)
                    pricePerPieceStr = formatMoneyInput(round(prixVal * (m2c / pcsPerCartonVal)))
                }
            }
            CalcFieldTarget.PCS_PER_CARTON -> {
                pcsPerCartonStr = newVal
                recomputeColisageFromTileDims(nextPcs = newVal)
            }
            CalcFieldTarget.PRICE_PER_M2 -> {
                pricePerM2Str = newVal
                if (!hasCustomPiecePrice) {
                    val pM2 = (newVal.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
                    pricePerPieceStr = formatMoneyInput(round(pM2 * (m2PerCartonVal / pcsPerCartonVal)))
                }
            }
            CalcFieldTarget.PRICE_PER_PIECE -> {
                hasCustomPiecePrice = true
                pricePerPieceStr = newVal
            }
        }
    }

    fun submitCalculation() {
        if (result.deliveredM2 <= 0.0) return
        val dimText = if (tileLengthStr.isNotBlank() && tileWidthStr.isNotBlank()) {
            "${tileLengthStr}×${tileWidthStr}${tileUnit.suffix}"
        } else {
            product?.dimension ?: "Standard"
        }
        val activePieceCost = if (result.loosePieces > 0) prixPieceVal else 0.0

        if (isOneTimeProduct) {
            val fallbackName = "Carrelage $dimText (${formatM2(m2PerCartonVal)}m²/ctn)"
            val finalProduct = TileProduct(
                id = product?.id ?: "onetime_${UUID.randomUUID().toString().take(8)}",
                name = customProductName.trim().ifBlank { fallbackName },
                usine = product?.usine?.ifBlank { "Vente unique" } ?: "Vente unique",
                dimension = dimText,
                m2Carton = m2PerCartonVal,
                pcs = pcsPerCartonVal,
                prix = prixVal,
                prixAchat = product?.prixAchat ?: 0.0,
                stock = 0,
                color = product?.color ?: "#F97316",
                colorName = product?.colorName ?: "Cuivre",
                image = product?.image ?: ""
            )
            onConfirmProductCart(
                finalProduct,
                result.cartons,
                result.loosePieces,
                result.deliveredM2,
                prixVal,
                bainNote.trim(),
                calibreNote.trim(),
                editingCartItem?.id,
                activePieceCost
            )
        } else if (isCatalogBound && product != null) {
            val updatedProduct = product.copy(
                m2Carton = m2PerCartonVal,
                pcs = pcsPerCartonVal
            )
            onConfirmProductCart(
                updatedProduct,
                result.cartons,
                result.loosePieces,
                result.deliveredM2,
                prixVal,
                bainNote.trim(),
                calibreNote.trim(),
                editingCartItem?.id,
                activePieceCost
            )
        } else {
            val titlePrefix = customProductName.trim().ifBlank { "Carrelage $dimText" }
            onConfirmStandaloneFreeLine(
                FreeLineItem(
                    id = "calc_${UUID.randomUUID().toString().take(8)}",
                    designation = "$titlePrefix (${formatM2(m2PerCartonVal)}m²/ctn • ${result.cartons} ctn)",
                    quantite = result.deliveredM2,
                    unite = "m²",
                    prixUnitaire = prixVal,
                    sousTotal = result.totalDa
                )
            )
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Slate950,
        contentColor = Slate100,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 640.dp)
                .align(Alignment.CenterHorizontally)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .testTag("smart_tile_calculator_sheet"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Navy Header Banner + Big Red Exit Button
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Navy950
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isCatalogBound && product != null) {
                            TileSwatchImage(
                                imageUrl = product.image,
                                colorHex = product.color,
                                contentDescription = product.name,
                                modifier = Modifier.size(48.dp),
                                cornerRadius = 12.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                        } else {
                            Surface(
                                modifier = Modifier.size(46.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = if (isOneTimeProduct) Amber500 else Cyan500
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isOneTimeProduct) Icons.Default.AddBusiness else Icons.Default.Calculate,
                                        contentDescription = "Calculateur",
                                        tint = OnDark
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                        }
                        Column {
                            Text(
                                text = when {
                                    isOneTimeProduct && editingCartItem != null -> "Modifier Vente Unique"
                                    isOneTimeProduct -> "Produit 1× (Calc Intégré)"
                                    product != null -> product.name
                                    editingCartItem != null -> editingCartItem.name
                                    else -> "Calculateur m² / Cartons"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                color = OnDark,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val subtitle = when {
                                isOneTimeProduct -> "Prix & m²/carton configurables"
                                product != null -> "${product.usine} • ${product.dimension} • ${formatM2(m2PerCartonVal)} m²/ctn"
                                editingCartItem != null -> "${editingCartItem.usine} • ${formatM2(m2PerCartonVal)} m²/ctn"
                                else -> "Surface ↔ Cartons & Pièces"
                            }
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = Amber500,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (isCatalogBound && product != null) {
                                Text(
                                    text = if (product.stock > 0) "En stock: ${product.stock} ctn" else "Rupture (0 ctn)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (product.stock > 0) Emerald500 else Rose400,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Rose500, contentColor = OnDark),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("calc_close_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("FERMER", fontWeight = FontWeight.Black)
                    }
                }
            }

            // Custom Name input for One-Time Sale or Standalone mode
            if (isOneTimeProduct || !isCatalogBound) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ClearableOutlinedTextField(
                        value = customProductName,
                        onValueChange = { customProductName = it },
                        label = {
                            Text(
                                if (isOneTimeProduct) "Nom du produit (vente unique)"
                                else "Désignation (optionnel)"
                            )
                        },
                        placeholder = { Text("Ex: Carrelage Marbre Beige 60×60", color = Slate400) },
                        onDismissKeypad = { showKeypad = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("calc_onetime_name_input")
                    )
                    if (isOneTimeProduct) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Carrelage Sol", "Faïence Mur", "Dalle Rectifiée", "Porcelaine", "Plinthe").forEach { quickName ->
                                val isSel = customProductName.startsWith(quickName)
                                FilterChip(
                                    selected = isSel,
                                    onClick = {
                                        focusManager.clearFocus(force = true)
                                        customProductName = "$quickName ${tileLengthStr}×${tileWidthStr}"
                                    },
                                    label = { Text(quickName, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Amber500,
                                        selectedLabelColor = OnDark,
                                        containerColor = Slate900,
                                        labelColor = Slate400
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 2 Focused Modes: Surface (m² -> Cartons) and Cartons & Pièces (with piece cost pricing)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CalcModeChip(
                    selected = mode != CalcInputMode.CARTONS_PIECES,
                    label = "Surface (m²) → Cartons",
                    icon = Icons.Default.SquareFoot,
                    onClick = {
                        mode = CalcInputMode.SURFACE
                        selectNumericField(CalcFieldTarget.SURFACE)
                    },
                    modifier = Modifier.weight(1f),
                    testTag = "calc_tab_surface"
                )
                CalcModeChip(
                    selected = mode == CalcInputMode.CARTONS_PIECES,
                    label = "Cartons & Pièces",
                    icon = Icons.Default.GridOn,
                    onClick = {
                        mode = CalcInputMode.CARTONS_PIECES
                        if (cartonsStr == "0") cartonsStr = result.cartons.coerceAtLeast(1).toString()
                        selectNumericField(CalcFieldTarget.CARTONS)
                    },
                    modifier = Modifier.weight(1f),
                    testTag = "calc_tab_cartons"
                )
            }

            // Colisage m²/carton toggle / builder
            if (isCatalogBound) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Colisage : ${formatDimNum(m2PerCartonVal)} m²/ctn ($pcsPerCartonVal pcs)",
                        style = MaterialTheme.typography.labelLarge,
                        color = Slate400,
                        fontWeight = FontWeight.SemiBold
                    )
                    FilterChip(
                        selected = showColisageBuilder,
                        onClick = { showColisageBuilder = !showColisageBuilder },
                        label = {
                            Text(
                                if (showColisageBuilder) "Masquer m²/ctn" else "Modifier m²/ctn",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Cyan500,
                            selectedLabelColor = OnDark,
                            containerColor = Slate900,
                            labelColor = Cyan400
                        )
                    )
                }
            }

            AnimatedVisibility(visible = showColisageBuilder) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Slate900,
                    border = BorderStroke(1.dp, Cyan500.copy(alpha = 0.45f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Colisage Carreau (m² / Carton)",
                                style = MaterialTheme.typography.labelLarge,
                                color = Cyan400,
                                fontWeight = FontWeight.Bold
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                TileDimensionUnit.entries.forEach { u ->
                                    val isSel = tileUnit == u
                                    Surface(
                                        modifier = Modifier
                                            .height(34.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                if (tileUnit != u) {
                                                    val factor = if (u == TileDimensionUnit.METER) 0.01 else 100.0
                                                    tileLengthStr.toDoubleOrNull()?.let { tileLengthStr = formatDimNum(it * factor) }
                                                    tileWidthStr.toDoubleOrNull()?.let { tileWidthStr = formatDimNum(it * factor) }
                                                    tileUnit = u
                                                    recomputeColisageFromTileDims(unit = u)
                                                }
                                            },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSel) Cyan500 else Slate850,
                                        border = BorderStroke(1.dp, if (isSel) Cyan500 else Slate700)
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(horizontal = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = u.suffix.uppercase(),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isSel) OnDark else Slate400,
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
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            TileCalculatorMath.TILE_FORMAT_PRESETS.forEach { preset ->
                                val targetL = if (tileUnit == TileDimensionUnit.CM) preset.lengthCm else preset.lengthCm / 100.0
                                val targetW = if (tileUnit == TileDimensionUnit.CM) preset.widthCm else preset.widthCm / 100.0
                                val isSel = kotlin.math.abs((tileLengthStr.toDoubleOrNull() ?: 0.0) - targetL) < 0.001 &&
                                    kotlin.math.abs((tileWidthStr.toDoubleOrNull() ?: 0.0) - targetW) < 0.001 &&
                                    pcsPerCartonVal == preset.piecesPerCarton
                                FilterChip(
                                    selected = isSel,
                                    onClick = {
                                        focusManager.clearFocus(force = true)
                                        keyboardController?.hide()
                                        val nextL = formatDimNum(targetL)
                                        val nextW = formatDimNum(targetW)
                                        val nextPcs = preset.piecesPerCarton.toString()
                                        tileLengthStr = nextL
                                        tileWidthStr = nextW
                                        pcsPerCartonStr = nextPcs
                                        recomputeColisageFromTileDims(nextL, nextW, nextPcs, tileUnit)
                                    },
                                    label = {
                                        Text("${preset.label} (${preset.piecesPerCarton}p)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Amber500,
                                        selectedLabelColor = OnDark,
                                        containerColor = Slate950,
                                        labelColor = Slate400
                                    )
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            NumericDisplayField(
                                label = "Long. carreau",
                                value = tileLengthStr,
                                isActive = showKeypad && activeField == CalcFieldTarget.TILE_LENGTH,
                                onSelect = { selectNumericField(CalcFieldTarget.TILE_LENGTH) },
                                onClear = { tileLengthStr = "" },
                                suffix = tileUnit.suffix,
                                accentColor = Cyan500,
                                modifier = Modifier.weight(1f),
                                testTag = "calc_tile_len_field"
                            )
                            NumericDisplayField(
                                label = "Larg. carreau",
                                value = tileWidthStr,
                                isActive = showKeypad && activeField == CalcFieldTarget.TILE_WIDTH,
                                onSelect = { selectNumericField(CalcFieldTarget.TILE_WIDTH) },
                                onClear = { tileWidthStr = "" },
                                suffix = tileUnit.suffix,
                                accentColor = Cyan500,
                                modifier = Modifier.weight(1f),
                                testTag = "calc_tile_wid_field"
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            NumericDisplayField(
                                label = "Pcs / Carton",
                                value = pcsPerCartonStr,
                                isActive = showKeypad && activeField == CalcFieldTarget.PCS_PER_CARTON,
                                onSelect = { selectNumericField(CalcFieldTarget.PCS_PER_CARTON) },
                                onClear = { pcsPerCartonStr = "" },
                                suffix = "pcs",
                                accentColor = Amber500,
                                modifier = Modifier.weight(1f),
                                testTag = "calc_pcs_per_carton_field"
                            )
                            NumericDisplayField(
                                label = "m² / Carton",
                                value = colisageM2Str,
                                isActive = showKeypad && activeField == CalcFieldTarget.COLISAGE_M2,
                                onSelect = { selectNumericField(CalcFieldTarget.COLISAGE_M2) },
                                onClear = { colisageM2Str = "" },
                                suffix = "m²",
                                accentColor = Emerald500,
                                modifier = Modifier.weight(1f),
                                testTag = "calc_colisage_field"
                            )
                        }

                        if (singleTileAreaPreview > 0.0) {
                            Text(
                                text = "1 carreau = ${formatDimNum(singleTileAreaPreview)} m² × $pcsPerCartonVal pcs = ${formatDimNum(m2PerCartonVal)} m²/ctn",
                                style = MaterialTheme.typography.labelMedium,
                                color = Emerald300,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Primary Calculation Inputs
            if (mode != CalcInputMode.CARTONS_PIECES) {
                NumericDisplayField(
                    label = "Surface souhaitée (m²) → propose cartons exacts",
                    value = surfaceStr,
                    isActive = showKeypad && activeField == CalcFieldTarget.SURFACE,
                    onSelect = { selectNumericField(CalcFieldTarget.SURFACE) },
                    onClear = { surfaceStr = "" },
                    suffix = "m²",
                    accentColor = Amber500,
                    testTag = "calc_surface_field"
                )
                MarginChipsRow(
                    selectedMargin = marginPercent,
                    onSelectMargin = {
                        focusManager.clearFocus(force = true)
                        keyboardController?.hide()
                        marginPercent = it
                    }
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        NumericDisplayField(
                            label = "Cartons complets",
                            value = cartonsStr,
                            isActive = showKeypad && activeField == CalcFieldTarget.CARTONS,
                            onSelect = { selectNumericField(CalcFieldTarget.CARTONS) },
                            onClear = { cartonsStr = "" },
                            suffix = "ctn",
                            accentColor = Amber500,
                            testTag = "calc_cartons_field"
                        )
                        StepperRow(
                            onMinus = {
                                val c = (cartonsStr.toIntOrNull() ?: 0).minus(1).coerceAtLeast(0)
                                cartonsStr = c.toString()
                            },
                            onPlus = {
                                val c = (cartonsStr.toIntOrNull() ?: 0) + 1
                                cartonsStr = c.toString()
                            }
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        NumericDisplayField(
                            label = "Pièces (au prix/pièce)",
                            value = piecesStr,
                            isActive = showKeypad && activeField == CalcFieldTarget.PIECES,
                            onSelect = { selectNumericField(CalcFieldTarget.PIECES) },
                            onClear = { piecesStr = "" },
                            suffix = "pcs",
                            accentColor = Cyan500,
                            testTag = "calc_pieces_field"
                        )
                        StepperRow(
                            onMinus = {
                                val p = (piecesStr.toIntOrNull() ?: 0).minus(1).coerceAtLeast(0)
                                piecesStr = p.toString()
                            },
                            onPlus = {
                                val p = (piecesStr.toIntOrNull() ?: 0) + 1
                                piecesStr = p.toString()
                            }
                        )
                    }
                }
            }

            // Live Carton Proposal & Total Hero Card (Deep Navy for ultra-crisp hierarchy)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = Navy950,
                border = BorderStroke(2.dp, Amber500)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(Navy950, Navy800)))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = buildString {
                                        append("Proposition : ${result.cartons} Carton")
                                        if (result.cartons > 1) append("s")
                                        if (result.loosePieces > 0) {
                                            append(" + ${result.loosePieces} Pcs")
                                        }
                                    },
                                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 21.sp),
                                    color = Amber500,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "${formatM2(result.deliveredM2)} m² livrés (${formatM2(result.m2PerCarton)} m²/ctn)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = OnDark,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = buildString {
                                        append("${result.cartons * result.pcsPerCarton + result.loosePieces} pièces")
                                        append(" • ${formatDa(prixVal * result.m2PerCarton)}/ctn")
                                        if (result.loosePieces > 0 && prixPieceVal > 0.0) {
                                            append(" • ${result.loosePieces}×${formatDa(prixPieceVal)}/p")
                                        }
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = OnDark.copy(alpha = 0.85f)
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Total Ligne",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OnDark.copy(alpha = 0.75f)
                                )
                                Text(
                                    text = formatDa(result.totalDa),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Emerald500,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        if (mode != CalcInputMode.CARTONS_PIECES && result.surplusM2 > 0.005) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (marginPercent > 0) {
                                        "Besoin: ${formatM2(result.requestedM2)} m² + $marginPercent% = ${formatM2(result.targetM2WithMargin)} m²"
                                    } else {
                                        "Besoin: ${formatM2(result.requestedM2)} m²"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = OnDark.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = "Surplus +${formatM2(result.surplusM2)} m²",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Amber500,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Editable Price / m² + Piece Cost Pricing (right away!)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NumericDisplayField(
                    label = "Prix / m² (Modifiable)",
                    value = pricePerM2Str,
                    isActive = showKeypad && activeField == CalcFieldTarget.PRICE_PER_M2,
                    onSelect = { selectNumericField(CalcFieldTarget.PRICE_PER_M2) },
                    onClear = { pricePerM2Str = "" },
                    suffix = "DA",
                    accentColor = Emerald500,
                    modifier = Modifier.weight(1f),
                    testTag = "calc_price_field"
                )
                if (mode == CalcInputMode.CARTONS_PIECES || loosePiecesCount > 0) {
                    NumericDisplayField(
                        label = "Prix / Pièce (Coût)",
                        value = pricePerPieceStr,
                        isActive = showKeypad && activeField == CalcFieldTarget.PRICE_PER_PIECE,
                        onSelect = { selectNumericField(CalcFieldTarget.PRICE_PER_PIECE) },
                        onClear = {
                            hasCustomPiecePrice = true
                            pricePerPieceStr = ""
                        },
                        suffix = "DA/p",
                        accentColor = Cyan500,
                        modifier = Modifier.weight(1f),
                        testTag = "calc_piece_price_field"
                    )
                } else if (isProductCartMode) {
                    ClearableOutlinedTextField(
                        value = bainNote,
                        onValueChange = { bainNote = it },
                        label = { Text("Bain / Calibre", maxLines = 1) },
                        onDismissKeypad = { showKeypad = false },
                        modifier = Modifier
                            .weight(1.1f)
                            .testTag("calc_bain_input")
                    )
                }
            }

            if (isProductCartMode && (mode == CalcInputMode.CARTONS_PIECES || loosePiecesCount > 0)) {
                ClearableOutlinedTextField(
                    value = bainNote,
                    onValueChange = { bainNote = it },
                    label = { Text("Bain / Calibre (Optionnel)", maxLines = 1) },
                    onDismissKeypad = { showKeypad = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("calc_bain_input")
                )
            }

            // Embedded Non-IME Keypad
            AnimatedVisibility(
                visible = showKeypad,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                val chips = when (activeField) {
                    CalcFieldTarget.SURFACE -> listOf("+5" to 5.0, "+10" to 10.0, "+25" to 25.0, "+50" to 50.0)
                    CalcFieldTarget.CARTONS -> listOf("+1" to 1.0, "+5" to 5.0, "+10" to 10.0, "+20" to 20.0)
                    CalcFieldTarget.PIECES -> listOf("+1" to 1.0, "+2" to 2.0, "+3" to 3.0)
                    CalcFieldTarget.PRICE_PER_M2, CalcFieldTarget.PRICE_PER_PIECE -> listOf("+50" to 50.0, "+100" to 100.0, "-50" to -50.0)
                    else -> emptyList()
                }
                NonImeNumericKeypad(
                    onKeyPress = { key ->
                        val (nextVal, nextPristine) = applyKeypadInput(
                            current = currentFieldValue(),
                            key = key,
                            allowDecimal = activeField.allowDecimal,
                            isPristine = isPristine
                        )
                        setFieldValue(nextVal)
                        isPristine = nextPristine
                    },
                    allowDecimal = activeField.allowDecimal,
                    quickChips = chips,
                    onQuickDelta = { delta ->
                        val next = applyKeypadQuickDelta(
                            current = currentFieldValue(),
                            delta = delta,
                            allowDecimal = activeField.allowDecimal
                        )
                        setFieldValue(next)
                        isPristine = false
                    },
                    onValidate = { submitCalculation() },
                    validateLabel = when {
                        editingCartItem != null -> "Mettre à jour"
                        isOneTimeProduct -> "Ajouter Produit 1×"
                        isCatalogBound -> "Ajouter (${result.cartons} ctn)"
                        else -> "Ajouter au Panier"
                    },
                    accentColor = Amber500
                )
            }

            // Secondary Action Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!isCatalogBound) {
                    OutlinedButton(
                        onClick = {
                            surfaceStr = "0"
                            cartonsStr = "0"
                            piecesStr = "0"
                            marginPercent = 0
                            selectNumericField(CalcFieldTarget.SURFACE)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("calc_reset_btn"),
                        border = BorderStroke(1.5.dp, Slate700),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Slate400, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Effacer", color = Slate100, fontWeight = FontWeight.Bold)
                    }
                }
                Button(
                    onClick = { submitCalculation() },
                    enabled = result.deliveredM2 > 0.0,
                    modifier = Modifier
                        .weight(1.7f)
                        .height(54.dp)
                        .testTag("calc_confirm_action_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Emerald500,
                        contentColor = OnDark
                    )
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (editingCartItem != null) {
                            "Valider Modification"
                        } else {
                            "Ajouter • ${formatDa(result.totalDa)}"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun CalcModeChip(
    selected: Boolean,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Surface(
        modifier = modifier
            .height(48.dp)
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) Amber500 else Slate900,
        border = BorderStroke(1.5.dp, if (selected) Amber400 else Slate700)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) OnDark else Slate400,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) OnDark else Slate100,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MarginChipsRow(
    selectedMargin: Int,
    onSelectMargin: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Coupe :",
            style = MaterialTheme.typography.labelMedium,
            color = Slate400,
            fontWeight = FontWeight.Bold
        )
        TileCalculatorMath.MARGIN_PRESETS.forEach { margin ->
            val isSel = selectedMargin == margin
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onSelectMargin(margin) }
                    .testTag("calc_margin_$margin"),
                shape = RoundedCornerShape(10.dp),
                color = if (isSel) Amber950 else Slate900,
                border = BorderStroke(1.5.dp, if (isSel) Amber500 else Slate700)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (margin == 0) "Exact 0%" else "+$margin%",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSel) Amber300 else Slate400,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun StepperRow(
    onMinus: () -> Unit,
    onPlus: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(42.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onMinus),
            shape = RoundedCornerShape(10.dp),
            color = Rose950,
            border = BorderStroke(1.dp, Rose500.copy(alpha = 0.5f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Remove, contentDescription = "Moins", tint = Rose400, modifier = Modifier.size(18.dp))
            }
        }
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(42.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onPlus),
            shape = RoundedCornerShape(10.dp),
            color = Emerald950,
            border = BorderStroke(1.dp, Emerald500.copy(alpha = 0.5f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Add, contentDescription = "Plus", tint = Emerald300, modifier = Modifier.size(18.dp))
            }
        }
    }
}

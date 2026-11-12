package com.example.ui.screens

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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableRows
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.PosSeeds
import com.example.data.model.TileProduct
import com.example.data.model.formatDa
import com.example.data.model.formatM2
import com.example.ui.components.ClearableOutlinedTextField
import com.example.ui.components.NonImeNumericKeypad
import com.example.ui.components.NumericDisplayField
import com.example.ui.components.ProductPhotoLightboxDialog
import com.example.ui.components.TileSwatchImage
import com.example.ui.components.applyKeypadInput
import com.example.ui.components.openWhatsApp
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
import java.util.UUID

@Composable
fun StockScreen(
    products: List<TileProduct>,
    canModifyStock: Boolean,
    onAdjustStock: (String, Int) -> Unit,
    onSetStock: (String, Int) -> Unit,
    onSaveProduct: (TileProduct) -> Unit,
    onRequestDepotDispo: (TileProduct) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var stockFilter by remember { mutableStateOf("Tous") }
    var usineFilter by remember { mutableStateOf("Toutes") }
    var compactMode by remember { mutableStateOf(false) }

    var directEditProduct by remember { mutableStateOf<TileProduct?>(null) }
    var ficheProduct by remember { mutableStateOf<TileProduct?>(null) }
    var showNewFiche by remember { mutableStateOf(false) }
    var previewProduct by remember { mutableStateOf<TileProduct?>(null) }

    val usines = remember(products) { listOf("Toutes") + products.map { it.usine }.distinct().sorted() }

    val filtered = remember(products, searchQuery, stockFilter, usineFilter) {
        val q = searchQuery.trim().lowercase()
        products.filter { p ->
            (q.isEmpty() || p.name.lowercase().contains(q) || p.usine.lowercase().contains(q) || p.dimension.lowercase().contains(q)) &&
                (usineFilter == "Toutes" || p.usine == usineFilter) &&
                when (stockFilter) {
                    "En Stock" -> p.stock > 0
                    "Ruptures" -> p.stock <= 0
                    else -> true
                }
        }
    }

    val totalCartons = remember(products) { products.sumOf { it.stock } }
    val totalM2Stock = remember(products) { products.sumOf { it.stock * it.m2Carton } }
    val totalRuptures = remember(products) { products.count { it.stock <= 0 } }

    Column(modifier = Modifier.fillMaxSize().background(Slate950)) {
        Surface(color = Navy950) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Stock Temps Réel", style = MaterialTheme.typography.titleLarge, color = OnDark, fontWeight = FontWeight.Black)
                        Text(
                            text = "$totalCartons ctn (${formatM2(totalM2Stock)} m²) • $totalRuptures ruptures",
                            style = MaterialTheme.typography.bodySmall,
                            color = Amber500,
                            fontWeight = FontWeight.Bold
                        )
                        if (!canModifyStock) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Cyan950
                            ) {
                                Text(
                                    text = "Lecture seule vendeur — Sync Cloud active",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Cyan300,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { compactMode = !compactMode }) {
                            Icon(
                                imageVector = if (compactMode) Icons.Default.GridView else Icons.Default.TableRows,
                                contentDescription = "Vue",
                                tint = OnDark
                            )
                        }
                        IconButton(
                            onClick = {
                                val ruptures = products.filter { it.stock <= 0 }.take(30)
                                val msg = buildString {
                                    appendLine("⚠️ *ÉTAT RUPTURES STOCK (${ruptures.size})*")
                                    ruptures.forEach { appendLine("• ${it.name} [${it.usine}]") }
                                }
                                openWhatsApp(context, PosSeeds.WHATSAPP_PATRON, msg)
                            }
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Partager ruptures WhatsApp", tint = Emerald500)
                        }
                        if (canModifyStock) {
                            Button(
                                onClick = { showNewFiche = true },
                                modifier = Modifier.height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Amber500, contentColor = OnDark),
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Modèle", fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }

                ClearableOutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Filtrer stock par nom, usine, format...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Cyan400) },
                    modifier = Modifier.fillMaxWidth().testTag("stock_search_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Tous", "En Stock", "Ruptures").forEach { f ->
                        FilterChip(
                            selected = stockFilter == f,
                            onClick = { stockFilter = f },
                            label = { Text(f, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Amber500,
                                selectedLabelColor = OnDark,
                                containerColor = Slate900,
                                labelColor = Slate100
                            )
                        )
                    }
                    usines.drop(1).forEach { u ->
                        FilterChip(
                            selected = usineFilter == u,
                            onClick = { usineFilter = if (usineFilter == u) "Toutes" else u },
                            label = { Text(u, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Cyan500,
                                selectedLabelColor = OnDark,
                                containerColor = Slate900,
                                labelColor = Slate100
                            )
                        )
                    }
                }
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filtered, key = { it.id }) { p ->
                val isLow = p.stock in 1..5
                val isOut = p.stock <= 0
                val badgeBg = when {
                    isOut -> Rose950
                    isLow -> Amber950
                    else -> Emerald950
                }
                val badgeFg = when {
                    isOut -> Rose400
                    isLow -> Amber300
                    else -> Emerald300
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Slate900,
                    border = BorderStroke(1.5.dp, Slate800)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (!compactMode) {
                                TileSwatchImage(
                                    imageUrl = p.image,
                                    colorHex = p.color,
                                    contentDescription = p.name,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clickable { previewProduct = p }
                                        .testTag("stock_photo_${p.id}"),
                                    cornerRadius = 12.dp
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(p.name, style = MaterialTheme.typography.titleMedium, color = Slate100, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(
                                    text = "${p.usine} • ${p.dimension} • ${formatM2(p.m2Carton)}m²/ctn • ${formatDa(p.prix)}/m²",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate400,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = badgeBg,
                                border = BorderStroke(1.dp, badgeFg.copy(alpha = 0.5f)),
                                modifier = Modifier.clickable(enabled = canModifyStock) {
                                    directEditProduct = p
                                }
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = "${p.stock} ctn",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = badgeFg,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = "${formatM2(p.stock * p.m2Carton)} m²",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Slate400,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { onRequestDepotDispo(p) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("stock_dispo_btn_${p.id}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Cyan500, contentColor = OnDark),
                                contentPadding = PaddingValues(horizontal = 10.dp)
                            ) {
                                Icon(Icons.Default.Warehouse, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Dispo Dépôt", fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelLarge)
                            }
                            if (canModifyStock) {
                                OutlinedButton(
                                    onClick = { ficheProduct = p },
                                    modifier = Modifier.height(44.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.5.dp, Amber500)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Éditer fiche", tint = Amber400, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Fiche", color = Amber400, fontWeight = FontWeight.Black)
                                }
                            }
                        }

                        if (canModifyStock) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf(-10, -1, +1, +10).forEach { delta ->
                                    val isPos = delta > 0
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { onAdjustStock(p.id, delta) },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isPos) Emerald950 else Rose950,
                                        border = BorderStroke(1.dp, if (isPos) Emerald500 else Rose500)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = if (isPos) "+$delta" else "$delta",
                                                color = if (isPos) Emerald300 else Rose400,
                                                fontWeight = FontWeight.Black,
                                                style = MaterialTheme.typography.titleSmall
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    directEditProduct?.let { prod ->
        DirectStockEditDialog(
            product = prod,
            onDismiss = { directEditProduct = null },
            onConfirm = { newStock ->
                onSetStock(prod.id, newStock)
                directEditProduct = null
            }
        )
    }

    if (showNewFiche || ficheProduct != null) {
        ProductFicheDialog(
            initial = ficheProduct,
            onDismiss = {
                ficheProduct = null
                showNewFiche = false
            },
            onSave = {
                onSaveProduct(it)
                ficheProduct = null
                showNewFiche = false
            }
        )
    }

    previewProduct?.let { prod ->
        ProductPhotoLightboxDialog(
            product = prod,
            onDismiss = { previewProduct = null },
            onCheckDepotDispo = { onRequestDepotDispo(prod) },
            onOpenCalculator = null
        )
    }
}

@Composable
private fun DirectStockEditDialog(
    product: TileProduct,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    BackHandler { onDismiss() }
    var stockStr by remember { mutableStateOf(product.stock.toString()) }
    var isPristine by remember { mutableStateOf(true) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
                .padding(14.dp),
            shape = RoundedCornerShape(22.dp),
            color = Slate900,
            border = BorderStroke(2.dp, Navy950)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = RoundedCornerShape(14.dp), color = Navy950) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(product.name, style = MaterialTheme.typography.titleMedium, color = OnDark, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.height(42.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Rose500, contentColor = OnDark),
                            contentPadding = PaddingValues(horizontal = 10.dp)
                        ) {
                            Text("FERMER", fontWeight = FontWeight.Black)
                        }
                    }
                }
                NumericDisplayField(
                    label = "Nouveau Stock (Cartons)",
                    value = stockStr,
                    isActive = true,
                    onSelect = {},
                    onClear = { stockStr = ""; isPristine = false },
                    suffix = "ctn",
                    accentColor = Emerald500,
                    testTag = "direct_stock_field"
                )
                NonImeNumericKeypad(
                    onKeyPress = { key ->
                        val (next, nextPristine) = applyKeypadInput(stockStr, key, false, isPristine)
                        stockStr = next
                        isPristine = nextPristine
                    },
                    allowDecimal = false,
                    onValidate = { onConfirm(stockStr.toIntOrNull() ?: 0) },
                    validateLabel = "Appliquer Stock",
                    accentColor = Emerald500
                )
            }
        }
    }
}

private enum class FicheNumField(val decimal: Boolean) { M2_CTN(true), PCS(false), PRIX(false), PRIX_ACHAT(false), STOCK(false) }

@Composable
private fun ProductFicheDialog(
    initial: TileProduct?,
    onDismiss: () -> Unit,
    onSave: (TileProduct) -> Unit
) {
    BackHandler { onDismiss() }
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var usine by remember { mutableStateOf(initial?.usine ?: "BCRAM") }
    var dimension by remember { mutableStateOf(initial?.dimension ?: "60×60") }
    var choix by remember { mutableStateOf(initial?.choix ?: "1er Choix") }
    var m2CtnStr by remember { mutableStateOf(formatM2(initial?.m2Carton ?: 1.44)) }
    var pcsStr by remember { mutableStateOf((initial?.pcs ?: 4).toString()) }
    var prixStr by remember { mutableStateOf((initial?.prix ?: 1200.0).toInt().toString()) }
    var prixAchatStr by remember { mutableStateOf((initial?.prixAchat ?: 950.0).toInt().toString()) }
    var stockStr by remember { mutableStateOf((initial?.stock ?: 0).toString()) }

    var activeField by remember { mutableStateOf(FicheNumField.PRIX) }
    var showKeypad by remember { mutableStateOf(false) }
    var isPristine by remember { mutableStateOf(true) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp)
                .padding(14.dp),
            shape = RoundedCornerShape(22.dp),
            color = Slate900,
            border = BorderStroke(2.dp, Navy950)
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(shape = RoundedCornerShape(14.dp), color = Navy950) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (initial == null) "Nouveau Modèle" else "Modifier Modèle", style = MaterialTheme.typography.titleLarge, color = Amber500, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Rose500, contentColor = OnDark),
                            contentPadding = PaddingValues(horizontal = 10.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("FERMER", fontWeight = FontWeight.Black)
                        }
                    }
                }

                ClearableOutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom du modèle") },
                    onDismissKeypad = { showKeypad = false },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ClearableOutlinedTextField(
                        value = usine,
                        onValueChange = { usine = it },
                        label = { Text("Usine") },
                        autoClearValues = setOf("BCRAM"),
                        onDismissKeypad = { showKeypad = false },
                        modifier = Modifier.weight(1f)
                    )
                    ClearableOutlinedTextField(
                        value = dimension,
                        onValueChange = { dimension = it },
                        label = { Text("Format") },
                        onDismissKeypad = { showKeypad = false },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("1er Choix", "2ème Choix").forEach { c ->
                        FilterChip(
                            selected = choix == c,
                            onClick = { choix = c; showKeypad = false },
                            label = { Text(c, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Navy950,
                                selectedLabelColor = OnDark
                            )
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumericDisplayField(
                        label = "m²/Ctn",
                        value = m2CtnStr,
                        isActive = showKeypad && activeField == FicheNumField.M2_CTN,
                        onSelect = { activeField = FicheNumField.M2_CTN; isPristine = true; showKeypad = true },
                        onClear = { m2CtnStr = ""; isPristine = false },
                        modifier = Modifier.weight(1f)
                    )
                    NumericDisplayField(
                        label = "Pcs/Ctn",
                        value = pcsStr,
                        isActive = showKeypad && activeField == FicheNumField.PCS,
                        onSelect = { activeField = FicheNumField.PCS; isPristine = true; showKeypad = true },
                        onClear = { pcsStr = ""; isPristine = false },
                        modifier = Modifier.weight(1f)
                    )
                    NumericDisplayField(
                        label = "Stock",
                        value = stockStr,
                        isActive = showKeypad && activeField == FicheNumField.STOCK,
                        onSelect = { activeField = FicheNumField.STOCK; isPristine = true; showKeypad = true },
                        onClear = { stockStr = ""; isPristine = false },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumericDisplayField(
                        label = "Prix Vente/m²",
                        value = prixStr,
                        isActive = showKeypad && activeField == FicheNumField.PRIX,
                        onSelect = { activeField = FicheNumField.PRIX; isPristine = true; showKeypad = true },
                        onClear = { prixStr = ""; isPristine = false },
                        suffix = "DA",
                        accentColor = Emerald500,
                        modifier = Modifier.weight(1f)
                    )
                    NumericDisplayField(
                        label = "Prix Achat/m²",
                        value = prixAchatStr,
                        isActive = showKeypad && activeField == FicheNumField.PRIX_ACHAT,
                        onSelect = { activeField = FicheNumField.PRIX_ACHAT; isPristine = true; showKeypad = true },
                        onClear = { prixAchatStr = ""; isPristine = false },
                        suffix = "DA",
                        accentColor = Amber500,
                        modifier = Modifier.weight(1f)
                    )
                }

                AnimatedVisibility(visible = showKeypad, enter = expandVertically(), exit = shrinkVertically()) {
                    NonImeNumericKeypad(
                        onKeyPress = { key ->
                            val cur = when (activeField) {
                                FicheNumField.M2_CTN -> m2CtnStr
                                FicheNumField.PCS -> pcsStr
                                FicheNumField.PRIX -> prixStr
                                FicheNumField.PRIX_ACHAT -> prixAchatStr
                                FicheNumField.STOCK -> stockStr
                            }
                            val (next, nextPristine) = applyKeypadInput(cur, key, activeField.decimal, isPristine)
                            when (activeField) {
                                FicheNumField.M2_CTN -> m2CtnStr = next
                                FicheNumField.PCS -> pcsStr = next
                                FicheNumField.PRIX -> prixStr = next
                                FicheNumField.PRIX_ACHAT -> prixAchatStr = next
                                FicheNumField.STOCK -> stockStr = next
                            }
                            isPristine = nextPristine
                        },
                        allowDecimal = activeField.decimal,
                        onValidate = { showKeypad = false },
                        validateLabel = "OK"
                    )
                }

                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            onSave(
                                TileProduct(
                                    id = initial?.id ?: "p_${UUID.randomUUID().toString().take(8)}",
                                    name = name.trim(),
                                    usine = usine.trim().ifBlank { "AUTRE" },
                                    dimension = dimension.trim().ifBlank { "60×60" },
                                    m2Carton = (m2CtnStr.toDoubleOrNull() ?: 1.44).coerceAtLeast(0.01),
                                    pcs = (pcsStr.toIntOrNull() ?: 4).coerceAtLeast(1),
                                    prix = (prixStr.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0),
                                    prixAchat = (prixAchatStr.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0),
                                    stock = (stockStr.toIntOrNull() ?: 0).coerceAtLeast(0),
                                    color = initial?.color ?: "#94a3b8",
                                    colorName = initial?.colorName ?: "Standard",
                                    image = initial?.image ?: "",
                                    finition = initial?.finition ?: "brillant",
                                    choix = choix,
                                    categorie = initial?.categorie ?: "sol",
                                    description = initial?.description ?: "${name.trim()} - ${usine.trim()}"
                                )
                            )
                        }
                    },
                    enabled = name.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald500, contentColor = OnDark)
                ) {
                    Text("Enregistrer Fiche", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

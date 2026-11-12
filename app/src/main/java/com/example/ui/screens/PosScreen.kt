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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CartItem
import com.example.data.model.Customer
import com.example.data.model.FreeLineItem
import com.example.data.model.Invoice
import com.example.data.model.InvoiceClient
import com.example.data.model.PosSeeds
import com.example.data.model.TileCalculatorMath
import com.example.data.model.TileDimensionUnit
import com.example.data.model.TileProduct
import com.example.data.model.formatDa
import com.example.data.model.formatM2
import com.example.ui.components.ClearableOutlinedTextField
import com.example.ui.components.NonImeNumericKeypad
import com.example.ui.components.NumericDisplayField
import com.example.ui.components.ProductPhotoLightboxDialog
import com.example.ui.components.TileSwatchImage
import com.example.ui.components.applyKeypadInput
import com.example.ui.components.applyKeypadQuickDelta
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
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt

private val cleanFmt = DecimalFormat("0.####", DecimalFormatSymbols(Locale.US))

private fun fmtClean(v: Double): String {
    val clean = (v * 10000.0).let { kotlin.math.round(it) / 10000.0 }
    return cleanFmt.format(clean)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(
    products: List<TileProduct>,
    cart: List<CartItem>,
    freeLines: List<FreeLineItem>,
    customers: List<Customer>,
    editingInvoice: Invoice?,
    onOpenCalculatorForProduct: (TileProduct) -> Unit,
    onRequestDepotDispo: (TileProduct) -> Unit,
    onOpenOneTimeProduct: () -> Unit,
    onEditCartItem: (CartItem) -> Unit,
    onUpdateCartItemDirect: (String, String, Int, Int, Double, Int, Double, Double) -> Unit,
    onAdjustCartItemCarton: (String, Int) -> Unit,
    onRemoveCartItem: (String) -> Unit,
    onAddFreeLine: (FreeLineItem) -> Unit,
    onUpdateFreeLine: (FreeLineItem) -> Unit,
    onAdjustFreeLineQty: (String, Double) -> Unit,
    onRemoveFreeLine: (String) -> Unit,
    onClearCart: () -> Unit,
    onFinalizeSale: (InvoiceClient, Double, Double, String, String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedUsine by remember { mutableStateOf("Toutes") }
    var selectedDimension by remember { mutableStateOf("Toutes") }
    var selectedChoix by remember { mutableStateOf("Tous") }
    var onlyInStock by remember { mutableStateOf(false) }

    var showCartSheet by remember { mutableStateOf(false) }
    var reopenCartSheetAfterDialog by remember { mutableStateOf(false) }
    var showDiversDialog by remember { mutableStateOf(false) }
    var editingFreeLine by remember { mutableStateOf<FreeLineItem?>(null) }
    var quickEditCartItem by remember { mutableStateOf<CartItem?>(null) }
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var previewProduct by remember { mutableStateOf<TileProduct?>(null) }

    val inStockCount = remember(products) { products.count { it.stock > 0 } }
    val usines = remember(products) {
        listOf("Toutes") + products.map { it.usine }.distinct().sorted()
    }
    val dimensions = remember(products) {
        listOf("Toutes") + products.map { it.dimension }.distinct().sorted()
    }

    val filteredProducts = remember(
        products, searchQuery, selectedUsine, selectedDimension, selectedChoix, onlyInStock
    ) {
        val q = searchQuery.trim().lowercase()
        products.filter { p ->
            (q.isEmpty() || p.name.lowercase().contains(q) || p.usine.lowercase().contains(q) || p.dimension.lowercase().contains(q)) &&
                (selectedUsine == "Toutes" || p.usine == selectedUsine) &&
                (selectedDimension == "Toutes" || p.dimension == selectedDimension) &&
                (selectedChoix == "Tous" || p.choix == selectedChoix) &&
                (!onlyInStock || p.stock > 0)
        }
    }

    val totalBrut = remember(cart, freeLines) {
        cart.sumOf { it.sousTotal } + freeLines.sumOf { it.sousTotal }
    }
    val totalLinesCount = cart.size + freeLines.size

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(Slate950)) {
        val isWide = maxWidth >= 840.dp

        Row(modifier = Modifier.fillMaxSize()) {
            // Main Catalog Pane
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Search + Quick Actions + Filter Strip
                    Surface(
                        color = Slate900,
                        border = BorderStroke(1.dp, Slate800)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (editingInvoice != null) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Amber950,
                                    border = BorderStroke(1.5.dp, Amber500)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Modification Facture #${editingInvoice.reference}",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = Amber300,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.weight(1f)
                                        )
                                        TextButton(onClick = onClearCart) {
                                            Text("Annuler", color = Rose400, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }
                            }

                            ClearableOutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = {
                                    Text(
                                        "Rechercher modèle, usine, format...",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Cyan400) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("pos_search_input")
                            )

                            // Quick Creation Row: One-Time Product (Smart Calc) & Divers (+ m²/ctn Calc)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = onOpenOneTimeProduct,
                                    modifier = Modifier
                                        .weight(1.25f)
                                        .height(48.dp)
                                        .testTag("pos_onetime_btn"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Cyan500,
                                        contentColor = OnDark
                                    ),
                                    contentPadding = PaddingValues(horizontal = 10.dp)
                                ) {
                                    Icon(Icons.Default.AddBusiness, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Produit 1x", fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }

                                Button(
                                    onClick = {
                                        editingFreeLine = null
                                        showDiversDialog = true
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("pos_divers_btn"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Amber500,
                                        contentColor = OnDark
                                    ),
                                    contentPadding = PaddingValues(horizontal = 10.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Divers / m²", fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }

                            // Filters Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FilterChip(
                                    selected = onlyInStock,
                                    onClick = { onlyInStock = !onlyInStock },
                                    label = { Text("En stock ($inStockCount)", fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Emerald500,
                                        selectedLabelColor = OnDark,
                                        containerColor = Slate950,
                                        labelColor = Slate400
                                    )
                                )
                                listOf("Tous", "1er Choix", "2ème Choix").forEach { choix ->
                                    FilterChip(
                                        selected = selectedChoix == choix,
                                        onClick = { selectedChoix = choix },
                                        label = { Text(choix, fontWeight = FontWeight.Bold) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Navy950,
                                            selectedLabelColor = OnDark,
                                            containerColor = Slate950,
                                            labelColor = Slate400
                                        )
                                    )
                                }
                                usines.forEach { u ->
                                    if (u != "Toutes") {
                                        FilterChip(
                                            selected = selectedUsine == u,
                                            onClick = { selectedUsine = if (selectedUsine == u) "Toutes" else u },
                                            label = { Text(u, fontWeight = FontWeight.Bold) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Amber500,
                                                selectedLabelColor = OnDark,
                                                containerColor = Slate950,
                                                labelColor = Slate400
                                            )
                                        )
                                    }
                                }
                                dimensions.forEach { d ->
                                    if (d != "Toutes") {
                                        FilterChip(
                                            selected = selectedDimension == d,
                                            onClick = { selectedDimension = if (selectedDimension == d) "Toutes" else d },
                                            label = { Text(d, fontWeight = FontWeight.Bold) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Cyan500,
                                                selectedLabelColor = OnDark,
                                                containerColor = Slate950,
                                                labelColor = Slate400
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (filteredProducts.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Inventory2, contentDescription = null, tint = Slate400, modifier = Modifier.size(52.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Aucun modèle trouvé", style = MaterialTheme.typography.titleMedium, color = Slate100, fontWeight = FontWeight.Bold)
                                Text("Essayez d'ajuster les filtres ou créez un Produit 1x.", style = MaterialTheme.typography.bodyMedium, color = Slate400)
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 168.dp),
                            contentPadding = PaddingValues(
                                start = 12.dp,
                                end = 12.dp,
                                top = 12.dp,
                                bottom = if (isWide) 16.dp else 104.dp
                            ),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredProducts, key = { it.id }) { product ->
                                ProductTileCard(
                                    product = product,
                                    onOpenCalculator = { onOpenCalculatorForProduct(product) },
                                    onCheckDepotDispo = { onRequestDepotDispo(product) },
                                    onPreviewImage = { previewProduct = product }
                                )
                            }
                        }
                    }
                }

                // Floating Deep Navy Panier Bar on Phones/Medium Screens
                if (!isWide) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { showCartSheet = true }
                            .testTag("pos_floating_cart_bar"),
                        shape = RoundedCornerShape(18.dp),
                        color = Navy950,
                        border = BorderStroke(2.dp, if (totalLinesCount > 0) Amber500 else Cyan500),
                        tonalElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                BadgedBox(
                                    badge = {
                                        if (totalLinesCount > 0) {
                                            Badge(containerColor = Amber500, contentColor = OnDark) {
                                                Text(totalLinesCount.toString(), fontWeight = FontWeight.Black)
                                            }
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.ShoppingCart, contentDescription = "Panier", tint = Amber500, modifier = Modifier.size(28.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (totalLinesCount == 0) "Panier vide" else "$totalLinesCount article(s) • ${cart.sumOf { it.cartons }} Ctn • ${formatM2(cart.sumOf { it.m2Total })} m²",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = OnDark,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = formatDa(totalBrut),
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Amber500,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = { showCartSheet = true },
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("pos_open_panier_btn"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (totalLinesCount > 0) Amber500 else Cyan500,
                                    contentColor = OnDark
                                )
                            ) {
                                Text(
                                    text = if (totalLinesCount > 0) "Voir Panier" else "Panier",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }

            // Persistent Side Cart Pane on Tablets/Expanded Screens
            if (isWide) {
                Surface(
                    modifier = Modifier
                        .width(400.dp)
                        .fillMaxHeight(),
                    color = Slate900,
                    border = BorderStroke(1.dp, Slate800)
                ) {
                    CartPanelContent(
                        cart = cart,
                        freeLines = freeLines,
                        totalBrut = totalBrut,
                        editingInvoice = editingInvoice,
                        onQuickEditCartItem = { quickEditCartItem = it },
                        onOpenFullCalcForCartItem = onEditCartItem,
                        onAdjustCartItemCarton = onAdjustCartItemCarton,
                        onRemoveCartItem = onRemoveCartItem,
                        onEditFreeLine = {
                            editingFreeLine = it
                            showDiversDialog = true
                        },
                        onAdjustFreeLineQty = onAdjustFreeLineQty,
                        onRemoveFreeLine = onRemoveFreeLine,
                        onClearCart = onClearCart,
                        onOpenOneTimeProduct = onOpenOneTimeProduct,
                        onOpenDivers = {
                            editingFreeLine = null
                            showDiversDialog = true
                        },
                        onCloseSheet = null,
                        onOpenCheckout = { showCheckoutDialog = true }
                    )
                }
            }
        }
    }

    if (showCartSheet) {
        BackHandler { showCartSheet = false }
        ModalBottomSheet(
            onDismissRequest = { showCartSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Slate900,
            contentColor = Slate100
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.86f)
            ) {
                CartPanelContent(
                    cart = cart,
                    freeLines = freeLines,
                    totalBrut = totalBrut,
                    editingInvoice = editingInvoice,
                    onQuickEditCartItem = {
                        reopenCartSheetAfterDialog = true
                        showCartSheet = false
                        quickEditCartItem = it
                    },
                    onOpenFullCalcForCartItem = {
                        reopenCartSheetAfterDialog = false
                        showCartSheet = false
                        onEditCartItem(it)
                    },
                    onAdjustCartItemCarton = onAdjustCartItemCarton,
                    onRemoveCartItem = onRemoveCartItem,
                    onEditFreeLine = {
                        reopenCartSheetAfterDialog = true
                        showCartSheet = false
                        editingFreeLine = it
                        showDiversDialog = true
                    },
                    onAdjustFreeLineQty = onAdjustFreeLineQty,
                    onRemoveFreeLine = onRemoveFreeLine,
                    onClearCart = onClearCart,
                    onOpenOneTimeProduct = {
                        reopenCartSheetAfterDialog = false
                        showCartSheet = false
                        onOpenOneTimeProduct()
                    },
                    onOpenDivers = {
                        reopenCartSheetAfterDialog = true
                        showCartSheet = false
                        editingFreeLine = null
                        showDiversDialog = true
                    },
                    onCloseSheet = { showCartSheet = false },
                    onOpenCheckout = {
                        reopenCartSheetAfterDialog = false
                        showCartSheet = false
                        showCheckoutDialog = true
                    }
                )
            }
        }
    }

    quickEditCartItem?.let { targetItem ->
        EditCartItemQuickDialog(
            item = targetItem,
            onDismiss = {
                quickEditCartItem = null
                if (reopenCartSheetAfterDialog) {
                    reopenCartSheetAfterDialog = false
                    showCartSheet = true
                }
            },
            onOpenFullCalculator = {
                reopenCartSheetAfterDialog = false
                quickEditCartItem = null
                showCartSheet = false
                onEditCartItem(targetItem)
            },
            onSave = { name, cartons, pieces, m2Ctn, pcs, prix, prixPiece ->
                onUpdateCartItemDirect(targetItem.id, name, cartons, pieces, m2Ctn, pcs, prix, prixPiece)
                quickEditCartItem = null
                if (reopenCartSheetAfterDialog) {
                    reopenCartSheetAfterDialog = false
                    showCartSheet = true
                }
            }
        )
    }

    if (showDiversDialog) {
        DiversLineDialog(
            editingLine = editingFreeLine,
            onDismiss = {
                showDiversDialog = false
                editingFreeLine = null
                if (reopenCartSheetAfterDialog) {
                    reopenCartSheetAfterDialog = false
                    showCartSheet = true
                }
            },
            onSaveLine = { line ->
                if (editingFreeLine != null) {
                    onUpdateFreeLine(line)
                } else {
                    onAddFreeLine(line)
                }
                showDiversDialog = false
                editingFreeLine = null
                if (reopenCartSheetAfterDialog) {
                    reopenCartSheetAfterDialog = false
                    showCartSheet = true
                }
            }
        )
    }

    if (showCheckoutDialog) {
        CheckoutModalDialog(
            totalBrut = totalBrut,
            customers = customers,
            editingInvoice = editingInvoice,
            onDismiss = { showCheckoutDialog = false },
            onConfirm = { client, remise, paye, mode, notes ->
                showCheckoutDialog = false
                onFinalizeSale(client, remise, paye, mode, notes)
            }
        )
    }

    previewProduct?.let { prod ->
        ProductPhotoLightboxDialog(
            product = prod,
            onDismiss = { previewProduct = null },
            onCheckDepotDispo = { onRequestDepotDispo(prod) },
            onOpenCalculator = { onOpenCalculatorForProduct(prod) }
        )
    }
}

@Composable
private fun ProductTileCard(
    product: TileProduct,
    onOpenCalculator: () -> Unit,
    onCheckDepotDispo: () -> Unit,
    onPreviewImage: () -> Unit
) {
    val isLow = product.stock in 1..5
    val isOut = product.stock <= 0
    val stockBadgeBg = when {
        isOut -> Rose950
        isLow -> Amber950
        else -> Emerald950
    }
    val stockBadgeFg = when {
        isOut -> Rose400
        isLow -> Amber300
        else -> Emerald300
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onOpenCalculator)
            .testTag("product_card_${product.id}"),
        shape = RoundedCornerShape(18.dp),
        color = Slate900,
        border = BorderStroke(1.5.dp, Slate800),
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box {
                TileSwatchImage(
                    imageUrl = product.image,
                    colorHex = product.color,
                    contentDescription = product.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(116.dp)
                        .clickable(onClick = onPreviewImage)
                        .testTag("product_photo_${product.id}"),
                    cornerRadius = 14.dp
                )
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Navy950.copy(alpha = 0.90f)
                ) {
                    Text(
                        text = product.dimension,
                        style = MaterialTheme.typography.labelMedium,
                        color = OnDark,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = stockBadgeBg,
                    border = BorderStroke(1.dp, stockBadgeFg.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = if (isOut) "Rupture" else "${product.stock} ctn",
                        style = MaterialTheme.typography.labelMedium,
                        color = stockBadgeFg,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "${product.usine} • ${product.choix}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Amber400,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Slate100,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    minLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "${formatDa(product.prix)}/m²",
                        style = MaterialTheme.typography.titleMedium,
                        color = Emerald400,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "${formatM2(product.m2Carton)} m²/ctn • ${product.pcs} pcs",
                        style = MaterialTheme.typography.labelMedium,
                        color = Slate400,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Fused Smart Calculator + Add Button (No standalone +1 Ctn)
            Button(
                onClick = onOpenCalculator,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_calc_${product.id}"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Amber500,
                    contentColor = OnDark
                ),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Calculer & Ajouter",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Explicit Dispo Dépôt Button on Every Product Card
            OutlinedButton(
                onClick = onCheckDepotDispo,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("btn_dispo_${product.id}"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, Cyan500),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Icon(Icons.Default.Warehouse, contentDescription = null, tint = Cyan400, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Dispo Dépôt",
                    color = Cyan400,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun CartPanelContent(
    cart: List<CartItem>,
    freeLines: List<FreeLineItem>,
    totalBrut: Double,
    editingInvoice: Invoice?,
    onQuickEditCartItem: (CartItem) -> Unit,
    onOpenFullCalcForCartItem: (CartItem) -> Unit,
    onAdjustCartItemCarton: (String, Int) -> Unit,
    onRemoveCartItem: (String) -> Unit,
    onEditFreeLine: (FreeLineItem) -> Unit,
    onAdjustFreeLineQty: (String, Double) -> Unit,
    onRemoveFreeLine: (String) -> Unit,
    onClearCart: () -> Unit,
    onOpenOneTimeProduct: () -> Unit,
    onOpenDivers: () -> Unit,
    onCloseSheet: (() -> Unit)?,
    onOpenCheckout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
            .testTag("panier_panel_content"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Navy Header inside Panier
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Navy950
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (editingInvoice != null) "Édition #${editingInvoice.reference}" else "Contenu du Panier",
                        style = MaterialTheme.typography.titleLarge,
                        color = OnDark,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "${cart.size + freeLines.size} article(s) • ${cart.sumOf { it.cartons }} cartons • ${formatM2(cart.sumOf { it.m2Total })} m²",
                        style = MaterialTheme.typography.bodySmall,
                        color = Amber500,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (cart.isNotEmpty() || freeLines.isNotEmpty()) {
                        IconButton(
                            onClick = onClearCart,
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Vider panier", tint = Rose400)
                        }
                    }
                    if (onCloseSheet != null) {
                        Button(
                            onClick = onCloseSheet,
                            modifier = Modifier.height(44.dp).testTag("panier_close_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Rose500, contentColor = OnDark),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer panier", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("FERMER", fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }

        // Quick Add Inside Panier
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (onCloseSheet != null) {
                OutlinedButton(
                    onClick = onCloseSheet,
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, Navy950),
                    contentPadding = PaddingValues(horizontal = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Navy950, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Catalogue", color = Navy950, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, maxLines = 1)
                }
            }
            Button(
                onClick = onOpenOneTimeProduct,
                modifier = Modifier
                    .weight(1.1f)
                    .height(44.dp)
                    .testTag("panier_add_onetime_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Cyan500, contentColor = OnDark),
                contentPadding = PaddingValues(horizontal = 6.dp)
            ) {
                Icon(Icons.Default.AddBusiness, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("Produit 1x", fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelMedium, maxLines = 1)
            }
            Button(
                onClick = onOpenDivers,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("panier_add_divers_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Amber500, contentColor = OnDark),
                contentPadding = PaddingValues(horizontal = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("Divers", fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelMedium, maxLines = 1)
            }
        }

        HorizontalDivider(color = Slate800)

        if (cart.isEmpty() && freeLines.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Slate400, modifier = Modifier.size(52.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Votre panier est vide", color = Slate100, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Ajoutez du catalogue, un Produit 1x ou Divers", color = Slate400, style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cart, key = { it.id }) { item ->
                    val isOneTime = item.productId.startsWith("onetime_")
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onQuickEditCartItem(item) }
                            .testTag("panier_cart_item_${item.id}"),
                        shape = RoundedCornerShape(14.dp),
                        color = Slate950,
                        border = BorderStroke(1.5.dp, if (isOneTime) Cyan500 else Slate800)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Slate100,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.weight(1f, fill = false),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (isOneTime) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Cyan950,
                                                border = BorderStroke(1.dp, Cyan500)
                                            ) {
                                                Text(
                                                    text = "1x",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Cyan300,
                                                    fontWeight = FontWeight.Black,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "${formatM2(item.m2Total)} m² • ${formatDa(item.prixUnitaire)}/m²" +
                                            (if (item.pieces > 0 && item.prixPiece > 0.0) " • ${item.pieces} pcs à ${formatDa(item.prixPiece)}/p" else "") +
                                            (if (item.bain.isNotBlank()) " • Bain ${item.bain}" else ""),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Cyan400,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Row {
                                    IconButton(
                                        onClick = { onQuickEditCartItem(item) },
                                        modifier = Modifier.minimumInteractiveComponentSize().testTag("cart_edit_btn_${item.id}")
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Modifier quantité et prix", tint = Amber400, modifier = Modifier.size(20.dp))
                                    }
                                    IconButton(
                                        onClick = { onOpenFullCalcForCartItem(item) },
                                        modifier = Modifier.minimumInteractiveComponentSize().testTag("cart_calc_btn_${item.id}")
                                    ) {
                                        Icon(Icons.Default.Calculate, contentDescription = "Ouvrir calculateur", tint = Cyan400, modifier = Modifier.size(20.dp))
                                    }
                                    IconButton(
                                        onClick = { onRemoveCartItem(item.id) },
                                        modifier = Modifier.minimumInteractiveComponentSize().testTag("cart_remove_btn_${item.id}")
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Retirer", tint = Rose500, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Surface(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { onAdjustCartItemCarton(item.id, -1) },
                                        color = Rose950,
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, Rose500)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Remove, contentDescription = "Diminuer cartons", tint = Rose400, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                    Text(
                                        text = "${item.cartons} ctn" + if (item.pieces > 0) " +${item.pieces}p" else "",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Amber400,
                                        fontWeight = FontWeight.Black
                                    )
                                    Surface(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { onAdjustCartItemCarton(item.id, 1) },
                                        color = Cyan950,
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, Cyan500)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Add, contentDescription = "Augmenter cartons", tint = Cyan400, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                                Text(
                                    text = formatDa(item.sousTotal),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Emerald400,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }

                items(freeLines, key = { it.id }) { line ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onEditFreeLine(line) }
                            .testTag("panier_free_item_${line.id}"),
                        shape = RoundedCornerShape(14.dp),
                        color = Slate950,
                        border = BorderStroke(1.5.dp, Amber500)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(line.designation, style = MaterialTheme.typography.titleMedium, color = Slate100, fontWeight = FontWeight.Black)
                                    Text(
                                        text = "${fmtClean(line.quantite)} ${line.unite} × ${formatDa(line.prixUnitaire)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Amber300,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Row {
                                    IconButton(
                                        onClick = { onEditFreeLine(line) },
                                        modifier = Modifier.minimumInteractiveComponentSize().testTag("free_edit_btn_${line.id}")
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Modifier article divers", tint = Amber400, modifier = Modifier.size(20.dp))
                                    }
                                    IconButton(
                                        onClick = { onRemoveFreeLine(line.id) },
                                        modifier = Modifier.minimumInteractiveComponentSize().testTag("free_remove_btn_${line.id}")
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Retirer", tint = Rose500, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Surface(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { onAdjustFreeLineQty(line.id, -1.0) },
                                        color = Rose950,
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, Rose500)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Remove, contentDescription = "Diminuer quantité", tint = Rose400, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                    Text(
                                        text = "${fmtClean(line.quantite)} ${line.unite}",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Amber400,
                                        fontWeight = FontWeight.Black
                                    )
                                    Surface(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { onAdjustFreeLineQty(line.id, 1.0) },
                                        color = Cyan950,
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, Cyan500)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Add, contentDescription = "Augmenter quantité", tint = Cyan400, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                                Text(
                                    text = formatDa(line.sousTotal),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Emerald400,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = Slate800)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Total Brut", style = MaterialTheme.typography.titleMedium, color = Slate400, fontWeight = FontWeight.Bold)
            Text(
                text = formatDa(totalBrut),
                style = MaterialTheme.typography.headlineSmall,
                color = Emerald400,
                fontWeight = FontWeight.Black
            )
        }

        Button(
            onClick = onOpenCheckout,
            enabled = cart.isNotEmpty() || freeLines.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("pos_checkout_btn"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Emerald500, contentColor = OnDark)
        ) {
            Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (editingInvoice != null) "Mettre à jour la facture" else "Conclure la vente • ${formatDa(totalBrut)}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Black
            )
        }
    }
}

private enum class QuickCartEditTarget(val allowDecimal: Boolean) {
    CARTONS(false),
    PIECES(false),
    M2_CARTON(true),
    PRICE(true),
    PRICE_PIECE(true)
}

@Composable
private fun EditCartItemQuickDialog(
    item: CartItem,
    onDismiss: () -> Unit,
    onOpenFullCalculator: () -> Unit,
    onSave: (name: String, cartons: Int, pieces: Int, m2Carton: Double, pcs: Int, prix: Double, prixPiece: Double) -> Unit
) {
    BackHandler { onDismiss() }
    val isOneTime = item.productId.startsWith("onetime_")
    var nameStr by remember { mutableStateOf(item.name) }
    var cartonsStr by remember { mutableStateOf(item.cartons.toString()) }
    var piecesStr by remember { mutableStateOf(item.pieces.toString()) }
    var m2CartonStr by remember { mutableStateOf(fmtClean(item.m2Carton)) }
    var priceStr by remember {
        mutableStateOf(
            if (item.prixUnitaire % 1.0 == 0.0) item.prixUnitaire.toInt().toString()
            else item.prixUnitaire.toString()
        )
    }
    val initialPiecePrice = if (item.prixPiece > 0.0) item.prixPiece else ((item.m2Carton / item.pcs.coerceAtLeast(1)) * item.prixUnitaire)
    var pricePieceStr by remember {
        mutableStateOf(
            if (initialPiecePrice % 1.0 == 0.0) initialPiecePrice.toInt().toString()
            else ((initialPiecePrice * 100.0).roundToInt() / 100.0).toString()
        )
    }

    var activeTarget by remember { mutableStateOf(QuickCartEditTarget.CARTONS) }
    var showKeypad by remember { mutableStateOf(true) }
    var isPristine by remember { mutableStateOf(true) }

    val cartonsVal = (cartonsStr.toIntOrNull() ?: 0).coerceAtLeast(0)
    val piecesVal = (piecesStr.toIntOrNull() ?: 0).coerceAtLeast(0)
    val m2CtnVal = (m2CartonStr.toDoubleOrNull() ?: item.m2Carton).coerceAtLeast(0.01)
    val priceVal = (priceStr.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
    val pricePieceVal = (pricePieceStr.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
    val safePcs = item.pcs.coerceAtLeast(1)
    val (previewM2, previewTotal) = TileCalculatorMath.calculateCartItemTotals(
        cartons = cartonsVal,
        pieces = piecesVal,
        m2PerCarton = m2CtnVal,
        pcsPerCarton = safePcs,
        prixPerM2 = priceVal,
        prixParPiece = pricePieceVal
    )

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
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .testTag("quick_edit_cart_dialog"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(shape = RoundedCornerShape(14.dp), color = Navy950) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Modifier Quantité & Prix", style = MaterialTheme.typography.titleLarge, color = Amber500, fontWeight = FontWeight.Black)
                            Text(item.name, style = MaterialTheme.typography.bodySmall, color = OnDark, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
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

                if (isOneTime) {
                    ClearableOutlinedTextField(
                        value = nameStr,
                        onValueChange = { nameStr = it },
                        label = { Text("Nom du produit") },
                        onDismissKeypad = { showKeypad = false },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    NumericDisplayField(
                        label = "Cartons",
                        value = cartonsStr,
                        isActive = showKeypad && activeTarget == QuickCartEditTarget.CARTONS,
                        onSelect = {
                            activeTarget = QuickCartEditTarget.CARTONS
                            isPristine = true
                            showKeypad = true
                        },
                        onClear = { cartonsStr = ""; isPristine = false },
                        suffix = "ctn",
                        accentColor = Amber500,
                        modifier = Modifier.weight(1f),
                        testTag = "quick_edit_cartons_field"
                    )
                    NumericDisplayField(
                        label = "Pièces",
                        value = piecesStr,
                        isActive = showKeypad && activeTarget == QuickCartEditTarget.PIECES,
                        onSelect = {
                            activeTarget = QuickCartEditTarget.PIECES
                            isPristine = true
                            showKeypad = true
                        },
                        onClear = { piecesStr = ""; isPristine = false },
                        suffix = "pcs",
                        accentColor = Cyan500,
                        modifier = Modifier.weight(1f),
                        testTag = "quick_edit_pieces_field"
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    NumericDisplayField(
                        label = "Prix / m²",
                        value = priceStr,
                        isActive = showKeypad && activeTarget == QuickCartEditTarget.PRICE,
                        onSelect = {
                            activeTarget = QuickCartEditTarget.PRICE
                            isPristine = true
                            showKeypad = true
                        },
                        onClear = { priceStr = ""; isPristine = false },
                        suffix = "DA",
                        accentColor = Emerald500,
                        modifier = Modifier.weight(1f),
                        testTag = "quick_edit_price_field"
                    )
                    NumericDisplayField(
                        label = "Prix / Pièce",
                        value = pricePieceStr,
                        isActive = showKeypad && activeTarget == QuickCartEditTarget.PRICE_PIECE,
                        onSelect = {
                            activeTarget = QuickCartEditTarget.PRICE_PIECE
                            isPristine = true
                            showKeypad = true
                        },
                        onClear = { pricePieceStr = ""; isPristine = false },
                        suffix = "DA/p",
                        accentColor = Amber500,
                        modifier = Modifier.weight(1f),
                        testTag = "quick_edit_price_piece_field"
                    )
                    NumericDisplayField(
                        label = "m² / Ctn",
                        value = m2CartonStr,
                        isActive = showKeypad && activeTarget == QuickCartEditTarget.M2_CARTON,
                        onSelect = {
                            activeTarget = QuickCartEditTarget.M2_CARTON
                            isPristine = true
                            showKeypad = true
                        },
                        onClear = { m2CartonStr = ""; isPristine = false },
                        suffix = "m²",
                        accentColor = Cyan500,
                        modifier = Modifier.weight(0.9f),
                        testTag = "quick_edit_m2ctn_field"
                    )
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
                        Column {
                            Text("${formatM2(previewM2)} m² livrés", color = OnDark, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                            Text(
                                text = "${formatDa(priceVal * m2CtnVal)} / carton" + if (piecesVal > 0 && pricePieceVal > 0.0) " • +${piecesVal}p (${formatDa(piecesVal * pricePieceVal)})" else "",
                                color = Amber500,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(formatDa(previewTotal), color = Emerald500, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    }
                }

                AnimatedVisibility(visible = showKeypad, enter = expandVertically(), exit = shrinkVertically()) {
                    val curVal = when (activeTarget) {
                        QuickCartEditTarget.CARTONS -> cartonsStr
                        QuickCartEditTarget.PIECES -> piecesStr
                        QuickCartEditTarget.M2_CARTON -> m2CartonStr
                        QuickCartEditTarget.PRICE -> priceStr
                        QuickCartEditTarget.PRICE_PIECE -> pricePieceStr
                    }
                    NonImeNumericKeypad(
                        onKeyPress = { key ->
                            val (next, nextPristine) = applyKeypadInput(curVal, key, activeTarget.allowDecimal, isPristine)
                            when (activeTarget) {
                                QuickCartEditTarget.CARTONS -> cartonsStr = next
                                QuickCartEditTarget.PIECES -> piecesStr = next
                                QuickCartEditTarget.M2_CARTON -> m2CartonStr = next
                                QuickCartEditTarget.PRICE -> priceStr = next
                                QuickCartEditTarget.PRICE_PIECE -> pricePieceStr = next
                            }
                            isPristine = nextPristine
                        },
                        allowDecimal = activeTarget.allowDecimal,
                        quickChips = when (activeTarget) {
                            QuickCartEditTarget.CARTONS -> listOf("+1" to 1.0, "+5" to 5.0, "+10" to 10.0)
                            QuickCartEditTarget.PRICE, QuickCartEditTarget.PRICE_PIECE -> listOf("+50" to 50.0, "+100" to 100.0, "-50" to -50.0)
                            else -> emptyList()
                        },
                        onQuickDelta = { delta ->
                            val next = applyKeypadQuickDelta(curVal, delta, activeTarget.allowDecimal)
                            when (activeTarget) {
                                QuickCartEditTarget.CARTONS -> cartonsStr = next
                                QuickCartEditTarget.PIECES -> piecesStr = next
                                QuickCartEditTarget.M2_CARTON -> m2CartonStr = next
                                QuickCartEditTarget.PRICE -> priceStr = next
                                QuickCartEditTarget.PRICE_PIECE -> pricePieceStr = next
                            }
                            isPristine = false
                        },
                        onValidate = {
                            if (cartonsVal > 0 || piecesVal > 0) {
                                onSave(nameStr.trim().ifBlank { item.name }, cartonsVal, piecesVal, m2CtnVal, safePcs, priceVal, pricePieceVal)
                            }
                        },
                        validateLabel = "Valider",
                        accentColor = Amber500
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onOpenFullCalculator,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, Cyan500)
                    ) {
                        Icon(Icons.Default.Calculate, contentDescription = null, tint = Cyan400, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Calc m²", color = Cyan400, fontWeight = FontWeight.Black, maxLines = 1)
                    }
                    Button(
                        onClick = {
                            onSave(nameStr.trim().ifBlank { item.name }, cartonsVal, piecesVal, m2CtnVal, safePcs, priceVal, pricePieceVal)
                        },
                        enabled = cartonsVal > 0 || piecesVal > 0,
                        modifier = Modifier.weight(1.4f).height(52.dp).testTag("quick_edit_save_btn"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald500, contentColor = OnDark)
                    ) {
                        Text("Enregistrer", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

private enum class DiversDialogMode { STANDARD, M2_PER_CARTON }

private enum class DiversNumTarget(val allowDecimal: Boolean) {
    QTY(true),
    PRICE(true),
    TILE_L(true),
    TILE_W(true),
    TILE_PCS(false),
    REQ_M2(true),
    CALC_PRICE(true)
}

@Composable
private fun DiversLineDialog(
    editingLine: FreeLineItem? = null,
    onDismiss: () -> Unit,
    onSaveLine: (FreeLineItem) -> Unit
) {
    BackHandler { onDismiss() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var dialogMode by remember { mutableStateOf(DiversDialogMode.STANDARD) }
    var designation by remember { mutableStateOf(editingLine?.designation ?: "") }
    var unite by remember { mutableStateOf(editingLine?.unite ?: "sac") }
    var qtyStr by remember { mutableStateOf(editingLine?.let { fmtClean(it.quantite) } ?: "1") }
    var priceStr by remember {
        mutableStateOf(
            editingLine?.let {
                if (it.prixUnitaire % 1.0 == 0.0) it.prixUnitaire.toInt().toString() else it.prixUnitaire.toString()
            } ?: "750"
        )
    }

    var tileUnit by remember { mutableStateOf(TileDimensionUnit.CM) }
    var tileLengthStr by remember { mutableStateOf("60") }
    var tileWidthStr by remember { mutableStateOf("60") }
    var tilePcsStr by remember { mutableStateOf("4") }
    var requiredAreaStr by remember { mutableStateOf(editingLine?.let { fmtClean(it.quantite) } ?: "20") }
    var calcMarginPct by remember { mutableIntStateOf(0) }
    var calcPriceStr by remember {
        mutableStateOf(
            editingLine?.let {
                if (it.prixUnitaire % 1.0 == 0.0) it.prixUnitaire.toInt().toString() else it.prixUnitaire.toString()
            } ?: "1200"
        )
    }

    var activeTarget by remember { mutableStateOf(DiversNumTarget.QTY) }
    var showKeypad by remember { mutableStateOf(false) }
    var isPristine by remember { mutableStateOf(true) }

    val qtyVal = qtyStr.toDoubleOrNull() ?: 0.0
    val priceVal = priceStr.toDoubleOrNull() ?: 0.0

    val boxCalc = remember(tileLengthStr, tileWidthStr, tileUnit, tilePcsStr, requiredAreaStr, calcMarginPct) {
        TileCalculatorMath.calculateM2PerCartonAndCartons(
            tileLength = tileLengthStr.toDoubleOrNull() ?: 0.0,
            tileWidth = tileWidthStr.toDoubleOrNull() ?: 0.0,
            unit = tileUnit,
            piecesPerCarton = tilePcsStr.toIntOrNull() ?: 0,
            requiredAreaM2 = requiredAreaStr.toDoubleOrNull() ?: 0.0,
            marginPercent = calcMarginPct
        )
    }
    val calcPriceVal = calcPriceStr.toDoubleOrNull() ?: 0.0
    val calcTotalDa = ((boxCalc.deliveredAreaM2 * calcPriceVal) * 100.0).roundToInt() / 100.0

    fun selectNumTarget(t: DiversNumTarget) {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
        activeTarget = t
        isPristine = true
        showKeypad = true
    }

    fun getTargetStr(): String = when (activeTarget) {
        DiversNumTarget.QTY -> qtyStr
        DiversNumTarget.PRICE -> priceStr
        DiversNumTarget.TILE_L -> tileLengthStr
        DiversNumTarget.TILE_W -> tileWidthStr
        DiversNumTarget.TILE_PCS -> tilePcsStr
        DiversNumTarget.REQ_M2 -> requiredAreaStr
        DiversNumTarget.CALC_PRICE -> calcPriceStr
    }

    fun setTargetStr(v: String) {
        when (activeTarget) {
            DiversNumTarget.QTY -> qtyStr = v
            DiversNumTarget.PRICE -> priceStr = v
            DiversNumTarget.TILE_L -> tileLengthStr = v
            DiversNumTarget.TILE_W -> tileWidthStr = v
            DiversNumTarget.TILE_PCS -> tilePcsStr = v
            DiversNumTarget.REQ_M2 -> requiredAreaStr = v
            DiversNumTarget.CALC_PRICE -> calcPriceStr = v
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 580.dp)
                .padding(14.dp),
            shape = RoundedCornerShape(22.dp),
            color = Slate900,
            border = BorderStroke(2.dp, Navy950)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .testTag("divers_dialog"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(shape = RoundedCornerShape(14.dp), color = Navy950) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (editingLine != null) "Modifier Ligne Divers" else "Divers & Calcul m²/Carton",
                            style = MaterialTheme.typography.titleLarge,
                            color = Amber500,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.weight(1f)
                        )
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

                // Mode Switch: Standard Divers vs m²/Carton Calculator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = dialogMode == DiversDialogMode.STANDARD,
                        onClick = {
                            dialogMode = DiversDialogMode.STANDARD
                            showKeypad = false
                        },
                        label = { Text("Article Divers / Colle", fontWeight = FontWeight.Black) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Amber500,
                            selectedLabelColor = OnDark,
                            containerColor = Slate950,
                            labelColor = Slate400
                        ),
                        modifier = Modifier.weight(1f).testTag("divers_mode_standard")
                    )
                    FilterChip(
                        selected = dialogMode == DiversDialogMode.M2_PER_CARTON,
                        onClick = {
                            dialogMode = DiversDialogMode.M2_PER_CARTON
                            selectNumTarget(DiversNumTarget.REQ_M2)
                        },
                        leadingIcon = { Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        label = { Text("Calc m² / Carton", fontWeight = FontWeight.Black) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Cyan500,
                            selectedLabelColor = OnDark,
                            containerColor = Slate950,
                            labelColor = Slate400
                        ),
                        modifier = Modifier.weight(1f).testTag("divers_mode_m2_calc")
                    )
                }

                if (dialogMode == DiversDialogMode.STANDARD) {
                    Text("Raccourcis rapides :", style = MaterialTheme.typography.labelMedium, color = Slate400, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PosSeeds.DIVERS_PRESETS.forEach { preset ->
                            FilterChip(
                                selected = designation == preset.designation,
                                onClick = {
                                    designation = preset.designation
                                    unite = preset.unite
                                    priceStr = preset.prix.toInt().toString()
                                    showKeypad = false
                                },
                                label = { Text("${preset.designation.take(18)} (${preset.prix.toInt()})", fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Amber500,
                                    selectedLabelColor = OnDark,
                                    containerColor = Slate950,
                                    labelColor = Slate100
                                )
                            )
                        }
                    }

                    ClearableOutlinedTextField(
                        value = designation,
                        onValueChange = { designation = it },
                        label = { Text("Désignation") },
                        onDismissKeypad = { showKeypad = false },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Unité:", style = MaterialTheme.typography.labelMedium, color = Slate400, fontWeight = FontWeight.Bold)
                        listOf("sac", "kg", "m²", "ctn", "ml", "pcs", "u").forEach { unitOpt ->
                            FilterChip(
                                selected = unite == unitOpt,
                                onClick = { unite = unitOpt },
                                label = { Text(unitOpt, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Cyan500,
                                    selectedLabelColor = OnDark,
                                    containerColor = Slate950,
                                    labelColor = Slate400
                                )
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        NumericDisplayField(
                            label = "Quantité ($unite)",
                            value = qtyStr,
                            isActive = showKeypad && activeTarget == DiversNumTarget.QTY,
                            onSelect = { selectNumTarget(DiversNumTarget.QTY) },
                            onClear = { qtyStr = ""; isPristine = false },
                            modifier = Modifier.weight(1f),
                            testTag = "divers_qty_field"
                        )
                        NumericDisplayField(
                            label = "Prix Unitaire",
                            value = priceStr,
                            isActive = showKeypad && activeTarget == DiversNumTarget.PRICE,
                            onSelect = { selectNumTarget(DiversNumTarget.PRICE) },
                            onClear = { priceStr = ""; isPristine = false },
                            suffix = "DA",
                            modifier = Modifier.weight(1.2f),
                            testTag = "divers_price_field"
                        )
                    }
                } else {
                    // Integrated m² par carton calculator in Divers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Dimensions Carreau", style = MaterialTheme.typography.labelMedium, color = Cyan400, fontWeight = FontWeight.Black)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TileDimensionUnit.entries.forEach { u ->
                                val isSel = tileUnit == u
                                Surface(
                                    modifier = Modifier
                                        .height(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            if (tileUnit != u) {
                                                val f = if (u == TileDimensionUnit.METER) 0.01 else 100.0
                                                tileLengthStr.toDoubleOrNull()?.let { tileLengthStr = fmtClean(it * f) }
                                                tileWidthStr.toDoubleOrNull()?.let { tileWidthStr = fmtClean(it * f) }
                                                tileUnit = u
                                            }
                                        },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSel) Cyan500 else Slate950,
                                    border = BorderStroke(1.dp, if (isSel) Cyan500 else Slate700)
                                ) {
                                    Box(modifier = Modifier.padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
                                        Text(u.suffix.uppercase(), style = MaterialTheme.typography.labelMedium, color = if (isSel) OnDark else Slate400, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TileCalculatorMath.TILE_FORMAT_PRESETS.forEach { preset ->
                            val targetL = if (tileUnit == TileDimensionUnit.CM) preset.lengthCm else preset.lengthCm / 100.0
                            val targetW = if (tileUnit == TileDimensionUnit.CM) preset.widthCm else preset.widthCm / 100.0
                            val isSel = kotlin.math.abs((tileLengthStr.toDoubleOrNull() ?: 0.0) - targetL) < 0.001 &&
                                kotlin.math.abs((tileWidthStr.toDoubleOrNull() ?: 0.0) - targetW) < 0.001
                            FilterChip(
                                selected = isSel,
                                onClick = {
                                    tileLengthStr = fmtClean(targetL)
                                    tileWidthStr = fmtClean(targetW)
                                    tilePcsStr = preset.piecesPerCarton.toString()
                                },
                                label = { Text("${preset.label} (${preset.piecesPerCarton}p)", fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Amber500,
                                    selectedLabelColor = OnDark,
                                    containerColor = Slate950,
                                    labelColor = Slate400
                                )
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        NumericDisplayField(
                            label = "Longueur",
                            value = tileLengthStr,
                            isActive = showKeypad && activeTarget == DiversNumTarget.TILE_L,
                            onSelect = { selectNumTarget(DiversNumTarget.TILE_L) },
                            onClear = { tileLengthStr = ""; isPristine = false },
                            suffix = tileUnit.suffix,
                            modifier = Modifier.weight(1f),
                            testTag = "divers_calc_len_field"
                        )
                        NumericDisplayField(
                            label = "Largeur",
                            value = tileWidthStr,
                            isActive = showKeypad && activeTarget == DiversNumTarget.TILE_W,
                            onSelect = { selectNumTarget(DiversNumTarget.TILE_W) },
                            onClear = { tileWidthStr = ""; isPristine = false },
                            suffix = tileUnit.suffix,
                            modifier = Modifier.weight(1f),
                            testTag = "divers_calc_wid_field"
                        )
                        NumericDisplayField(
                            label = "Pcs/Ctn",
                            value = tilePcsStr,
                            isActive = showKeypad && activeTarget == DiversNumTarget.TILE_PCS,
                            onSelect = { selectNumTarget(DiversNumTarget.TILE_PCS) },
                            onClear = { tilePcsStr = ""; isPristine = false },
                            suffix = "pcs",
                            accentColor = Amber500,
                            modifier = Modifier.weight(0.95f),
                            testTag = "divers_calc_pcs_field"
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        NumericDisplayField(
                            label = "Surface requise",
                            value = requiredAreaStr,
                            isActive = showKeypad && activeTarget == DiversNumTarget.REQ_M2,
                            onSelect = { selectNumTarget(DiversNumTarget.REQ_M2) },
                            onClear = { requiredAreaStr = ""; isPristine = false },
                            suffix = "m²",
                            accentColor = Amber500,
                            modifier = Modifier.weight(1f),
                            testTag = "divers_calc_area_field"
                        )
                        NumericDisplayField(
                            label = "Prix / m²",
                            value = calcPriceStr,
                            isActive = showKeypad && activeTarget == DiversNumTarget.CALC_PRICE,
                            onSelect = { selectNumTarget(DiversNumTarget.CALC_PRICE) },
                            onClear = { calcPriceStr = ""; isPristine = false },
                            suffix = "DA",
                            accentColor = Emerald500,
                            modifier = Modifier.weight(1f),
                            testTag = "divers_calc_price_field"
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Coupe:", style = MaterialTheme.typography.labelMedium, color = Slate400, fontWeight = FontWeight.Bold)
                        TileCalculatorMath.MARGIN_PRESETS.forEach { pct ->
                            FilterChip(
                                selected = calcMarginPct == pct,
                                onClick = { calcMarginPct = pct },
                                label = { Text(if (pct == 0) "0%" else "+$pct%", fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Amber500,
                                    selectedLabelColor = OnDark,
                                    containerColor = Slate950,
                                    labelColor = Slate400
                                )
                            )
                        }
                    }

                    ClearableOutlinedTextField(
                        value = designation,
                        onValueChange = { designation = it },
                        label = { Text("Désignation (auto si vide)") },
                        placeholder = {
                            Text("Divers ${tileLengthStr}×${tileWidthStr}${tileUnit.suffix} (${fmtClean(boxCalc.m2PerCarton)} m²/ctn)", color = Slate400)
                        },
                        onDismissKeypad = { showKeypad = false },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Navy950
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("1 carreau = ${fmtClean(boxCalc.tileAreaM2)} m²", style = MaterialTheme.typography.labelMedium, color = OnDark.copy(alpha = 0.8f))
                                Text("${fmtClean(boxCalc.m2PerCarton)} m² / carton", style = MaterialTheme.typography.labelLarge, color = Amber500, fontWeight = FontWeight.Black)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${boxCalc.cartonsNeeded} ctn • ${formatM2(boxCalc.deliveredAreaM2)} m² livrés",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = OnDark,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = formatDa(calcTotalDa),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Emerald500,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }

                AnimatedVisibility(visible = showKeypad, enter = expandVertically(), exit = shrinkVertically()) {
                    NonImeNumericKeypad(
                        onKeyPress = { key ->
                            val cur = getTargetStr()
                            val (next, nextPristine) = applyKeypadInput(cur, key, activeTarget.allowDecimal, isPristine)
                            setTargetStr(next)
                            isPristine = nextPristine
                        },
                        allowDecimal = activeTarget.allowDecimal,
                        onValidate = { showKeypad = false },
                        validateLabel = "OK"
                    )
                }

                val isSaveEnabled = if (dialogMode == DiversDialogMode.STANDARD) {
                    designation.isNotBlank() && qtyVal > 0
                } else {
                    boxCalc.deliveredAreaM2 > 0.0
                }
                val btnAmount = if (dialogMode == DiversDialogMode.STANDARD) qtyVal * priceVal else calcTotalDa

                Button(
                    onClick = {
                        if (dialogMode == DiversDialogMode.STANDARD) {
                            if (designation.isNotBlank() && qtyVal > 0) {
                                onSaveLine(
                                    FreeLineItem(
                                        id = editingLine?.id ?: "div_${UUID.randomUUID().toString().take(8)}",
                                        designation = designation.trim(),
                                        quantite = qtyVal,
                                        unite = unite,
                                        prixUnitaire = priceVal,
                                        sousTotal = ((qtyVal * priceVal) * 100.0).roundToInt() / 100.0
                                    )
                                )
                            }
                        } else if (boxCalc.deliveredAreaM2 > 0.0) {
                            val finalDesignation = designation.trim().ifBlank {
                                "Carrelage ${tileLengthStr}×${tileWidthStr}${tileUnit.suffix} (${fmtClean(boxCalc.m2PerCarton)} m²/ctn • ${boxCalc.cartonsNeeded} ctn)"
                            }
                            onSaveLine(
                                FreeLineItem(
                                    id = editingLine?.id ?: "div_${UUID.randomUUID().toString().take(8)}",
                                    designation = finalDesignation,
                                    quantite = boxCalc.deliveredAreaM2,
                                    unite = "m²",
                                    prixUnitaire = calcPriceVal,
                                    sousTotal = calcTotalDa
                                )
                            )
                        }
                    },
                    enabled = isSaveEnabled,
                    modifier = Modifier.fillMaxWidth().height(54.dp).testTag("divers_save_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald500, contentColor = OnDark)
                ) {
                    Text(
                        text = if (editingLine != null) "Enregistrer (${formatDa(btnAmount)})" else "Ajouter (${formatDa(btnAmount)})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

private enum class CheckoutNumTarget { REMISE, PAYE }

@Composable
private fun CheckoutModalDialog(
    totalBrut: Double,
    customers: List<Customer>,
    editingInvoice: Invoice?,
    onDismiss: () -> Unit,
    onConfirm: (InvoiceClient, Double, Double, String, String) -> Unit
) {
    BackHandler { onDismiss() }
    var selectedCustomerId by remember { mutableStateOf(editingInvoice?.client?.id) }
    var clientName by remember { mutableStateOf(editingInvoice?.client?.nom ?: "Client Comptoir") }
    var clientPhone by remember { mutableStateOf(editingInvoice?.client?.tel ?: "") }
    var remiseStr by remember { mutableStateOf((editingInvoice?.remise ?: 0.0).toInt().toString()) }
    // Cash-Only (Espèces) or Crédit — No Chèque / Virement
    val initialMode = if (editingInvoice?.modePaiement == "Crédit") "Crédit" else "Espèces"
    var modePaiement by remember { mutableStateOf(initialMode) }
    val remiseVal = (remiseStr.toDoubleOrNull() ?: 0.0).coerceIn(0.0, totalBrut)
    val totalNet = (totalBrut - remiseVal).coerceAtLeast(0.0)
    var payeStr by remember {
        mutableStateOf((editingInvoice?.montantPaye ?: totalNet).toInt().toString())
    }
    var notes by remember { mutableStateOf(editingInvoice?.notes ?: "") }
    var activeTarget by remember { mutableStateOf(CheckoutNumTarget.PAYE) }
    var showKeypad by remember { mutableStateOf(false) }
    var isPristine by remember { mutableStateOf(true) }

    val payeVal = if (modePaiement == "Crédit") 0.0 else (payeStr.toDoubleOrNull() ?: 0.0).coerceIn(0.0, totalNet)
    val reste = (totalNet - payeVal).coerceAtLeast(0.0)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 580.dp)
                .padding(14.dp),
            shape = RoundedCornerShape(22.dp),
            color = Slate900,
            border = BorderStroke(2.dp, Navy950)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(shape = RoundedCornerShape(14.dp), color = Navy950) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Encaissement Espèces", style = MaterialTheme.typography.titleLarge, color = OnDark, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
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

                Text("Client :", style = MaterialTheme.typography.labelMedium, color = Slate400, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedCustomerId == null,
                        onClick = {
                            selectedCustomerId = null
                            clientName = "Client Comptoir"
                            clientPhone = ""
                            showKeypad = false
                        },
                        label = { Text("Client Comptoir", fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Navy950,
                            selectedLabelColor = OnDark
                        )
                    )
                    customers.forEach { c ->
                        FilterChip(
                            selected = selectedCustomerId == c.id,
                            onClick = {
                                selectedCustomerId = c.id
                                clientName = c.nom
                                clientPhone = c.tel
                                showKeypad = false
                            },
                            label = { Text(c.nom, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Amber500,
                                selectedLabelColor = OnDark
                            )
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    ClearableOutlinedTextField(
                        value = clientName,
                        onValueChange = { clientName = it },
                        label = { Text("Nom Client") },
                        autoClearValues = setOf("Client Comptoir"),
                        onDismissKeypad = { showKeypad = false },
                        modifier = Modifier.weight(1.2f)
                    )
                    ClearableOutlinedTextField(
                        value = clientPhone,
                        onValueChange = { clientPhone = it },
                        label = { Text("Tél") },
                        onDismissKeypad = { showKeypad = false },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Cash Only Modes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Espèces", "Crédit").forEach { mode ->
                        val isSel = modePaiement == mode
                        Button(
                            onClick = {
                                modePaiement = mode
                                if (mode == "Crédit") {
                                    payeStr = "0"
                                    showKeypad = false
                                } else if ((payeStr.toDoubleOrNull() ?: 0.0) == 0.0) {
                                    payeStr = totalNet.toInt().toString()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("checkout_mode_$mode"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSel) (if (mode == "Espèces") Emerald500 else Rose500) else Slate850,
                                contentColor = if (isSel) OnDark else Slate100
                            )
                        ) {
                            Text(
                                text = if (mode == "Espèces") "Espèces (Cash)" else "À Crédit",
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    NumericDisplayField(
                        label = "Remise",
                        value = remiseStr,
                        isActive = showKeypad && activeTarget == CheckoutNumTarget.REMISE,
                        onSelect = {
                            activeTarget = CheckoutNumTarget.REMISE
                            isPristine = true
                            showKeypad = true
                        },
                        onClear = { remiseStr = ""; isPristine = false },
                        suffix = "DA",
                        accentColor = Amber500,
                        modifier = Modifier.weight(1f),
                        testTag = "checkout_remise_field"
                    )
                    NumericDisplayField(
                        label = "Montant Payé",
                        value = if (modePaiement == "Crédit") "0" else payeStr,
                        isActive = showKeypad && activeTarget == CheckoutNumTarget.PAYE,
                        onSelect = {
                            if (modePaiement == "Crédit") modePaiement = "Espèces"
                            activeTarget = CheckoutNumTarget.PAYE
                            isPristine = true
                            showKeypad = true
                        },
                        onClear = { payeStr = ""; isPristine = false },
                        suffix = "DA",
                        accentColor = Emerald500,
                        modifier = Modifier.weight(1.2f),
                        testTag = "checkout_paye_field"
                    )
                }

                ClearableOutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Livraison (optionnel)") },
                    onDismissKeypad = { showKeypad = false },
                    modifier = Modifier.fillMaxWidth()
                )

                AnimatedVisibility(visible = showKeypad, enter = expandVertically(), exit = shrinkVertically()) {
                    NonImeNumericKeypad(
                        onKeyPress = { key ->
                            val cur = if (activeTarget == CheckoutNumTarget.REMISE) remiseStr else payeStr
                            val (next, nextPristine) = applyKeypadInput(cur, key, true, isPristine)
                            if (activeTarget == CheckoutNumTarget.REMISE) {
                                remiseStr = next
                                val nextNet = (totalBrut - (next.toDoubleOrNull() ?: 0.0)).coerceAtLeast(0.0)
                                payeStr = nextNet.toInt().toString()
                            } else {
                                payeStr = next
                            }
                            isPristine = nextPristine
                        },
                        quickChips = listOf("+500" to 500.0, "+1000" to 1000.0, "+5000" to 5000.0),
                        onQuickDelta = { delta ->
                            val cur = if (activeTarget == CheckoutNumTarget.REMISE) remiseStr else payeStr
                            val next = applyKeypadQuickDelta(cur, delta, false)
                            if (activeTarget == CheckoutNumTarget.REMISE) {
                                remiseStr = next
                                val nextNet = (totalBrut - (next.toDoubleOrNull() ?: 0.0)).coerceAtLeast(0.0)
                                payeStr = nextNet.toInt().toString()
                            } else {
                                payeStr = next
                            }
                            isPristine = false
                        },
                        allowDecimal = false,
                        onValidate = { showKeypad = false },
                        validateLabel = "OK"
                    )
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Navy950
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Net :", color = OnDark.copy(alpha = 0.8f), style = MaterialTheme.typography.titleSmall)
                            Text(formatDa(totalNet), color = OnDark, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Reste / Crédit :", color = OnDark.copy(alpha = 0.8f), style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = formatDa(reste),
                                color = if (reste > 0) Amber500 else Emerald500,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        onConfirm(
                            InvoiceClient(
                                id = selectedCustomerId,
                                nom = clientName.ifBlank { "Client Comptoir" },
                                tel = clientPhone.trim()
                            ),
                            remiseVal,
                            payeVal,
                            modePaiement,
                            notes.trim()
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("checkout_confirm_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald500, contentColor = OnDark)
                ) {
                    Text(
                        text = if (editingInvoice != null) "Enregistrer Modifications" else "Confirmer & Facturer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

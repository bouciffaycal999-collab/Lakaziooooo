package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.auth.AuthAttemptResult
import com.example.auth.GoogleAuthManager
import com.example.data.local.AppDatabase
import com.example.data.remote.FirestorePosRepository
import com.example.data.repository.PosRepository
import com.example.ui.PosUiState
import com.example.ui.PosViewModel
import com.example.ui.components.InvoicePreviewPrintSheet
import com.example.ui.components.SmartTileCalculatorSheet
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CaisseScreen
import com.example.ui.screens.CustomersScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.InvoicesScreen
import com.example.ui.screens.M2PerCartonCalculatorScreen
import com.example.ui.screens.NotificationsSheet
import com.example.ui.screens.PosScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StaffLockScreen
import com.example.ui.screens.StockScreen
import com.example.ui.screens.SuppliersScreen
import com.example.ui.screens.UsersScreen
import com.example.ui.theme.Amber500
import com.example.ui.theme.Cyan500
import com.example.ui.theme.KaderPosTheme
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy950
import com.example.ui.theme.OnDark
import com.example.ui.theme.Rose500
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KaderPosTheme {
                KaderCeramicPosApp()
            }
        }
    }
}

private data class NavTabMeta(
    val key: String,
    val label: String,
    val icon: ImageVector
)

private val ALL_NAV_TABS = listOf(
    NavTabMeta("pos", "Vente", Icons.Default.Storefront),
    NavTabMeta("stock", "Stock", Icons.Default.Inventory2),
    NavTabMeta("invoices", "Factures", Icons.Default.ReceiptLong),
    NavTabMeta("dashboard", "Stats", Icons.Default.BarChart),
    NavTabMeta("customers", "Clients", Icons.Default.Group),
    NavTabMeta("suppliers", "Usines", Icons.Default.Factory),
    NavTabMeta("caisse", "Caisse", Icons.Default.PointOfSale),
    NavTabMeta("users", "Équipe", Icons.Default.ManageAccounts),
    NavTabMeta("settings", "Réglages", Icons.Default.Settings),
    NavTabMeta("calculator", "Calc m²", Icons.Default.Calculate)
)

@Composable
fun KaderCeramicPosApp() {
    val auth = remember { Firebase.auth }
    val currentUser by GoogleAuthManager.authStateFlow(auth)
        .collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    val context = LocalContext.current
    val prefs = remember(context) {
        context.getSharedPreferences("kader_pos_prefs", Context.MODE_PRIVATE)
    }
    var allowOfflineMode by remember {
        mutableStateOf(prefs.getBoolean("offline_mode_enabled", false))
    }

    if (user == null && !allowOfflineMode) {
        AuthScreen(
            onContinueOffline = {
                prefs.edit().putBoolean("offline_mode_enabled", true).apply()
                allowOfflineMode = true
            }
        )
        return
    }

    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val viewModel: PosViewModel = viewModel(
        key = user?.uid ?: "offline_local",
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val localRepo = PosRepository(app.applicationContext, AppDatabase.getInstance(app.applicationContext))
                val remoteRepo = if (user != null) {
                    val databaseId = app.getString(R.string.firestore_database_id)
                    FirestorePosRepository(FirebaseFirestore.getInstance(databaseId))
                } else {
                    null
                }
                PosViewModel(
                    repository = localRepo,
                    firestoreRepo = remoteRepo,
                    currentUserId = user?.uid,
                    userEmail = user?.email,
                    userDisplayName = user?.displayName
                )
            }
        }
    )

    KaderPosSignedInContent(
        viewModel = viewModel,
        onSignInGoogle = {
            when (val res = GoogleAuthManager.signInInteractive(context, credentialManager, auth)) {
                AuthAttemptResult.Success -> null
                AuthAttemptResult.Cancelled -> "Connexion Google annulée."
                AuthAttemptResult.NoSavedCredential -> "Aucun compte Google disponible sur cet appareil."
                is AuthAttemptResult.Error -> res.message
            }
        },
        onSignOutGoogle = {
            prefs.edit().putBoolean("offline_mode_enabled", false).apply()
            allowOfflineMode = false
            GoogleAuthManager.signOut(credentialManager, scope, auth)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KaderPosSignedInContent(
    viewModel: PosViewModel,
    onSignInGoogle: suspend () -> String?,
    onSignOutGoogle: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showMoreTabsSheet by remember { mutableStateOf(false) }

    LaunchedEffect(state.statusMessage) {
        val msg = state.statusMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    if (state.isLoading) {
        Surface(modifier = Modifier.fillMaxSize(), color = Slate950) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = Cyan500)
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    "Chargement catalogue Kader Ceramic...",
                    color = Navy950,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        return
    }

    if (state.isLocked) {
        StaffLockScreen(
            users = state.users,
            initialSelectedUser = state.currentUser,
            pinError = state.pinError,
            onClearError = { viewModel.clearPinError() },
            onUnlock = { user, pin -> viewModel.attemptUnlock(user, pin) }
        )
        return
    }

    val allowedTabKeys = (state.currentUser?.allowedTabs ?: listOf("pos")) + "calculator"
    val allowedTabs = remember(allowedTabKeys) {
        ALL_NAV_TABS.filter { allowedTabKeys.contains(it.key) }
    }

    if (state.activeTab != "pos") {
        BackHandler { viewModel.selectTab("pos") }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isMediumOrExpanded = maxWidth >= 600.dp
        val primaryPhoneTabs = remember(allowedTabs) { allowedTabs.take(4) }
        val overflowPhoneTabs = remember(allowedTabs) { allowedTabs.drop(4) }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Slate950,
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Navy950,
                        titleContentColor = OnDark
                    ),
                    title = {
                        Column {
                            Text(
                                text = state.storeInfo.nom,
                                style = MaterialTheme.typography.titleMedium,
                                color = OnDark,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${state.currentUser?.name ?: ""} • ${state.settings.activeStore}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Amber500,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                if (state.activeTab == "calculator") {
                                    viewModel.selectTab("pos")
                                } else {
                                    viewModel.selectTab("calculator")
                                }
                            },
                            modifier = Modifier.testTag("topbar_calc_btn")
                        ) {
                            Icon(
                                Icons.Default.Calculate,
                                contentDescription = "Calculateur m²/Ctn",
                                tint = Amber500
                            )
                        }
                        IconButton(
                            onClick = { viewModel.openNotificationsSheet() },
                            modifier = Modifier.testTag("topbar_notifications_btn")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (state.unreadNotificationsCount > 0) {
                                        Badge(containerColor = Rose500, contentColor = OnDark) {
                                            Text(
                                                state.unreadNotificationsCount.toString(),
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Notifications, contentDescription = "Alertes Patron & Dépôt", tint = OnDark)
                            }
                        }
                        IconButton(
                            onClick = { viewModel.lockSession() },
                            modifier = Modifier.testTag("topbar_lock_btn")
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = "Verrouiller / Changer profil", tint = OnDark)
                        }
                    }
                )
            },
            bottomBar = {
                if (!isMediumOrExpanded) {
                    NavigationBar(
                        containerColor = Navy950,
                        tonalElevation = 8.dp
                    ) {
                        primaryPhoneTabs.forEach { tab ->
                            NavigationBarItem(
                                selected = state.activeTab == tab.key,
                                onClick = { viewModel.selectTab(tab.key) },
                                icon = { Icon(tab.icon, contentDescription = tab.label) },
                                label = {
                                    Text(
                                        tab.label,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Amber500,
                                    selectedTextColor = Amber500,
                                    indicatorColor = Navy800,
                                    unselectedIconColor = OnDark.copy(alpha = 0.72f),
                                    unselectedTextColor = OnDark.copy(alpha = 0.72f)
                                ),
                                modifier = Modifier.testTag("nav_${tab.key}")
                            )
                        }
                        if (overflowPhoneTabs.isNotEmpty()) {
                            val isOverflowSelected = overflowPhoneTabs.any { it.key == state.activeTab }
                            NavigationBarItem(
                                selected = isOverflowSelected,
                                onClick = { showMoreTabsSheet = true },
                                icon = { Icon(Icons.Default.Menu, contentDescription = "Plus") },
                                label = { Text("Plus", maxLines = 1, fontWeight = FontWeight.Bold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Amber500,
                                    selectedTextColor = Amber500,
                                    indicatorColor = Navy800,
                                    unselectedIconColor = OnDark.copy(alpha = 0.72f),
                                    unselectedTextColor = OnDark.copy(alpha = 0.72f)
                                ),
                                modifier = Modifier.testTag("nav_more")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isMediumOrExpanded) {
                    NavigationRail(
                        containerColor = Navy950,
                        modifier = Modifier
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Spacer(modifier = Modifier.height(6.dp))
                        allowedTabs.forEach { tab ->
                            NavigationRailItem(
                                selected = state.activeTab == tab.key,
                                onClick = { viewModel.selectTab(tab.key) },
                                icon = { Icon(tab.icon, contentDescription = tab.label) },
                                label = { Text(tab.label, maxLines = 1, fontWeight = FontWeight.Bold) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = Amber500,
                                    selectedTextColor = Amber500,
                                    indicatorColor = Navy800,
                                    unselectedIconColor = OnDark.copy(alpha = 0.72f),
                                    unselectedTextColor = OnDark.copy(alpha = 0.72f)
                                ),
                                modifier = Modifier.testTag("rail_${tab.key}")
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    ActiveTabContent(
                        state = state,
                        viewModel = viewModel,
                        onSignInGoogle = onSignInGoogle,
                        onSignOutGoogle = onSignOutGoogle
                    )
                }
            }
        }

        if (showMoreTabsSheet) {
            BackHandler { showMoreTabsSheet = false }
            ModalBottomSheet(
                onDismissRequest = { showMoreTabsSheet = false },
                containerColor = Slate950,
                contentColor = Slate100
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Autres Modules",
                        style = MaterialTheme.typography.titleLarge,
                        color = Navy950,
                        fontWeight = FontWeight.Black
                    )
                    overflowPhoneTabs.forEach { tab ->
                        val isSel = state.activeTab == tab.key
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectTab(tab.key)
                                    showMoreTabsSheet = false
                                }
                                .testTag("more_nav_${tab.key}"),
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSel) Navy950 else Slate900,
                            border = BorderStroke(1.5.dp, if (isSel) Amber500 else Slate700)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    tab.icon,
                                    contentDescription = null,
                                    tint = if (isSel) Amber500 else Cyan500
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    tab.label,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isSel) OnDark else Slate100,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        state.calculatorTarget?.let { target ->
            SmartTileCalculatorSheet(
                product = target.product,
                editingCartItem = target.editingCartItem,
                isOneTimeProduct = target.isOneTimeProduct,
                onDismiss = { viewModel.closeCalculator() },
                onConfirmProductCart = { product, cartons, pieces, m2Total, prixPerM2, bain, calibre, editId, prixPiece ->
                    viewModel.confirmCalculatorToCart(
                        product = product,
                        cartons = cartons,
                        pieces = pieces,
                        m2Total = m2Total,
                        prixPerM2 = prixPerM2,
                        bain = bain,
                        calibre = calibre,
                        editingItemId = editId,
                        prixPiece = prixPiece
                    )
                },
                onConfirmStandaloneFreeLine = { freeLine ->
                    viewModel.addFreeLine(freeLine)
                }
            )
        }

        state.previewInvoice?.let { inv ->
            InvoicePreviewPrintSheet(
                invoice = inv,
                storeInfo = state.storeInfo,
                settings = state.settings,
                autoEmailPdf = state.autoEmailPdf,
                onUpdateSettings = { viewModel.saveSettings(it) },
                onDismiss = { viewModel.closeInvoicePreview() }
            )
        }

        if (state.showNotificationsSheet) {
            NotificationsSheet(
                notifications = state.notifications,
                storeInfo = state.storeInfo,
                onDismiss = { viewModel.closeNotificationsSheet() }
            )
        }
    }
}

@Composable
private fun ActiveTabContent(
    state: PosUiState,
    viewModel: PosViewModel,
    onSignInGoogle: suspend () -> String?,
    onSignOutGoogle: () -> Unit
) {
    val user = state.currentUser
    when (state.activeTab) {
        "pos" -> PosScreen(
            products = state.products,
            cart = state.cart,
            freeLines = state.freeLines,
            customers = state.customers,
            editingInvoice = state.editingInvoice,
            onOpenCalculatorForProduct = { viewModel.openProductCalculator(it) },
            onRequestDepotDispo = { viewModel.requestDepotAvailability(it) },
            onOpenOneTimeProduct = { viewModel.openOneTimeProductCalculator() },
            onEditCartItem = { viewModel.openCartItemCalculator(it) },
            onUpdateCartItemDirect = { id, name, cartons, pieces, m2Ctn, pcs, prix, prixPiece ->
                viewModel.updateCartItemDirect(id, name, cartons, pieces, m2Ctn, pcs, prix, prixPiece)
            },
            onAdjustCartItemCarton = { id, delta -> viewModel.adjustCartItemCartons(id, delta) },
            onRemoveCartItem = { viewModel.removeCartItem(it) },
            onAddFreeLine = { viewModel.addFreeLine(it) },
            onUpdateFreeLine = { viewModel.updateFreeLine(it) },
            onAdjustFreeLineQty = { id, delta -> viewModel.adjustFreeLineQuantity(id, delta) },
            onRemoveFreeLine = { viewModel.removeFreeLine(it) },
            onClearCart = { viewModel.clearCart() },
            onFinalizeSale = { client, remise, paye, mode, notes ->
                viewModel.finalizeSale(client, remise, paye, mode, notes)
            }
        )

        "stock" -> StockScreen(
            products = state.products,
            canModifyStock = user?.canModifyStock ?: false,
            onAdjustStock = { id, delta -> viewModel.adjustStock(id, delta) },
            onSetStock = { id, stock -> viewModel.setStock(id, stock) },
            onSaveProduct = { viewModel.saveProduct(it) },
            onRequestDepotDispo = { viewModel.requestDepotAvailability(it) }
        )

        "invoices" -> InvoicesScreen(
            invoices = state.invoices,
            canEdit = user?.canEditInvoices ?: false,
            canDelete = user?.canDeleteInvoices ?: false,
            onPreviewInvoice = { viewModel.openInvoicePreview(it) },
            onEditInvoice = { viewModel.startEditingInvoice(it) },
            onDeleteInvoice = { viewModel.deleteInvoice(it) }
        )

        "dashboard" -> DashboardScreen(
            invoices = state.invoices,
            customers = state.customers
        )

        "customers" -> CustomersScreen(
            customers = state.customers,
            onSaveCustomer = { viewModel.saveCustomer(it) },
            onPayDebt = { id, amt -> viewModel.payCustomerDebt(id, amt) }
        )

        "suppliers" -> SuppliersScreen(
            suppliers = state.suppliers,
            movements = state.supplierMovements,
            onSaveSupplier = { viewModel.saveSupplier(it) },
            onRecordVersement = { id, amt, motif, ref -> viewModel.recordSupplierVersement(id, amt, motif, ref) },
            onRecordInvoice = { id, amt, motif, ref -> viewModel.recordSupplierInvoice(id, amt, motif, ref) }
        )

        "caisse" -> CaisseScreen(
            movements = state.caisseMovements,
            dailyCounts = state.dailyCaisseCounts,
            activeStore = state.settings.activeStore,
            onAddMovement = { type, motif, amt -> viewModel.addCaisseMovement(type, motif, amt) },
            onSaveDailyCount = { f, v, e, s, th, c, n ->
                viewModel.saveDailyCaisseCount(f, v, e, s, th, c, n)
            }
        )

        "users" -> UsersScreen(
            users = state.users,
            currentUser = state.currentUser,
            onSaveUser = { viewModel.saveUser(it) },
            onDeleteUser = { viewModel.deleteUser(it) }
        )

        "settings" -> SettingsScreen(
            storeInfo = state.storeInfo,
            settings = state.settings,
            autoEmailPdf = state.autoEmailPdf,
            firebaseUserEmail = state.firebaseUserEmail,
            isCloudSynced = state.isCloudSynced,
            cloudSyncError = state.cloudSyncError,
            onSignInGoogle = onSignInGoogle,
            onSignOutGoogle = onSignOutGoogle,
            onSaveStoreInfo = { viewModel.saveStoreInfo(it) },
            onSaveSettings = { viewModel.saveSettings(it) },
            onSetAutoEmailPdf = { viewModel.setAutoEmailPdf(it) },
            onResetCatalog = { viewModel.resetCatalog() },
            onExportJson = { viewModel.exportAllDataJson(it) },
            onImportJson = { viewModel.importAllDataJson(it) }
        )

        "calculator" -> M2PerCartonCalculatorScreen(
            onAddCalculatedLineToCart = { freeLine ->
                viewModel.addFreeLine(freeLine)
                viewModel.selectTab("pos")
            }
        )
    }
}

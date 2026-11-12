package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.example.data.model.AppSettings
import com.example.data.model.CaisseMovement
import com.example.data.model.DailyCaisseCount
import com.example.data.model.DepotNotification
import com.example.data.model.PosSeeds
import com.example.data.model.StaffUser
import com.example.data.model.StoreInfo
import com.example.data.model.formatDa
import com.example.data.model.formatM2
import com.example.data.model.toDzWhatsAppDigits
import com.example.ui.components.ClearableOutlinedTextField
import com.example.ui.components.NonImeNumericKeypad
import com.example.ui.components.NumericDisplayField
import com.example.ui.components.applyKeypadInput
import com.example.ui.components.applyKeypadQuickDelta
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
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun CaisseScreen(
    movements: List<CaisseMovement>,
    dailyCounts: List<DailyCaisseCount>,
    activeStore: String,
    onAddMovement: (type: String, motif: String, montant: Double) -> Unit,
    onSaveDailyCount: (Double, Double, Double, Double, Double, Double, String) -> Unit
) {
    val context = LocalContext.current
    var showMoveType by remember { mutableStateOf<String?>(null) }
    var showPointageDialog by remember { mutableStateOf(false) }

    val solde = remember(movements) { movements.sumOf { if (it.type == "entree") it.montant else -it.montant } }
    val todayPrefix = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    val todayMoves = remember(movements) { movements.filter { it.date.startsWith(todayPrefix) } }

    Column(modifier = Modifier.fillMaxSize().background(Slate950)) {
        Surface(color = Navy950) {
            Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Caisse & Trésorerie", style = MaterialTheme.typography.titleLarge, color = OnDark, fontWeight = FontWeight.Black)
                        Text("Solde Actuel : ${formatDa(solde)}", style = MaterialTheme.typography.titleMedium, color = Emerald500, fontWeight = FontWeight.Black)
                    }
                    Button(
                        onClick = { showPointageDialog = true },
                        modifier = Modifier.height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Amber500, contentColor = OnDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pointage", fontWeight = FontWeight.Black)
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { showMoveType = "entree" },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald500, contentColor = OnDark)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Entrée", fontWeight = FontWeight.Black)
                    }
                    Button(
                        onClick = { showMoveType = "sortie" },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Rose500, contentColor = OnDark)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Dépense / Sortie", fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (dailyCounts.isNotEmpty()) {
                item {
                    val last = dailyCounts.first()
                    Surface(shape = RoundedCornerShape(14.dp), color = Slate900, border = BorderStroke(1.5.dp, Amber500)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Dernier Pointage (${last.date.take(10)})", style = MaterialTheme.typography.labelMedium, color = Amber400, fontWeight = FontWeight.Black)
                                Text("Compté: ${formatDa(last.compteReel)} • Théorique: ${formatDa(last.theorique)}", style = MaterialTheme.typography.bodySmall, color = Slate100)
                            }
                            Text(
                                text = "Écart: ${formatDa(last.ecart)}",
                                color = if (kotlin.math.abs(last.ecart) < 1.0) Emerald400 else Rose400,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
            items(movements, key = { it.id }) { m ->
                Surface(shape = RoundedCornerShape(14.dp), color = Slate900, border = BorderStroke(1.dp, Slate800)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(m.motif, style = MaterialTheme.typography.titleSmall, color = Slate100, fontWeight = FontWeight.Bold)
                            Text("${m.date.take(16).replace('T', ' ')} • ${m.auteur}", style = MaterialTheme.typography.labelSmall, color = Slate400)
                        }
                        Text(
                            text = (if (m.type == "entree") "+" else "-") + formatDa(m.montant),
                            style = MaterialTheme.typography.titleMedium,
                            color = if (m.type == "entree") Emerald400 else Rose400,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }

    showMoveType?.let { type ->
        BackHandler { showMoveType = null }
        val defaultMotif = if (type == "entree") "Apport Caisse" else "Dépense magasin"
        var amtStr by remember { mutableStateOf("0") }
        var motif by remember { mutableStateOf(defaultMotif) }
        var showKeypad by remember { mutableStateOf(true) }
        var isPristine by remember { mutableStateOf(true) }
        Dialog(onDismissRequest = { showMoveType = null }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Surface(
                modifier = Modifier.fillMaxWidth().widthIn(max = 500.dp).padding(14.dp),
                shape = RoundedCornerShape(22.dp),
                color = Slate900,
                border = BorderStroke(2.dp, Navy950)
            ) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(shape = RoundedCornerShape(14.dp), color = Navy950) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (type == "entree") "Nouvelle Entrée" else "Nouvelle Dépense",
                                style = MaterialTheme.typography.titleLarge,
                                color = if (type == "entree") Emerald500 else Amber500,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = { showMoveType = null },
                                modifier = Modifier.height(42.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Rose500, contentColor = OnDark)
                            ) { Text("FERMER", fontWeight = FontWeight.Black) }
                        }
                    }
                    NumericDisplayField(
                        label = "Montant (DA)",
                        value = amtStr,
                        isActive = showKeypad,
                        onSelect = { isPristine = true; showKeypad = true },
                        onClear = { amtStr = ""; isPristine = false },
                        suffix = "DA",
                        accentColor = if (type == "entree") Emerald500 else Rose500
                    )
                    ClearableOutlinedTextField(
                        value = motif,
                        onValueChange = { motif = it },
                        label = { Text("Motif") },
                        autoClearValues = setOf("Apport Caisse", "Dépense magasin"),
                        onDismissKeypad = { showKeypad = false },
                        modifier = Modifier.fillMaxWidth()
                    )
                    AnimatedVisibility(visible = showKeypad, enter = expandVertically(), exit = shrinkVertically()) {
                        NonImeNumericKeypad(
                            onKeyPress = { k ->
                                val (n, p) = applyKeypadInput(amtStr, k, false, isPristine)
                                amtStr = n; isPristine = p
                            },
                            quickChips = listOf("+500" to 500.0, "+2000" to 2000.0, "+5000" to 5000.0),
                            onQuickDelta = { d -> amtStr = applyKeypadQuickDelta(amtStr, d, false); isPristine = false },
                            allowDecimal = false,
                            onValidate = {
                                val v = amtStr.toDoubleOrNull() ?: 0.0
                                val finalMotif = motif.trim().ifBlank { defaultMotif }
                                if (v > 0) {
                                    onAddMovement(type, finalMotif, v)
                                    showMoveType = null
                                }
                            },
                            accentColor = if (type == "entree") Emerald500 else Rose500
                        )
                    }
                }
            }
        }
    }

    if (showPointageDialog) {
        BackHandler { showPointageDialog = false }
        val ventesJour = todayMoves.filter { it.type == "entree" && it.referenceFacture != null }.sumOf { it.montant }
        val entreesDiv = todayMoves.filter { it.type == "entree" && it.referenceFacture == null }.sumOf { it.montant }
        val sortiesJour = todayMoves.filter { it.type == "sortie" }.sumOf { it.montant }
        var compteStr by remember { mutableStateOf(solde.coerceAtLeast(0.0).toInt().toString()) }
        var notes by remember { mutableStateOf("") }
        var showKeypad by remember { mutableStateOf(true) }
        var isPristine by remember { mutableStateOf(true) }
        val compteReel = compteStr.toDoubleOrNull() ?: 0.0
        val ecart = compteReel - solde

        Dialog(onDismissRequest = { showPointageDialog = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Surface(
                modifier = Modifier.fillMaxWidth().widthIn(max = 520.dp).padding(14.dp),
                shape = RoundedCornerShape(22.dp),
                color = Slate900,
                border = BorderStroke(2.dp, Navy950)
            ) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(shape = RoundedCornerShape(14.dp), color = Navy950) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Pointage ($activeStore)", style = MaterialTheme.typography.titleMedium, color = OnDark, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                            Button(
                                onClick = { showPointageDialog = false },
                                modifier = Modifier.height(42.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Rose500, contentColor = OnDark)
                            ) { Text("FERMER", fontWeight = FontWeight.Black) }
                        }
                    }
                    Text("Solde Théorique : ${formatDa(solde)}", style = MaterialTheme.typography.titleSmall, color = Cyan400, fontWeight = FontWeight.Black)
                    NumericDisplayField(
                        label = "Montant Compté Réel",
                        value = compteStr,
                        isActive = showKeypad,
                        onSelect = { isPristine = true; showKeypad = true },
                        onClear = { compteStr = ""; isPristine = false },
                        suffix = "DA",
                        accentColor = Emerald500
                    )
                    Text("Écart : ${formatDa(ecart)}", style = MaterialTheme.typography.titleSmall, color = if (kotlin.math.abs(ecart) < 1.0) Emerald400 else Rose400, fontWeight = FontWeight.Black)
                    ClearableOutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes clôture") },
                        onDismissKeypad = { showKeypad = false },
                        modifier = Modifier.fillMaxWidth()
                    )
                    AnimatedVisibility(visible = showKeypad, enter = expandVertically(), exit = shrinkVertically()) {
                        NonImeNumericKeypad(
                            onKeyPress = { k -> val (n, p) = applyKeypadInput(compteStr, k, false, isPristine); compteStr = n; isPristine = p },
                            allowDecimal = false,
                            onValidate = {
                                onSaveDailyCount(50000.0, ventesJour, entreesDiv, sortiesJour, solde, compteReel, notes)
                                showPointageDialog = false
                            },
                            validateLabel = "Clôturer Caisse",
                            accentColor = Emerald500
                        )
                    }
                    Button(
                        onClick = {
                            val msg = "💰 *CLÔTURE CAISSE $activeStore*\nThéorique: ${formatDa(solde)}\nCompté: ${formatDa(compteReel)}\nÉcart: ${formatDa(ecart)}"
                            openWhatsApp(context, PosSeeds.WHATSAPP_PATRON, msg)
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Cyan500, contentColor = OnDark)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Envoyer Bilan au Patron (WA)", fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun UsersScreen(
    users: List<StaffUser>,
    currentUser: StaffUser?,
    onSaveUser: (StaffUser) -> Unit,
    onDeleteUser: (String) -> Unit
) {
    var editingUser by remember { mutableStateOf<StaffUser?>(null) }
    var showCreate by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(Slate950)) {
        Surface(color = Navy950) {
            Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Gestion Équipe & Rôles", style = MaterialTheme.typography.titleLarge, color = OnDark, fontWeight = FontWeight.Black)
                    Text("${users.size} comptes (Nom + PIN)", style = MaterialTheme.typography.bodySmall, color = Amber500, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { showCreate = true },
                    modifier = Modifier.height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Amber500, contentColor = OnDark)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Profil", fontWeight = FontWeight.Black)
                }
            }
        }

        LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(users, key = { it.id }) { u ->
                Surface(shape = RoundedCornerShape(16.dp), color = Slate900, border = BorderStroke(1.5.dp, Slate800)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(u.name, style = MaterialTheme.typography.titleMedium, color = Slate100, fontWeight = FontWeight.Black)
                            Text("${u.role.uppercase()} • ${u.magasin} • PIN: ${u.pin}", style = MaterialTheme.typography.bodySmall, color = Cyan400, fontWeight = FontWeight.Bold)
                            Text(
                                text = "Stock: ${if (u.canModifyStock) "Modif" else "Lecture"} • Factures: ${if (u.canEditInvoices) "Éditable" else "Lecture"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate400
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedButton(
                                onClick = { editingUser = u },
                                modifier = Modifier.height(44.dp),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.5.dp, Amber500)
                            ) { Text("Éditer", color = Amber400, fontWeight = FontWeight.Black) }
                            if (u.id != currentUser?.id && u.id != "u_kader") {
                                IconButton(onClick = { onDeleteUser(u.id) }, modifier = Modifier.minimumInteractiveComponentSize()) {
                                    Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = Rose500)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreate || editingUser != null) {
        BackHandler { showCreate = false; editingUser = null }
        val init = editingUser
        var name by remember { mutableStateOf(init?.name ?: "") }
        var pinStr by remember { mutableStateOf(init?.pin ?: "0000") }
        var role by remember { mutableStateOf(init?.role ?: "vendeur") }
        var magasin by remember { mutableStateOf(init?.magasin ?: "Depot Remchi") }
        var canEditInvoices by remember { mutableStateOf(init?.canEditInvoices ?: true) }
        var canModifyStock by remember { mutableStateOf(init?.canModifyStock ?: (role != "vendeur")) }
        var canViewCaisse by remember { mutableStateOf(init?.canViewCaisse ?: (role != "vendeur")) }
        var canDeleteInvoices by remember { mutableStateOf(init?.canDeleteInvoices ?: (role == "admin")) }
        var showKeypad by remember { mutableStateOf(false) }
        var isPristine by remember { mutableStateOf(true) }

        Dialog(onDismissRequest = { showCreate = false; editingUser = null }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Surface(
                modifier = Modifier.fillMaxWidth().widthIn(max = 520.dp).padding(14.dp),
                shape = RoundedCornerShape(22.dp),
                color = Slate900,
                border = BorderStroke(2.dp, Navy950)
            ) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(shape = RoundedCornerShape(14.dp), color = Navy950) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(if (init == null) "Nouveau Profil Staff" else "Modifier ${init.name}", style = MaterialTheme.typography.titleLarge, color = Amber500, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                            Button(
                                onClick = { showCreate = false; editingUser = null },
                                modifier = Modifier.height(42.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Rose500, contentColor = OnDark)
                            ) { Text("FERMER", fontWeight = FontWeight.Black) }
                        }
                    }

                    ClearableOutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nom") },
                        onDismissKeypad = { showKeypad = false },
                        modifier = Modifier.fillMaxWidth()
                    )
                    NumericDisplayField(
                        label = "Code PIN (4 chiffres)",
                        value = pinStr,
                        isActive = showKeypad,
                        onSelect = { isPristine = true; showKeypad = true },
                        onClear = { pinStr = ""; isPristine = false },
                        accentColor = Amber500
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("admin", "gerant", "vendeur").forEach { r ->
                            FilterChip(
                                selected = role == r,
                                onClick = {
                                    role = r
                                    showKeypad = false
                                    val isAdm = r == "admin"
                                    val isGer = r == "gerant" || isAdm
                                    canEditInvoices = true
                                    canModifyStock = isGer
                                    canViewCaisse = isGer
                                    canDeleteInvoices = isAdm
                                },
                                label = { Text(r, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Navy950, selectedLabelColor = OnDark)
                            )
                        }
                    }
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        PosSeeds.SITES.forEach { s ->
                            FilterChip(
                                selected = magasin == s,
                                onClick = { magasin = s; showKeypad = false },
                                label = { Text(s.replace("Magasin ", "Mag "), fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Cyan500, selectedLabelColor = OnDark)
                            )
                        }
                    }

                    Surface(shape = RoundedCornerShape(12.dp), color = Slate950, border = BorderStroke(1.dp, Slate800)) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Peut modifier factures (alerte patron)", style = MaterialTheme.typography.bodySmall, color = Slate100)
                                Switch(checked = canEditInvoices, onCheckedChange = { canEditInvoices = it })
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Peut modifier stock & prix", style = MaterialTheme.typography.bodySmall, color = Slate100)
                                Switch(checked = canModifyStock, onCheckedChange = { canModifyStock = it })
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Accès Caisse & Fournisseurs", style = MaterialTheme.typography.bodySmall, color = Slate100)
                                Switch(checked = canViewCaisse, onCheckedChange = { canViewCaisse = it })
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Peut supprimer factures", style = MaterialTheme.typography.bodySmall, color = Slate100)
                                Switch(checked = canDeleteInvoices, onCheckedChange = { canDeleteInvoices = it })
                            }
                        }
                    }

                    AnimatedVisibility(visible = showKeypad, enter = expandVertically(), exit = shrinkVertically()) {
                        NonImeNumericKeypad(
                            onKeyPress = { k -> val (n, p) = applyKeypadInput(pinStr, k, false, isPristine); pinStr = n.take(4); isPristine = p },
                            allowDecimal = false,
                            onValidate = { showKeypad = false },
                            validateLabel = "OK"
                        )
                    }
                    Button(
                        onClick = {
                            if (name.isNotBlank() && pinStr.length == 4) {
                                val isAdmin = role == "admin"
                                val tabs = mutableListOf("pos", "stock", "invoices", "customers")
                                if (canViewCaisse || role == "gerant" || isAdmin) {
                                    tabs.addAll(listOf("dashboard", "suppliers", "caisse", "settings"))
                                }
                                if (isAdmin) tabs.add("users")
                                onSaveUser(
                                    StaffUser(
                                        id = init?.id ?: "u_${UUID.randomUUID().toString().take(6)}",
                                        name = name.trim(),
                                        role = role,
                                        pin = pinStr,
                                        magasin = magasin,
                                        allowedTabs = tabs.distinct(),
                                        canEditInvoices = canEditInvoices,
                                        canDeleteInvoices = canDeleteInvoices,
                                        canModifyStock = canModifyStock,
                                        canViewSuppliers = canViewCaisse,
                                        canViewCaisse = canViewCaisse,
                                        canManageUsers = isAdmin,
                                        active = true
                                    )
                                )
                                showCreate = false; editingUser = null
                            }
                        },
                        enabled = name.isNotBlank() && pinStr.length == 4,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald500, contentColor = OnDark)
                    ) { Text("Enregistrer Profil", fontWeight = FontWeight.Black) }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    storeInfo: StoreInfo,
    settings: AppSettings,
    autoEmailPdf: Boolean,
    firebaseUserEmail: String?,
    isCloudSynced: Boolean,
    cloudSyncError: String?,
    onSignInGoogle: suspend () -> String?,
    onSignOutGoogle: () -> Unit,
    onSaveStoreInfo: (StoreInfo) -> Unit,
    onSaveSettings: (AppSettings) -> Unit,
    onSetAutoEmailPdf: (Boolean) -> Unit,
    onResetCatalog: () -> Unit,
    onExportJson: ((String) -> Unit) -> Unit,
    onImportJson: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var nom by remember(storeInfo) { mutableStateOf(storeInfo.nom) }
    var adresse by remember(storeInfo) { mutableStateOf(storeInfo.adresse) }
    var tel by remember(storeInfo) { mutableStateOf(storeInfo.tel) }
    var tel2 by remember(storeInfo) { mutableStateOf(storeInfo.tel2) }
    var emailBureau by remember(storeInfo) { mutableStateOf(storeInfo.email) }
    var pendingExportJson by remember { mutableStateOf<String?>(null) }
    var isSharingApk by remember { mutableStateOf(false) }
    var shareApkError by remember { mutableStateOf<String?>(null) }
    var isConnectingCloud by remember { mutableStateOf(false) }
    var cloudSignInError by remember { mutableStateOf<String?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        val json = pendingExportJson
        if (uri != null && json != null) {
            context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
        }
        pendingExportJson = null
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            if (!text.isNullOrBlank()) onImportJson(text)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Slate950).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(color = Navy950) {
            Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                Text("Paramètres Magasin & Bureau", style = MaterialTheme.typography.titleLarge, color = OnDark, fontWeight = FontWeight.Black)
                Text("Configuration cloud, impression PDF et alertes patron", style = MaterialTheme.typography.bodySmall, color = Amber500, fontWeight = FontWeight.Bold)
            }
        }

        Column(modifier = Modifier.padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Automatic Bureau Email PDF & WhatsApp Settings
            Surface(shape = RoundedCornerShape(16.dp), color = Slate900, border = BorderStroke(1.5.dp, Amber500)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Envoi Automatique Bureau (Gmail PDF & WhatsApp)", style = MaterialTheme.typography.titleMedium, color = Amber400, fontWeight = FontWeight.Black)
                    ClearableOutlinedTextField(
                        value = emailBureau,
                        onValueChange = { emailBureau = it },
                        label = { Text("Gmail du Bureau (réception PDF auto)") },
                        modifier = Modifier.fillMaxWidth().testTag("settings_bureau_email_input")
                    )
                    ClearableOutlinedTextField(
                        value = tel,
                        onValueChange = { tel = it },
                        label = { Text("WhatsApp Patron") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    ClearableOutlinedTextField(
                        value = tel2,
                        onValueChange = { tel2 = it },
                        label = { Text("WhatsApp Dépôt") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Ouvrir Gmail PDF auto après vente", color = Slate100, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text("Prépare et envoie la facture PDF au bureau pour imprimer avant l'arrivée du client", color = Slate400, style = MaterialTheme.typography.labelSmall)
                        }
                        Switch(
                            checked = autoEmailPdf,
                            onCheckedChange = onSetAutoEmailPdf,
                            modifier = Modifier.testTag("settings_auto_email_switch")
                        )
                    }
                    Button(
                        onClick = {
                            onSaveStoreInfo(
                                storeInfo.copy(
                                    nom = nom.trim().ifBlank { storeInfo.nom },
                                    adresse = adresse.trim().ifBlank { storeInfo.adresse },
                                    tel = tel.trim(),
                                    tel2 = tel2.trim(),
                                    email = emailBureau.trim()
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald500, contentColor = OnDark)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Enregistrer Gmail & WhatsApp", fontWeight = FontWeight.Black)
                    }
                }
            }

            Surface(shape = RoundedCornerShape(16.dp), color = Slate900, border = BorderStroke(1.5.dp, if (firebaseUserEmail == null) Amber500 else Slate800)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (firebaseUserEmail == null) Icons.Default.CloudOff else Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = if (firebaseUserEmail == null) Amber400 else Emerald400,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (firebaseUserEmail == null) "Mode Hors-Ligne Local" else "Base Cloud Partagée Temps Réel",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (firebaseUserEmail == null) Amber400 else Cyan400,
                            fontWeight = FontWeight.Black
                        )
                    }
                    if (firebaseUserEmail == null) {
                        Text(
                            text = "Toutes les données (stock, ventes, factures PDF, caisse, clients) sont enregistrées localement sur cet appareil sans internet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate100,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (cloudSignInError != null) {
                            Text(
                                text = cloudSignInError ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = Rose400,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Button(
                            onClick = {
                                if (isConnectingCloud) return@Button
                                isConnectingCloud = true
                                cloudSignInError = null
                                coroutineScope.launch {
                                    val err = onSignInGoogle()
                                    if (err != null) {
                                        cloudSignInError = err
                                    }
                                    isConnectingCloud = false
                                }
                            },
                            enabled = !isConnectingCloud,
                            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("settings_google_signin_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Cyan500, contentColor = OnDark)
                        ) {
                            if (isConnectingCloud) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = OnDark)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Connexion Google Cloud...", fontWeight = FontWeight.Black)
                            } else {
                                Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Connecter Google Cloud (Passer en Ligne)", fontWeight = FontWeight.Black)
                            }
                        }
                    } else {
                        Text(
                            text = firebaseUserEmail,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate100,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = when {
                                cloudSyncError != null -> "Synchronisation : $cloudSyncError"
                                isCloudSynced -> "Cloud Firestore synchronisé en temps réel (multi-villes actif)"
                                else -> "Connexion au Cloud Firestore..."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = when {
                                cloudSyncError != null -> Amber400
                                isCloudSynced -> Emerald400
                                else -> Slate400
                            },
                            fontWeight = FontWeight.SemiBold
                        )
                        OutlinedButton(
                            onClick = onSignOutGoogle,
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("google_sign_out_btn"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.5.dp, Rose500)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Rose400, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Déconnecter le compte Google", color = Rose400, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            Surface(shape = RoundedCornerShape(16.dp), color = Slate900, border = BorderStroke(1.5.dp, Slate800)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Site Actif & Impression", style = MaterialTheme.typography.titleMedium, color = Cyan400, fontWeight = FontWeight.Black)
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        PosSeeds.SITES.forEach { s ->
                            FilterChip(
                                selected = settings.activeStore == s,
                                onClick = { onSaveSettings(settings.copy(activeStore = s)) },
                                label = { Text(s.replace("Magasin ", "Mag "), fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Navy950, selectedLabelColor = OnDark)
                            )
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Masquer Bain/Teinte sur facture", color = Slate100, style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = settings.hideBainTeinteFacture,
                            onCheckedChange = { onSaveSettings(settings.copy(hideBainTeinteFacture = it)) }
                        )
                    }
                }
            }

            Surface(shape = RoundedCornerShape(16.dp), color = Slate900, border = BorderStroke(1.5.dp, Slate800)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Identité Magasin (Factures)", style = MaterialTheme.typography.titleMedium, color = Navy950, fontWeight = FontWeight.Black)
                    ClearableOutlinedTextField(value = nom, onValueChange = { nom = it }, label = { Text("Nom commercial") }, modifier = Modifier.fillMaxWidth())
                    ClearableOutlinedTextField(value = adresse, onValueChange = { adresse = it }, label = { Text("Adresse") }, modifier = Modifier.fillMaxWidth())
                    Button(
                        onClick = {
                            onSaveStoreInfo(
                                storeInfo.copy(
                                    nom = nom.trim(),
                                    adresse = adresse.trim(),
                                    tel = tel.trim(),
                                    tel2 = tel2.trim(),
                                    email = emailBureau.trim()
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Cyan500, contentColor = OnDark)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sauvegarder Identité Magasin", fontWeight = FontWeight.Black)
                    }
                }
            }

            Surface(shape = RoundedCornerShape(16.dp), color = Slate900, border = BorderStroke(1.5.dp, Slate800)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Sauvegarde JSON & Catalogue", style = MaterialTheme.typography.titleMedium, color = Emerald400, fontWeight = FontWeight.Black)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                onExportJson { json ->
                                    pendingExportJson = json
                                    exportLauncher.launch("KADER_CERAMIC_BACKUP.json")
                                }
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald500, contentColor = OnDark)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Exporter", fontWeight = FontWeight.Black)
                        }
                        OutlinedButton(
                            onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.5.dp, Cyan500)
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, tint = Cyan400, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Importer", color = Cyan400, fontWeight = FontWeight.Black)
                        }
                    }
                    OutlinedButton(
                        onClick = onResetCatalog,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, Rose500)
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null, tint = Rose400, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Réinitialiser Catalogue Usines", color = Rose400, fontWeight = FontWeight.Black)
                    }
                }
            }

            Surface(shape = RoundedCornerShape(16.dp), color = Slate900, border = BorderStroke(1.5.dp, Slate800)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Partager l'application (.apk)", style = MaterialTheme.typography.titleMedium, color = Cyan400, fontWeight = FontWeight.Black)
                    Text(
                        "Envoyez directement le fichier d'installation Kader_POS.apk vers un autre téléphone via WhatsApp, Telegram ou Bluetooth.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400
                    )
                    if (shareApkError != null) {
                        Text(
                            text = shareApkError ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = Rose400
                        )
                    }
                    Button(
                        onClick = {
                            if (isSharingApk) return@Button
                            isSharingApk = true
                            shareApkError = null
                            coroutineScope.launch {
                                val result = runCatching {
                                    val apkFile = withContext(Dispatchers.IO) {
                                        val sourcePath = context.applicationInfo.sourceDir
                                            ?: error("Fichier APK introuvable sur cet appareil.")
                                        val sourceApk = File(sourcePath)
                                        if (!sourceApk.exists() || !sourceApk.canRead()) {
                                            error("Impossible de lire le fichier APK installé.")
                                        }
                                        val shareDir = File(context.cacheDir, "shared_apks").apply { mkdirs() }
                                        val targetApk = File(shareDir, "Kader_POS.apk")
                                        sourceApk.copyTo(targetApk, overwrite = true)
                                        targetApk
                                    }
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        apkFile
                                    )
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/vnd.android.package-archive"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        putExtra(Intent.EXTRA_SUBJECT, "Kader POS (.apk)")
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "Installez Kader POS sur votre téléphone Android en ouvrant le fichier Kader_POS.apk joint."
                                        )
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    val chooserIntent = Intent.createChooser(sendIntent, "Partager Kader POS (.apk)").apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(chooserIntent)
                                }
                                result.onFailure { err ->
                                    shareApkError = err.localizedMessage ?: "Impossible de partager l'APK."
                                }
                                isSharingApk = false
                            }
                        },
                        enabled = !isSharingApk,
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("share_apk_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Cyan500, contentColor = OnDark)
                    ) {
                        if (isSharingApk) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = OnDark
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Préparation de Kader_POS.apk...", fontWeight = FontWeight.Black)
                        } else {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Partager Kader_POS.apk", fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsSheet(
    notifications: List<DepotNotification>,
    storeInfo: StoreInfo,
    onDismiss: () -> Unit
) {
    BackHandler { onDismiss() }
    val context = LocalContext.current
    var categoryFilter by remember { mutableStateOf("Toutes") }

    val filtered = remember(notifications, categoryFilter) {
        notifications.filter { n ->
            val ref = n.reference.uppercase()
            when (categoryFilter) {
                "Ventes" -> ref.contains("[VENTE]")
                "Modifs" -> ref.contains("[MODIF]") || ref.contains("[SUPPR]")
                "Stock/Alertes" -> ref.contains("[STOCK]") || ref.contains("[ALERTE]")
                "Dispo Dépôt" -> ref.contains("[DISPO]")
                else -> true
            }
        }
    }

    val waPatronDigits = remember(storeInfo.tel) {
        toDzWhatsAppDigits(storeInfo.tel)?.takeIf { it.isNotBlank() } ?: PosSeeds.WHATSAPP_PATRON
    }
    val waDepotDigits = remember(storeInfo.tel2) {
        toDzWhatsAppDigits(storeInfo.tel2)?.takeIf { it.isNotBlank() } ?: PosSeeds.WHATSAPP_DEPOT
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Slate950,
        contentColor = Slate100
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("notifications_sheet"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(shape = RoundedCornerShape(16.dp), color = Navy950) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = Amber500, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Alertes Patron & Dépôt (${filtered.size})", style = MaterialTheme.typography.titleMedium, color = OnDark, fontWeight = FontWeight.Black)
                            Text("Ventes • Modifs Factures • Stock • Dispo", style = MaterialTheme.typography.labelSmall, color = Amber500)
                        }
                    }
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.height(44.dp).testTag("notifications_close_btn"),
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

            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("Toutes", "Ventes", "Modifs", "Stock/Alertes", "Dispo Dépôt").forEach { cat ->
                    FilterChip(
                        selected = categoryFilter == cat,
                        onClick = { categoryFilter = cat },
                        label = { Text(cat, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Navy950,
                            selectedLabelColor = OnDark,
                            containerColor = Slate900,
                            labelColor = Slate100
                        )
                    )
                }
            }

            HorizontalDivider(color = Slate800)

            if (filtered.isEmpty()) {
                Surface(shape = RoundedCornerShape(14.dp), color = Slate900, border = BorderStroke(1.dp, Slate800)) {
                    Text(
                        text = "Aucune alerte dans cette catégorie.",
                        color = Slate400,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth().padding(18.dp)
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.height(420.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(filtered, key = { it.id }) { n ->
                        val upperRef = n.reference.uppercase()
                        val badgeColor = when {
                            upperRef.contains("[MODIF]") || upperRef.contains("[ALERTE]") -> Amber500
                            upperRef.contains("[SUPPR]") -> Rose500
                            upperRef.contains("[STOCK]") || upperRef.contains("[DISPO]") -> Cyan500
                            else -> Emerald500
                        }
                        val badgeLabel = when {
                            upperRef.contains("[MODIF]") -> "MODIF"
                            upperRef.contains("[SUPPR]") -> "SUPPR"
                            upperRef.contains("[ALERTE]") -> "ALERTE STOCK"
                            upperRef.contains("[STOCK]") -> "STOCK"
                            upperRef.contains("[DISPO]") -> "DISPO DÉPÔT"
                            else -> "VENTE"
                        }

                        Surface(shape = RoundedCornerShape(14.dp), color = Slate900, border = BorderStroke(1.5.dp, badgeColor.copy(alpha = 0.6f))) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Surface(shape = RoundedCornerShape(6.dp), color = badgeColor) {
                                        Text(
                                            text = badgeLabel,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = OnDark,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    Text(
                                        text = "${n.cartonsTotal} ctn • ${formatM2(n.m2Total)} m² • ${n.date.take(16).replace('T', ' ')}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Slate400,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text("#${n.reference} • ${n.client}", style = MaterialTheme.typography.titleSmall, color = Slate100, fontWeight = FontWeight.Black)
                                Text(n.articlesSummary, style = MaterialTheme.typography.bodySmall, color = Slate400)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Button(
                                        onClick = {
                                            val msg = "🔔 *ALERTE PATRON #${n.reference}*\nClient/Auteur: ${n.client}\n${n.articlesSummary}"
                                            openWhatsApp(context, waPatronDigits, msg)
                                        },
                                        modifier = Modifier.weight(1f).height(42.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Emerald500, contentColor = OnDark),
                                        contentPadding = PaddingValues(horizontal = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("WA Patron", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black)
                                    }
                                    Button(
                                        onClick = {
                                            val msg = "📦 *DÉPÔT #${n.reference}*\nClient: ${n.client}\n${n.articlesSummary}"
                                            openWhatsApp(context, waDepotDigits, msg)
                                        },
                                        modifier = Modifier.weight(1f).height(42.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Cyan500, contentColor = OnDark),
                                        contentPadding = PaddingValues(horizontal = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Warehouse, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("WA Dépôt", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black)
                                    }
                                    Button(
                                        onClick = {
                                            val emailIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                if (storeInfo.email.isNotBlank()) {
                                                    putExtra(Intent.EXTRA_EMAIL, arrayOf(storeInfo.email.trim()))
                                                }
                                                putExtra(Intent.EXTRA_SUBJECT, "Alerte Kader POS #${n.reference}")
                                                putExtra(Intent.EXTRA_TEXT, "Alerte: ${n.reference}\nClient/Auteur: ${n.client}\n${n.articlesSummary}\nDate: ${n.date}")
                                            }
                                            val gmailIntent = Intent(emailIntent).apply { setPackage("com.google.android.gm") }
                                            runCatching {
                                                if (gmailIntent.resolveActivity(context.packageManager) != null) {
                                                    context.startActivity(gmailIntent)
                                                } else {
                                                    context.startActivity(Intent.createChooser(emailIntent, "Envoyer alerte par Gmail"))
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(0.85f).height(42.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Amber500, contentColor = OnDark),
                                        contentPadding = PaddingValues(horizontal = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Gmail", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black)
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

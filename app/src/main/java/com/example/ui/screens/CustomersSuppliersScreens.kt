package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Customer
import com.example.data.model.Supplier
import com.example.data.model.SupplierMovement
import com.example.data.model.formatDa
import com.example.data.model.toDzWhatsAppDigits
import com.example.ui.components.ClearableOutlinedTextField
import com.example.ui.components.NonImeNumericKeypad
import com.example.ui.components.NumericDisplayField
import com.example.ui.components.applyKeypadInput
import com.example.ui.components.applyKeypadQuickDelta
import com.example.ui.components.openWhatsApp
import com.example.ui.theme.Amber500
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
import java.util.UUID

@Composable
fun CustomersScreen(
    customers: List<Customer>,
    onSaveCustomer: (Customer) -> Unit,
    onPayDebt: (String, Double) -> Unit
) {
    val context = LocalContext.current
    var search by remember { mutableStateOf("") }
    var repayTarget by remember { mutableStateOf<Customer?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val filtered = remember(customers, search) {
        val q = search.trim().lowercase()
        customers.filter { q.isEmpty() || it.nom.lowercase().contains(q) || it.tel.contains(q) }
    }

    Column(modifier = Modifier.fillMaxSize().background(Slate950)) {
        Surface(color = Navy950) {
            Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Clients & Crédits", style = MaterialTheme.typography.titleLarge, color = OnDark, fontWeight = FontWeight.Black)
                        Text("Total Crédits: ${formatDa(customers.sumOf { it.dette })}", style = MaterialTheme.typography.bodySmall, color = Amber500, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Amber500, contentColor = OnDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Client", fontWeight = FontWeight.Black)
                    }
                }
                ClearableOutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = { Text("Rechercher client ou téléphone...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Cyan400) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(filtered, key = { it.id }) { c ->
                Surface(shape = RoundedCornerShape(16.dp), color = Slate900, border = BorderStroke(1.5.dp, Slate800)) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(c.nom, style = MaterialTheme.typography.titleMedium, color = Slate100, fontWeight = FontWeight.Black)
                                Text("${c.tel} • ${c.adresse}", style = MaterialTheme.typography.bodySmall, color = Slate400)
                                Text("Achats cumulés: ${formatDa(c.totalAchats)}", style = MaterialTheme.typography.labelMedium, color = Cyan400, fontWeight = FontWeight.Bold)
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (c.dette > 0) Rose950 else Emerald950,
                                border = BorderStroke(1.dp, if (c.dette > 0) Rose500 else Emerald500)
                            ) {
                                Text(
                                    text = if (c.dette > 0) "Crédit: ${formatDa(c.dette)}" else "À jour",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (c.dette > 0) Rose400 else Emerald300,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (c.dette > 0) {
                                Button(
                                    onClick = { repayTarget = c },
                                    modifier = Modifier.weight(1.4f).height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Emerald500, contentColor = OnDark)
                                ) {
                                    Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Remboursement", fontWeight = FontWeight.Black)
                                }
                            }
                            if (c.tel.isNotBlank()) {
                                OutlinedButton(
                                    onClick = { openWhatsApp(context, toDzWhatsAppDigits(c.tel), "Bonjour ${c.nom}, message de KADER CERAMIC Remchi.") },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.5.dp, Emerald500)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, tint = Emerald400, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("WhatsApp", color = Emerald400, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    repayTarget?.let { cust ->
        NonImeAmountDialog(
            title = "Remboursement — ${cust.nom}",
            initialAmount = cust.dette,
            accentColor = Emerald500,
            onDismiss = { repayTarget = null },
            onConfirm = { amt ->
                onPayDebt(cust.id, amt)
                repayTarget = null
            }
        )
    }

    if (showAddDialog) {
        BackHandler { showAddDialog = false }
        var nom by remember { mutableStateOf("") }
        var tel by remember { mutableStateOf("") }
        var adr by remember { mutableStateOf("") }
        Dialog(onDismissRequest = { showAddDialog = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Surface(
                modifier = Modifier.fillMaxWidth().widthIn(max = 500.dp).padding(14.dp),
                shape = RoundedCornerShape(22.dp),
                color = Slate900,
                border = BorderStroke(2.dp, Navy950)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(shape = RoundedCornerShape(14.dp), color = Navy950) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Nouveau Client", style = MaterialTheme.typography.titleLarge, color = Amber500, fontWeight = FontWeight.Black)
                            Button(
                                onClick = { showAddDialog = false },
                                modifier = Modifier.height(42.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Rose500, contentColor = OnDark)
                            ) { Text("FERMER", fontWeight = FontWeight.Black) }
                        }
                    }
                    ClearableOutlinedTextField(value = nom, onValueChange = { nom = it }, label = { Text("Nom complet") }, modifier = Modifier.fillMaxWidth())
                    ClearableOutlinedTextField(value = tel, onValueChange = { tel = it }, label = { Text("Téléphone") }, modifier = Modifier.fillMaxWidth())
                    ClearableOutlinedTextField(value = adr, onValueChange = { adr = it }, label = { Text("Adresse") }, modifier = Modifier.fillMaxWidth())
                    Button(
                        onClick = {
                            if (nom.isNotBlank()) {
                                onSaveCustomer(Customer("c_${UUID.randomUUID().toString().take(6)}", nom.trim(), tel.trim(), adr.trim(), 0.0, 0.0))
                                showAddDialog = false
                            }
                        },
                        enabled = nom.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald500, contentColor = OnDark)
                    ) { Text("Créer Client", fontWeight = FontWeight.Black) }
                }
            }
        }
    }
}

@Composable
fun SuppliersScreen(
    suppliers: List<Supplier>,
    movements: List<SupplierMovement>,
    onSaveSupplier: (Supplier) -> Unit,
    onRecordVersement: (String, Double, String, String?) -> Unit,
    onRecordInvoice: (String, Double, String, String?) -> Unit
) {
    val context = LocalContext.current
    var search by remember { mutableStateOf("") }
    var versementTarget by remember { mutableStateOf<Supplier?>(null) }
    var factureTarget by remember { mutableStateOf<Supplier?>(null) }
    var historyTarget by remember { mutableStateOf<Supplier?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val filtered = remember(suppliers, search) {
        val q = search.trim().lowercase()
        suppliers.filter { q.isEmpty() || it.nom.lowercase().contains(q) || it.usineRepresentee.lowercase().contains(q) }
    }

    Column(modifier = Modifier.fillMaxSize().background(Slate950)) {
        Surface(color = Navy950) {
            Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Fournisseurs & Usines", style = MaterialTheme.typography.titleLarge, color = OnDark, fontWeight = FontWeight.Black)
                        Text("Dettes Usines: ${formatDa(suppliers.sumOf { it.soldeDette })}", style = MaterialTheme.typography.bodySmall, color = Amber500, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Amber500, contentColor = OnDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Usine", fontWeight = FontWeight.Black)
                    }
                }
                ClearableOutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = { Text("Rechercher fournisseur ou usine...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Cyan400) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(filtered, key = { it.id }) { s ->
                Surface(shape = RoundedCornerShape(16.dp), color = Slate900, border = BorderStroke(1.5.dp, Slate800)) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(shape = RoundedCornerShape(6.dp), color = Cyan950) {
                                    Text(s.usineRepresentee, style = MaterialTheme.typography.labelMedium, color = Cyan300, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(s.nom, style = MaterialTheme.typography.titleMedium, color = Slate100, fontWeight = FontWeight.Black)
                                Text("${s.contact} • ${s.tel}", style = MaterialTheme.typography.bodySmall, color = Slate400)
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (s.soldeDette > 0) Rose950 else Emerald950,
                                border = BorderStroke(1.dp, if (s.soldeDette > 0) Rose500 else Emerald500)
                            ) {
                                Text(
                                    text = if (s.soldeDette > 0) formatDa(s.soldeDette) else "Soldé",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (s.soldeDette > 0) Rose400 else Emerald300,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { versementTarget = s },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald500, contentColor = OnDark)
                            ) { Text("+ Versement", fontWeight = FontWeight.Black) }
                            Button(
                                onClick = { factureTarget = s },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Rose500, contentColor = OnDark)
                            ) { Text("+ Facture", fontWeight = FontWeight.Black) }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            OutlinedButton(
                                onClick = { historyTarget = s },
                                modifier = Modifier.height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.5.dp, Cyan500)
                            ) {
                                Icon(Icons.Default.History, contentDescription = null, tint = Cyan400, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Historique", color = Cyan400, fontWeight = FontWeight.Bold)
                            }
                            if (s.tel.isNotBlank()) {
                                OutlinedButton(
                                    onClick = { openWhatsApp(context, toDzWhatsAppDigits(s.tel), "Salam ${s.nom}, KADER CERAMIC Remchi.") },
                                    modifier = Modifier.height(44.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.5.dp, Emerald500)
                                ) {
                                    Text("WhatsApp", color = Emerald400, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    versementTarget?.let { sup ->
        SupplierMoveDialog(
            title = "Versement à ${sup.nom}",
            defaultMotif = "Versement acompte dette usine",
            initialAmount = sup.soldeDette.coerceAtLeast(0.0),
            accentColor = Emerald500,
            onDismiss = { versementTarget = null },
            onConfirm = { amt, motif, ref ->
                onRecordVersement(sup.id, amt, motif, ref)
                versementTarget = null
            }
        )
    }

    factureTarget?.let { sup ->
        SupplierMoveDialog(
            title = "Facture Achat — ${sup.nom}",
            defaultMotif = "Réception camion carrelage",
            initialAmount = 0.0,
            accentColor = Rose500,
            onDismiss = { factureTarget = null },
            onConfirm = { amt, motif, ref ->
                onRecordInvoice(sup.id, amt, motif, ref)
                factureTarget = null
            }
        )
    }

    historyTarget?.let { sup ->
        BackHandler { historyTarget = null }
        val supMoves = movements.filter { it.supplierId == sup.id }
        Dialog(onDismissRequest = { historyTarget = null }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Surface(
                modifier = Modifier.fillMaxWidth().widthIn(max = 540.dp).padding(14.dp),
                shape = RoundedCornerShape(22.dp),
                color = Slate900,
                border = BorderStroke(2.dp, Navy950)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(shape = RoundedCornerShape(14.dp), color = Navy950) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Historique — ${sup.nom}", style = MaterialTheme.typography.titleMedium, color = OnDark, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                            Button(
                                onClick = { historyTarget = null },
                                modifier = Modifier.height(42.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Rose500, contentColor = OnDark)
                            ) { Text("FERMER", fontWeight = FontWeight.Black) }
                        }
                    }
                    if (supMoves.isEmpty()) {
                        Text("Aucun mouvement enregistré.", color = Slate400, style = MaterialTheme.typography.bodyMedium)
                    } else {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            supMoves.forEach { m ->
                                Surface(shape = RoundedCornerShape(12.dp), color = Slate950, border = BorderStroke(1.dp, Slate800)) {
                                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(m.motif, style = MaterialTheme.typography.bodyMedium, color = Slate100, fontWeight = FontWeight.SemiBold)
                                            Text("${m.date.take(10)} • ${m.auteur}", style = MaterialTheme.typography.labelSmall, color = Slate400)
                                        }
                                        Text(
                                            text = (if (m.type == "versement") "-" else "+") + formatDa(m.montant),
                                            style = MaterialTheme.typography.titleSmall,
                                            color = if (m.type == "versement") Emerald400 else Rose400,
                                            fontWeight = FontWeight.Black
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

    if (showAddDialog) {
        BackHandler { showAddDialog = false }
        var nom by remember { mutableStateOf("") }
        var usine by remember { mutableStateOf("") }
        var contact by remember { mutableStateOf("") }
        var tel by remember { mutableStateOf("") }
        Dialog(onDismissRequest = { showAddDialog = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Surface(
                modifier = Modifier.fillMaxWidth().widthIn(max = 500.dp).padding(14.dp),
                shape = RoundedCornerShape(22.dp),
                color = Slate900,
                border = BorderStroke(2.dp, Navy950)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(shape = RoundedCornerShape(14.dp), color = Navy950) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Nouveau Fournisseur", style = MaterialTheme.typography.titleLarge, color = Amber500, fontWeight = FontWeight.Black)
                            Button(
                                onClick = { showAddDialog = false },
                                modifier = Modifier.height(42.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Rose500, contentColor = OnDark)
                            ) { Text("FERMER", fontWeight = FontWeight.Black) }
                        }
                    }
                    ClearableOutlinedTextField(value = nom, onValueChange = { nom = it }, label = { Text("Raison sociale") }, modifier = Modifier.fillMaxWidth())
                    ClearableOutlinedTextField(value = usine, onValueChange = { usine = it }, label = { Text("Usine représentée") }, modifier = Modifier.fillMaxWidth())
                    ClearableOutlinedTextField(value = contact, onValueChange = { contact = it }, label = { Text("Contact") }, modifier = Modifier.fillMaxWidth())
                    ClearableOutlinedTextField(value = tel, onValueChange = { tel = it }, label = { Text("Téléphone") }, modifier = Modifier.fillMaxWidth())
                    Button(
                        onClick = {
                            if (nom.isNotBlank()) {
                                onSaveSupplier(Supplier("f_${UUID.randomUUID().toString().take(6)}", nom.trim(), contact.trim(), tel.trim(), "", usine.trim().ifBlank { "USINE" }, 0.0, 0.0))
                                showAddDialog = false
                            }
                        },
                        enabled = nom.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald500, contentColor = OnDark)
                    ) { Text("Créer Fournisseur", fontWeight = FontWeight.Black) }
                }
            }
        }
    }
}

@Composable
fun NonImeAmountDialog(
    title: String,
    initialAmount: Double,
    accentColor: Color,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    BackHandler { onDismiss() }
    var amtStr by remember { mutableStateOf(if (initialAmount > 0) initialAmount.toInt().toString() else "0") }
    var isPristine by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth().widthIn(max = 480.dp).padding(14.dp),
            shape = RoundedCornerShape(22.dp),
            color = Slate900,
            border = BorderStroke(2.dp, Navy950)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(shape = RoundedCornerShape(14.dp), color = Navy950) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(title, style = MaterialTheme.typography.titleMedium, color = OnDark, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.height(42.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Rose500, contentColor = OnDark)
                        ) { Text("FERMER", fontWeight = FontWeight.Black) }
                    }
                }
                NumericDisplayField(
                    label = "Montant (DA)",
                    value = amtStr,
                    isActive = true,
                    onSelect = {},
                    onClear = { amtStr = ""; isPristine = false },
                    suffix = "DA",
                    accentColor = accentColor
                )
                NonImeNumericKeypad(
                    onKeyPress = { key ->
                        val (next, nextPristine) = applyKeypadInput(amtStr, key, false, isPristine)
                        amtStr = next
                        isPristine = nextPristine
                    },
                    quickChips = listOf("+1000" to 1000.0, "+5000" to 5000.0, "+10000" to 10000.0),
                    onQuickDelta = { d -> amtStr = applyKeypadQuickDelta(amtStr, d, false); isPristine = false },
                    allowDecimal = false,
                    onValidate = {
                        val v = amtStr.toDoubleOrNull() ?: 0.0
                        if (v > 0) onConfirm(v)
                    },
                    validateLabel = "Valider",
                    accentColor = accentColor
                )
            }
        }
    }
}

@Composable
private fun SupplierMoveDialog(
    title: String,
    defaultMotif: String,
    initialAmount: Double,
    accentColor: Color,
    onDismiss: () -> Unit,
    onConfirm: (Double, String, String?) -> Unit
) {
    BackHandler { onDismiss() }
    var amtStr by remember { mutableStateOf(if (initialAmount > 0) initialAmount.toInt().toString() else "0") }
    var motif by remember { mutableStateOf(defaultMotif) }
    var ref by remember { mutableStateOf("") }
    var showKeypad by remember { mutableStateOf(true) }
    var isPristine by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
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
                        Text(title, style = MaterialTheme.typography.titleMedium, color = OnDark, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                        Button(
                            onClick = onDismiss,
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
                    accentColor = accentColor
                )
                ClearableOutlinedTextField(
                    value = motif,
                    onValueChange = { motif = it },
                    label = { Text("Motif / Mode") },
                    autoClearValues = setOf(defaultMotif),
                    onDismissKeypad = { showKeypad = false },
                    modifier = Modifier.fillMaxWidth()
                )
                ClearableOutlinedTextField(
                    value = ref,
                    onValueChange = { ref = it },
                    label = { Text("N° Pièce / BL (optionnel)") },
                    onDismissKeypad = { showKeypad = false },
                    modifier = Modifier.fillMaxWidth()
                )
                AnimatedVisibility(visible = showKeypad, enter = expandVertically(), exit = shrinkVertically()) {
                    NonImeNumericKeypad(
                        onKeyPress = { key ->
                            val (next, nextPristine) = applyKeypadInput(amtStr, key, false, isPristine)
                            amtStr = next
                            isPristine = nextPristine
                        },
                        quickChips = listOf("+5000" to 5000.0, "+20000" to 20000.0, "+50000" to 50000.0),
                        onQuickDelta = { d -> amtStr = applyKeypadQuickDelta(amtStr, d, false); isPristine = false },
                        allowDecimal = false,
                        onValidate = {
                            val v = amtStr.toDoubleOrNull() ?: 0.0
                            if (v > 0) onConfirm(v, motif.ifBlank { defaultMotif }, ref.takeIf { it.isNotBlank() })
                        },
                        validateLabel = "Confirmer",
                        accentColor = accentColor
                    )
                }
            }
        }
    }
}

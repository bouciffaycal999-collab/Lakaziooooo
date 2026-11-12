package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Customer
import com.example.data.model.Invoice
import com.example.data.model.PosSeeds
import com.example.data.model.formatDa
import com.example.data.model.formatM2
import com.example.ui.theme.Amber400
import com.example.ui.theme.Amber500
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Cyan500
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Navy950
import com.example.ui.theme.OnDark
import com.example.ui.theme.Rose400
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    invoices: List<Invoice>,
    customers: List<Customer>
) {
    var siteFilter by remember { mutableStateOf("Tous") }
    var periodFilter by remember { mutableStateOf("Tout") }

    val todayPrefix = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    val monthPrefix = remember { SimpleDateFormat("yyyy-MM", Locale.US).format(Date()) }
    val weekCutoff = remember { System.currentTimeMillis() - 7L * 24 * 3600 * 1000 }

    val filtered = remember(invoices, siteFilter, periodFilter) {
        invoices.filter { inv ->
            val matchSite = siteFilter == "Tous" || inv.magasin == siteFilter
            val matchPeriod = when (periodFilter) {
                "Aujourd'hui" -> inv.date.startsWith(todayPrefix)
                "7 Jours" -> parseIsoMillis(inv.date) >= weekCutoff
                "Ce Mois" -> inv.date.startsWith(monthPrefix)
                else -> true
            }
            matchSite && matchPeriod
        }
    }

    val totalCa = remember(filtered) { filtered.sumOf { it.totalNet } }
    val totalM2 = remember(filtered) { filtered.sumOf { inv -> inv.articles.sumOf { it.m2Total } } }
    val totalMarge = remember(filtered) {
        filtered.sumOf { inv ->
            inv.articles.sumOf { a -> (a.prixUnitaire - a.prixAchat) * a.m2Total }
        } - filtered.sumOf { it.remise }
    }
    val totalCreditClients = remember(customers) { customers.sumOf { it.dette } }

    val topTiles = remember(filtered) {
        val grouped = mutableMapOf<String, Pair<Double, Double>>()
        filtered.forEach { inv ->
            inv.articles.forEach { a ->
                val cur = grouped[a.name] ?: (0.0 to 0.0)
                grouped[a.name] = (cur.first + a.m2Total) to (cur.second + a.sousTotal)
            }
        }
        grouped.entries.sortedByDescending { it.value.second }.take(10)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(color = Navy950) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Tableau de Bord Commercial", style = MaterialTheme.typography.titleLarge, color = OnDark, fontWeight = FontWeight.Black)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    (listOf("Tous") + PosSeeds.SITES).forEach { s ->
                        FilterChip(
                            selected = siteFilter == s,
                            onClick = { siteFilter = s },
                            label = { Text(s, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Cyan500,
                                selectedLabelColor = OnDark,
                                containerColor = Slate900,
                                labelColor = Slate100
                            )
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Tout", "Aujourd'hui", "7 Jours", "Ce Mois").forEach { p ->
                        FilterChip(
                            selected = periodFilter == p,
                            onClick = { periodFilter = p },
                            label = { Text(p, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Amber500,
                                selectedLabelColor = OnDark,
                                containerColor = Slate900,
                                labelColor = Slate100
                            )
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                KpiCard("Chiffre d'Affaires", formatDa(totalCa), "${filtered.size} ventes", Emerald400, Modifier.weight(1f))
                KpiCard("Marge Estimée", formatDa(totalMarge), "Hors remises", Cyan400, Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                KpiCard("Surface Vendue", "${formatM2(totalM2)} m²", "${filtered.sumOf { i -> i.articles.sumOf { it.cartons } }} cartons", Amber400, Modifier.weight(1f))
                KpiCard("Créances Clients", formatDa(totalCreditClients), "${customers.count { it.dette > 0 }} débiteurs", Rose400, Modifier.weight(1f))
            }

            Surface(shape = RoundedCornerShape(16.dp), color = Slate900, border = BorderStroke(1.5.dp, Slate800)) {
                Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Performance par Site", style = MaterialTheme.typography.titleMedium, color = Navy950, fontWeight = FontWeight.Black)
                    PosSeeds.SITES.forEach { site ->
                        val siteInvoices = invoices.filter { it.magasin == site }
                        val siteCa = siteInvoices.sumOf { it.totalNet }
                        val totalAllCa = invoices.sumOf { it.totalNet }
                        val share = if (totalAllCa > 0) (siteCa / totalAllCa).toFloat() else 0f
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("$site (${siteInvoices.size})", style = MaterialTheme.typography.bodyMedium, color = Slate100, fontWeight = FontWeight.Bold)
                                Text(formatDa(siteCa), style = MaterialTheme.typography.bodyMedium, color = Cyan400, fontWeight = FontWeight.Black)
                            }
                            LinearProgressIndicator(
                                progress = { share },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = Cyan500,
                                trackColor = Slate850
                            )
                        }
                    }
                }
            }

            Surface(shape = RoundedCornerShape(16.dp), color = Slate900, border = BorderStroke(1.5.dp, Slate800)) {
                Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Top 10 Carreaux Vendus", style = MaterialTheme.typography.titleMedium, color = Navy950, fontWeight = FontWeight.Black)
                    if (topTiles.isEmpty()) {
                        Text("Aucune vente sur la période sélectionnée.", style = MaterialTheme.typography.bodyMedium, color = Slate400)
                    } else {
                        val maxCa = topTiles.first().value.second.coerceAtLeast(1.0)
                        topTiles.forEach { (name, pair) ->
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(name, style = MaterialTheme.typography.bodyMedium, color = Slate100, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                    Text("${formatM2(pair.first)}m² • ${formatDa(pair.second)}", style = MaterialTheme.typography.labelMedium, color = Amber400, fontWeight = FontWeight.Black)
                                }
                                LinearProgressIndicator(
                                    progress = { (pair.second / maxCa).toFloat() },
                                    modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(4.dp)),
                                    color = Amber500,
                                    trackColor = Slate850
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    sub: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), color = Slate900, border = BorderStroke(1.5.dp, accent.copy(alpha = 0.45f))) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = Slate400, fontWeight = FontWeight.Bold)
            Text(value, style = MaterialTheme.typography.titleLarge, color = accent, fontWeight = FontWeight.Black)
            Text(sub, style = MaterialTheme.typography.labelSmall, color = Slate400, fontWeight = FontWeight.SemiBold)
        }
    }
}

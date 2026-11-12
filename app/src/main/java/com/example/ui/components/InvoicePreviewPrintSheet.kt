package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppSettings
import com.example.data.model.Invoice
import com.example.data.model.PosSeeds
import com.example.data.model.StoreInfo
import com.example.data.model.formatDa
import com.example.data.model.formatM2
import com.example.data.model.toDzWhatsAppDigits
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
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoicePreviewPrintSheet(
    invoice: Invoice,
    storeInfo: StoreInfo,
    settings: AppSettings,
    autoEmailPdf: Boolean = false,
    onUpdateSettings: (AppSettings) -> Unit,
    onDismiss: () -> Unit
) {
    BackHandler { onDismiss() }
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var format by remember { mutableStateOf(settings.printFormat) }
    var density by remember { mutableStateOf(settings.a5Density) }
    var hideBain by remember { mutableStateOf(settings.hideBainTeinteFacture) }
    var savedPdfFile by remember { mutableStateOf<File?>(null) }
    var autoEmailLaunchedForId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(invoice.id, format, hideBain) {
        val file = withContext(Dispatchers.IO) {
            runCatching {
                InvoicePdfGenerator.generateAndSavePdf(
                    context = context,
                    invoice = invoice,
                    storeInfo = storeInfo,
                    format = format,
                    hideBain = hideBain
                )
            }.getOrNull()
        }
        savedPdfFile = file
        if (autoEmailPdf && file != null && autoEmailLaunchedForId != invoice.id) {
            autoEmailLaunchedForId = invoice.id
            InvoicePdfGenerator.sharePdfViaGmail(context, file, invoice, storeInfo)
        }
    }

    val patronWa = toDzWhatsAppDigits(storeInfo.tel) ?: PosSeeds.WHATSAPP_PATRON
    val depotWa = toDzWhatsAppDigits(storeInfo.tel2) ?: PosSeeds.WHATSAPP_DEPOT

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
                .widthIn(max = 680.dp)
                .align(Alignment.CenterHorizontally)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .testTag("invoice_preview_sheet"),
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
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Facture #${invoice.reference}",
                            style = MaterialTheme.typography.titleLarge,
                            color = OnDark,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "${invoice.magasin} • ${invoice.vendeur}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Amber500
                        )
                    }
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Rose500, contentColor = OnDark),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("invoice_preview_close_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("FERMER", fontWeight = FontWeight.Black)
                    }
                }
            }

            savedPdfFile?.let { file ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Emerald950,
                    border = BorderStroke(1.dp, Emerald500)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Emerald300, modifier = Modifier.size(18.dp))
                        Text(
                            text = "PDF enregistré automatiquement : ${file.name}",
                            style = MaterialTheme.typography.labelLarge,
                            color = Emerald300,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Format & Density Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("A4", "A5").forEach { fmt ->
                    FilterChip(
                        selected = format == fmt,
                        onClick = {
                            format = fmt
                            onUpdateSettings(settings.copy(printFormat = fmt))
                        },
                        label = { Text("Format $fmt", fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Cyan500,
                            selectedLabelColor = OnDark,
                            containerColor = Slate900,
                            labelColor = Slate400
                        )
                    )
                }
                if (format == "A5") {
                    listOf("ultra" to "Ultra", "compact" to "Compact", "normal" to "Normal").forEach { (key, label) ->
                        FilterChip(
                            selected = density == key,
                            onClick = {
                                density = key
                                onUpdateSettings(settings.copy(a5Density = key))
                            },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Masquer Bain/Teinte sur facture client",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate100,
                    fontWeight = FontWeight.SemiBold
                )
                Switch(
                    checked = hideBain,
                    onCheckedChange = {
                        hideBain = it
                        onUpdateSettings(settings.copy(hideBainTeinteFacture = it))
                    }
                )
            }

            // Clean Light-Paper Invoice Preview Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Slate900,
                border = BorderStroke(1.5.dp, Slate700)
            ) {
                val pad = if (format == "A5" && density == "ultra") 10.dp else 16.dp
                Column(
                    modifier = Modifier.padding(pad),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = storeInfo.nom,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = Navy950
                            )
                            Text(
                                text = storeInfo.nomArabe,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Cyan400
                            )
                            Text(
                                text = storeInfo.adresse,
                                fontSize = 11.sp,
                                color = Slate400
                            )
                            Text(
                                text = "Tél: ${storeInfo.tel} / ${storeInfo.tel2}",
                                fontSize = 11.sp,
                                color = Slate200
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "BON DE LIVRAISON / FACTURE",
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                color = Navy950
                            )
                            Text(
                                text = "N° ${invoice.reference}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Amber400
                            )
                            Text(
                                text = invoice.date.take(10),
                                fontSize = 11.sp,
                                color = Slate400
                            )
                            Text(
                                text = "Format: $format",
                                fontSize = 11.sp,
                                color = Cyan400
                            )
                        }
                    }

                    HorizontalDivider(color = Slate700)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Slate850, RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("CLIENT: ${invoice.client.nom}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Slate100)
                            if (invoice.client.tel.isNotBlank()) {
                                Text("Tél: ${invoice.client.tel}", fontSize = 11.sp, color = Slate400)
                            }
                        }
                        Text(
                            text = invoice.statut.uppercase(),
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = if (invoice.resteAPayer <= 0) Emerald400 else Rose400
                        )
                    }

                    // Table Header in Deep Navy
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Navy950, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 7.dp)
                    ) {
                        Text("Désignation", color = OnDark, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2.2f))
                        Text("Qté", color = OnDark, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                        Text("P.U", color = OnDark, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.9f), textAlign = TextAlign.End)
                        Text("Total", color = Amber500, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.1f), textAlign = TextAlign.End)
                    }

                    invoice.articles.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(2.2f)) {
                                Text(item.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate100)
                                val extra = buildString {
                                    append("${item.usine} • ${item.dimension}")
                                    if (!hideBain && item.bain.isNotBlank()) {
                                        append(" • Bain: ${item.bain}")
                                    }
                                }
                                Text(extra, fontSize = 10.sp, color = Slate400)
                            }
                            Text(
                                text = buildString {
                                    append("${item.cartons} ctn")
                                    if (item.pieces > 0) append(" +${item.pieces}p")
                                    append("\n${formatM2(item.m2Total)} m²")
                                },
                                fontSize = 10.sp,
                                color = Slate200,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = if (item.pieces > 0 && item.prixPiece > 0) {
                                    "${item.prixUnitaire.toInt()}\n(${item.prixPiece.toInt()}/p)"
                                } else item.prixUnitaire.toInt().toString(),
                                fontSize = 10.sp,
                                color = Cyan400,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(0.9f)
                            )
                            Text(
                                text = formatDa(item.sousTotal),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Emerald400,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1.1f)
                            )
                        }
                        HorizontalDivider(color = Slate800)
                    }

                    invoice.lignesLibres.forEach { line ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(line.designation, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Slate100, modifier = Modifier.weight(2.2f))
                            Text("${formatM2(line.quantite)} ${line.unite}", fontSize = 10.sp, color = Slate200, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                            Text(line.prixUnitaire.toInt().toString(), fontSize = 10.sp, color = Cyan400, textAlign = TextAlign.End, modifier = Modifier.weight(0.9f))
                            Text(formatDa(line.sousTotal), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Emerald400, textAlign = TextAlign.End, modifier = Modifier.weight(1.1f))
                        }
                        HorizontalDivider(color = Slate800)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Total Cartons: ${invoice.articles.sumOf { it.cartons }} ctn • Surface: ${formatM2(invoice.articles.sumOf { it.m2Total })} m²",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate400
                            )
                            Text(
                                text = "RC: ${storeInfo.rc} • NIF: ${storeInfo.nif}",
                                fontSize = 9.sp,
                                color = Slate500
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            if (invoice.remise > 0) {
                                Text("Remise: -${formatDa(invoice.remise)}", fontSize = 11.sp, color = Rose400)
                            }
                            Text("NET À PAYER: ${formatDa(invoice.totalNet)}", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Amber400)
                            Text("Payé (${invoice.modePaiement}): ${formatDa(invoice.montantPaye)}", fontSize = 11.sp, color = Emerald400)
                            if (invoice.resteAPayer > 0) {
                                Text("Reste / Crédit: ${formatDa(invoice.resteAPayer)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Rose400)
                            }
                        }
                    }
                }
            }

            // Primary Action Buttons: Print, View PDF, Gmail Bureau
            Button(
                onClick = {
                    val file = savedPdfFile ?: InvoicePdfGenerator.generateAndSavePdf(
                        context = context,
                        invoice = invoice,
                        storeInfo = storeInfo,
                        format = format,
                        hideBain = hideBain
                    ).also { savedPdfFile = it }
                    printInvoicePdf(
                        context = context,
                        pdfFile = file,
                        invoice = invoice,
                        format = format
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("invoice_print_pdf_btn"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Amber500, contentColor = OnDark)
            ) {
                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Imprimer Direct ($format)", fontWeight = FontWeight.Black, fontSize = 16.sp)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val file = savedPdfFile ?: InvoicePdfGenerator.generateAndSavePdf(
                            context, invoice, storeInfo, format, hideBain
                        ).also { savedPdfFile = it }
                        InvoicePdfGenerator.openPdfViewer(context, file)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("invoice_open_pdf_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Cyan500, contentColor = OnDark)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ouvrir PDF", fontWeight = FontWeight.Black)
                }

                Button(
                    onClick = {
                        val file = savedPdfFile ?: InvoicePdfGenerator.generateAndSavePdf(
                            context, invoice, storeInfo, format, hideBain
                        ).also { savedPdfFile = it }
                        InvoicePdfGenerator.sharePdfViaGmail(context, file, invoice, storeInfo)
                    },
                    modifier = Modifier
                        .weight(1.25f)
                        .height(52.dp)
                        .testTag("invoice_gmail_pdf_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy950, contentColor = OnDark)
                ) {
                    Icon(Icons.Default.Email, contentDescription = null, tint = Amber500, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Gmail Bureau", fontWeight = FontWeight.Black)
                }
            }

            // WhatsApp PDF / Message Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val msg = buildWhatsAppDepotMessage(invoice)
                        val file = savedPdfFile
                        if (file != null) {
                            InvoicePdfGenerator.sharePdfViaWhatsApp(context, file, depotWa, msg)
                        } else {
                            openWhatsApp(context, depotWa, msg)
                        }
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    border = BorderStroke(1.5.dp, Cyan500),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Cyan400, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("WA Dépôt", color = Cyan400, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        val msg = buildWhatsAppClientMessage(invoice, storeInfo)
                        val phone = toDzWhatsAppDigits(invoice.client.tel)
                        val file = savedPdfFile
                        if (file != null) {
                            InvoicePdfGenerator.sharePdfViaWhatsApp(context, file, phone, msg)
                        } else {
                            openWhatsApp(context, phone, msg)
                        }
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    border = BorderStroke(1.5.dp, Emerald500),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Emerald400, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("WA Client", color = Emerald400, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        val msg = buildWhatsAppPatronMessage(invoice)
                        val file = savedPdfFile
                        if (file != null) {
                            InvoicePdfGenerator.sharePdfViaWhatsApp(context, file, patronWa, msg)
                        } else {
                            openWhatsApp(context, patronWa, msg)
                        }
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    border = BorderStroke(1.5.dp, Amber500),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Amber400, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("WA Patron", color = Amber400, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

fun openWhatsApp(context: Context, phone213: String?, message: String) {
    val encoded = Uri.encode(message)
    val url = if (!phone213.isNullOrBlank()) {
        "https://wa.me/$phone213?text=$encoded"
    } else {
        "https://wa.me/?text=$encoded"
    }
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}

fun buildWhatsAppDepotMessage(invoice: Invoice): String {
    val lines = invoice.articles.joinToString("\n") {
        val pcsTag = if (it.pieces > 0) " + ${it.pieces} pcs" else ""
        "• ${it.cartons} ctn$pcsTag (${formatM2(it.m2Total)} m²) - ${it.name} [${it.usine}]"
    }
    return buildString {
        appendLine("📦 *PRÉPARATION DÉPÔT - ${invoice.magasin}*")
        appendLine("Réf: #${invoice.reference} | Vendeur: ${invoice.vendeur}")
        appendLine("Client: ${invoice.client.nom}")
        appendLine("----------------------")
        appendLine(lines)
        appendLine("Total: ${invoice.articles.sumOf { it.cartons }} Cartons / ${formatM2(invoice.articles.sumOf { it.m2Total })} m²")
    }
}

fun buildWhatsAppClientMessage(invoice: Invoice, store: StoreInfo): String {
    val items = invoice.articles.joinToString("\n") {
        val pcsTag = if (it.pieces > 0) " + ${it.pieces} pcs" else ""
        "• ${it.name}: ${it.cartons} ctn$pcsTag (${formatM2(it.m2Total)} m²) = ${formatDa(it.sousTotal)}"
    }
    return buildString {
        appendLine("🏛️ *${store.nom}*")
        appendLine("Facture #${invoice.reference} (${invoice.date.take(10)})")
        appendLine("Client: ${invoice.client.nom}")
        appendLine(items)
        if (invoice.lignesLibres.isNotEmpty()) {
            invoice.lignesLibres.forEach {
                appendLine("• ${it.designation}: ${formatM2(it.quantite)} ${it.unite} = ${formatDa(it.sousTotal)}")
            }
        }
        appendLine("----------------------")
        appendLine("Total Net: *${formatDa(invoice.totalNet)}*")
        appendLine("Payé: ${formatDa(invoice.montantPaye)} | Reste: ${formatDa(invoice.resteAPayer)}")
        appendLine("Merci de votre confiance !")
    }
}

fun buildWhatsAppPatronMessage(invoice: Invoice): String {
    return "📊 *FACTURE #${invoice.reference}* (${invoice.magasin})\n" +
        "Vendeur: ${invoice.vendeur} | Client: ${invoice.client.nom}\n" +
        "Total Net: ${formatDa(invoice.totalNet)} | Encaissé: ${formatDa(invoice.montantPaye)} (${invoice.modePaiement})\n" +
        "Reste: ${formatDa(invoice.resteAPayer)}"
}

private fun printInvoicePdf(
    context: Context,
    pdfFile: File,
    invoice: Invoice,
    format: String
) {
    val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
    val mediaSize = if (format.equals("A5", ignoreCase = true)) {
        PrintAttributes.MediaSize.ISO_A5
    } else {
        PrintAttributes.MediaSize.ISO_A4
    }
    val jobName = "Facture_${invoice.reference}"
    val adapter = object : PrintDocumentAdapter() {
        override fun onLayout(
            oldAttributes: PrintAttributes?,
            newAttributes: PrintAttributes?,
            cancellationSignal: CancellationSignal?,
            callback: LayoutResultCallback,
            extras: Bundle?
        ) {
            if (cancellationSignal?.isCanceled == true) {
                callback.onLayoutCancelled()
                return
            }
            val info = PrintDocumentInfo.Builder(pdfFile.name)
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                .build()
            callback.onLayoutFinished(info, true)
        }

        override fun onWrite(
            pages: Array<out PageRange>?,
            destination: ParcelFileDescriptor?,
            cancellationSignal: CancellationSignal?,
            callback: WriteResultCallback
        ) {
            if (cancellationSignal?.isCanceled == true) {
                callback.onWriteCancelled()
                return
            }
            val dest = destination ?: run {
                callback.onWriteFailed("Destination introuvable")
                return
            }
            runCatching {
                pdfFile.inputStream().use { input ->
                    ParcelFileDescriptor.AutoCloseOutputStream(dest).use { output ->
                        input.copyTo(output)
                    }
                }
            }.onSuccess {
                callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
            }.onFailure { err ->
                callback.onWriteFailed(err.localizedMessage ?: "Erreur impression PDF")
            }
        }
    }
    printManager.print(
        jobName,
        adapter,
        PrintAttributes.Builder().setMediaSize(mediaSize).build()
    )
}

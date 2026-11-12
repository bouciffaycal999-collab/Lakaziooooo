package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.Invoice
import com.example.data.model.StoreInfo
import com.example.data.model.formatDa
import com.example.data.model.formatM2
import java.io.File
import java.io.FileOutputStream

object InvoicePdfGenerator {

    private const val DIR_NAME = "invoices_pdf"

    fun generateAndSavePdf(
        context: Context,
        invoice: Invoice,
        storeInfo: StoreInfo,
        format: String = "A4",
        hideBain: Boolean = true
    ): File {
        val dir = File(context.filesDir, DIR_NAME).apply { mkdirs() }
        val safeRef = invoice.reference.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").ifBlank { invoice.id }
        val targetFile = File(dir, "Facture_$safeRef.pdf")

        val isA5 = format.equals("A5", ignoreCase = true)
        val pageWidth = if (isA5) 420 else 595
        val pageHeight = if (isA5) 595 else 842
        val margin = if (isA5) 20f else 28f
        val contentWidth = pageWidth - (margin * 2)

        val navy = Color.parseColor("#0A1D37")
        val orange = Color.parseColor("#F97316")
        val green = Color.parseColor("#16A34A")
        val red = Color.parseColor("#DC2626")
        val slateText = Color.parseColor("#1D3354")
        val mutedText = Color.parseColor("#5D7494")
        val lightBg = Color.parseColor("#EFF4FA")
        val borderCol = Color.parseColor("#CBD5E1")

        val paintFill = Paint(Paint.ANTI_ALIAS_FLAG)
        val paintLine = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = borderCol
            strokeWidth = 1f
        }
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = if (isA5) 14f else 18f
            color = Color.WHITE
        }
        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = if (isA5) 8.5f else 10f
            color = Color.parseColor("#DBEAFE")
        }
        val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = if (isA5) 9.5f else 11f
            color = navy
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = if (isA5) 8.5f else 10f
            color = slateText
        }
        val smallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = if (isA5) 7.5f else 8.5f
            color = mutedText
        }

        val doc = PdfDocument()
        var pageNum = 1
        var page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create())
        var canvas = page.canvas

        fun drawHeader(c: Canvas): Float {
            val headerH = if (isA5) 68f else 84f
            paintFill.color = navy
            c.drawRoundRect(RectF(margin, margin, pageWidth - margin, margin + headerH), 8f, 8f, paintFill)

            c.drawText(storeInfo.nom, margin + 12f, margin + 22f, titlePaint)
            c.drawText(storeInfo.adresse, margin + 12f, margin + 37f, subtitlePaint)
            c.drawText("Tél: ${storeInfo.tel} / ${storeInfo.tel2}", margin + 12f, margin + 50f, subtitlePaint)
            if (!isA5 && storeInfo.email.isNotBlank()) {
                c.drawText("Email: ${storeInfo.email}", margin + 12f, margin + 63f, subtitlePaint)
            }

            val badgeW = if (isA5) 136f else 175f
            paintFill.color = orange
            c.drawRoundRect(
                RectF(pageWidth - margin - badgeW - 10f, margin + 10f, pageWidth - margin - 10f, margin + headerH - 10f),
                6f, 6f, paintFill
            )
            val rightBold = Paint(boldPaint).apply {
                color = Color.WHITE
                textAlign = Paint.Align.RIGHT
                textSize = if (isA5) 9.5f else 11.5f
            }
            val rightSmall = Paint(smallPaint).apply {
                color = Color.WHITE
                textAlign = Paint.Align.RIGHT
                textSize = if (isA5) 7.5f else 9f
            }
            val rx = pageWidth - margin - 18f
            c.drawText("FACTURE N° ${invoice.reference}", rx, margin + 27f, rightBold)
            c.drawText("Date: ${invoice.date.take(10)}", rx, margin + 41f, rightSmall)
            c.drawText("${invoice.magasin} • ${invoice.vendeur}", rx, margin + 54f, rightSmall)

            var y = margin + headerH + 10f
            // Client bar
            val clientBarH = if (isA5) 30f else 36f
            paintFill.color = lightBg
            c.drawRoundRect(RectF(margin, y, pageWidth - margin, y + clientBarH), 6f, 6f, paintFill)
            c.drawText("Client: ${invoice.client.nom}", margin + 10f, y + 14f, boldPaint)
            val clientSub = listOf(invoice.client.tel, invoice.client.adresse).filter { it.isNotBlank() }.joinToString(" • ")
            if (clientSub.isNotBlank()) {
                c.drawText(clientSub, margin + 10f, y + 26f, smallPaint)
            }
            val statusPaint = Paint(boldPaint).apply {
                textAlign = Paint.Align.RIGHT
                color = if (invoice.resteAPayer <= 0.01) green else red
            }
            c.drawText("Statut: ${invoice.statut.uppercase()}", pageWidth - margin - 10f, y + 18f, statusPaint)
            return y + clientBarH + 10f
        }

        fun drawTableHeader(c: Canvas, yStart: Float): Float {
            val h = if (isA5) 20f else 24f
            paintFill.color = navy
            c.drawRoundRect(RectF(margin, yStart, pageWidth - margin, yStart + h), 4f, 4f, paintFill)
            val thLeft = Paint(boldPaint).apply { color = Color.WHITE; textSize = if (isA5) 8.5f else 9.5f }
            val thRight = Paint(thLeft).apply { textAlign = Paint.Align.RIGHT }
            val textY = yStart + (h * 0.68f)
            c.drawText("Désignation", margin + 8f, textY, thLeft)
            c.drawText("Qté", margin + contentWidth * 0.62f, textY, thRight)
            c.drawText("P.U", margin + contentWidth * 0.78f, textY, thRight)
            c.drawText("Sous-total", pageWidth - margin - 8f, textY, thRight)
            return yStart + h + 4f
        }

        var y = drawTableHeader(canvas, drawHeader(canvas))
        val footerReserve = if (isA5) 86f else 105f

        fun ensureSpace(neededHeight: Float) {
            if (y + neededHeight > pageHeight - margin - footerReserve) {
                doc.finishPage(page)
                pageNum++
                page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create())
                canvas = page.canvas
                y = drawTableHeader(canvas, margin)
            }
        }

        val rightBody = Paint(bodyPaint).apply { textAlign = Paint.Align.RIGHT }
        val rightBold = Paint(boldPaint).apply { textAlign = Paint.Align.RIGHT }
        val rightSmall = Paint(smallPaint).apply { textAlign = Paint.Align.RIGHT }

        invoice.articles.forEach { item ->
            val rowH = if (isA5) 26f else 30f
            ensureSpace(rowH + 4f)
            canvas.drawText(item.name.take(36), margin + 6f, y + 11f, boldPaint)
            val meta = buildString {
                append("${item.usine} • ${item.dimension}")
                if (!hideBain && item.bain.isNotBlank()) append(" • Bain ${item.bain}")
            }
            canvas.drawText(meta.take(46), margin + 6f, y + 22f, smallPaint)

            val qteStr = if (item.pieces > 0) "${item.cartons} ctn + ${item.pieces} pcs" else "${item.cartons} ctn"
            canvas.drawText(qteStr, margin + contentWidth * 0.62f, y + 11f, rightBody)
            canvas.drawText("${formatM2(item.m2Total)} m²", margin + contentWidth * 0.62f, y + 22f, rightSmall)

            val puLabel = if (item.pieces > 0 && item.prixPiece > 0) {
                "${item.prixUnitaire.toInt()} / ${item.prixPiece.toInt()}p"
            } else {
                "${item.prixUnitaire.toInt()} DA"
            }
            canvas.drawText(puLabel, margin + contentWidth * 0.78f, y + 15f, rightBody)
            canvas.drawText(formatDa(item.sousTotal), pageWidth - margin - 6f, y + 15f, rightBold)

            y += rowH
            canvas.drawLine(margin, y, pageWidth - margin, y, paintLine)
            y += 3f
        }

        invoice.lignesLibres.forEach { line ->
            val rowH = if (isA5) 22f else 24f
            ensureSpace(rowH + 4f)
            canvas.drawText(line.designation.take(38), margin + 6f, y + 14f, boldPaint)
            canvas.drawText("${formatM2(line.quantite)} ${line.unite}", margin + contentWidth * 0.62f, y + 14f, rightBody)
            canvas.drawText("${line.prixUnitaire.toInt()} DA", margin + contentWidth * 0.78f, y + 14f, rightBody)
            canvas.drawText(formatDa(line.sousTotal), pageWidth - margin - 6f, y + 14f, rightBold)
            y += rowH
            canvas.drawLine(margin, y, pageWidth - margin, y, paintLine)
            y += 3f
        }

        ensureSpace(footerReserve)
        y += 8f
        paintFill.color = lightBg
        val summaryH = if (isA5) 68f else 80f
        canvas.drawRoundRect(RectF(margin, y, pageWidth - margin, y + summaryH), 8f, 8f, paintFill)

        val totalCartons = invoice.articles.sumOf { it.cartons }
        val totalM2 = invoice.articles.sumOf { it.m2Total }
        canvas.drawText("Total Cartons: $totalCartons ctn", margin + 10f, y + 18f, boldPaint)
        canvas.drawText("Surface totale: ${formatM2(totalM2)} m²", margin + 10f, y + 32f, bodyPaint)
        canvas.drawText("Mode: ${invoice.modePaiement}", margin + 10f, y + 46f, bodyPaint)
        if (storeInfo.rc.isNotBlank() || storeInfo.nif.isNotBlank()) {
            canvas.drawText("RC: ${storeInfo.rc} • NIF: ${storeInfo.nif}", margin + 10f, y + 60f, smallPaint)
        }

        val totalNetPaint = Paint(boldPaint).apply {
            textAlign = Paint.Align.RIGHT
            textSize = if (isA5) 12f else 14f
            color = navy
        }
        val paidPaint = Paint(boldPaint).apply {
            textAlign = Paint.Align.RIGHT
            color = green
        }
        val duePaint = Paint(boldPaint).apply {
            textAlign = Paint.Align.RIGHT
            color = red
        }
        val rx = pageWidth - margin - 10f
        var ty = y + 18f
        if (invoice.remise > 0) {
            canvas.drawText("Remise: -${formatDa(invoice.remise)}", rx, ty, duePaint)
            ty += 14f
        }
        canvas.drawText("TOTAL NET: ${formatDa(invoice.totalNet)}", rx, ty, totalNetPaint)
        ty += 15f
        canvas.drawText("Payé: ${formatDa(invoice.montantPaye)}", rx, ty, paidPaint)
        if (invoice.resteAPayer > 0.01) {
            ty += 14f
            canvas.drawText("Reste / Crédit: ${formatDa(invoice.resteAPayer)}", rx, ty, duePaint)
        }

        doc.finishPage(page)
        FileOutputStream(targetFile).use { out ->
            doc.writeTo(out)
        }
        doc.close()
        return targetFile
    }

    fun getPdfUri(context: Context, file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    fun openPdfViewer(context: Context, file: File) {
        val uri = getPdfUri(context, file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching {
            context.startActivity(Intent.createChooser(intent, "Ouvrir la facture PDF"))
        }
    }

    fun sharePdfViaGmail(
        context: Context,
        file: File,
        invoice: Invoice,
        storeInfo: StoreInfo
    ) {
        val uri = getPdfUri(context, file)
        val recipient = storeInfo.email.trim()
        val body = buildString {
            appendLine("Bonjour,")
            appendLine("Veuillez trouver ci-joint la facture PDF #${invoice.reference}.")
            appendLine("Magasin: ${invoice.magasin} | Vendeur: ${invoice.vendeur}")
            appendLine("Client: ${invoice.client.nom} ${if (invoice.client.tel.isNotBlank()) "(${invoice.client.tel})" else ""}")
            appendLine("Total Net: ${formatDa(invoice.totalNet)} | Payé: ${formatDa(invoice.montantPaye)} | Reste: ${formatDa(invoice.resteAPayer)}")
            appendLine("-- ${storeInfo.nom}")
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            if (recipient.isNotBlank()) {
                putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
            }
            putExtra(
                Intent.EXTRA_SUBJECT,
                "Facture #${invoice.reference} - ${invoice.client.nom} (${formatDa(invoice.totalNet)})"
            )
            putExtra(Intent.EXTRA_TEXT, body)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val gmailIntent = Intent(intent).apply { setPackage("com.google.android.gm") }
        val resolvedGmail = gmailIntent.resolveActivity(context.packageManager)
        runCatching {
            if (resolvedGmail != null) {
                context.startActivity(gmailIntent)
            } else {
                context.startActivity(Intent.createChooser(intent, "Envoyer PDF par Email"))
            }
        }
    }

    fun sharePdfViaWhatsApp(
        context: Context,
        file: File,
        phone213: String?,
        message: String
    ) {
        val uri = getPdfUri(context, file)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, message)
            if (!phone213.isNullOrBlank()) {
                putExtra("jid", "$phone213@s.whatsapp.net")
            }
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val waStandard = Intent(sendIntent).apply { setPackage("com.whatsapp") }
        val waBusiness = Intent(sendIntent).apply { setPackage("com.whatsapp.w4b") }
        when {
            waStandard.resolveActivity(context.packageManager) != null -> {
                runCatching { context.startActivity(waStandard) }
            }
            waBusiness.resolveActivity(context.packageManager) != null -> {
                runCatching { context.startActivity(waBusiness) }
            }
            else -> {
                openWhatsApp(context, phone213, message)
            }
        }
    }
}

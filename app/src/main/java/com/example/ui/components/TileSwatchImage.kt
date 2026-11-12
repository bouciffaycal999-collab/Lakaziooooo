package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.TileProduct
import com.example.data.model.formatDa
import com.example.data.model.formatM2
import com.example.ui.theme.Amber500
import com.example.ui.theme.Cyan300
import com.example.ui.theme.Cyan500
import com.example.ui.theme.Cyan950
import com.example.ui.theme.Emerald300
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
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

fun parseCeramicColor(hex: String): Color {
    return runCatching {
        val clean = hex.trim().removePrefix("#")
        val parsed = when (clean.length) {
            6 -> ("FF$clean").toLong(16)
            8 -> clean.toLong(16)
            else -> 0xFF94A3B8
        }
        Color(parsed)
    }.getOrDefault(Color(0xFF94A3B8))
}

@Composable
fun TileSwatchImage(
    imageUrl: String,
    colorHex: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp
) {
    val swatchColor = remember(colorHex) { parseCeramicColor(colorHex) }
    val shape = remember(cornerRadius) { RoundedCornerShape(cornerRadius) }
    val context = LocalContext.current
    val request = remember(imageUrl, context) {
        if (imageUrl.isBlank()) null
        else ImageRequest.Builder(context)
            .data(imageUrl)
            .size(360)
            .crossfade(false)
            .build()
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(swatchColor)
    ) {
        CeramicSwatchFallback(swatchColor)
        if (request != null) {
            AsyncImage(
                model = request,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun CeramicSwatchFallback(baseColor: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    baseColor,
                    baseColor.copy(alpha = 0.80f),
                    Slate700.copy(alpha = 0.25f)
                ),
                start = Offset.Zero,
                end = Offset(size.width, size.height)
            )
        )
        val jointColor = Slate950.copy(alpha = 0.35f)
        drawLine(
            color = jointColor,
            start = Offset(size.width * 0.5f, 0f),
            end = Offset(size.width * 0.5f, size.height),
            strokeWidth = 1.5f
        )
        drawLine(
            color = jointColor,
            start = Offset(0f, size.height * 0.5f),
            end = Offset(size.width, size.height * 0.5f),
            strokeWidth = 1.5f
        )
    }
}

@Composable
fun ProductPhotoLightboxDialog(
    product: TileProduct,
    onDismiss: () -> Unit,
    onCheckDepotDispo: () -> Unit,
    onOpenCalculator: (() -> Unit)? = null
) {
    val isLow = product.stock in 1..5
    val isOut = product.stock <= 0
    val stockBg = when {
        isOut -> Rose950
        isLow -> com.example.ui.theme.Amber950
        else -> Emerald950
    }
    val stockFg = when {
        isOut -> Rose400
        isLow -> com.example.ui.theme.Amber300
        else -> Emerald300
    }
    val stockLabel = when {
        isOut -> "Rupture de stock (0 ctn)"
        isLow -> "Stock critique : ${product.stock} ctn (${formatM2(product.stock * product.m2Carton)} m²)"
        else -> "En stock : ${product.stock} ctn (${formatM2(product.stock * product.m2Carton)} m²)"
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .testTag("product_photo_lightbox"),
            shape = RoundedCornerShape(24.dp),
            color = Slate900,
            border = BorderStroke(2.dp, Navy950)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Navy950
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.titleLarge,
                            color = OnDark,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "${product.usine} • ${product.dimension} • ${product.finition.uppercase()} • ${product.choix}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Amber500,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                TileSwatchImage(
                    imageUrl = product.image,
                    colorHex = product.color,
                    contentDescription = product.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(270.dp),
                    cornerRadius = 18.dp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = stockBg,
                        border = BorderStroke(1.dp, stockFg.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = stockLabel,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = stockFg,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Cyan950,
                        border = BorderStroke(1.dp, Cyan500.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "${formatDa(product.prix)} / m²",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = Cyan300,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Text(
                    text = "Colisage : ${formatM2(product.m2Carton)} m²/ctn • ${product.pcs} pcs/ctn • Couleur : ${product.colorName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate100,
                    fontWeight = FontWeight.SemiBold
                )
                if (product.description.isNotBlank()) {
                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onCheckDepotDispo,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("lightbox_dispo_btn"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Cyan500, contentColor = OnDark)
                    ) {
                        Icon(Icons.Default.Warehouse, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Dispo Dépôt", fontWeight = FontWeight.Black)
                    }

                    if (onOpenCalculator != null) {
                        Button(
                            onClick = {
                                onDismiss()
                                onOpenCalculator()
                            },
                            modifier = Modifier
                                .weight(1.2f)
                                .height(52.dp)
                                .testTag("lightbox_calc_btn"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Amber500, contentColor = OnDark)
                        ) {
                            Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Calculer & Ajouter", fontWeight = FontWeight.Black)
                        }
                    }
                }

                // Big Prominent Red Exit Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("lightbox_close_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Rose500, contentColor = OnDark)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Fermer la photo", modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("FERMER", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.AppSettings
import com.example.data.model.CaisseMovement
import com.example.data.model.CartItem
import com.example.data.model.Customer
import com.example.data.model.DailyCaisseCount
import com.example.data.model.DepotNotification
import com.example.data.model.FreeLineItem
import com.example.data.model.Invoice
import com.example.data.model.InvoiceClient
import com.example.data.model.StaffUser
import com.example.data.model.StoreInfo
import com.example.data.model.Supplier
import com.example.data.model.SupplierMovement
import com.example.data.model.TileProduct
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val usine: String,
    val dimension: String,
    val m2Carton: Double,
    val pcs: Int,
    val prix: Double,
    val prixAchat: Double,
    val stock: Int,
    val color: String,
    val colorName: String,
    val image: String,
    val finition: String,
    val choix: String,
    val categorie: String,
    val description: String
)

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey val id: String,
    val reference: String,
    val date: String,
    val vendeur: String,
    val magasin: String,
    val clientJson: String,
    val articlesJson: String,
    val lignesLibresJson: String,
    val totalBrut: Double,
    val remise: Double,
    val totalNet: Double,
    val montantPaye: Double,
    val resteAPayer: Double,
    val modePaiement: String,
    val statut: String,
    val notes: String
)

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey val id: String,
    val nom: String,
    val tel: String,
    val adresse: String,
    val dette: Double,
    val totalAchats: Double
)

@Entity(tableName = "suppliers")
data class SupplierEntity(
    @PrimaryKey val id: String,
    val nom: String,
    val contact: String,
    val tel: String,
    val adresse: String,
    val usineRepresentee: String,
    val soldeDette: Double,
    val totalAchats: Double
)

@Entity(tableName = "supplier_movements")
data class SupplierMovementEntity(
    @PrimaryKey val id: String,
    val supplierId: String,
    val date: String,
    val type: String,
    val montant: Double,
    val motif: String,
    val pieceJointeRef: String?,
    val auteur: String
)

@Entity(tableName = "caisse_movements")
data class CaisseMovementEntity(
    @PrimaryKey val id: String,
    val date: String,
    val type: String,
    val motif: String,
    val montant: Double,
    val auteur: String,
    val referenceFacture: String?
)

@Entity(tableName = "daily_caisse_counts")
data class DailyCaisseCountEntity(
    @PrimaryKey val id: String,
    val date: String,
    val magasin: String,
    val auteur: String,
    val fondOuverture: Double,
    val ventesEspecesJour: Double,
    val entreesDiversJour: Double,
    val sortiesJour: Double,
    val theorique: Double,
    val compteReel: Double,
    val ecart: Double,
    val notes: String
)

@Entity(tableName = "staff_users")
data class StaffUserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val role: String,
    val pin: String,
    val magasin: String,
    val allowedTabsJson: String,
    val canEditInvoices: Boolean,
    val canDeleteInvoices: Boolean,
    val canModifyStock: Boolean,
    val canViewSuppliers: Boolean,
    val canViewCaisse: Boolean,
    val canManageUsers: Boolean,
    val active: Boolean
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val invoiceId: String,
    val reference: String,
    val date: String,
    val vendeur: String,
    val magasin: String,
    val client: String,
    val clientTel: String,
    val totalNet: Double,
    val m2Total: Double,
    val cartonsTotal: Int,
    val articlesSummary: String,
    val read: Boolean
)

@Entity(tableName = "app_kv")
data class AppKvEntity(
    @PrimaryKey val key: String,
    val jsonValue: String
)

object PosJsonMapper {
    fun TileProduct.toEntity() = ProductEntity(
        id, name, usine, dimension, m2Carton, pcs, prix, prixAchat,
        stock, color, colorName, image, finition, choix, categorie, description
    )

    fun ProductEntity.toModel() = TileProduct(
        id, name, usine, dimension, m2Carton, pcs, prix, prixAchat,
        stock, color, colorName, image, finition, choix, categorie, description
    )

    fun Customer.toEntity() = CustomerEntity(id, nom, tel, adresse, dette, totalAchats)
    fun CustomerEntity.toModel() = Customer(id, nom, tel, adresse, dette, totalAchats)

    fun Supplier.toEntity() = SupplierEntity(
        id, nom, contact, tel, adresse, usineRepresentee, soldeDette, totalAchats
    )

    fun SupplierEntity.toModel() = Supplier(
        id, nom, contact, tel, adresse, usineRepresentee, soldeDette, totalAchats
    )

    fun SupplierMovement.toEntity() = SupplierMovementEntity(
        id, supplierId, date, type, montant, motif, pieceJointeRef, auteur
    )

    fun SupplierMovementEntity.toModel() = SupplierMovement(
        id, supplierId, date, type, montant, motif, pieceJointeRef, auteur
    )

    fun CaisseMovement.toEntity() = CaisseMovementEntity(
        id, date, type, motif, montant, auteur, referenceFacture
    )

    fun CaisseMovementEntity.toModel() = CaisseMovement(
        id, date, type, motif, montant, auteur, referenceFacture
    )

    fun DailyCaisseCount.toEntity() = DailyCaisseCountEntity(
        id, date, magasin, auteur, fondOuverture, ventesEspecesJour,
        entreesDiversJour, sortiesJour, theorique, compteReel, ecart, notes
    )

    fun DailyCaisseCountEntity.toModel() = DailyCaisseCount(
        id, date, magasin, auteur, fondOuverture, ventesEspecesJour,
        entreesDiversJour, sortiesJour, theorique, compteReel, ecart, notes
    )

    fun DepotNotification.toEntity() = NotificationEntity(
        id, invoiceId, reference, date, vendeur, magasin, client, clientTel,
        totalNet, m2Total, cartonsTotal, articlesSummary, read
    )

    fun NotificationEntity.toModel() = DepotNotification(
        id, invoiceId, reference, date, vendeur, magasin, client, clientTel,
        totalNet, m2Total, cartonsTotal, articlesSummary, read
    )

    fun StaffUser.toEntity() = StaffUserEntity(
        id = id,
        name = name,
        role = role,
        pin = pin,
        magasin = magasin,
        allowedTabsJson = JSONArray(allowedTabs).toString(),
        canEditInvoices = canEditInvoices,
        canDeleteInvoices = canDeleteInvoices,
        canModifyStock = canModifyStock,
        canViewSuppliers = canViewSuppliers,
        canViewCaisse = canViewCaisse,
        canManageUsers = canManageUsers,
        active = active
    )

    fun StaffUserEntity.toModel(): StaffUser {
        val arr = runCatching { JSONArray(allowedTabsJson) }.getOrDefault(JSONArray())
        val tabs = List(arr.length()) { arr.optString(it) }.filter { it.isNotBlank() }
        return StaffUser(
            id, name, role, pin, magasin, tabs,
            canEditInvoices, canDeleteInvoices, canModifyStock,
            canViewSuppliers, canViewCaisse, canManageUsers, active
        )
    }

    fun cartItemsToJson(items: List<CartItem>): String {
        val arr = JSONArray()
        items.forEach { item ->
            arr.put(JSONObject().apply {
                put("id", item.id)
                put("productId", item.productId)
                put("name", item.name)
                put("usine", item.usine)
                put("dimension", item.dimension)
                put("m2Carton", item.m2Carton)
                put("pcs", item.pcs)
                put("prixUnitaire", item.prixUnitaire)
                put("prixAchat", item.prixAchat)
                put("cartons", item.cartons)
                put("pieces", item.pieces)
                put("m2Total", item.m2Total)
                put("sousTotal", item.sousTotal)
                put("bain", item.bain)
                put("calibre", item.calibre)
                put("color", item.color)
                put("colorName", item.colorName)
                put("image", item.image)
                put("prixPiece", item.prixPiece)
            })
        }
        return arr.toString()
    }

    fun jsonToCartItems(json: String): List<CartItem> {
        val arr = runCatching { JSONArray(json) }.getOrNull() ?: return emptyList()
        return buildList {
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                add(
                    CartItem(
                        id = o.optString("id"),
                        productId = o.optString("productId"),
                        name = o.optString("name"),
                        usine = o.optString("usine"),
                        dimension = o.optString("dimension"),
                        m2Carton = o.optDouble("m2Carton", 1.0),
                        pcs = o.optInt("pcs", 1),
                        prixUnitaire = o.optDouble("prixUnitaire", 0.0),
                        prixAchat = o.optDouble("prixAchat", 0.0),
                        cartons = o.optInt("cartons", 0),
                        pieces = o.optInt("pieces", 0),
                        m2Total = o.optDouble("m2Total", 0.0),
                        sousTotal = o.optDouble("sousTotal", 0.0),
                        bain = o.optString("bain"),
                        calibre = o.optString("calibre"),
                        color = o.optString("color", "#94a3b8"),
                        colorName = o.optString("colorName"),
                        image = o.optString("image"),
                        prixPiece = o.optDouble("prixPiece", 0.0)
                    )
                )
            }
        }
    }

    fun freeLinesToJson(lines: List<FreeLineItem>): String {
        val arr = JSONArray()
        lines.forEach { l ->
            arr.put(JSONObject().apply {
                put("id", l.id)
                put("designation", l.designation)
                put("quantite", l.quantite)
                put("unite", l.unite)
                put("prixUnitaire", l.prixUnitaire)
                put("sousTotal", l.sousTotal)
            })
        }
        return arr.toString()
    }

    fun jsonToFreeLines(json: String): List<FreeLineItem> {
        val arr = runCatching { JSONArray(json) }.getOrNull() ?: return emptyList()
        return buildList {
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                add(
                    FreeLineItem(
                        id = o.optString("id"),
                        designation = o.optString("designation"),
                        quantite = o.optDouble("quantite", 1.0),
                        unite = o.optString("unite", "u"),
                        prixUnitaire = o.optDouble("prixUnitaire", 0.0),
                        sousTotal = o.optDouble("sousTotal", 0.0)
                    )
                )
            }
        }
    }

    fun clientToJson(client: InvoiceClient): String = JSONObject().apply {
        put("id", client.id ?: JSONObject.NULL)
        put("nom", client.nom)
        put("tel", client.tel)
        put("adresse", client.adresse)
    }.toString()

    fun jsonToClient(json: String): InvoiceClient {
        val o = runCatching { JSONObject(json) }.getOrNull() ?: return InvoiceClient(nom = "Client Comptoir")
        val idVal = if (o.isNull("id")) null else o.optString("id").takeIf { it.isNotBlank() }
        return InvoiceClient(
            id = idVal,
            nom = o.optString("nom", "Client Comptoir"),
            tel = o.optString("tel", ""),
            adresse = o.optString("adresse", "")
        )
    }

    fun Invoice.toEntity() = InvoiceEntity(
        id = id,
        reference = reference,
        date = date,
        vendeur = vendeur,
        magasin = magasin,
        clientJson = clientToJson(client),
        articlesJson = cartItemsToJson(articles),
        lignesLibresJson = freeLinesToJson(lignesLibres),
        totalBrut = totalBrut,
        remise = remise,
        totalNet = totalNet,
        montantPaye = montantPaye,
        resteAPayer = resteAPayer,
        modePaiement = modePaiement,
        statut = statut,
        notes = notes
    )

    fun InvoiceEntity.toModel() = Invoice(
        id = id,
        reference = reference,
        date = date,
        vendeur = vendeur,
        magasin = magasin,
        client = jsonToClient(clientJson),
        articles = jsonToCartItems(articlesJson),
        lignesLibres = jsonToFreeLines(lignesLibresJson),
        totalBrut = totalBrut,
        remise = remise,
        totalNet = totalNet,
        montantPaye = montantPaye,
        resteAPayer = resteAPayer,
        modePaiement = modePaiement,
        statut = statut,
        notes = notes
    )

    fun storeInfoToJson(info: StoreInfo): String = JSONObject().apply {
        put("nom", info.nom)
        put("nomArabe", info.nomArabe)
        put("slogan", info.slogan)
        put("adresse", info.adresse)
        put("tel", info.tel)
        put("tel2", info.tel2)
        put("email", info.email)
        put("rc", info.rc)
        put("nif", info.nif)
        put("nis", info.nis)
        put("ai", info.ai)
    }.toString()

    fun jsonToStoreInfo(json: String, fallback: StoreInfo): StoreInfo {
        val o = runCatching { JSONObject(json) }.getOrNull() ?: return fallback
        return StoreInfo(
            nom = o.optString("nom", fallback.nom),
            nomArabe = o.optString("nomArabe", fallback.nomArabe),
            slogan = o.optString("slogan", fallback.slogan),
            adresse = o.optString("adresse", fallback.adresse),
            tel = o.optString("tel", fallback.tel),
            tel2 = o.optString("tel2", fallback.tel2),
            email = o.optString("email", fallback.email),
            rc = o.optString("rc", fallback.rc),
            nif = o.optString("nif", fallback.nif),
            nis = o.optString("nis", fallback.nis),
            ai = o.optString("ai", fallback.ai)
        )
    }

    fun settingsToJson(s: AppSettings): String = JSONObject().apply {
        put("printFormat", s.printFormat)
        put("a5Density", s.a5Density)
        put("hideBainTeinteFacture", s.hideBainTeinteFacture)
        put("colorTheme", s.colorTheme)
        put("touchFriendlyMode", s.touchFriendlyMode)
        put("activeStore", s.activeStore)
        put("enableSoundAlerts", s.enableSoundAlerts)
    }.toString()

    fun jsonToSettings(json: String, fallback: AppSettings): AppSettings {
        val o = runCatching { JSONObject(json) }.getOrNull() ?: return fallback
        return AppSettings(
            printFormat = o.optString("printFormat", fallback.printFormat),
            a5Density = o.optString("a5Density", fallback.a5Density),
            hideBainTeinteFacture = o.optBoolean("hideBainTeinteFacture", fallback.hideBainTeinteFacture),
            colorTheme = o.optString("colorTheme", fallback.colorTheme),
            touchFriendlyMode = o.optBoolean("touchFriendlyMode", fallback.touchFriendlyMode),
            activeStore = o.optString("activeStore", fallback.activeStore),
            enableSoundAlerts = o.optBoolean("enableSoundAlerts", fallback.enableSoundAlerts)
        )
    }
}

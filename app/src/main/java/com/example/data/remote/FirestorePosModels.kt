package com.example.data.remote

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
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserAccountDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val email: String? = null,
    val displayName: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class TileProductDoc(
    @DocumentId val id: String = "",
    val name: String = "",
    val usine: String = "",
    val dimension: String = "",
    val m2Carton: Double = 1.0,
    val pcs: Int = 1,
    val prix: Double = 0.0,
    val prixAchat: Double = 0.0,
    val stock: Int = 0,
    val color: String = "#94a3b8",
    val colorName: String = "Gris Minéral",
    val image: String = "",
    val finition: String = "brillant",
    val choix: String = "1er Choix",
    val categorie: String = "sol",
    val description: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toDomain() = TileProduct(
        id = id,
        name = name,
        usine = usine,
        dimension = dimension,
        m2Carton = m2Carton,
        pcs = pcs,
        prix = prix,
        prixAchat = prixAchat,
        stock = stock,
        color = color,
        colorName = colorName,
        image = image,
        finition = finition,
        choix = choix,
        categorie = categorie,
        description = description
    )

    companion object {
        fun fromDomain(
            p: TileProduct,
            createdAt: Timestamp? = null,
            updatedAt: Timestamp? = null
        ) = TileProductDoc(
            id = p.id,
            name = p.name,
            usine = p.usine,
            dimension = p.dimension,
            m2Carton = p.m2Carton,
            pcs = p.pcs,
            prix = p.prix,
            prixAchat = p.prixAchat,
            stock = p.stock,
            color = p.color,
            colorName = p.colorName,
            image = p.image,
            finition = p.finition,
            choix = p.choix,
            categorie = p.categorie,
            description = p.description,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}

@IgnoreExtraProperties
data class InvoiceClientDoc(
    val id: String? = null,
    val nom: String = "Client Comptoir",
    val tel: String = "",
    val adresse: String = ""
) {
    fun toDomain() = InvoiceClient(id = id, nom = nom, tel = tel, adresse = adresse)

    companion object {
        fun fromDomain(c: InvoiceClient) = InvoiceClientDoc(
            id = c.id,
            nom = c.nom,
            tel = c.tel,
            adresse = c.adresse
        )
    }
}

@IgnoreExtraProperties
data class CartItemDoc(
    val id: String = "",
    val productId: String = "",
    val name: String = "",
    val usine: String = "",
    val dimension: String = "",
    val m2Carton: Double = 1.0,
    val pcs: Int = 1,
    val prixUnitaire: Double = 0.0,
    val prixAchat: Double = 0.0,
    val cartons: Int = 0,
    val pieces: Int = 0,
    val m2Total: Double = 0.0,
    val sousTotal: Double = 0.0,
    val bain: String = "",
    val calibre: String = "",
    val color: String = "#94a3b8",
    val colorName: String = "",
    val image: String = "",
    val prixPiece: Double = 0.0
) {
    fun toDomain() = CartItem(
        id = id,
        productId = productId,
        name = name,
        usine = usine,
        dimension = dimension,
        m2Carton = m2Carton,
        pcs = pcs,
        prixUnitaire = prixUnitaire,
        prixAchat = prixAchat,
        cartons = cartons,
        pieces = pieces,
        m2Total = m2Total,
        sousTotal = sousTotal,
        bain = bain,
        calibre = calibre,
        color = color,
        colorName = colorName,
        image = image,
        prixPiece = prixPiece
    )

    companion object {
        fun fromDomain(c: CartItem) = CartItemDoc(
            id = c.id,
            productId = c.productId,
            name = c.name,
            usine = c.usine,
            dimension = c.dimension,
            m2Carton = c.m2Carton,
            pcs = c.pcs,
            prixUnitaire = c.prixUnitaire,
            prixAchat = c.prixAchat,
            cartons = c.cartons,
            pieces = c.pieces,
            m2Total = c.m2Total,
            sousTotal = c.sousTotal,
            bain = c.bain,
            calibre = c.calibre,
            color = c.color,
            colorName = c.colorName,
            image = c.image,
            prixPiece = c.prixPiece
        )
    }
}

@IgnoreExtraProperties
data class FreeLineItemDoc(
    val id: String = "",
    val designation: String = "",
    val quantite: Double = 1.0,
    val unite: String = "u",
    val prixUnitaire: Double = 0.0,
    val sousTotal: Double = 0.0
) {
    fun toDomain() = FreeLineItem(
        id = id,
        designation = designation,
        quantite = quantite,
        unite = unite,
        prixUnitaire = prixUnitaire,
        sousTotal = sousTotal
    )

    companion object {
        fun fromDomain(f: FreeLineItem) = FreeLineItemDoc(
            id = f.id,
            designation = f.designation,
            quantite = f.quantite,
            unite = f.unite,
            prixUnitaire = f.prixUnitaire,
            sousTotal = f.sousTotal
        )
    }
}

@IgnoreExtraProperties
data class InvoiceDoc(
    @DocumentId val id: String = "",
    val reference: String = "",
    val date: String = "",
    val vendeur: String = "",
    val magasin: String = "",
    val client: InvoiceClientDoc = InvoiceClientDoc(),
    val articles: List<CartItemDoc> = emptyList(),
    val lignesLibres: List<FreeLineItemDoc> = emptyList(),
    val totalBrut: Double = 0.0,
    val remise: Double = 0.0,
    val totalNet: Double = 0.0,
    val montantPaye: Double = 0.0,
    val resteAPayer: Double = 0.0,
    val modePaiement: String = "Espèces",
    val statut: String = "Payé",
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toDomain() = Invoice(
        id = id,
        reference = reference,
        date = date,
        vendeur = vendeur,
        magasin = magasin,
        client = client.toDomain(),
        articles = articles.map { it.toDomain() },
        lignesLibres = lignesLibres.map { it.toDomain() },
        totalBrut = totalBrut,
        remise = remise,
        totalNet = totalNet,
        montantPaye = montantPaye,
        resteAPayer = resteAPayer,
        modePaiement = modePaiement,
        statut = statut,
        notes = notes
    )

    companion object {
        fun fromDomain(
            inv: Invoice,
            createdAt: Timestamp? = null,
            updatedAt: Timestamp? = null
        ) = InvoiceDoc(
            id = inv.id,
            reference = inv.reference,
            date = inv.date,
            vendeur = inv.vendeur,
            magasin = inv.magasin,
            client = InvoiceClientDoc.fromDomain(inv.client),
            articles = inv.articles.map { CartItemDoc.fromDomain(it) },
            lignesLibres = inv.lignesLibres.map { FreeLineItemDoc.fromDomain(it) },
            totalBrut = inv.totalBrut,
            remise = inv.remise,
            totalNet = inv.totalNet,
            montantPaye = inv.montantPaye,
            resteAPayer = inv.resteAPayer,
            modePaiement = inv.modePaiement,
            statut = inv.statut,
            notes = inv.notes,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}

@IgnoreExtraProperties
data class CustomerDoc(
    @DocumentId val id: String = "",
    val nom: String = "",
    val tel: String = "",
    val adresse: String = "",
    val dette: Double = 0.0,
    val totalAchats: Double = 0.0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toDomain() = Customer(id = id, nom = nom, tel = tel, adresse = adresse, dette = dette, totalAchats = totalAchats)

    companion object {
        fun fromDomain(c: Customer, createdAt: Timestamp? = null, updatedAt: Timestamp? = null) = CustomerDoc(
            id = c.id,
            nom = c.nom,
            tel = c.tel,
            adresse = c.adresse,
            dette = c.dette,
            totalAchats = c.totalAchats,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}

@IgnoreExtraProperties
data class SupplierDoc(
    @DocumentId val id: String = "",
    val nom: String = "",
    val contact: String = "",
    val tel: String = "",
    val adresse: String = "",
    val usineRepresentee: String = "",
    val soldeDette: Double = 0.0,
    val totalAchats: Double = 0.0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toDomain() = Supplier(
        id = id,
        nom = nom,
        contact = contact,
        tel = tel,
        adresse = adresse,
        usineRepresentee = usineRepresentee,
        soldeDette = soldeDette,
        totalAchats = totalAchats
    )

    companion object {
        fun fromDomain(s: Supplier, createdAt: Timestamp? = null, updatedAt: Timestamp? = null) = SupplierDoc(
            id = s.id,
            nom = s.nom,
            contact = s.contact,
            tel = s.tel,
            adresse = s.adresse,
            usineRepresentee = s.usineRepresentee,
            soldeDette = s.soldeDette,
            totalAchats = s.totalAchats,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}

@IgnoreExtraProperties
data class SupplierMovementDoc(
    @DocumentId val id: String = "",
    val supplierId: String = "",
    val date: String = "",
    val type: String = "versement",
    val montant: Double = 0.0,
    val motif: String = "",
    val pieceJointeRef: String? = null,
    val auteur: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toDomain() = SupplierMovement(
        id = id,
        supplierId = supplierId,
        date = date,
        type = type,
        montant = montant,
        motif = motif,
        pieceJointeRef = pieceJointeRef,
        auteur = auteur
    )

    companion object {
        fun fromDomain(m: SupplierMovement, createdAt: Timestamp? = null, updatedAt: Timestamp? = null) =
            SupplierMovementDoc(
                id = m.id,
                supplierId = m.supplierId,
                date = m.date,
                type = m.type,
                montant = m.montant,
                motif = m.motif,
                pieceJointeRef = m.pieceJointeRef,
                auteur = m.auteur,
                createdAt = createdAt,
                updatedAt = updatedAt
            )
    }
}

@IgnoreExtraProperties
data class CaisseMovementDoc(
    @DocumentId val id: String = "",
    val date: String = "",
    val type: String = "entree",
    val motif: String = "",
    val montant: Double = 0.0,
    val auteur: String = "",
    val referenceFacture: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toDomain() = CaisseMovement(
        id = id,
        date = date,
        type = type,
        motif = motif,
        montant = montant,
        auteur = auteur,
        referenceFacture = referenceFacture
    )

    companion object {
        fun fromDomain(m: CaisseMovement, createdAt: Timestamp? = null, updatedAt: Timestamp? = null) =
            CaisseMovementDoc(
                id = m.id,
                date = m.date,
                type = m.type,
                motif = m.motif,
                montant = m.montant,
                auteur = m.auteur,
                referenceFacture = m.referenceFacture,
                createdAt = createdAt,
                updatedAt = updatedAt
            )
    }
}

@IgnoreExtraProperties
data class DailyCaisseCountDoc(
    @DocumentId val id: String = "",
    val date: String = "",
    val magasin: String = "",
    val auteur: String = "",
    val fondOuverture: Double = 0.0,
    val ventesEspecesJour: Double = 0.0,
    val entreesDiversJour: Double = 0.0,
    val sortiesJour: Double = 0.0,
    val theorique: Double = 0.0,
    val compteReel: Double = 0.0,
    val ecart: Double = 0.0,
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toDomain() = DailyCaisseCount(
        id = id,
        date = date,
        magasin = magasin,
        auteur = auteur,
        fondOuverture = fondOuverture,
        ventesEspecesJour = ventesEspecesJour,
        entreesDiversJour = entreesDiversJour,
        sortiesJour = sortiesJour,
        theorique = theorique,
        compteReel = compteReel,
        ecart = ecart,
        notes = notes
    )

    companion object {
        fun fromDomain(c: DailyCaisseCount, createdAt: Timestamp? = null, updatedAt: Timestamp? = null) =
            DailyCaisseCountDoc(
                id = c.id,
                date = c.date,
                magasin = c.magasin,
                auteur = c.auteur,
                fondOuverture = c.fondOuverture,
                ventesEspecesJour = c.ventesEspecesJour,
                entreesDiversJour = c.entreesDiversJour,
                sortiesJour = c.sortiesJour,
                theorique = c.theorique,
                compteReel = c.compteReel,
                ecart = c.ecart,
                notes = c.notes,
                createdAt = createdAt,
                updatedAt = updatedAt
            )
    }
}

@IgnoreExtraProperties
data class StaffUserDoc(
    @DocumentId val id: String = "",
    val name: String = "",
    val role: String = "vendeur",
    val pin: String = "0000",
    val magasin: String = "Depot Remchi",
    val allowedTabs: List<String> = listOf("pos"),
    val canEditInvoices: Boolean = false,
    val canDeleteInvoices: Boolean = false,
    val canModifyStock: Boolean = false,
    val canViewSuppliers: Boolean = false,
    val canViewCaisse: Boolean = false,
    val canManageUsers: Boolean = false,
    val active: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toDomain() = StaffUser(
        id = id,
        name = name,
        role = role,
        pin = pin,
        magasin = magasin,
        allowedTabs = allowedTabs,
        canEditInvoices = canEditInvoices,
        canDeleteInvoices = canDeleteInvoices,
        canModifyStock = canModifyStock,
        canViewSuppliers = canViewSuppliers,
        canViewCaisse = canViewCaisse,
        canManageUsers = canManageUsers,
        active = active
    )

    companion object {
        fun fromDomain(u: StaffUser, createdAt: Timestamp? = null, updatedAt: Timestamp? = null) = StaffUserDoc(
            id = u.id,
            name = u.name,
            role = u.role,
            pin = u.pin,
            magasin = u.magasin,
            allowedTabs = u.allowedTabs,
            canEditInvoices = u.canEditInvoices,
            canDeleteInvoices = u.canDeleteInvoices,
            canModifyStock = u.canModifyStock,
            canViewSuppliers = u.canViewSuppliers,
            canViewCaisse = u.canViewCaisse,
            canManageUsers = u.canManageUsers,
            active = u.active,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}

@IgnoreExtraProperties
data class DepotNotificationDoc(
    @DocumentId val id: String = "",
    val invoiceId: String = "",
    val reference: String = "",
    val date: String = "",
    val vendeur: String = "",
    val magasin: String = "",
    val client: String = "",
    val clientTel: String = "",
    val totalNet: Double = 0.0,
    val m2Total: Double = 0.0,
    val cartonsTotal: Int = 0,
    val articlesSummary: String = "",
    val read: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toDomain() = DepotNotification(
        id = id,
        invoiceId = invoiceId,
        reference = reference,
        date = date,
        vendeur = vendeur,
        magasin = magasin,
        client = client,
        clientTel = clientTel,
        totalNet = totalNet,
        m2Total = m2Total,
        cartonsTotal = cartonsTotal,
        articlesSummary = articlesSummary,
        read = read
    )

    companion object {
        fun fromDomain(n: DepotNotification, createdAt: Timestamp? = null, updatedAt: Timestamp? = null) =
            DepotNotificationDoc(
                id = n.id,
                invoiceId = n.invoiceId,
                reference = n.reference,
                date = n.date,
                vendeur = n.vendeur,
                magasin = n.magasin,
                client = n.client,
                clientTel = n.clientTel,
                totalNet = n.totalNet,
                m2Total = n.m2Total,
                cartonsTotal = n.cartonsTotal,
                articlesSummary = n.articlesSummary,
                read = n.read,
                createdAt = createdAt,
                updatedAt = updatedAt
            )
    }
}

@IgnoreExtraProperties
data class StoreInfoDoc(
    @DocumentId val id: String = "default",
    val nom: String = "",
    val nomArabe: String = "",
    val slogan: String = "",
    val adresse: String = "",
    val tel: String = "",
    val tel2: String = "",
    val email: String = "",
    val rc: String = "",
    val nif: String = "",
    val nis: String = "",
    val ai: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toDomain() = StoreInfo(
        nom = nom,
        nomArabe = nomArabe,
        slogan = slogan,
        adresse = adresse,
        tel = tel,
        tel2 = tel2,
        email = email,
        rc = rc,
        nif = nif,
        nis = nis,
        ai = ai
    )

    companion object {
        fun fromDomain(s: StoreInfo, createdAt: Timestamp? = null, updatedAt: Timestamp? = null) = StoreInfoDoc(
            id = "default",
            nom = s.nom,
            nomArabe = s.nomArabe,
            slogan = s.slogan,
            adresse = s.adresse,
            tel = s.tel,
            tel2 = s.tel2,
            email = s.email,
            rc = s.rc,
            nif = s.nif,
            nis = s.nis,
            ai = s.ai,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}

@IgnoreExtraProperties
data class AppSettingsDoc(
    @DocumentId val id: String = "default",
    val printFormat: String = "A4",
    val a5Density: String = "compact",
    val hideBainTeinteFacture: Boolean = true,
    val colorTheme: String = "ceramic_blue",
    val touchFriendlyMode: Boolean = true,
    val activeStore: String = "Depot Remchi",
    val enableSoundAlerts: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toDomain() = AppSettings(
        printFormat = printFormat,
        a5Density = a5Density,
        hideBainTeinteFacture = hideBainTeinteFacture,
        colorTheme = colorTheme,
        touchFriendlyMode = touchFriendlyMode,
        activeStore = activeStore,
        enableSoundAlerts = enableSoundAlerts
    )

    companion object {
        fun fromDomain(s: AppSettings, createdAt: Timestamp? = null, updatedAt: Timestamp? = null) = AppSettingsDoc(
            id = "default",
            printFormat = s.printFormat,
            a5Density = s.a5Density,
            hideBainTeinteFacture = s.hideBainTeinteFacture,
            colorTheme = s.colorTheme,
            touchFriendlyMode = s.touchFriendlyMode,
            activeStore = s.activeStore,
            enableSoundAlerts = s.enableSoundAlerts,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}

package com.example.data.remote

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.AppSettings
import com.example.data.model.CartItem
import com.example.data.model.Customer
import com.example.data.model.Invoice
import com.example.data.model.InvoiceClient
import com.example.data.model.PosSeeds
import com.example.data.model.Supplier
import com.example.data.model.SupplierMovement
import com.example.data.model.TileProduct
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class PosFirestoreRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun ownerCanCreateUpdateObserveAndDeleteProductAndInvoice() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repo = FirestorePosRepository(firestore)

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.ensureUserAccount(aliceUid, ALICE_EMAIL, "Alice Owner")
        }

        val prodId = "p_test_${UUID.randomUUID().toString().take(8)}"
        val product = TileProduct(
            id = prodId,
            name = "60/60 BATI LUXOR TEST",
            usine = "BATI",
            dimension = "60×60",
            m2Carton = 1.44,
            pcs = 4,
            prix = 1200.0,
            prixAchat = 950.0,
            stock = 30,
            color = "#94a3b8",
            colorName = "Gris Minéral",
            finition = "rectifie",
            choix = "1er Choix",
            categorie = "sol",
            description = "Test tile"
        )
        withTimeout(DEFAULT_TIMEOUT_MS) { repo.upsertProduct(aliceUid, product) }
        withTimeout(DEFAULT_TIMEOUT_MS) { repo.updateProductStock(aliceUid, prodId, 25) }

        val observedProducts = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeProducts(aliceUid).first { list -> list.any { it.id == prodId && it.stock == 25 } }
        }
        assertTrue(observedProducts.any { it.id == prodId && it.stock == 25 })

        val invId = "inv_${UUID.randomUUID().toString().take(8)}"
        val invoice = Invoice(
            id = invId,
            reference = "KC-TEST-001",
            date = "2026-11-12T10:00:00.000Z",
            vendeur = "Kader",
            magasin = "Depot Remchi",
            client = InvoiceClient(nom = "Ahmed Benali", tel = "0550123456", adresse = "Remchi"),
            articles = listOf(
                CartItem(
                    id = "line_1",
                    productId = prodId,
                    name = product.name,
                    usine = product.usine,
                    dimension = product.dimension,
                    m2Carton = 1.44,
                    pcs = 4,
                    prixUnitaire = 1200.0,
                    prixAchat = 950.0,
                    cartons = 5,
                    pieces = 0,
                    m2Total = 7.2,
                    sousTotal = 8640.0
                )
            ),
            lignesLibres = emptyList(),
            totalBrut = 8640.0,
            remise = 0.0,
            totalNet = 8640.0,
            montantPaye = 8640.0,
            resteAPayer = 0.0,
            modePaiement = "Espèces",
            statut = "Payé",
            notes = ""
        )
        withTimeout(DEFAULT_TIMEOUT_MS) { repo.upsertInvoice(aliceUid, invoice) }
        val loadedInv = withTimeout(DEFAULT_TIMEOUT_MS) { repo.getInvoiceById(aliceUid, invId) }
        assertNotNull(loadedInv)
        assertEquals("KC-TEST-001", loadedInv?.reference)

        withTimeout(DEFAULT_TIMEOUT_MS) { repo.deleteInvoice(aliceUid, invId) }
    }

    @Test
    fun ownerCanWriteCustomerSupplierMovementAndSettings() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repo = FirestorePosRepository(firestore)

        val custId = "cust_${UUID.randomUUID().toString().take(8)}"
        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.upsertCustomer(aliceUid, Customer(custId, "Client Test", "0550112233", "Remchi", 1000.0, 5000.0))
        }

        val supId = "sup_${UUID.randomUUID().toString().take(8)}"
        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.upsertSupplier(aliceUid, Supplier(supId, "BCRAM Test", "Contact", "041000000", "Oran", "BCRAM", 50000.0, 100000.0))
            repo.createSupplierMovement(
                aliceUid,
                SupplierMovement(
                    id = "mov_${UUID.randomUUID().toString().take(8)}",
                    supplierId = supId,
                    date = "2026-11-12T10:00:00.000Z",
                    type = "versement",
                    montant = 20000.0,
                    motif = "Acompte test",
                    pieceJointeRef = null,
                    auteur = "Kader"
                )
            )
            repo.saveStoreInfo(aliceUid, PosSeeds.DEFAULT_STORE_INFO)
            repo.saveAppSettings(aliceUid, AppSettings())
        }
    }

    @Test
    fun crossUserAccessFailsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = FirestorePosRepository(firestore)
        val invId = "inv_${UUID.randomUUID().toString().take(8)}"
        val invoice = Invoice(
            id = invId,
            reference = "KC-PRIV-001",
            date = "2026-11-12T10:00:00.000Z",
            vendeur = "Kader",
            magasin = "Depot Remchi",
            client = InvoiceClient(nom = "Private Client"),
            articles = emptyList(),
            lignesLibres = emptyList(),
            totalBrut = 1000.0,
            remise = 0.0,
            totalNet = 1000.0,
            montantPaye = 1000.0,
            resteAPayer = 0.0,
            modePaiement = "Espèces",
            statut = "Payé"
        )
        withTimeout(DEFAULT_TIMEOUT_MS) { aliceRepo.upsertInvoice(aliceUid, invoice) }

        signInTestUser(BOB_EMAIL)
        val bobRepo = FirestorePosRepository(firestore)
        var captured: FirebaseFirestoreException? = null
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) { bobRepo.getInvoiceById(aliceUid, invId) }
        } catch (e: Exception) {
            captured = e.unwrapFirestoreException()
        }
        assertNotNull(captured)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, captured?.code)
    }

    @Test
    fun unauthenticatedObserveProductsFailsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repo = FirestorePosRepository(firestore)
        var captured: FirebaseFirestoreException? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) { repo.observeProducts("alice_unauth_test").first() }
        } catch (e: Exception) {
            captured = e.unwrapFirestoreException()
        }
        assertNotNull(captured)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, captured?.code)
    }

    private companion object {
        const val ALICE_EMAIL = "alice.pos@test.com"
        const val BOB_EMAIL = "bob.pos@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}

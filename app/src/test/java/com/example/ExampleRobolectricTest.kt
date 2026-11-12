package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.CartItem
import com.example.data.model.Invoice
import com.example.data.model.InvoiceClient
import com.example.data.repository.PosRepository
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Kader POS", appName)
  }

  @Test
  fun `catalog seed asset parses cleanly`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val raw = context.assets.open("catalog_seed.json").bufferedReader().use { it.readText() }
    val arr = JSONArray(raw)
    assertTrue("Expected seeded ceramic catalog items", arr.length() > 300)
  }

  @Test
  fun `offline repository seeds catalog and saves cash invoice cleanly`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    try {
      val repo = PosRepository(context, db)
      repo.ensureSeeded()
      val products = repo.getProductsSnapshot()
      val users = repo.getUsersSnapshot()
      assertTrue("Expected catalog products seeded offline", products.size > 300)
      assertTrue("Expected staff users seeded offline", users.isNotEmpty())

      val sample = products.first { it.stock >= 5 }
      val line = CartItem(
        id = "line_offline_1",
        productId = sample.id,
        name = sample.name,
        usine = sample.usine,
        dimension = sample.dimension,
        m2Carton = sample.m2Carton,
        pcs = sample.pcs,
        prixUnitaire = sample.prix,
        prixAchat = sample.prixAchat,
        cartons = 2,
        pieces = 0,
        m2Total = sample.m2Carton * 2,
        sousTotal = sample.m2Carton * 2 * sample.prix,
        bain = "",
        calibre = "",
        color = sample.color,
        colorName = sample.colorName,
        image = sample.image,
        prixPiece = 0.0
      )
      val inv = Invoice(
        id = "inv_offline_test_1",
        reference = "KC-TEST-001",
        date = "2026-11-12T12:00:00.000Z",
        vendeur = users.first().name,
        magasin = "Magasin Principal",
        client = InvoiceClient(nom = "Client Comptoir"),
        articles = listOf(line),
        lignesLibres = emptyList(),
        totalBrut = line.sousTotal,
        remise = 0.0,
        totalNet = line.sousTotal,
        montantPaye = line.sousTotal,
        resteAPayer = 0.0,
        modePaiement = "Espèces",
        statut = "Payé",
        notes = "Test vente hors-ligne"
      )
      val commit = repo.saveNewInvoice(inv)
      assertEquals(sample.stock - 2, commit.updatedProductStocks[sample.id])
      assertEquals(1, repo.getInvoicesSnapshot().size)
    } finally {
      db.close()
    }
  }
}

const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");
const rulesPkgPath = require.resolve("@firebase/rules-unit-testing");
const firebase = require(require.resolve("firebase/compat/app", { paths: [rulesPkgPath] }));
require(require.resolve("firebase/compat/firestore", { paths: [rulesPkgPath] }));

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

const serverTs = () => firebase.firestore.FieldValue.serverTimestamp();

function validProductPayload() {
  return {
    name: "60/60 BATI LUXOR",
    usine: "BATI",
    dimension: "60×60",
    m2Carton: 1.44,
    pcs: 4,
    prix: 1150,
    prixAchat: 950,
    stock: 20,
    color: "#94a3b8",
    colorName: "Gris Minéral",
    image: "",
    finition: "rectifie",
    choix: "1er Choix",
    categorie: "sol",
    description: "Carrelage haute qualité",
    createdAt: serverTs(),
    updatedAt: serverTs(),
  };
}

function validInvoicePayload() {
  return {
    reference: "KC-261112-001",
    date: "2026-11-12T10:00:00.000Z",
    vendeur: "Kader (Patron)",
    magasin: "Depot Remchi",
    client: { nom: "Ahmed Benali", tel: "0550123456", adresse: "Remchi" },
    articles: [{ productId: "p_1", cartons: 5, m2Total: 7.2, sousTotal: 8280 }],
    lignesLibres: [],
    totalBrut: 8280,
    remise: 0,
    totalNet: 8280,
    montantPaye: 8280,
    resteAPayer: 0,
    modePaiement: "Espèces",
    statut: "Payé",
    notes: "",
    createdAt: serverTs(),
    updatedAt: serverTs(),
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write any user collections", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("products").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("invoices").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("products").doc("p_1").set(validProductPayload())
  );
});

test("Owner can create, read, list, update, and delete own products", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const prodRef = aliceDb.collection("users").doc(ALICE_UID).collection("products").doc("p_60_60_bati_luxor");

  await assertSucceeds(prodRef.set(validProductPayload()));
  await assertSucceeds(prodRef.get());
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("products").get());
  await assertSucceeds(prodRef.update({ stock: 15, updatedAt: serverTs() }));
  await assertSucceeds(prodRef.delete());
});

test("Cross-user isolation blocks Bob from reading or writing Alice POS data", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("invoices").doc("inv_1").set(validInvoicePayload())
  );

  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("invoices").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("invoices").doc("inv_1").get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("invoices").doc("inv_2").set(validInvoicePayload())
  );
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("invoices").doc("inv_1").delete());
});

test("Shadow update with ghost field and invalid ID poisoning are rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const badProd = { ...validProductPayload(), isAdminBypass: true };

  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("products").doc("p_1").set(badProd)
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("products").doc("bad.id$with!chars").set(validProductPayload())
  );
});

test("Supplier movement enforces parent supplier existence", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const supRef = aliceDb.collection("users").doc(ALICE_UID).collection("suppliers").doc("f1");
  const movRef = aliceDb.collection("users").doc(ALICE_UID).collection("supplier_movements").doc("mov_1");

  const movPayload = {
    supplierId: "f1",
    date: "2026-11-12T10:00:00.000Z",
    type: "versement",
    montant: 50000,
    motif: "Acompte BCRAM",
    auteur: "Kader",
    createdAt: serverTs(),
    updatedAt: serverTs(),
  };

  // Parent supplier does not exist yet -> must fail
  await assertFails(movRef.set(movPayload));

  // Create parent supplier -> movement now succeeds
  await assertSucceeds(
    supRef.set({
      nom: "BCRAM",
      contact: "Direction",
      tel: "041556677",
      adresse: "Oran",
      usineRepresentee: "BCRAM",
      soldeDette: 100000,
      totalAchats: 500000,
      createdAt: serverTs(),
      updatedAt: serverTs(),
    })
  );
  await assertSucceeds(movRef.set(movPayload));
});

test("Staff user and store config enforce strict enum and immutable createdAt", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const staffRef = aliceDb.collection("users").doc(ALICE_UID).collection("staff_users").doc("u_kader");

  await assertFails(
    staffRef.set({
      name: "Kader",
      role: "superhacker",
      pin: "1234",
      magasin: "Depot Remchi",
      allowedTabs: ["pos"],
      canEditInvoices: true,
      canDeleteInvoices: true,
      canModifyStock: true,
      canViewSuppliers: true,
      canViewCaisse: true,
      canManageUsers: true,
      active: true,
      createdAt: serverTs(),
      updatedAt: serverTs(),
    })
  );

  await assertSucceeds(
    staffRef.set({
      name: "Kader",
      role: "admin",
      pin: "1234",
      magasin: "Depot Remchi",
      allowedTabs: ["pos", "stock"],
      canEditInvoices: true,
      canDeleteInvoices: true,
      canModifyStock: true,
      canViewSuppliers: true,
      canViewCaisse: true,
      canManageUsers: true,
      active: true,
      createdAt: serverTs(),
      updatedAt: serverTs(),
    })
  );

  // Attempting to mutate createdAt on update must fail
  await assertFails(
    staffRef.update({
      createdAt: firebase.firestore.Timestamp.fromMillis(1000000),
      updatedAt: serverTs(),
    })
  );
});

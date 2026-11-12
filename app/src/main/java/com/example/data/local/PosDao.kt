package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PosDao {
    // Products
    @Query("SELECT * FROM products ORDER BY stock DESC, name ASC")
    fun observeProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products")
    suspend fun getProductsSnapshot(): List<ProductEntity>

    @Query("SELECT COUNT(*) FROM products")
    suspend fun countProducts(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(items: List<ProductEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProduct(item: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductById(id: String)

    @Query("DELETE FROM products")
    suspend fun clearProducts()

    @Query("UPDATE products SET stock = MAX(0, stock + :delta) WHERE id = :id")
    suspend fun adjustProductStock(id: String, delta: Int)

    @Query("UPDATE products SET stock = MAX(0, :newStock) WHERE id = :id")
    suspend fun setProductStock(id: String, newStock: Int)

    // Invoices
    @Query("SELECT * FROM invoices ORDER BY date DESC")
    fun observeInvoices(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices ORDER BY date DESC")
    suspend fun getInvoicesSnapshot(): List<InvoiceEntity>

    @Query("SELECT * FROM invoices WHERE id = :id LIMIT 1")
    suspend fun getInvoiceById(id: String): InvoiceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertInvoice(invoice: InvoiceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoices(invoices: List<InvoiceEntity>)

    @Query("DELETE FROM invoices WHERE id = :id")
    suspend fun deleteInvoiceById(id: String)

    @Query("DELETE FROM invoices")
    suspend fun clearInvoices()

    // Customers
    @Query("SELECT * FROM customers ORDER BY nom ASC")
    fun observeCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers")
    suspend fun getCustomersSnapshot(): List<CustomerEntity>

    @Query("SELECT COUNT(*) FROM customers")
    suspend fun countCustomers(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(items: List<CustomerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCustomer(item: CustomerEntity)

    @Query("DELETE FROM customers")
    suspend fun clearCustomers()

    // Suppliers & Movements
    @Query("SELECT * FROM suppliers ORDER BY nom ASC")
    fun observeSuppliers(): Flow<List<SupplierEntity>>

    @Query("SELECT * FROM suppliers")
    suspend fun getSuppliersSnapshot(): List<SupplierEntity>

    @Query("SELECT COUNT(*) FROM suppliers")
    suspend fun countSuppliers(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuppliers(items: List<SupplierEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSupplier(item: SupplierEntity)

    @Query("DELETE FROM suppliers")
    suspend fun clearSuppliers()

    @Query("SELECT * FROM supplier_movements ORDER BY date DESC")
    fun observeSupplierMovements(): Flow<List<SupplierMovementEntity>>

    @Query("SELECT * FROM supplier_movements ORDER BY date DESC")
    suspend fun getSupplierMovementsSnapshot(): List<SupplierMovementEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplierMovement(movement: SupplierMovementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplierMovements(movements: List<SupplierMovementEntity>)

    @Query("DELETE FROM supplier_movements")
    suspend fun clearSupplierMovements()

    // Caisse & Daily Counts
    @Query("SELECT * FROM caisse_movements ORDER BY date DESC")
    fun observeCaisseMovements(): Flow<List<CaisseMovementEntity>>

    @Query("SELECT * FROM caisse_movements ORDER BY date DESC")
    suspend fun getCaisseSnapshot(): List<CaisseMovementEntity>

    @Query("SELECT COUNT(*) FROM caisse_movements")
    suspend fun countCaisse(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCaisseMovement(movement: CaisseMovementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCaisseMovements(movements: List<CaisseMovementEntity>)

    @Query("DELETE FROM caisse_movements")
    suspend fun clearCaisse()

    @Query("SELECT * FROM daily_caisse_counts ORDER BY date DESC")
    fun observeDailyCaisseCounts(): Flow<List<DailyCaisseCountEntity>>

    @Query("SELECT * FROM daily_caisse_counts")
    suspend fun getDailyCaisseCountsSnapshot(): List<DailyCaisseCountEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyCaisseCount(count: DailyCaisseCountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyCaisseCounts(counts: List<DailyCaisseCountEntity>)

    @Query("DELETE FROM daily_caisse_counts")
    suspend fun clearDailyCaisseCounts()

    // Staff Users
    @Query("SELECT * FROM staff_users")
    fun observeUsers(): Flow<List<StaffUserEntity>>

    @Query("SELECT * FROM staff_users")
    suspend fun getUsersSnapshot(): List<StaffUserEntity>

    @Query("SELECT COUNT(*) FROM staff_users")
    suspend fun countUsers(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<StaffUserEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUser(user: StaffUserEntity)

    @Query("DELETE FROM staff_users WHERE id = :id")
    suspend fun deleteUserById(id: String)

    @Query("DELETE FROM staff_users")
    suspend fun clearUsers()

    // Notifications
    @Query("SELECT * FROM notifications ORDER BY date DESC")
    fun observeNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications ORDER BY date DESC")
    suspend fun getNotificationsSnapshot(): List<NotificationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Query("UPDATE notifications SET read = 1")
    suspend fun markAllNotificationsRead()

    @Query("DELETE FROM notifications")
    suspend fun clearNotifications()

    // Key-Value Store (Cart, Free Lines, StoreInfo, Settings, CurrentUserId)
    @Query("SELECT * FROM app_kv WHERE `key` = :key LIMIT 1")
    fun observeKv(key: String): Flow<AppKvEntity?>

    @Query("SELECT * FROM app_kv WHERE `key` = :key LIMIT 1")
    suspend fun getKv(key: String): AppKvEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putKv(entity: AppKvEntity)
}

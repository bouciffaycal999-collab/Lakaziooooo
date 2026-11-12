package com.example.auth

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

sealed interface AuthAttemptResult {
    data object Success : AuthAttemptResult
    data object Cancelled : AuthAttemptResult
    data object NoSavedCredential : AuthAttemptResult
    data class Error(val message: String) : AuthAttemptResult
}

object GoogleAuthManager {
    private const val TAG = "Auth"

    fun authStateFlow(auth: FirebaseAuth = Firebase.auth): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun attemptSilentSignIn(
        context: Context,
        credentialManager: CredentialManager = CredentialManager.create(context),
        auth: FirebaseAuth = Firebase.auth
    ): AuthAttemptResult {
        if (auth.currentUser != null) return AuthAttemptResult.Success
        val serverClientId = context.getString(R.string.default_web_client_id)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(serverClientId)
            .setAutoSelectEnabled(true)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val result = credentialManager.getCredential(context = context, request = request)
            signInWithGoogleCredential(result.credential, auth)
        } catch (e: CancellationException) {
            throw e
        } catch (_: NoCredentialException) {
            AuthAttemptResult.NoSavedCredential
        } catch (e: GetCredentialCancellationException) {
            Log.w(TAG, "Silent Google Sign-In cancelled: ${e.message}", e)
            AuthAttemptResult.Cancelled
        } catch (e: Exception) {
            Log.w(TAG, "Silent Google Sign-In skipped: ${e.message}", e)
            AuthAttemptResult.NoSavedCredential
        }
    }

    suspend fun signInInteractive(
        context: Context,
        credentialManager: CredentialManager = CredentialManager.create(context),
        auth: FirebaseAuth = Firebase.auth
    ): AuthAttemptResult {
        val serverClientId = context.getString(R.string.default_web_client_id)
        val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(serverClientId)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        return try {
            val result = credentialManager.getCredential(context = context, request = request)
            signInWithGoogleCredential(result.credential, auth)
        } catch (e: CancellationException) {
            throw e
        } catch (e: GetCredentialCancellationException) {
            Log.w(TAG, "Google Sign-In cancelled or dismissed: ${e.message}", e)
            AuthAttemptResult.Cancelled
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign-In failed: ${e.message}", e)
            AuthAttemptResult.Error(e.localizedMessage ?: "Échec de la connexion Google.")
        }
    }

    private suspend fun signInWithGoogleCredential(
        credential: Credential,
        auth: FirebaseAuth
    ): AuthAttemptResult {
        if (credential !is CustomCredential || credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            return AuthAttemptResult.Error("Type d'identifiant Google non reconnu.")
        }
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
        val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
        auth.signInWithCredential(firebaseCredential).await()
        return AuthAttemptResult.Success
    }

    fun signOut(
        credentialManager: CredentialManager,
        scope: CoroutineScope,
        auth: FirebaseAuth = Firebase.auth
    ) {
        scope.launch {
            withContext(NonCancellable) {
                try {
                    auth.signOut()
                    credentialManager.clearCredentialState(ClearCredentialStateRequest())
                } catch (e: Exception) {
                    Log.e(TAG, "Sign-out failed", e)
                }
            }
        }
    }
}

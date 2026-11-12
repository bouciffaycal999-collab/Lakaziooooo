package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.StaffUser
import com.example.ui.components.NonImeNumericKeypad
import com.example.ui.components.applyKeypadInput
import com.example.ui.theme.Amber500
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy950
import com.example.ui.theme.OnDark
import com.example.ui.theme.Rose400
import com.example.ui.theme.Rose950
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

@Composable
fun StaffLockScreen(
    users: List<StaffUser>,
    initialSelectedUser: StaffUser?,
    pinError: String?,
    onClearError: () -> Unit,
    onUnlock: (StaffUser, String) -> Unit
) {
    val activeUsers = remember(users) { users.filter { it.active } }
    var selectedUser by remember(activeUsers, initialSelectedUser) {
        mutableStateOf(
            activeUsers.find { it.id == initialSelectedUser?.id } ?: activeUsers.firstOrNull()
        )
    }
    var pinBuffer by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Slate950
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Navy950,
                    border = BorderStroke(2.dp, Amber500),
                    modifier = Modifier.size(68.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Verrouillé",
                            tint = Amber500,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "KADER CERAMIC POS",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Navy950,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Connexion Rapide : Nom + Code PIN",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Cyan400,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Name + Role selector pills (Name & PIN only — zero email)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(activeUsers, key = { it.id }) { user ->
                        val isSel = selectedUser?.id == user.id
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    selectedUser = user
                                    pinBuffer = ""
                                    onClearError()
                                }
                                .testTag("lock_user_${user.id}"),
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSel) Navy950 else Slate900,
                            border = BorderStroke(if (isSel) 2.dp else 1.dp, if (isSel) Amber500 else Slate700)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (isSel) Amber500 else Cyan400,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = user.name,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isSel) OnDark else Slate100,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = user.magasin,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSel) Amber500 else Slate400
                                )
                            }
                        }
                    }
                }

                // PIN dots display (zero IME)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = Slate900,
                    border = BorderStroke(1.5.dp, if (pinError != null) Rose400 else Slate700)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = selectedUser?.let { "Code PIN — ${it.name}" } ?: "Code PIN",
                            style = MaterialTheme.typography.titleMedium,
                            color = Navy950,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(4) { idx ->
                                val filled = idx < pinBuffer.length
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(if (filled) Amber500 else Slate850)
                                )
                            }
                        }
                        if (pinError != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Rose950
                            ) {
                                Text(
                                    text = pinError,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Rose400,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                NonImeNumericKeypad(
                    onKeyPress = { key ->
                        onClearError()
                        val (next, _) = applyKeypadInput(
                            current = pinBuffer,
                            key = key,
                            allowDecimal = false,
                            isPristine = false
                        )
                        val trimmed = next.take(4)
                        pinBuffer = trimmed
                        val user = selectedUser
                        if (user != null && trimmed.length == 4) {
                            onUnlock(user, trimmed)
                            if (trimmed != user.pin) {
                                pinBuffer = ""
                            }
                        }
                    },
                    allowDecimal = false,
                    onValidate = {
                        val user = selectedUser
                        if (user != null) {
                            onUnlock(user, pinBuffer)
                            if (pinBuffer != user.pin) pinBuffer = ""
                        }
                    },
                    validateLabel = "Déverrouiller (${selectedUser?.role?.uppercase() ?: "OK"})",
                    accentColor = Amber500
                )
            }
        }
    }
}

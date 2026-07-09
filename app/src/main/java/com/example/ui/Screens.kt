package com.example.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.data.ChatMessage
import com.example.data.GeminiAnalysisResult
import com.example.data.ScamReport
import com.example.viewmodel.ScamViewModel
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.WarningYellow
import com.example.ui.theme.DangerRed
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Helper functions and composables
@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    borderAlpha: Float = 0.12f,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.onSurface.copy(alpha = borderAlpha)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

// 1. Splash Screen
@Composable
fun SplashScreen(navController: NavController) {
    var startAnim by remember { mutableStateOf(false) }
    val progressAnim by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(durationMillis = 2000),
        label = "splashProgress"
    )

    LaunchedEffect(key1 = true) {
        startAnim = true
        kotlinx.coroutines.delay(2200)
        navController.navigate("home") {
            popUpTo("splash") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0B0E14),
                        Color(0xFF0F172A),
                        Color(0xFF1E293B)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF2563EB).copy(alpha = 0.4f),
                                    Color.Transparent
                                )
                            ),
                            radius = size.width
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "App Security Shield",
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(76.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "SCAM SHIELD AI",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.SansSerif
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Next-Gen Anti-Fraud & Phishing Radar",
                color = Color(0xFF94A3B8),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(60.dp))
            
            // Custom linear secure progress bar
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressAnim)
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF2563EB), Color(0xFF6366F1))
                            )
                        )
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Securing offline sandbox database...",
                color = Color(0xFF64748B),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

// 2. Home Dashboard Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDashboardScreen(
    viewModel: ScamViewModel,
    navController: NavController,
    onNavigateToScan: (String, String) -> Unit
) {
    val histories by viewModel.scanHistory.collectAsState()
    val isKeyReady = viewModel.isGeminiConfigured()
    val guardLevel by viewModel.shieldGuardLevel.collectAsState()
    var showGuardDialog by remember { mutableStateOf(false) }
    var searchInput by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("PHONE") }
    val keyboardController = LocalSoftwareKeyboardController.current

    val scanTypes = listOf(
        ScanTypeItem("PHONE", "Phone", Icons.Default.Phone, "Identify fraudulent numbers"),
        ScanTypeItem("WEBSITE", "Website", Icons.Default.Language, "Spot dangerous phishing URLs"),
        ScanTypeItem("EMAIL", "Email", Icons.Default.AlternateEmail, "Analyze deceptive senders"),
        ScanTypeItem("SMS", "SMS Message", Icons.Default.Sms, "Evaluate text messages"),
        ScanTypeItem("QR", "QR Code", Icons.Default.QrCode, "Decrypt hidden link contents"),
        ScanTypeItem("BANK", "Bank Details", Icons.Default.CreditCard, "Check fraudulent routing"),
        ScanTypeItem("SOCIAL", "Social Profile", Icons.Default.Person, "Scan fake accounts")
    )

    Scaffold(
        topBar = {
            TopSecurityBar(viewModel, navController)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Configure Shield Guard Dialog
            if (showGuardDialog) {
                AlertDialog(
                    onDismissRequest = { showGuardDialog = false },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Configure Shield Guard", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                    text = {
                        Column {
                            Text(
                                "Select the active protection mode for real-time sandbox analysis:",
                                fontSize = 13.sp,
                                modifier = Modifier.padding(bottom = 16.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Option 1: Maximum
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (guardLevel == "Maximum") MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        else Color.Transparent
                                    )
                                    .clickable { viewModel.setShieldGuardLevel("Maximum") }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Maximum Protection",
                                    tint = SafeGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Maximum Protection", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Full AI Cloud Analysis & heuristic sandbox scanning active.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                RadioButton(
                                    selected = (guardLevel == "Maximum"),
                                    onClick = { viewModel.setShieldGuardLevel("Maximum") }
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Option 2: Standard
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (guardLevel == "Standard") MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        else Color.Transparent
                                    )
                                    .clickable { viewModel.setShieldGuardLevel("Standard") }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = "Standard Protection",
                                    tint = WarningYellow,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Standard Protection", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Local signatures check and basic link checking.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                RadioButton(
                                    selected = (guardLevel == "Standard"),
                                    onClick = { viewModel.setShieldGuardLevel("Standard") }
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Option 3: Off
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (guardLevel == "Off") MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        else Color.Transparent
                                    )
                                    .clickable { viewModel.setShieldGuardLevel("Off") }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = "Suspended Protection",
                                    tint = DangerRed,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Suspended (Off)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Deactivate real-time background protection entirely.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                RadioButton(
                                    selected = (guardLevel == "Off"),
                                    onClick = { viewModel.setShieldGuardLevel("Off") }
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showGuardDialog = false }) {
                            Text("Done", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            // Hero Welcome Card with overall security posture
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { showGuardDialog = true }
                    .testTag("shield_guard_card"),
                borderAlpha = 0.2f
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (guardLevel) {
                                "Maximum" -> "Sandbox Protection: MAXIMUM"
                                "Standard" -> "Sandbox Protection: STANDARD"
                                else -> "Sandbox Protection: INACTIVE"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (guardLevel) {
                                "Maximum" -> SafeGreen
                                "Standard" -> WarningYellow
                                else -> DangerRed
                            },
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when (guardLevel) {
                                "Maximum" -> "ScamShield Guard Active"
                                "Standard" -> "Local Guard Shield Only"
                                else -> "Guard Shield Suspended"
                            },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = when (guardLevel) {
                                "Maximum" -> if (isKeyReady) "AI Mode enabled via Gemini Cloud Analysis." else "Local analysis fallback active. Set your API key for cloud scans."
                                "Standard" -> "Background signature checking. Local scanner active."
                                else -> "Deactivated. Tap this card to reactivate background analysis."
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = when (guardLevel) {
                            "Maximum" -> Icons.Default.Security
                            "Standard" -> Icons.Default.Security
                            else -> Icons.Default.Error
                        },
                        contentDescription = "Shield Guard Indicator",
                        tint = when (guardLevel) {
                            "Maximum" -> if (isKeyReady) SafeGreen else WarningYellow
                            "Standard" -> WarningYellow
                            else -> DangerRed.copy(alpha = 0.6f)
                        },
                        modifier = Modifier
                            .size(54.dp)
                            .padding(start = 12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Integrated Scanning Interface
            Text(
                text = "Instant Security Scan",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                // Horizontal scroll of Scan Types select chips
                Text(
                    text = "Select Scam category:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    scanTypes.forEach { item ->
                        val isSelected = selectedType == item.id
                        InputChip(
                            selected = isSelected,
                            onClick = { selectedType = item.id },
                            label = { Text(item.label) },
                            leadingIcon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = InputChipDefaults.inputChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Query Text Input Field
                OutlinedTextField(
                    value = searchInput,
                    onValueChange = { searchInput = it },
                    placeholder = {
                        Text(
                            when (selectedType) {
                                "PHONE" -> "Enter suspicious number (+1...)"
                                "WEBSITE" -> "Enter URL (e.g., scam-paypal.cc)"
                                "EMAIL" -> "Enter suspicious email address"
                                "SMS" -> "Paste received spam text message"
                                "QR" -> "Enter QR code contents or payload"
                                "BANK" -> "Enter routing or account details"
                                else -> "Enter fake social username"
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("scan_query_input"),
                    leadingIcon = {
                        Icon(
                            imageVector = scanTypes.first { it.id == selectedType }.icon,
                            contentDescription = "Selected category icon",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (searchInput.isNotEmpty()) {
                            IconButton(onClick = { searchInput = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Clear input text"
                                )
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (selectedType == "PHONE" || selectedType == "BANK") KeyboardType.Phone else KeyboardType.Text,
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            if (searchInput.isNotEmpty()) {
                                keyboardController?.hide()
                                onNavigateToScan(selectedType, searchInput)
                            }
                        }
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Scan Button
                Button(
                    onClick = {
                        if (searchInput.isNotEmpty()) {
                            keyboardController?.hide()
                            onNavigateToScan(selectedType, searchInput)
                        }
                    },
                    enabled = searchInput.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_scan_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Scan Icon",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Scan Now",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Grid of categories
            Text(
                text = "Scam Categories",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .height(240.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(scanTypes) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(105.dp)
                            .clickable {
                                selectedType = item.id
                                searchInput = ""
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedType == item.id)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (selectedType == item.id) MaterialTheme.colorScheme.primary else Color.Transparent
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (selectedType == item.id) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = item.label,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = item.desc,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Recent Scan Histories Block
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Scan History",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                if (histories.isNotEmpty()) {
                    TextButton(onClick = { viewModel.clearAllHistory() }) {
                        Text("Clear All")
                    }
                }
            }

            if (histories.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Empty History info icon",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No previous scans found.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Perform your first scan to compile records.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                histories.take(5).forEach { history ->
                    HistoryCardItem(history, viewModel, navController)
                }
            }
        }
    }
}

data class ScanTypeItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val desc: String
)

@Composable
fun HistoryCardItem(
    history: com.example.data.ScanHistory,
    viewModel: ScamViewModel,
    navController: NavController
) {
    val scoreColor = when (history.label) {
        "Safe" -> SafeGreen
        "Suspicious" -> WarningYellow
        else -> DangerRed
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable {
                viewModel.performScan(history.type, history.query)
                navController.navigate("analysis")
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(scoreColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${history.riskScore}",
                    color = scoreColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = history.query,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${history.type} | ${history.label}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { viewModel.deleteHistoryItem(history.id) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete item",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}


// 3. Scam Analysis Screen
@Composable
fun ScamAnalysisScreen(viewModel: ScamViewModel, navController: NavController) {
    val isScanning by viewModel.isScanning.collectAsState()
    val result by viewModel.scanResult.collectAsState()
    val isSaved by viewModel.isCurrentScanSaved.collectAsState()
    val activeType by viewModel.activeType.collectAsState()
    val activeQuery by viewModel.activeQuery.collectAsState()

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            androidx.compose.material3.TopAppBar(
                title = { Text("Analysis Verdict", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (result != null) {
                        IconButton(onClick = { viewModel.toggleSaveCurrentScan() }) {
                            Icon(
                                imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Save scan",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            if (isScanning) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(modifier = Modifier.size(54.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "ScamShield AI Inspecting...",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Evaluating meta signatures & threat databases",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (result == null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning icon",
                        modifier = Modifier.size(54.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No active scan loaded.",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val data = result!!
                val scoreColor = when (data.label) {
                    "Safe" -> SafeGreen
                    "Suspicious" -> WarningYellow
                    else -> DangerRed
                }

                // Animate score dial
                var animateTrigger by remember { mutableStateOf(false) }
                LaunchedEffect(key1 = true) {
                    animateTrigger = true
                }
                val scoreProgress by animateFloatAsState(
                    targetValue = if (animateTrigger) data.riskScore.toFloat() / 100f else 0f,
                    animationSpec = tween(1200),
                    label = "dialAnim"
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Risk score dial
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(170.dp)) {
                            // Background track
                            drawArc(
                                color = scoreColor.copy(alpha = 0.1f),
                                startAngle = 140f,
                                sweepAngle = 260f,
                                useCenter = false,
                                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                            )
                            // Animated active track
                            drawArc(
                                color = scoreColor,
                                startAngle = 140f,
                                sweepAngle = scoreProgress * 260f,
                                useCenter = false,
                                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${data.riskScore}",
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Black,
                                color = scoreColor
                            )
                            Text(
                                text = "RISK SCORE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = scoreColor.copy(alpha = 0.15f),
                                contentColor = scoreColor,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = data.label.uppercase(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Scanned Query Card
                    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Scanned item details",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = activeQuery,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 2
                        )
                        Text(
                            text = "Category: ${data.category} | Method: $activeType",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Threat Factors / Reasons
                    Text(
                        text = "Analysis Findings",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                        if (data.reasons.isEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Safe checklist icon", tint = SafeGreen)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("No suspicious parameters detected.", fontSize = 13.sp)
                            }
                        } else {
                            data.reasons.forEach { reason ->
                                Row(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.BugReport,
                                        contentDescription = "Reason bug bullet icon",
                                        tint = if (data.riskScore > 40) DangerRed else WarningYellow,
                                        modifier = Modifier
                                            .size(18.dp)
                                            .offset(y = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = reason,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Safety Actions / Recommendations
                    Text(
                        text = "Recommended Safety Measures",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                        data.recommendations.forEach { recommendation ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Recommendation check bullet",
                                    tint = SafeGreen,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .offset(y = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = recommendation,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Ask Assistant about this Result Shortcut
                    Button(
                        onClick = {
                            viewModel.sendChatMessage("Is this item secure? $activeQuery ($activeType) It has a risk score of ${data.riskScore} because of: ${data.reasons.joinToString(", ")}. What should I do?")
                            navController.navigate("ai_chat")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Chat, contentDescription = "Consult AI")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Discuss Result with AI Advisor", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Back to safety
                    OutlinedButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Return to Dashboard")
                    }
                }
            }
        }
    }
}


// 4. Report Scam Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScamScreen(viewModel: ScamViewModel, navController: NavController) {
    var title by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("SMS Phishing") }
    var isAnonymous by remember { mutableStateOf(false) }
    var mockScreenshotBase64 by remember { mutableStateOf<String?>(null) }
    var isCategoryExpanded by remember { mutableStateOf(false) }

    val categories = listOf("SMS Phishing", "Phone Call scam", "Email Phishing", "Website Impersonation", "Social Media Impersonation", "Tech Support Fraud", "Investment Fraud")

    Scaffold(
        topBar = {
            TopSecurityBar(viewModel, navController)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Report Fraudulent Activity",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Help other ScamShield users stay alert by submitting suspicious phone calls, phishing portals, or social handles to our community database.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Scam Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Scam Title / Identifier") },
                placeholder = { Text("e.g., Fake Netflix Billing Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("report_title_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Scam Category Dropdown
            ExposedDropdownMenuBox(
                expanded = isCategoryExpanded,
                onExpandedChange = { isCategoryExpanded = !isCategoryExpanded }
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = selectedCategory,
                    onValueChange = {},
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryExpanded) },
                    colors = OutlinedTextFieldDefaults.colors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = isCategoryExpanded,
                    onDismissRequest = { isCategoryExpanded = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedCategory = category
                                isCategoryExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Details description
            OutlinedTextField(
                value = details,
                onValueChange = { details = it },
                label = { Text("Scam Description & Details") },
                placeholder = { Text("Paste the scam message, phone details, URLs, or high pressure speech used by the fraudster.") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .testTag("report_details_input"),
                shape = RoundedCornerShape(12.dp),
                maxLines = 6
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Custom Virtual Screenshot Generator (Mock screenshot picker to work consistently on cloud android container)
            Text(
                text = "Attachment / Evidence",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .clickable {
                        // Generate a sleek placeholder base64 digital blueprint graphic
                        mockScreenshotBase64 = "GENERATED_SECURITY_BLUEPRINT"
                    },
                contentAlignment = Alignment.Center
            ) {
                if (mockScreenshotBase64 == null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Upload screenshot placeholder icon",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Attach Scam Screenshot", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text("Simulate photo upload", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    val primaryColor = MaterialTheme.colorScheme.primary
                    val secondaryColor = MaterialTheme.colorScheme.secondary
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                            .drawBehind {
                                drawRect(
                                    brush = Brush.linearGradient(
                                        listOf(primaryColor, secondaryColor)
                                    ),
                                    alpha = 0.15f
                                )
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success uploaded icon",
                            tint = SafeGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("scam_proof_evidence.png", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Secure Base64 simulation compiled", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        TextButton(onClick = { mockScreenshotBase64 = null }) {
                            Text("Clear", color = DangerRed)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Anonymous Switch Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Report Anonymously", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Your name/profile details won't be displayed on the report.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = isAnonymous,
                    onCheckedChange = { isAnonymous = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button
            Button(
                onClick = {
                    if (title.isNotEmpty() && details.isNotEmpty()) {
                        viewModel.reportScam(title, details, selectedCategory, isAnonymous, mockScreenshotBase64)
                        title = ""
                        details = ""
                        mockScreenshotBase64 = null
                        // Redirect to community page
                        navController.navigate("community")
                    }
                },
                enabled = title.isNotEmpty() && details.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_report_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Submit Scam Report", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}


// 5. Community Forum Screen
@Composable
fun CommunityForumScreen(viewModel: ScamViewModel, navController: NavController) {
    val reports by viewModel.scamReports.collectAsState()

    Scaffold(
        topBar = {
            TopSecurityBar(viewModel, navController)
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Live Fraud Radar",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Real-time alerts, reports, and warnings shared by users in the safety community.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Trending Alert Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = DangerRed.copy(alpha = 0.12f)),
                    border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = "Alert", tint = DangerRed, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("CRITICAL TRENDING SCAM ALERT", fontSize = 11.sp, fontWeight = FontWeight.Black, color = DangerRed, fontFamily = FontFamily.Monospace)
                            Text("USPS Redelivery Text Campaign", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Tens of thousands of SMS messages sent claiming failed postal redelivery. Never click link.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Recent Community Submissions",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            if (reports.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Empty", modifier = Modifier.size(48.dp), tint = SafeGreen)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Community radar is clear.", fontWeight = FontWeight.Bold)
                            Text("Report a scam to kickstart the community forum.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(reports) { report ->
                    CommunityReportCardItem(report, viewModel)
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun CommunityReportCardItem(report: ScamReport, viewModel: ScamViewModel) {
    val dateString = SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault()).format(Date(report.timestamp))

    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                contentColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = report.category.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Text(
                text = dateString,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = report.title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = if (report.isAnonymous) "Submitted Anonymously" else "Submitted by verified citizen",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = report.details,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Visual digital blueprint layout if screenshot exists
        if (report.screenshotBase64 != null) {
            val primaryColor = MaterialTheme.colorScheme.primary
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(primaryColor.copy(alpha = 0.08f))
                    .drawBehind {
                        drawLine(
                            color = primaryColor.copy(alpha = 0.2f),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, size.height),
                            strokeWidth = 2f
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = "evidence", tint = primaryColor, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SECURED IMAGE EVIDENCE ATTACHED", fontSize = 11.sp, color = primaryColor, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Like/Confirm button
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { viewModel.likeReport(report) }
                    .padding(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ThumbUp,
                    contentDescription = "Confirm Alert",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Confirm (${report.likes})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Flag/Report Button
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { viewModel.reportReport(report) }
                    .padding(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Flag Alert",
                    tint = DangerRed,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Flag (${report.reports})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = DangerRed
                )
            }
        }
    }
}


// 6. Scam Database Lookup Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScamDatabaseScreen(viewModel: ScamViewModel, navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }

    val categories = listOf("ALL", "PHONE", "WEBSITE", "SOCIAL")

    // Realistic static blacklisted entities to allow instant user lookup in scam database
    val blacklist = listOf(
        BlacklistItem("+1 (888) 555-0199", "PHONE", "Fake IRS Tax Auditing", "Threatens immediate arrest over non-existent debt.", 95),
        BlacklistItem("+1 (800) 901-4432", "PHONE", "Chase Security OTP SMS Bot", "Asks to verify credentials to cancel fake card charge.", 92),
        BlacklistItem("+1 (509) 304-2211", "PHONE", "WhatsApp Daughter Impersonator", "Asks parents for transfer for a phone repair.", 89),
        BlacklistItem("paypal-update-account.cc", "WEBSITE", "Credential Phishing Link", "Cloned login panel harvesting passwords.", 98),
        BlacklistItem("netflix-billings.xyz", "WEBSITE", "Billing Phishing", "Steals credit card numbers via fake suspension alerts.", 96),
        BlacklistItem("amazon-promos-claim.xyz", "WEBSITE", "Fake Survey Sweepstakes", "Harvests phone numbers & emails for spam lists.", 85),
        BlacklistItem("usps-post-redelivery.xyz", "WEBSITE", "Postal Phishing Portal", "Steals addresses and card numbers.", 95),
        BlacklistItem("@CryptoJack_Yield", "SOCIAL", "Crypto Telegram Double Bot", "Guarantees 300% return on direct USDT wire.", 90),
        BlacklistItem("@giftcards_free_claim", "SOCIAL", "Fake Facebook Giveaway Page", "Redirects to malware portals.", 88),
        BlacklistItem("bank-transfer-routing-swift", "BANK", "Swift Wire Clearing Fraud", "Offshore wire transfer mule account.", 70)
    )

    val filteredList = blacklist.filter { item ->
        val matchesSearch = item.query.contains(searchQuery, ignoreCase = true) || item.name.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory == "ALL" || item.type == selectedCategory
        matchesSearch && matchesCategory
    }

    Scaffold(
        topBar = {
            TopSecurityBar(viewModel, navController)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Scam Repository Lookup",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Inspect our curated offline database of reported spam phone numbers, fraud links, and fake profiles.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Search input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by scam name or query details...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("database_search_input"),
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search icon") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear search")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Category select chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    val isSelected = selectedCategory == category
                    InputChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = { Text(category) },
                        colors = InputChipDefaults.inputChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = "No result", modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No matching scam records.", fontWeight = FontWeight.Bold)
                        Text("Try search words like 'USPS', 'Netflix', or '+1'.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredList) { item ->
                        BlacklistCardItem(item, viewModel, navController)
                    }
                }
            }
        }
    }
}

data class BlacklistItem(
    val query: String,
    val type: String,
    val name: String,
    val description: String,
    val riskScore: Int
)

@Composable
fun BlacklistCardItem(item: BlacklistItem, viewModel: ScamViewModel, navController: NavController) {
    val scoreColor = when {
        item.riskScore > 75 -> DangerRed
        item.riskScore > 40 -> WarningYellow
        else -> SafeGreen
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                viewModel.performScan(item.type, item.query)
                navController.navigate("analysis")
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = scoreColor.copy(alpha = 0.15f),
                        contentColor = scoreColor,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "${item.type} | Risk ${item.riskScore}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.query,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = item.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = item.description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Run detailed analysis scan",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}


// 7. AI Security Assistant (Chatbot)
@Composable
fun AiChatAssistantScreen(viewModel: ScamViewModel, navController: NavController) {
    val messages by viewModel.chatMessages.collectAsState()
    var inputMessage by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    val advisoryChips = listOf(
        "Is this SMS safe?",
        "Check this link",
        "Explain OTP safety",
        "Robocall block tips"
    )

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            androidx.compose.material3.TopAppBar(
                title = {
                    Column {
                        Text("ScamShield Advisor", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("Interactive cybersecurity assistant", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearChat() }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Clear Thread")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Preset advisor chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                advisoryChips.forEach { tip ->
                    Surface(
                        modifier = Modifier
                            .clickable {
                                inputMessage = tip
                            },
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    ) {
                        Text(
                            text = tip,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Messages scroll container
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages) { message ->
                    ChatBubbleItem(message)
                }
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // Input Bar
            Surface(
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputMessage,
                        onValueChange = { inputMessage = it },
                        placeholder = { Text("Ask ScamShield anything suspicious...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ai_chat_input"),
                        shape = RoundedCornerShape(24.dp),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Send
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (inputMessage.trim().isNotEmpty()) {
                                    viewModel.sendChatMessage(inputMessage)
                                    inputMessage = ""
                                    keyboardController?.hide()
                                }
                            }
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable(enabled = inputMessage.trim().isNotEmpty()) {
                                viewModel.sendChatMessage(inputMessage)
                                inputMessage = ""
                                keyboardController?.hide()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send msg",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubbleItem(message: ChatMessage) {
    val bubbleColor = if (message.isUser) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    val textColor = if (message.isUser) {
        Color.White
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val alignment = if (message.isUser) {
        Alignment.CenterEnd
    } else {
        Alignment.CenterStart
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Surface(
            color = bubbleColor,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isUser) 16.dp else 2.dp,
                bottomEnd = if (message.isUser) 2.dp else 16.dp
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message.message,
                color = textColor,
                fontSize = 13.sp,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}


// 8. Notifications Screen
@Composable
fun NotificationsScreen(viewModel: ScamViewModel, navController: NavController) {
    val alerts = listOf(
        NotificationAlert("🚨 Bank Spoof SMS Alert", "A surge of fraudulent SMS messages mimicking Citizens Bank OTP prompts has been identified in your region. Do not tap short URLs.", "System Alert", "1 hr ago", true),
        NotificationAlert("🛡️ App Update Complete", "ScamShield offline signature catalog updated with 2,400+ freshly reported phishing domains.", "Security Update", "4 hrs ago", false),
        NotificationAlert("⚠️ Fake Package Delivery SMS", "Incoming texts claiming package delivery delays are active globally. High risk of credential phishing.", "Trending Alert", "1 day ago", true),
        NotificationAlert("💡 Weekly Tip: OTP Safety", "No customer representative will ever ask for your one-time passwords. Keep your verification codes strictly confidential.", "Security Tip", "2 days ago", false)
    )

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            androidx.compose.material3.TopAppBar(
                title = { Text("Security Notifications", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(alerts) { alert ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (alert.isUrgent) DangerRed.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (alert.isUrgent) DangerRed.copy(alpha = 0.3f) else Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = if (alert.isUrgent) Icons.Default.Warning else Icons.Default.Info,
                            contentDescription = "Alert classification icon",
                            tint = if (alert.isUrgent) DangerRed else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = alert.category.uppercase(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (alert.isUrgent) DangerRed else MaterialTheme.colorScheme.primary,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = alert.time,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = alert.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = alert.message,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

data class NotificationAlert(
    val title: String,
    val message: String,
    val category: String,
    val time: String,
    val isUrgent: Boolean
)


// 9 & 10. Unified Profile, Saved Searches & Settings Screen
@Composable
fun ProfileAndSettingsScreen(viewModel: ScamViewModel, navController: NavController) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val email by viewModel.userEmail.collectAsState()
    val savedItems by viewModel.savedSearches.collectAsState()
    val histories by viewModel.scanHistory.collectAsState()

    val darkModeActive by viewModel.darkMode.collectAsState()
    val activeLanguage by viewModel.language.collectAsState()
    val alertsActive by viewModel.notificationsEnabled.collectAsState()
    val privacyActive by viewModel.privacyControlsEnabled.collectAsState()
    val guardLevel by viewModel.shieldGuardLevel.collectAsState()

    var loginFieldEmail by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopSecurityBar(viewModel, navController)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Profile / Auth Block
            Text(
                text = "My Protection Account",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (!isLoggedIn) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Secure Profile Backup", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Sign up to backup your scan history, custom community reports, and sync bookmarks across devices.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = loginFieldEmail,
                            onValueChange = { loginFieldEmail = it },
                            placeholder = { Text("Enter your email address") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_email_input"),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                if (loginFieldEmail.trim().isNotEmpty()) {
                                    viewModel.login(loginFieldEmail)
                                    loginFieldEmail = ""
                                }
                            },
                            enabled = loginFieldEmail.trim().isNotEmpty(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_submit_button")
                        ) {
                            Text("Simulate Login / Register")
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = email.take(2).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Welcome back!", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace)
                            Text(text = email, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(text = "Premium Sandbox Active", fontSize = 11.sp, color = SafeGreen)
                        }
                        IconButton(onClick = { viewModel.logout() }) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "logout", tint = DangerRed)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Saved Searches / Bookmarks
            Text(
                text = "Bookmarked Scams",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))

            if (savedItems.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No saved searches yet. Bookmark results during detailed scan scans.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                savedItems.forEach { saved ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                viewModel.performScan(saved.type, saved.query)
                                navController.navigate("analysis")
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Bookmark, contentDescription = "Saved icon", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(saved.query, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("Type: ${saved.type}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { viewModel.deleteSavedSearchItem(saved.id) }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete bookmark", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Settings Section
            Text(
                text = "System Settings",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                // Dark mode toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Dark Visual Theme", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Sleek cybersecurity appearance", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = darkModeActive,
                        onCheckedChange = { viewModel.toggleDarkMode() }
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 4.dp))

                // Language toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("App Language", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Selected language: $activeLanguage", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(
                        onClick = {
                            viewModel.setLanguage(if (activeLanguage == "English") "Español" else "English")
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Language, contentDescription = "Change Language", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 4.dp))

                // Notifications Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Push Notifications", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Real-time local threat broadcast alerts", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = alertsActive,
                        onCheckedChange = { viewModel.toggleNotifications() }
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 4.dp))

                // Privacy Sandbox check
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Secure Anonymous Telemetry", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Permit analytical safety reports anonymously", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = privacyActive,
                        onCheckedChange = { viewModel.togglePrivacy() }
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 4.dp))

                // App Scan Shield Guard Configuration
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("App Scan Shield Guard", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Real-time local and cloud threat sandbox", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            text = guardLevel.uppercase(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (guardLevel) {
                                "Maximum" -> SafeGreen
                                "Standard" -> WarningYellow
                                else -> DangerRed
                            },
                            modifier = Modifier
                                .background(
                                    color = (when (guardLevel) {
                                        "Maximum" -> SafeGreen
                                        "Standard" -> WarningYellow
                                        else -> DangerRed
                                    }).copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Max button
                        Button(
                            onClick = { viewModel.setShieldGuardLevel("Maximum") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (guardLevel == "Maximum") SafeGreen.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                contentColor = if (guardLevel == "Maximum") SafeGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("guard_max_btn"),
                            border = BorderStroke(
                                1.dp,
                                if (guardLevel == "Maximum") SafeGreen else Color.Transparent
                            )
                        ) {
                            Text("MAX", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Standard button
                        Button(
                            onClick = { viewModel.setShieldGuardLevel("Standard") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (guardLevel == "Standard") WarningYellow.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                contentColor = if (guardLevel == "Standard") WarningYellow else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("guard_std_btn"),
                            border = BorderStroke(
                                1.dp,
                                if (guardLevel == "Standard") WarningYellow else Color.Transparent
                            )
                        ) {
                            Text("STANDARD", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Off button
                        Button(
                            onClick = { viewModel.setShieldGuardLevel("Off") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (guardLevel == "Off") DangerRed.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                contentColor = if (guardLevel == "Off") DangerRed else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("guard_off_btn"),
                            border = BorderStroke(
                                1.dp,
                                if (guardLevel == "Off") DangerRed else Color.Transparent
                            )
                        ) {
                            Text("OFF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "ScamShield AI v1.0.2 - Sandboxed Security Layer",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}


// Shared Top bar implementation
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopSecurityBar(viewModel: ScamViewModel, navController: NavController) {
    androidx.compose.material3.TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Shield Guard",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ScamShield AI",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    fontSize = 18.sp
                )
            }
        },
        actions = {
            IconButton(onClick = { navController.navigate("notifications") }) {
                Box {
                    Icon(imageVector = Icons.Default.Notifications, contentDescription = "Threat notifications")
                    // Real-time unread dot simulation
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(DangerRed)
                            .align(Alignment.TopEnd)
                            .offset(x = (-2).dp, y = 2.dp)
                    )
                }
            }
            IconButton(onClick = { navController.navigate("profile") }) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = "Profile & Settings")
            }
        }
    )
}

package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Fully edge-to-edge content support
        enableEdgeToEdge()
        
        setContent {
            // Dark Theme Override state
            val systemDarkTheme = isSystemInDarkTheme()
            var darkThemeOverride by remember { mutableStateOf<Boolean?>(null) }
            val darkTheme = darkThemeOverride ?: systemDarkTheme

            MyApplicationTheme(darkTheme = darkTheme) {
                val viewModel: MainViewModel = viewModel()
                
                if (!viewModel.isAuthenticated) {
                    LoginScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
                } else {
                    MainAppWorkspace(
                        viewModel = viewModel,
                        darkTheme = darkTheme,
                        onToggleDarkTheme = {
                            darkThemeOverride = !darkTheme
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppWorkspace(
    viewModel: MainViewModel,
    darkTheme: Boolean,
    onToggleDarkTheme: () -> Unit
) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf("dashboard") }
    var showInfoDialog by remember { mutableStateOf(false) }

    // Adaptive Checks for Device Form Factor
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    // Tab items list with their labels and identifiers.
    // Shortened to "Novo" to ensure perfect symmetry and proportional sizes.
    val navItems = listOf(
        "dashboard" to "Dashboard",
        "clients" to "Clientes",
        "services" to "Serviços",
        "new_budget" to "Novo",
        "history" to "Histórico"
    )

    Row(modifier = Modifier.fillMaxSize()) {
        // Navigation Rail on Tablets / Widescreens (Expanded Form Factor)
        if (isTablet) {
            Row(modifier = Modifier.fillMaxHeight()) {
                NavigationRail(
                    modifier = Modifier
                        .fillMaxHeight()
                        .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Start + WindowInsetsSides.Vertical)),
                    containerColor = MaterialTheme.colorScheme.surface,
                    header = {
                        Box(
                            modifier = Modifier.padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "OF",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    navItems.forEach { (route, label) ->
                        val isSelected = currentTab == route
                        val icon = when (route) {
                            "dashboard" -> if (isSelected) Icons.Filled.Dashboard else Icons.Outlined.Dashboard
                            "clients" -> if (isSelected) Icons.Filled.Person else Icons.Outlined.Person
                            "services" -> if (isSelected) Icons.Filled.Build else Icons.Outlined.Build
                            "new_budget" -> if (isSelected) Icons.Filled.AddCircle else Icons.Outlined.AddCircle
                            "history" -> if (isSelected) Icons.Filled.History else Icons.Outlined.History
                            else -> if (isSelected) Icons.Filled.Home else Icons.Outlined.Home
                        }
                        NavigationRailItem(
                            selected = isSelected,
                            onClick = { currentTab = route },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label, fontWeight = FontWeight.SemiBold) },
                            modifier = Modifier.testTag("nav_rail_item_$route")
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
                VerticalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    thickness = 1.dp,
                    modifier = Modifier.fillMaxHeight()
                )
            }
        }

        // Main Scaffold Workspace
        Scaffold(
            modifier = Modifier.weight(1f),
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "OrçaFácil",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    actions = {
                        // Light/Dark Theme manual toggle button
                        IconButton(
                            onClick = onToggleDarkTheme,
                            modifier = Modifier.testTag("toggle_dark_theme_button")
                        ) {
                            Icon(
                                imageVector = if (darkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Alternar Modo de Cor",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // App/Security Info dialog trigger
                        IconButton(
                            onClick = { showInfoDialog = true },
                            modifier = Modifier.testTag("app_info_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Sobre a Segurança",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Logout action button
                        IconButton(
                            onClick = {
                                viewModel.logout()
                                Toast.makeText(context, "Sessão encerrada com segurança.", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("logout_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Sair",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            bottomBar = {
                // Bottom Navigation Bar on standard smartphones (Compact Form Factor)
                if (!isTablet) {
                    Column(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            thickness = 1.dp
                        )
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 0.dp
                        ) {
                            navItems.forEach { (route, label) ->
                                val isSelected = currentTab == route
                                val icon = when (route) {
                                    "dashboard" -> if (isSelected) Icons.Filled.Dashboard else Icons.Outlined.Dashboard
                                    "clients" -> if (isSelected) Icons.Filled.Person else Icons.Outlined.Person
                                    "services" -> if (isSelected) Icons.Filled.Build else Icons.Outlined.Build
                                    "new_budget" -> if (isSelected) Icons.Filled.AddCircle else Icons.Outlined.AddCircle
                                    "history" -> if (isSelected) Icons.Filled.History else Icons.Outlined.History
                                    else -> if (isSelected) Icons.Filled.Home else Icons.Outlined.Home
                                }
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { currentTab = route },
                                    icon = { Icon(icon, contentDescription = label) },
                                    label = { Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                                    modifier = Modifier.testTag("bottom_nav_item_$route")
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    "dashboard" -> DashboardScreen(
                        viewModel = viewModel,
                        onCreateBudgetClicked = { currentTab = "new_budget" }
                    )
                    "clients" -> ClientsScreen(viewModel = viewModel)
                    "services" -> ServicesScreen(viewModel = viewModel)
                    "new_budget" -> BudgetCreationScreen(
                        viewModel = viewModel,
                        onSuccessNav = { currentTab = "history" }
                    )
                    "history" -> BudgetHistoryScreen(viewModel = viewModel)
                }
            }
        }
    }

    // Security & Compliance Informational Dialog
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            icon = { Icon(Icons.Default.Shield, contentDescription = "Shield Icon", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp)) },
            title = {
                Text(
                    text = "Segurança & Privacidade LGPD",
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "A segurança dos seus dados é nossa prioridade máxima:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "1. Banco de Dados Local: Todas as informações de clientes, serviços e orçamentos são salvas de forma persistente e criptografada localmente no dispositivo.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "2. Autenticação Segura: O acesso ao aplicativo exige autenticação local de 4 dígitos via PIN para evitar acessos não autorizados por terceiros no mesmo aparelho.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "3. Conformidade LGPD: Este aplicativo segue rigorosamente a Lei Geral de Proteção de Dados (Lei nº 13.709/2018), garantindo transparência, sigilo e o direito à exclusão total das informações cadastradas a qualquer momento.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showInfoDialog = false }) {
                    Text("Entendi")
                }
            }
        )
    }
}

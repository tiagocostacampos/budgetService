package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Service
import com.example.ui.viewmodel.MainViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val services by viewModel.services.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    // Dialog state
    var showAddEditDialog by remember { mutableStateOf(false) }
    var selectedServiceForEdit by remember { mutableStateOf<Service?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf<Service?>(null) }

    // Dialog input states
    var serviceName by remember { mutableStateOf("") }
    var serviceDescription by remember { mutableStateOf("") }
    var servicePrice by remember { mutableStateOf("") }

    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("pt", "BR")) }

    // Filtered list
    val filteredServices = remember(services, searchQuery) {
        if (searchQuery.isEmpty()) services
        else {
            services.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedServiceForEdit = null
                    serviceName = ""
                    serviceDescription = ""
                    servicePrice = ""
                    showAddEditDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_service_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Serviço")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Title
            Text(
                text = "Tabela de Serviços",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar por serviço ou descrição...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear Search")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Services List
            if (filteredServices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = "No Services Icon",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isEmpty()) "Nenhum serviço cadastrado ainda." else "Nenhum serviço encontrado.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredServices) { service ->
                        ServiceItemCard(
                            service = service,
                            currencyFormat = currencyFormat,
                            onEditClicked = {
                                selectedServiceForEdit = service
                                serviceName = service.name
                                serviceDescription = service.description
                                servicePrice = service.price.toString()
                                showAddEditDialog = true
                            },
                            onDeleteClicked = {
                                showDeleteConfirmDialog = service
                            }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Service Dialog
    if (showAddEditDialog) {
        AlertDialog(
            onDismissRequest = { showAddEditDialog = false },
            title = {
                Text(
                    text = if (selectedServiceForEdit == null) "Cadastrar Novo Serviço" else "Editar Serviço",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = serviceName,
                        onValueChange = { serviceName = it },
                        label = { Text("Nome do Serviço *") },
                        modifier = Modifier.fillMaxWidth().testTag("service_name_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = serviceDescription,
                        onValueChange = { serviceDescription = it },
                        label = { Text("Descrição / Detalhes") },
                        modifier = Modifier.fillMaxWidth().testTag("service_description_input"),
                        maxLines = 3
                    )
                    OutlinedTextField(
                        value = servicePrice,
                        onValueChange = { servicePrice = it },
                        label = { Text("Valor Unitário (R$) *") },
                        placeholder = { Text("Ex: 150.00") },
                        modifier = Modifier.fillMaxWidth().testTag("service_price_input"),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsedPrice = servicePrice.toDoubleOrNull()
                        if (serviceName.trim().isEmpty() || parsedPrice == null) return@Button

                        if (selectedServiceForEdit == null) {
                            viewModel.addService(
                                name = serviceName.trim(),
                                description = serviceDescription.trim(),
                                price = parsedPrice
                            )
                        } else {
                            viewModel.updateService(
                                selectedServiceForEdit!!.copy(
                                    name = serviceName.trim(),
                                    description = serviceDescription.trim(),
                                    price = parsedPrice
                                )
                            )
                        }
                        showAddEditDialog = false
                    },
                    modifier = Modifier.testTag("save_service_button")
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddEditDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text("Excluir Serviço?", fontWeight = FontWeight.Bold) },
            text = { Text("Tem certeza que deseja excluir o serviço ${showDeleteConfirmDialog!!.name}? Esta ação não poderá ser desfeita.") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        viewModel.deleteService(showDeleteConfirmDialog!!)
                        showDeleteConfirmDialog = null
                    }
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun ServiceItemCard(
    service: Service,
    currencyFormat: NumberFormat,
    onEditClicked: () -> Unit,
    onDeleteClicked: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = service.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = currencyFormat.format(service.price),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32) // Styled green for price value
                    )
                }

                // Edit / Delete Action Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onEditClicked,
                        modifier = Modifier.size(36.dp).testTag("edit_service_${service.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Service",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onDeleteClicked,
                        modifier = Modifier.size(36.dp).testTag("delete_service_${service.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Service",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (service.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = service.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}

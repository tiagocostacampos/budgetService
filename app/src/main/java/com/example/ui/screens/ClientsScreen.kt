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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Client
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val clients by viewModel.clients.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    
    // Dialog state
    var showAddEditDialog by remember { mutableStateOf(false) }
    var selectedClientForEdit by remember { mutableStateOf<Client?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf<Client?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Dialog input states
    var clientName by remember { mutableStateOf("") }
    var clientPhone by remember { mutableStateOf("") }
    var clientEmail by remember { mutableStateOf("") }
    var clientAddress by remember { mutableStateOf("") }
    var clientCep by remember { mutableStateOf("") }
    var clientReferencePoint by remember { mutableStateOf("") }
    var isFetchingCep by remember { mutableStateOf(false) }

    // Filtered list
    val filteredClients = remember(clients, searchQuery) {
        if (searchQuery.isEmpty()) clients
        else {
            clients.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.phone.contains(searchQuery, ignoreCase = true) ||
                it.email.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedClientForEdit = null
                    clientName = ""
                    clientPhone = ""
                    clientEmail = ""
                    clientAddress = ""
                    clientCep = ""
                    clientReferencePoint = ""
                    showAddEditDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_client_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Cliente")
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
                text = "Gerenciar Clientes",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar por nome, telefone ou e-mail...") },
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

            // Clients List
            if (filteredClients.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "No Clients Icon",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isEmpty()) "Nenhum cliente cadastrado ainda." else "Nenhum cliente encontrado.",
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
                    items(filteredClients) { client ->
                        ClientItemCard(
                            client = client,
                            onEditClicked = {
                                selectedClientForEdit = client
                                clientName = client.name
                                clientPhone = client.phone
                                clientEmail = client.email
                                clientAddress = client.address
                                clientCep = client.cep
                                clientReferencePoint = client.referencePoint
                                showAddEditDialog = true
                            },
                            onDeleteClicked = {
                                showDeleteConfirmDialog = client
                            }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Client Dialog
    if (showAddEditDialog) {
        AlertDialog(
            onDismissRequest = { showAddEditDialog = false },
            title = {
                Text(
                    text = if (selectedClientForEdit == null) "Adicionar Novo Cliente" else "Editar Cliente",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = clientName,
                        onValueChange = { clientName = it },
                        label = { Text("Nome Completo *") },
                        modifier = Modifier.fillMaxWidth().testTag("client_name_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = clientPhone,
                        onValueChange = { clientPhone = it },
                        label = { Text("Telefone / WhatsApp *") },
                        placeholder = { Text("Ex: 11988887777") },
                        modifier = Modifier.fillMaxWidth().testTag("client_phone_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = clientEmail,
                        onValueChange = { clientEmail = it },
                        label = { Text("E-mail") },
                        modifier = Modifier.fillMaxWidth().testTag("client_email_input"),
                        singleLine = true
                    )
                    
                    // CEP Search Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = clientCep,
                            onValueChange = { newValue ->
                                val filtered = newValue.filter { it.isDigit() || it == '-' }
                                if (filtered.length <= 9) {
                                    clientCep = filtered
                                }
                            },
                            label = { Text("CEP") },
                            placeholder = { Text("Ex: 01001-000") },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("client_cep_input")
                        )
                        
                        Button(
                            onClick = {
                                if (clientCep.isNotBlank()) {
                                    coroutineScope.launch {
                                        isFetchingCep = true
                                        val result = com.example.util.CepHelper.fetchAddressByCep(clientCep)
                                        isFetchingCep = false
                                        if (result != null) {
                                            if (result.erro) {
                                                android.widget.Toast.makeText(context, "CEP não localizado.", android.widget.Toast.LENGTH_SHORT).show()
                                            } else {
                                                clientAddress = result.toFormattedAddress()
                                                android.widget.Toast.makeText(context, "Endereço preenchido!", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            android.widget.Toast.makeText(context, "Erro ao buscar CEP.", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    android.widget.Toast.makeText(context, "Insira um CEP.", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = !isFetchingCep && clientCep.isNotBlank(),
                            modifier = Modifier.height(56.dp).testTag("client_cep_search_button")
                        ) {
                            if (isFetchingCep) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Search, contentDescription = "Buscar CEP")
                            }
                        }
                    }

                    OutlinedTextField(
                        value = clientAddress,
                        onValueChange = { clientAddress = it },
                        label = { Text("Endereço Completo") },
                        modifier = Modifier.fillMaxWidth().testTag("client_address_input")
                    )

                    OutlinedTextField(
                        value = clientReferencePoint,
                        onValueChange = { clientReferencePoint = it },
                        label = { Text("Ponto de Referência") },
                        placeholder = { Text("Ex: Próximo à padaria, casa amarela...") },
                        modifier = Modifier.fillMaxWidth().testTag("client_reference_input"),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (clientName.trim().isEmpty() || clientPhone.trim().isEmpty()) return@Button
                        
                        if (selectedClientForEdit == null) {
                            viewModel.addClient(
                                name = clientName.trim(),
                                phone = clientPhone.trim(),
                                email = clientEmail.trim(),
                                address = clientAddress.trim(),
                                cep = clientCep.trim(),
                                referencePoint = clientReferencePoint.trim()
                            )
                        } else {
                            viewModel.updateClient(
                                selectedClientForEdit!!.copy(
                                    name = clientName.trim(),
                                    phone = clientPhone.trim(),
                                    email = clientEmail.trim(),
                                    address = clientAddress.trim(),
                                    cep = clientCep.trim(),
                                    referencePoint = clientReferencePoint.trim()
                                )
                            )
                        }
                        showAddEditDialog = false
                    },
                    modifier = Modifier.testTag("save_client_button")
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
            title = { Text("Excluir Cliente?", fontWeight = FontWeight.Bold) },
            text = { Text("Tem certeza que deseja excluir o cliente ${showDeleteConfirmDialog!!.name}? Esta ação não poderá ser desfeita.") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        viewModel.deleteClient(showDeleteConfirmDialog!!)
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
fun ClientItemCard(
    client: Client,
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
                        text = client.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Phone Icon",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = client.phone,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Edit & Delete Action Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onEditClicked,
                        modifier = Modifier.size(36.dp).testTag("edit_client_${client.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Client",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onDeleteClicked,
                        modifier = Modifier.size(36.dp).testTag("delete_client_${client.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Client",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (client.email.isNotEmpty() || client.address.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(8.dp))
                
                if (client.email.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(bottom = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email Icon",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = client.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }

                if (client.address.isNotEmpty() || client.cep.isNotEmpty() || client.referencePoint.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location Icon",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(14.dp).padding(top = 2.dp)
                        )
                        Column {
                            if (client.address.isNotEmpty()) {
                                Text(
                                    text = client.address,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                            if (client.cep.isNotEmpty() || client.referencePoint.isNotEmpty()) {
                                val details = buildString {
                                    if (client.cep.isNotEmpty()) {
                                        append("CEP: ${client.cep}")
                                    }
                                    if (client.referencePoint.isNotEmpty()) {
                                        if (isNotEmpty()) append(" • ")
                                        append("Ref: ${client.referencePoint}")
                                    }
                                }
                                Text(
                                    text = details,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

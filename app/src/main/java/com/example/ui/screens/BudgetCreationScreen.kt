package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Client
import com.example.data.model.SelectedService
import com.example.data.model.Service
import com.example.ui.viewmodel.MainViewModel
import com.example.util.PdfHelper
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetCreationScreen(
    viewModel: MainViewModel,
    onSuccessNav: () -> Unit, // Callback to redirect to history tab upon success
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clients by viewModel.clients.collectAsState()
    val services by viewModel.services.collectAsState()

    var selectedClient by remember { mutableStateOf<Client?>(null) }
    var selectedServicesList = remember { mutableStateListOf<SelectedService>() }
    
    var discountInput by remember { mutableStateOf("") }
    var additionInput by remember { mutableStateOf("") }
    var statusState by remember { mutableStateOf("Pendente") } // Pendente, Aprovado, Concluído

    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("pt", "BR")) }

    // Client select dropdown state
    var clientDropdownExpanded by remember { mutableStateOf(false) }

    // Service select state
    var showAddServiceDialog by remember { mutableStateOf(false) }

    // Live mathematical totals
    val subtotal = selectedServicesList.sumOf { it.price * it.quantity }
    val discount = discountInput.toDoubleOrNull() ?: 0.0
    val addition = additionInput.toDoubleOrNull() ?: 0.0
    val totalAmount = (subtotal - discount + addition).coerceAtLeast(0.0)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        item {
            Text(
                text = "Novo Orçamento",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Section 1: Client Selection
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. Selecione o Cliente *",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    ExposedDropdownMenuBox(
                        expanded = clientDropdownExpanded,
                        onExpandedChange = { clientDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedClient?.name ?: "Escolher cliente cadastrado...",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = clientDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("select_client_dropdown"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = if (selectedClient == null) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = clientDropdownExpanded,
                            onDismissRequest = { clientDropdownExpanded = false }
                        ) {
                            if (clients.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Nenhum cliente cadastrado. Cadastre primeiro!") },
                                    onClick = { clientDropdownExpanded = false }
                                )
                            } else {
                                clients.forEach { client ->
                                    DropdownMenuItem(
                                        text = { Text("${client.name} - ${client.phone}") },
                                        onClick = {
                                            selectedClient = client
                                            clientDropdownExpanded = false
                                        },
                                        modifier = Modifier.testTag("client_option_${client.id}")
                                    )
                                }
                            }
                        }
                    }

                    // Selected Client Info Card
                    selectedClient?.let { client ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Person, contentDescription = "Client", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(client.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Phone, contentDescription = "Phone", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(client.phone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (client.email.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Email, contentDescription = "Email", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(client.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Services / Items list
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "2. Adicionar Itens *",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Button(
                            onClick = { showAddServiceDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("add_service_to_budget_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Service Icon", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Incluir Item", fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    if (selectedServicesList.isEmpty()) {
                        Text(
                            text = "Nenhum serviço incluído neste orçamento ainda.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            selectedServicesList.forEach { selectedItem ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(selectedItem.name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                        Text(currencyFormat.format(selectedItem.price), style = MaterialTheme.typography.bodySmall, color = Color(0xFF2E7D32))
                                    }

                                    // Quantity Selector Column
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        IconButton(
                                            onClick = {
                                                val index = selectedServicesList.indexOf(selectedItem)
                                                if (selectedItem.quantity > 1) {
                                                    selectedServicesList[index] = selectedItem.copy(quantity = selectedItem.quantity - 1)
                                                } else {
                                                    selectedServicesList.removeAt(index)
                                                }
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = "Dec quantity", modifier = Modifier.size(16.dp))
                                        }

                                        Text(
                                            text = selectedItem.quantity.toString(),
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )

                                        IconButton(
                                            onClick = {
                                                val index = selectedServicesList.indexOf(selectedItem)
                                                selectedServicesList[index] = selectedItem.copy(quantity = selectedItem.quantity + 1)
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Inc quantity", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Financial Adjustments & Status
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "3. Ajustes & Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = discountInput,
                            onValueChange = { discountInput = it },
                            label = { Text("Desconto (R$)") },
                            placeholder = { Text("0.00") },
                            modifier = Modifier.weight(1f).testTag("discount_input"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = additionInput,
                            onValueChange = { additionInput = it },
                            label = { Text("Acréscimo (R$)") },
                            placeholder = { Text("0.00") },
                            modifier = Modifier.weight(1f).testTag("addition_input"),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Status selection
                    Text("Status Inicial:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Pendente", "Aprovado", "Concluído").forEach { status ->
                            val isSelected = statusState == status
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                    .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                    .clickable { statusState = status }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = status,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 4: Live Totals and Emit Action
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal:", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                        Text(currencyFormat.format(subtotal), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                    }
                    if (discount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Desconto:", style = MaterialTheme.typography.bodyLarge, color = Color(0xFFC62828))
                            Text("- " + currencyFormat.format(discount), style = MaterialTheme.typography.bodyLarge, color = Color(0xFFC62828))
                        }
                    }
                    if (addition > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Acréscimo:", style = MaterialTheme.typography.bodyLarge, color = Color(0xFF2E7D32))
                            Text("+ " + currencyFormat.format(addition), style = MaterialTheme.typography.bodyLarge, color = Color(0xFF2E7D32))
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("TOTAL:", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(currencyFormat.format(totalAmount), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Finalize and Share Options
                    Button(
                        onClick = {
                            if (selectedClient == null) {
                                Toast.makeText(context, "Selecione um cliente para prosseguir.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (selectedServicesList.isEmpty()) {
                                Toast.makeText(context, "Inclua pelo menos um serviço no orçamento.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            viewModel.createBudget(
                                client = selectedClient!!,
                                selectedServices = selectedServicesList.toList(),
                                discount = discount,
                                addition = addition,
                                totalAmount = totalAmount,
                                status = statusState
                            ) { createdBudget ->
                                // Trigger PDF generation in background/safely on UI
                                val pdfFile = PdfHelper.generateBudgetPdf(context, createdBudget)
                                if (pdfFile != null) {
                                    // Successfully generated PDF, now trigger share with WhatsApp preference
                                    PdfHelper.sharePdfViaWhatsApp(context, pdfFile, createdBudget.clientPhone)
                                    Toast.makeText(context, "Orçamento gravado! Abrindo PDF no WhatsApp...", Toast.LENGTH_LONG).show()
                                    
                                    // Reset fields
                                    selectedClient = null
                                    selectedServicesList.clear()
                                    discountInput = ""
                                    additionInput = ""
                                    statusState = "Pendente"
                                    
                                    // Navigate back or to history tab
                                    onSuccessNav()
                                } else {
                                    Toast.makeText(context, "Erro ao gerar PDF do orçamento.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("submit_budget_pdf_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share PDF Icon", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Emitir & Enviar PDF", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (selectedClient == null) {
                                Toast.makeText(context, "Selecione um cliente para prosseguir.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (selectedServicesList.isEmpty()) {
                                Toast.makeText(context, "Inclua pelo menos um serviço no orçamento.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            viewModel.createBudget(
                                client = selectedClient!!,
                                selectedServices = selectedServicesList.toList(),
                                discount = discount,
                                addition = addition,
                                totalAmount = totalAmount,
                                status = statusState
                            ) { createdBudget ->
                                // Trigger direct WhatsApp contact link opening
                                PdfHelper.shareViaWhatsAppDeepLink(context, createdBudget)
                                Toast.makeText(context, "Orçamento gravado! Abrindo conversa direta...", Toast.LENGTH_LONG).show()
                                
                                // Reset fields
                                selectedClient = null
                                selectedServicesList.clear()
                                discountInput = ""
                                additionInput = ""
                                statusState = "Pendente"
                                
                                // Navigate back or to history tab
                                onSuccessNav()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("submit_budget_whatsapp_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)), // Emerald Green
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "WhatsApp Link Icon", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Emitir & Link Direto WhatsApp", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                    }
                }
            }
        }
    }

    // Include Service Dialog selection list
    if (showAddServiceDialog) {
        AlertDialog(
            onDismissRequest = { showAddServiceDialog = false },
            title = { Text("Selecione o Serviço", fontWeight = FontWeight.Bold) },
            text = {
                Box(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
                    if (services.isEmpty()) {
                        Text(
                            "Nenhum serviço cadastrado ainda. Cadastre na aba de Serviços antes de emitir orçamentos.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(services) { service ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable {
                                            // Check if already exists, if so increase quantity
                                            val existingIndex = selectedServicesList.indexOfFirst { it.serviceId == service.id }
                                            if (existingIndex != -1) {
                                                val existing = selectedServicesList[existingIndex]
                                                selectedServicesList[existingIndex] = existing.copy(quantity = existing.quantity + 1)
                                            } else {
                                                selectedServicesList.add(
                                                    SelectedService(
                                                        serviceId = service.id,
                                                        name = service.name,
                                                        description = service.description,
                                                        price = service.price,
                                                        quantity = 1
                                                    )
                                                )
                                            }
                                            showAddServiceDialog = false
                                        }
                                        .padding(12.dp)
                                        .testTag("service_dialog_item_${service.id}"),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(service.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        if (service.description.isNotEmpty()) {
                                            Text(service.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), maxLines = 1)
                                        }
                                    }
                                    Text(currencyFormat.format(service.price), fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddServiceDialog = false }) {
                    Text("Fechar")
                }
            }
        )
    }
}

package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Budget
import com.example.ui.viewmodel.MainViewModel
import com.example.util.PdfHelper
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetHistoryScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val budgets by viewModel.budgets.collectAsState()

    var statusFilter by remember { mutableStateOf("Todos") }
    var showDeleteConfirmDialog by remember { mutableStateOf<Budget?>(null) }
    var selectedBudgetForDetails by remember { mutableStateOf<Budget?>(null) }

    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("pt", "BR")) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR")) }

    // Filtered list
    val filteredBudgets = remember(budgets, statusFilter) {
        if (statusFilter == "Todos") budgets
        else budgets.filter { it.status == statusFilter }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Title
        Text(
            text = "Histórico de Orçamentos",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Status Filter Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("Todos", "Pendente", "Aprovado", "Concluído", "Cancelado").forEach { filter ->
                val isSelected = statusFilter == filter
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { statusFilter = filter }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = filter,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // History list
        if (filteredBudgets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "No History Icon",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (statusFilter == "Todos") "Nenhum orçamento emitido ainda." else "Nenhum orçamento com este status.",
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
                items(filteredBudgets) { budget ->
                    BudgetHistoryItemCard(
                        budget = budget,
                        currencyFormat = currencyFormat,
                        dateFormat = dateFormat,
                        onCardClicked = { selectedBudgetForDetails = budget },
                        onSharePdfClicked = {
                            val file = PdfHelper.generateBudgetPdf(context, budget)
                            if (file != null) {
                                PdfHelper.sharePdfViaWhatsApp(context, file, budget.clientPhone)
                            } else {
                                Toast.makeText(context, "Erro ao regenerar PDF.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onWhatsAppDeepLinkClicked = {
                            PdfHelper.shareViaWhatsAppDeepLink(context, budget)
                        },
                        onStatusChanged = { newStatus ->
                            viewModel.updateBudgetStatus(budget, newStatus)
                        },
                        onDeleteClicked = {
                            showDeleteConfirmDialog = budget
                        }
                    )
                }
            }
        }
    }

    // Detail Dialog (showing snapshot items list)
    if (selectedBudgetForDetails != null) {
        val budget = selectedBudgetForDetails!!
        val items = budget.getSelectedServices()

        AlertDialog(
            onDismissRequest = { selectedBudgetForDetails = null },
            title = {
                Text(
                    text = "Detalhes do Orçamento #${budget.id}",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cliente: ${budget.clientName}", fontWeight = FontWeight.SemiBold)
                    Text("Telefone: ${budget.clientPhone}", style = MaterialTheme.typography.bodySmall)
                    if (budget.clientEmail.isNotEmpty()) {
                        Text("E-mail: ${budget.clientEmail}", style = MaterialTheme.typography.bodySmall)
                    }
                    Text("Data: ${dateFormat.format(Date(budget.dateMillis))}", style = MaterialTheme.typography.bodySmall)
                    Text("Status: ${budget.status}", fontWeight = FontWeight.Bold, color = getStatusColor(budget.status))

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Text("Serviços Contratados:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    
                    Box(modifier = Modifier.heightIn(max = 160.dp)) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(items) { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                        Text("${item.quantity} x ${currencyFormat.format(item.price)}", style = MaterialTheme.typography.labelSmall)
                                    }
                                    Text(currencyFormat.format(item.price * item.quantity), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    if (budget.discount > 0) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Desconto:", style = MaterialTheme.typography.bodySmall)
                            Text("- " + currencyFormat.format(budget.discount), style = MaterialTheme.typography.bodySmall, color = Color.Red)
                        }
                    }
                    if (budget.addition > 0) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Acréscimo:", style = MaterialTheme.typography.bodySmall)
                            Text("+ " + currencyFormat.format(budget.addition), style = MaterialTheme.typography.bodySmall, color = Color(0xFF2E7D32))
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("VALOR TOTAL:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                        Text(currencyFormat.format(budget.totalAmount), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Outlined button for PDF sharing
                    OutlinedButton(
                        onClick = {
                            val file = PdfHelper.generateBudgetPdf(context, budget)
                            if (file != null) {
                                PdfHelper.sharePdfViaWhatsApp(context, file, budget.clientPhone)
                            } else {
                                Toast.makeText(context, "Erro ao gerar PDF.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "PDF Icon", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PDF", fontSize = 11.sp)
                    }

                    // Green WhatsApp deep link button
                    Button(
                        onClick = {
                            PdfHelper.shareViaWhatsAppDeepLink(context, budget)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)), // Green
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "WhatsApp Link Icon", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("WhatsApp", fontSize = 11.sp, color = Color.White)
                    }

                    // Close button
                    TextButton(
                        onClick = { selectedBudgetForDetails = null },
                        modifier = Modifier.weight(0.7f)
                    ) {
                        Text("Fechar", fontSize = 11.sp)
                    }
                }
            }
        )
    }

    // Delete confirmation Dialog
    if (showDeleteConfirmDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text("Excluir Registro?", fontWeight = FontWeight.Bold) },
            text = { Text("Tem certeza que deseja excluir o orçamento #${showDeleteConfirmDialog!!.id} do cliente ${showDeleteConfirmDialog!!.clientName}?") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        viewModel.deleteBudget(showDeleteConfirmDialog!!)
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
fun BudgetHistoryItemCard(
    budget: Budget,
    currencyFormat: NumberFormat,
    dateFormat: SimpleDateFormat,
    onCardClicked: () -> Unit,
    onSharePdfClicked: () -> Unit,
    onWhatsAppDeepLinkClicked: () -> Unit,
    onStatusChanged: (String) -> Unit,
    onDeleteClicked: () -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClicked() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Card Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Orçamento #${budget.id}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = dateFormat.format(Date(budget.dateMillis)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                // Status Badge (Interactive dropdown trigger)
                Box {
                    AssistChip(
                        onClick = { dropdownExpanded = true },
                        label = { Text(budget.status, fontWeight = FontWeight.Bold) },
                        colors = AssistChipDefaults.assistChipColors(
                            labelColor = getStatusColor(budget.status)
                        ),
                        border = BorderStroke(1.dp, getStatusColor(budget.status).copy(alpha = 0.5f)),
                        modifier = Modifier.testTag("status_chip_${budget.id}")
                    )

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        listOf("Pendente", "Aprovado", "Concluído", "Cancelado").forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status, fontWeight = FontWeight.Bold, color = getStatusColor(status)) },
                                onClick = {
                                    onStatusChanged(status)
                                    dropdownExpanded = false
                                },
                                modifier = Modifier.testTag("status_option_${status}_${budget.id}")
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Card Body (Client info and value)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Cliente: ${budget.clientName}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Contato: ${budget.clientPhone}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }

                Text(
                    text = currencyFormat.format(budget.totalAmount),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(8.dp))

            // Card Footer (Sharing & deletion Quick Actions)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Number of items indicator
                val itemCount = budget.getSelectedServices().sumOf { it.quantity }
                Text(
                    text = "$itemCount ${if (itemCount == 1) "item contratado" else "itens contratados"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )

                // Share PDF / WhatsApp Link / Delete Row
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // WhatsApp Deep Link Button
                    IconButton(
                        onClick = onWhatsAppDeepLinkClicked,
                        modifier = Modifier.size(36.dp).testTag("whatsapp_deep_link_${budget.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "WhatsApp Message",
                            tint = Color(0xFF10B981), // Green WhatsApp Theme
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // PDF Share Button
                    IconButton(
                        onClick = onSharePdfClicked,
                        modifier = Modifier.size(36.dp).testTag("share_pdf_${budget.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share PDF Document",
                            tint = MaterialTheme.colorScheme.primary, // Indigo Theme
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Delete Button
                    IconButton(
                        onClick = onDeleteClicked,
                        modifier = Modifier.size(36.dp).testTag("delete_budget_${budget.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Budget Log",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun getStatusColor(status: String): Color {
    return when (status.lowercase()) {
        "aprovado" -> Color(0xFF00796B)   // Teal
        "concluído" -> Color(0xFF2E7D32)  // Green
        "cancelado" -> Color(0xFFC62828)  // Red
        else -> Color(0xFFEF6C00)         // Orange/Pendente
    }
}

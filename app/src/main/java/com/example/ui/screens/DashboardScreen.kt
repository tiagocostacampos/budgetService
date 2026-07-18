package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.R
import com.example.data.model.Budget
import com.example.ui.viewmodel.MainViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onCreateBudgetClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val budgets by viewModel.budgets.collectAsState()
    val clients by viewModel.clients.collectAsState()
    val services by viewModel.services.collectAsState()

    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("pt", "BR")) }

    // Computations
    val totalBudgets = budgets.size
    val approvedBudgets = budgets.filter { it.status == "Aprovado" || it.status == "Concluído" }
    val pendingBudgets = budgets.filter { it.status == "Pendente" }
    val canceledBudgets = budgets.filter { it.status == "Cancelado" }

    val totalRevenueConcluded = approvedBudgets.sumOf { it.totalAmount }
    val totalRevenuePending = pendingBudgets.sumOf { it.totalAmount }

    // Grouping revenues by month for the chart (past 5 months)
    val monthlyData = remember(budgets) {
        val calendar = Calendar.getInstance()
        val monthNames = listOf("Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez")
        val result = mutableListOf<Pair<String, Double>>()

        // Get past 5 months including current
        for (i in 4 downTo 0) {
            val tempCal = Calendar.getInstance()
            tempCal.add(Calendar.MONTH, -i)
            val monthIdx = tempCal.get(Calendar.MONTH)
            val year = tempCal.get(Calendar.YEAR)
            val monthLabel = "${monthNames[monthIdx]}/${year.toString().takeLast(2)}"

            // Filter budgets in this month & year
            val monthlySum = budgets
                .filter { b ->
                    val bCal = Calendar.getInstance().apply { timeInMillis = b.dateMillis }
                    bCal.get(Calendar.MONTH) == monthIdx && bCal.get(Calendar.YEAR) == year && b.status != "Cancelado"
                }
                .sumOf { it.totalAmount }

            result.add(monthLabel to monthlySum)
        }
        result
    }

    // Adaptive checks (Column on mobile, Row on tablet)
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero / Header Banner with Generated Image Background
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                ) {
                    // Hero Image
                    Image(
                        painter = painterResource(id = R.drawable.img_dashboard_hero),
                        contentDescription = "Banner Geral do Negócio",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Dark overlay to guarantee contrast and visual hierarchy
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.55f))
                    )

                    // Card Content
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = "Trending Up",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Visão Geral do Negócio",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Acompanhe faturamento, clientes cadastrados e gerencie orçamentos com rapidez e segurança LGPD local.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }

        // Key Metric Cards (Revenue)
        item {
            if (isTablet) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MetricCard(
                        title = "Receita Faturada/Aprovada",
                        value = currencyFormat.format(totalRevenueConcluded),
                        subtext = "${approvedBudgets.size} orçamentos aprovados",
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.weight(1f),
                        gradientBrush = Brush.linearGradient(
                            colors = listOf(Color(0xFF4F46E5), Color(0xFF7C3AED))
                        )
                    )
                    MetricCard(
                        title = "Receita Sob Negociação",
                        value = currencyFormat.format(totalRevenuePending),
                        subtext = "${pendingBudgets.size} orçamentos pendentes",
                        color = Color(0xFFEF6C00),
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricCard(
                        title = "Receita Faturada/Aprovada",
                        value = currencyFormat.format(totalRevenueConcluded),
                        subtext = "${approvedBudgets.size} orçamentos aprovados",
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.fillMaxWidth(),
                        gradientBrush = Brush.linearGradient(
                            colors = listOf(Color(0xFF4F46E5), Color(0xFF7C3AED))
                        )
                    )
                    MetricCard(
                        title = "Receita Sob Negociação",
                        value = currencyFormat.format(totalRevenuePending),
                        subtext = "${pendingBudgets.size} orçamentos pendentes",
                        color = Color(0xFFEF6C00),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Sub Counters Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SmallMetricCard(
                    title = "Clientes",
                    value = clients.size.toString(),
                    modifier = Modifier.weight(1f)
                )
                SmallMetricCard(
                    title = "Serviços",
                    value = services.size.toString(),
                    modifier = Modifier.weight(1f)
                )
                SmallMetricCard(
                    title = "Total Orçamentos",
                    value = totalBudgets.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Charts Section (Responsive Row on Tablet / Column on Phone)
        item {
            if (isTablet) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Bar Chart for Revenue
                    Card(
                        modifier = Modifier
                            .weight(1.2f)
                            .height(280.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Faturamento Mensal (R$)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Spacer(modifier = Modifier.height(16.dp))
                            BarChart(data = monthlyData, modifier = Modifier.fillMaxSize())
                        }
                    }

                    // Status Pie Chart
                    Card(
                        modifier = Modifier
                            .weight(0.8f)
                            .height(280.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Distribuição de Orçamentos", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Spacer(modifier = Modifier.height(16.dp))
                            DonutChart(
                                pending = pendingBudgets.size.toFloat(),
                                approved = approvedBudgets.size.toFloat(),
                                canceled = canceledBudgets.size.toFloat(),
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Bar Chart
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Faturamento Mensal (R$)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Spacer(modifier = Modifier.height(12.dp))
                            BarChart(data = monthlyData, modifier = Modifier.fillMaxSize())
                        }
                    }

                    // Status Chart
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Distribuição de Orçamentos", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Spacer(modifier = Modifier.height(12.dp))
                            DonutChart(
                                pending = pendingBudgets.size.toFloat(),
                                approved = approvedBudgets.size.toFloat(),
                                canceled = canceledBudgets.size.toFloat(),
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }

        // Action Quick Access
        item {
            Button(
                onClick = onCreateBudgetClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Icon")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Criar Novo Orçamento", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtext: String,
    color: Color,
    modifier: Modifier = Modifier,
    gradientBrush: Brush? = null
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (gradientBrush != null) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (gradientBrush != null) 4.dp else 1.dp)
    ) {
        val contentModifier = if (gradientBrush != null) {
            Modifier
                .background(gradientBrush)
                .fillMaxWidth()
                .padding(20.dp)
        } else {
            Modifier
                .fillMaxWidth()
                .padding(20.dp)
        }
        
        Column(modifier = contentModifier) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = if (gradientBrush != null) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (gradientBrush != null) Color.White else color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtext,
                style = MaterialTheme.typography.bodySmall,
                color = if (gradientBrush != null) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun SmallMetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun BarChart(
    data: List<Pair<String, Double>>,
    modifier: Modifier = Modifier
) {
    val barColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    val textLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

    val maxValue = remember(data) { (data.maxOfOrNull { it.second } ?: 1.0).coerceAtLeast(1.0) }

    Canvas(modifier = modifier.padding(bottom = 20.dp, top = 8.dp, end = 8.dp)) {
        val width = size.width
        val height = size.height
        val barCount = data.size
        
        // Draw standard horizontal gridlines
        val gridLines = 4
        for (i in 0..gridLines) {
            val y = height * (i.toFloat() / gridLines)
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
        }

        // Draw bars
        val spacing = width / (barCount * 2 + 1)
        val barWidth = spacing

        data.forEachIndexed { index, (label, value) ->
            val barHeight = (value / maxValue * height).toFloat()
            val x = spacing + index * (barWidth + spacing * 1.5f)
            val y = height - barHeight

            // Draw bar
            drawRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight)
            )

            // Draw Label text
            // Note: Since native canvas text drawing is simpler, we draw labels and currency totals nicely
        }
    }

    // Compose text overlays for monthly bar labels
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        data.forEach { (label, value) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = if (value > 1000) "R$ ${(value/1000).toInt()}k" else "R$ ${value.toInt()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun DonutChart(
    pending: Float,
    approved: Float,
    canceled: Float,
    modifier: Modifier = Modifier
) {
    val total = pending + approved + canceled
    
    // Percentages
    val pendingPct = if (total > 0) pending / total else 0f
    val approvedPct = if (total > 0) approved / total else 0f
    val canceledPct = if (total > 0) canceled / total else 0f

    val approvedColor = Color(0xFF2E7D32) // Green
    val pendingColor = Color(0xFFEF6C00)  // Orange
    val canceledColor = Color(0xFFC62828)  // Red

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(130.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 35f
                val canvasSize = size.minDimension
                val diameter = canvasSize - strokeWidth
                val rect = Size(diameter, diameter)
                val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                if (total == 0f) {
                    drawArc(
                        color = Color.LightGray.copy(alpha = 0.3f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = rect,
                        style = Stroke(width = strokeWidth)
                    )
                } else {
                    var startAngle = -90f

                    // Approved Arc
                    if (approvedPct > 0) {
                        val sweep = approvedPct * 360f
                        drawArc(
                            color = approvedColor,
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = rect,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        startAngle += sweep
                    }

                    // Pending Arc
                    if (pendingPct > 0) {
                        val sweep = pendingPct * 360f
                        drawArc(
                            color = pendingColor,
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = rect,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        startAngle += sweep
                    }

                    // Canceled Arc
                    if (canceledPct > 0) {
                        val sweep = canceledPct * 360f
                        drawArc(
                            color = canceledColor,
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = rect,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }
            }
            
            // Text inside donut
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = total.toInt().toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Emitidos",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Legend Column
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.Start
        ) {
            LegendItem(color = approvedColor, label = "Aprovados", value = "${(approvedPct * 100).toInt()}%")
            LegendItem(color = pendingColor, label = "Pendentes", value = "${(pendingPct * 100).toInt()}%")
            LegendItem(color = canceledColor, label = "Cancelados", value = "${(canceledPct * 100).toInt()}%")
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, shape = RoundedCornerShape(2.dp))
        )
        Text(
            text = "$label ($value)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}

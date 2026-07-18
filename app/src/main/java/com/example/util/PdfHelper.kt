package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.Budget
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfHelper {

    fun formatPhoneNumberForWhatsApp(phone: String): String {
        val digits = phone.replace(Regex("[^0-9]"), "")
        return if (digits.length == 10 || digits.length == 11) {
            "55$digits" // Prepend Brazil country code if not present
        } else {
            digits
        }
    }

    fun generateBudgetPdf(context: Context, budget: Budget): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size at 72 DPI
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint()
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))

        // --- Colors ---
        val primaryColor = Color.parseColor("#4F46E5") // Sleek Indigo
        val textColorDark = Color.rgb(33, 33, 33)     // Off-Black
        val textColorLight = Color.rgb(117, 117, 117) // Gray
        val lightBgColor = Color.parseColor("#F8FAFC")   // Light Slate

        // --- Gradient Header / Title ---
        val gradientShader = LinearGradient(
            0f, 0f, 595f, 0f,
            Color.parseColor("#4F46E5"), // Sleek Indigo
            Color.parseColor("#7C3AED"), // Sleek Violet
            Shader.TileMode.CLAMP
        )
        paint.shader = gradientShader
        paint.style = Paint.Style.FILL
        canvas.drawRect(0f, 0f, 595f, 110f, paint) // Modern premium banner at the top
        paint.shader = null // reset shader

        // Draw elegant circular monogram logo "OF"
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        canvas.drawCircle(60f, 55f, 25f, paint)

        paint.shader = gradientShader
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 18f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("OF", 60f, 62f, paint)
        paint.shader = null // reset shader
        paint.textAlign = Paint.Align.LEFT

        // App Name and Subtitle
        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 22f
        canvas.drawText("ORÇAFÁCIL", 100f, 52f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 11f
        canvas.drawText("Orçamento de Serviços Profissionais", 100f, 72f, paint)

        // Date and ID on top right
        paint.textAlign = Paint.Align.RIGHT
        paint.textSize = 10f
        canvas.drawText("Data: ${dateFormat.format(Date(budget.dateMillis))}", 565f, 45f, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Número: #${budget.id}", 565f, 65f, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textAlign = Paint.Align.LEFT // Reset alignment

        // --- Client Information Section ---
        var currentY = 145f
        paint.color = Color.parseColor("#4F46E5") // Elegant Indigo Accent
        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("CLIENTE", 30f, currentY, paint)

        paint.color = Color.parseColor("#E2E8F0") // Modern Divider
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f
        canvas.drawLine(30f, currentY + 6f, 565f, currentY + 6f, paint) // divider line

        currentY += 28f
        paint.style = Paint.Style.FILL
        paint.color = textColorDark
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 11f
        canvas.drawText("Nome:", 30f, currentY, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(budget.clientName, 75f, currentY, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Contato:", 340f, currentY, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(budget.clientPhone, 400f, currentY, paint)

        currentY += 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("E-mail:", 30f, currentY, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(budget.clientEmail.ifEmpty { "N/A" }, 75f, currentY, paint)

        currentY += 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Endereço:", 30f, currentY, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(budget.clientAddress.ifEmpty { "N/A" }, 95f, currentY, paint)

        // --- Status Badge ---
        currentY += 28f
        paint.color = Color.parseColor("#F1F5F9") // Light slate surface
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(30f, currentY, 565f, currentY + 36f, 8f, 8f, paint)
        
        paint.color = textColorDark
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 11f
        canvas.drawText("Status do Orçamento:", 45f, currentY + 22f, paint)
        
        // Color status based on value
        val statusColor = when (budget.status.lowercase()) {
            "aprovado", "concluído" -> Color.parseColor("#10B981") // Sleek Emerald
            "cancelado" -> Color.parseColor("#EF4444") // Red
            else -> Color.parseColor("#F59E0B") // Amber/Orange
        }
        
        // Draw small colorful status indicator circle
        paint.color = statusColor
        canvas.drawCircle(175f, currentY + 18f, 5f, paint)
        
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(budget.status.uppercase(), 188f, currentY + 22f, paint)

        // --- Services Table Headers ---
        currentY += 60f
        paint.color = Color.parseColor("#4F46E5") // Indigo Accent
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 13f
        canvas.drawText("ITENS DO ORÇAMENTO", 30f, currentY, paint)

        currentY += 12f
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#4F46E5")
        canvas.drawRoundRect(30f, currentY, 565f, currentY + 24f, 4f, 4f, paint) // Table header BG

        paint.color = Color.WHITE
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Serviço / Descrição", 40f, currentY + 16f, paint)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Qtd", 380f, currentY + 16f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Unitário", 470f, currentY + 16f, paint)
        canvas.drawText("Total", 555f, currentY + 16f, paint)
        paint.textAlign = Paint.Align.LEFT

        // --- Services List ---
        currentY += 24f
        val services = budget.getSelectedServices()
        paint.textSize = 10f

        for ((index, item) in services.withIndex()) {
            if (currentY > 670f) break // Avoid page overflow

            // Alternate row backgrounds using a very subtle slate tint
            if (index % 2 == 0) {
                paint.color = Color.parseColor("#F8FAFC")
                canvas.drawRect(30f, currentY, 565f, currentY + 26f, paint)
            }

            paint.color = textColorDark
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(item.name, 40f, currentY + 17f, paint)
            
            // Description in smaller text if available
            if (item.description.isNotEmpty()) {
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.color = Color.parseColor("#64748B") // slate gray
                paint.textSize = 8.5f
                val desc = if (item.description.length > 60) item.description.substring(0, 57) + "..." else item.description
                canvas.drawText(desc, 40f, currentY + 26f, paint)
                paint.textSize = 10f
            }

            paint.color = textColorDark
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText(item.quantity.toString(), 380f, currentY + 17f, paint)

            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(currencyFormat.format(item.price), 470f, currentY + 17f, paint)
            
            val itemTotal = item.price * item.quantity
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(currencyFormat.format(itemTotal), 555f, currentY + 17f, paint)
            
            paint.textAlign = Paint.Align.LEFT
            currentY += 28f
        }

        // --- Financial Calculations ---
        currentY += 15f
        paint.color = Color.parseColor("#E2E8F0")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawLine(30f, currentY, 565f, currentY, paint)

        currentY += 20f
        paint.style = Paint.Style.FILL
        paint.color = textColorDark
        paint.textSize = 10f
        
        val subtotal = services.sumOf { it.price * it.quantity }
        
        paint.textAlign = Paint.Align.RIGHT
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Subtotal:", 450f, currentY, paint)
        canvas.drawText(currencyFormat.format(subtotal), 555f, currentY, paint)

        if (budget.discount > 0) {
            currentY += 18f
            paint.color = Color.parseColor("#EF4444") // Red for discount
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Desconto:", 450f, currentY, paint)
            canvas.drawText("- " + currencyFormat.format(budget.discount), 555f, currentY, paint)
        }

        if (budget.addition > 0) {
            currentY += 18f
            paint.color = Color.parseColor("#10B981") // Green for addition
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Acréscimo:", 450f, currentY, paint)
            canvas.drawText("+ " + currencyFormat.format(budget.addition), 555f, currentY, paint)
        }

        currentY += 26f
        paint.color = Color.parseColor("#4F46E5") // Elegant Accent
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 13f
        canvas.drawText("VALOR TOTAL:", 450f, currentY, paint)
        canvas.drawText(currencyFormat.format(budget.totalAmount), 555f, currentY, paint)

        paint.textAlign = Paint.Align.LEFT // Reset alignment

        // --- Signature Section ---
        val sigY = 705f
        paint.color = Color.parseColor("#CBD5E1")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawLine(50f, sigY, 220f, sigY, paint)
        canvas.drawLine(375f, sigY, 545f, sigY, paint)
        
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#64748B")
        paint.textSize = 8f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Assinatura do Prestador", 135f, sigY + 12f, paint)
        canvas.drawText("Assinatura do Cliente", 460f, sigY + 12f, paint)
        paint.textAlign = Paint.Align.LEFT

        // --- Terms / Legal Notice (LGPD Compliance) ---
        currentY = 745f
        paint.color = textColorLight
        paint.textSize = 7.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        
        paint.color = Color.parseColor("#E2E8F0")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.8f
        canvas.drawLine(30f, currentY, 565f, currentY, paint)
        paint.style = Paint.Style.FILL
        paint.color = textColorLight

        currentY += 13f
        canvas.drawText("Observações / Condições:", 30f, currentY, paint)
        currentY += 11f
        canvas.drawText("1. Este orçamento é válido por 15 dias a partir da data de emissão.", 30f, currentY, paint)
        currentY += 10f
        canvas.drawText("2. Os dados coletados neste documento são confidenciais e tratados exclusivamente para faturamento,", 30f, currentY, paint)
        currentY += 10f
        canvas.drawText("   em conformidade com a Lei Geral de Proteção de Dados Pessoais (LGPD) - Lei nº 13.709/2018.", 30f, currentY, paint)

        // Footer Brand
        currentY += 21f
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        paint.color = textColorLight
        canvas.drawText("Obrigado por escolher nossos serviços! OrçaFacil - Tecnologia e Transparência.", 297f, currentY, paint)

        pdfDocument.finishPage(page)

        // Save PDF to cache dir
        val file = File(context.cacheDir, "Orcamento_${budget.id}.pdf")
        try {
            val fos = FileOutputStream(file)
            pdfDocument.writeTo(fos)
            pdfDocument.close()
            fos.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            return null
        }
    }

    fun sharePdfViaWhatsApp(context: Context, pdfFile: File, clientPhone: String) {
        val authority = "${context.packageName}.fileprovider"
        try {
            val pdfUri: Uri = FileProvider.getUriForFile(context, authority, pdfFile)
            
            // Build share intent
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, pdfUri)
                putExtra(Intent.EXTRA_SUBJECT, "Orçamento de Serviços - OrçaFacil")
                putExtra(Intent.EXTRA_TEXT, "Olá! Segue em anexo o orçamento solicitado via OrçaFacil.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            // Format phone number to target WhatsApp directly
            val formattedPhone = clientPhone.replace(Regex("[^0-9]"), "")
            if (formattedPhone.isNotEmpty()) {
                val jidPhone = formatPhoneNumberForWhatsApp(clientPhone)
                shareIntent.putExtra("jid", "$jidPhone@s.whatsapp.net") // Standard WhatsApp protocol format
                
                // Let's check if WhatsApp package is available
                try {
                    val pm = context.packageManager
                    pm.getPackageInfo("com.whatsapp", 0)
                    shareIntent.`package` = "com.whatsapp"
                } catch (e: Exception) {
                    // WhatsApp is not installed, open standard chooser instead
                }
            }

            val chooser = Intent.createChooser(shareIntent, "Compartilhar Orçamento")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Erro ao compartilhar arquivo: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareViaWhatsAppDeepLink(context: Context, budget: Budget) {
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        val cleanPhone = formatPhoneNumberForWhatsApp(budget.clientPhone)
        
        // Build a highly professional summary of the quote
        val services = budget.getSelectedServices()
        val servicesListText = services.joinToString("\n") { item ->
            "• *${item.quantity}x ${item.name}*: ${currencyFormat.format(item.price * item.quantity)}"
        }

        val message = """
            *ORÇAMENTO PROFISSIONAL*
            *OrçaFácil - Número:* #${budget.id}
            
            Olá, *${budget.clientName}*!
            Aqui está o resumo do orçamento solicitado para seus serviços:
            
            $servicesListText
            
            ---------------------------------------
            ${if (budget.discount > 0) "*Desconto:* - ${currencyFormat.format(budget.discount)}\n" else ""}${if (budget.addition > 0) "*Acréscimo:* + ${currencyFormat.format(budget.addition)}\n" else ""}*VALOR TOTAL:* *${currencyFormat.format(budget.totalAmount)}*
            *Status:* ${budget.status.uppercase()}
            
            _Emitido de forma segura via aplicativo OrçaFácil._
        """.trimIndent()

        try {
            val encodedMessage = java.net.URLEncoder.encode(message, "UTF-8")
            val uriString = "https://wa.me/$cleanPhone?text=$encodedMessage"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Erro ao iniciar contato no WhatsApp: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

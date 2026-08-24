package com.example.data.export

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.data.model.Goal
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.data.model.formatRupee
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object JsonBackupManager {

  fun exportToJson(
    transactions: List<Transaction>,
    goals: List<Goal>
  ): String {
    val root = JSONObject()
    val txArray = JSONArray()
    for (tx in transactions) {
      val obj = JSONObject()
      obj.put("id", tx.id)
      obj.put("title", tx.title)
      obj.put("amount", tx.amount)
      obj.put("type", tx.type.name)
      obj.put("category", tx.category)
      obj.put("accountTag", tx.accountTag)
      obj.put("timestamp", tx.timestamp)
      obj.put("isAuto", tx.isAuto)
      obj.put("isSecret", tx.isSecret)
      obj.put("note", tx.note)
      obj.put("receiptUri", tx.receiptUri ?: "")
      txArray.put(obj)
    }
    root.put("transactions", txArray)

    val goalArray = JSONArray()
    for (goal in goals) {
      val obj = JSONObject()
      obj.put("id", goal.id)
      obj.put("title", goal.title)
      obj.put("cadence", goal.cadence)
      obj.put("category", goal.category)
      obj.put("targetAmount", goal.targetAmount)
      goalArray.put(obj)
    }
    root.put("goals", goalArray)

    return root.toString(2)
  }

  fun importFromJson(jsonString: String): Pair<List<Transaction>, List<Goal>> {
    val root = JSONObject(jsonString)
    val transactions = mutableListOf<Transaction>()
    val goals = mutableListOf<Goal>()

    if (root.has("transactions")) {
      val txArray = root.getJSONArray("transactions")
      for (i in 0 until txArray.length()) {
        val obj = txArray.getJSONObject(i)
        val receipt = obj.optString("receiptUri", "")
        transactions.add(
          Transaction(
            id = obj.optLong("id", 0),
            title = obj.optString("title", ""),
            amount = obj.optDouble("amount", 0.0),
            type = if (obj.optString("type") == "INCOME") TransactionType.INCOME else TransactionType.EXPENSE,
            category = obj.optString("category", "Food"),
            accountTag = obj.optString("accountTag", "UPI"),
            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
            isAuto = obj.optBoolean("isAuto", false),
            isSecret = obj.optBoolean("isSecret", false),
            note = obj.optString("note", ""),
            receiptUri = if (receipt.isBlank()) null else receipt
          )
        )
      }
    }

    if (root.has("goals")) {
      val goalArray = root.getJSONArray("goals")
      for (i in 0 until goalArray.length()) {
        val obj = goalArray.getJSONObject(i)
        goals.add(
          Goal(
            id = obj.optLong("id", 0),
            title = obj.optString("title", "Budget"),
            cadence = obj.optString("cadence", "monthly"),
            category = obj.optString("category", "Food"),
            targetAmount = obj.optDouble("targetAmount", 1000.0)
          )
        )
      }
    }

    return Pair(transactions, goals)
  }
}

object PdfExportManager {

  fun generateActivityPdf(
    context: Context,
    transactions: List<Transaction>,
    periodLabel: String
  ): File {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size in points
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas

    val titlePaint = Paint().apply {
      color = Color.parseColor("#111827")
      textSize = 22f
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      isAntiAlias = true
    }

    val subPaint = Paint().apply {
      color = Color.parseColor("#6B7280")
      textSize = 12f
      isAntiAlias = true
    }

    val headerBgPaint = Paint().apply {
      color = Color.parseColor("#F8F9FA")
      style = Paint.Style.FILL
    }

    val headerTextPaint = Paint().apply {
      color = Color.parseColor("#111827")
      textSize = 11f
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      isAntiAlias = true
    }

    val rowTextPaint = Paint().apply {
      color = Color.parseColor("#111827")
      textSize = 10f
      isAntiAlias = true
    }

    val expenseAmountPaint = Paint().apply {
      color = Color.parseColor("#FF4D4D")
      textSize = 11f
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      isAntiAlias = true
      textAlign = Paint.Align.RIGHT
    }

    val incomeAmountPaint = Paint().apply {
      color = Color.parseColor("#22C55E")
      textSize = 11f
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      isAntiAlias = true
      textAlign = Paint.Align.RIGHT
    }

    val linePaint = Paint().apply {
      color = Color.parseColor("#E5E7EB")
      strokeWidth = 1f
    }

    // Title
    var y = 48f
    canvas.drawText("Bachat — Activity & Expense Report", 40f, y, titlePaint)
    y += 18f
    val sdf = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
    canvas.drawText("Period: $periodLabel  |  Generated on: ${sdf.format(Date())}", 40f, y, subPaint)
    y += 24f

    // Header Row
    canvas.drawRect(40f, y, 555f, y + 24f, headerBgPaint)
    canvas.drawText("DATE & TIME", 48f, y + 16f, headerTextPaint)
    canvas.drawText("CATEGORY", 160f, y + 16f, headerTextPaint)
    canvas.drawText("TAG", 260f, y + 16f, headerTextPaint)
    canvas.drawText("NOTE", 320f, y + 16f, headerTextPaint)
    canvas.drawText("AMOUNT", 545f, y + 16f, Paint(headerTextPaint).apply { textAlign = Paint.Align.RIGHT })
    y += 30f

    val dateFmt = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())

    var totalExpense = 0.0
    var totalIncome = 0.0

    for (tx in transactions) {
      if (y > 780f) break // Single page fit

      if (tx.type == TransactionType.EXPENSE) totalExpense += tx.amount else totalIncome += tx.amount

      val dateStr = dateFmt.format(Date(tx.timestamp))
      canvas.drawText(dateStr, 48f, y + 14f, rowTextPaint)
      canvas.drawText(tx.category + if (tx.isAuto) " (AUTO)" else "", 160f, y + 14f, rowTextPaint)
      canvas.drawText(tx.accountTag, 260f, y + 14f, rowTextPaint)
      val noteStr = if (tx.note.isNotBlank()) tx.note else "—"
      canvas.drawText(if (noteStr.length > 25) noteStr.take(22) + "..." else noteStr, 320f, y + 14f, rowTextPaint)

      val amountStr = (if (tx.type == TransactionType.EXPENSE) "-" else "+") + formatRupee(tx.amount)
      val p = if (tx.type == TransactionType.EXPENSE) expenseAmountPaint else incomeAmountPaint
      canvas.drawText(amountStr, 545f, y + 14f, p)

      canvas.drawLine(40f, y + 22f, 555f, y + 22f, linePaint)
      y += 26f
    }

    // Summary at bottom
    y += 16f
    canvas.drawText("Total Income: ${formatRupee(totalIncome)}", 48f, y, incomeAmountPaint.apply { textAlign = Paint.Align.LEFT })
    canvas.drawText("Total Expense: ${formatRupee(totalExpense)}", 300f, y, expenseAmountPaint.apply { textAlign = Paint.Align.LEFT })

    pdfDocument.finishPage(page)

    val outputDir = File(context.cacheDir, "reports").apply { mkdirs() }
    val outputFile = File(outputDir, "Bachat_Activity_${System.currentTimeMillis()}.pdf")
    val fos = FileOutputStream(outputFile)
    pdfDocument.writeTo(fos)
    pdfDocument.close()
    fos.close()

    return outputFile
  }
}

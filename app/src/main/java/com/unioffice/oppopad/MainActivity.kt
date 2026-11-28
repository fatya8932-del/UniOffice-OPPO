package com.unioffice.oppopad

import android.content.ContentValues
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private var colorIndex = 0
    private val colors = listOf(Color.BLUE, Color.RED, Color.BLACK, Color.parseColor("#107C41"))
    private val colorNames = listOf("น้ำเงิน", "แดง", "ดำ", "เขียว")

    private var isBold = false
    private var isItalic = false
    private var isUnderline = false
    private var isCentered = false
    private var currentFontSize = 18f

    private val excelCells = mutableListOf<EditText>()
    private val pptSlides = mutableListOf("")
    private var currentSlideIndex = 0
    private var currentAppMode = "WORD"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val layoutHomeHub = findViewById<LinearLayout>(R.id.layoutHomeHub)
        val layoutEditorWorkspace = findViewById<LinearLayout>(R.id.layoutEditorWorkspace)
        val ribbonHeader = findViewById<HorizontalScrollView>(R.id.ribbonHeader)
        val tvAppTitle = findViewById<TextView>(R.id.tvAppTitle)

        val btnOpenWord = findViewById<Button>(R.id.btnOpenWord)
        val btnOpenExcel = findViewById<Button>(R.id.btnOpenExcel)
        val btnOpenPpt = findViewById<Button>(R.id.btnOpenPpt)
        val btnBackHome = findViewById<Button>(R.id.btnBackHome)

        val btnBold = findViewById<Button>(R.id.btnBold)
        val btnItalic = findViewById<Button>(R.id.btnItalic)
        val btnUnderline = findViewById<Button>(R.id.btnUnderline)
        val btnAlignCenter = findViewById<Button>(R.id.btnAlignCenter)
        val btnFontSize = findViewById<Button>(R.id.btnFontSize)
        val btnExcelSum = findViewById<Button>(R.id.btnExcelSum)
        val btnPptNextSlide = findViewById<Button>(R.id.btnPptNextSlide)

        val btnModeType = findViewById<Button>(R.id.btnModeType)
        val btnModeDraw = findViewById<Button>(R.id.btnModeDraw)
        val btnPenColor = findViewById<Button>(R.id.btnPenColor)
        val btnClearDraw = findViewById<Button>(R.id.btnClearDraw)
        val btnExportPdf = findViewById<Button>(R.id.btnExportPdf)

        val etDocument = findViewById<EditText>(R.id.etDocument)
        val scrollExcel = findViewById<ScrollView>(R.id.scrollExcel)
        val tableExcel = findViewById<TableLayout>(R.id.tableExcel)
        val stylusCanvas = findViewById<StylusCanvasView>(R.id.stylusCanvas)
        val documentContainer = findViewById<FrameLayout>(R.id.documentContainer)

        stylusCanvas.isDrawingEnabled = false
        buildExcelGrid(tableExcel)

        fun openWorkspace(mode: String) {
            currentAppMode = mode
            layoutHomeHub.visibility = View.GONE
            layoutEditorWorkspace.visibility = View.VISIBLE
            stylusCanvas.clearCanvas()

            when (mode) {
                "WORD" -> {
                    tvAppTitle.text = "Word"
                    ribbonHeader.setBackgroundColor(Color.parseColor("#185ABD"))
                    etDocument.visibility = View.VISIBLE
                    scrollExcel.visibility = View.GONE
                    btnExcelSum.visibility = View.GONE
                    btnPptNextSlide.visibility = View.GONE
                    etDocument.hint = "พิมพ์รายงานหรือเลกเชอร์ที่นี่..."
                }
                "EXCEL" -> {
                    tvAppTitle.text = "Excel"
                    ribbonHeader.setBackgroundColor(Color.parseColor("#107C41"))
                    etDocument.visibility = View.GONE
                    scrollExcel.visibility = View.VISIBLE
                    btnExcelSum.visibility = View.VISIBLE
                    btnPptNextSlide.visibility = View.GONE
                }
                "PPT" -> {
                    tvAppTitle.text = "PowerPoint"
                    ribbonHeader.setBackgroundColor(Color.parseColor("#C43E1C"))
                    etDocument.visibility = View.VISIBLE
                    scrollExcel.visibility = View.GONE
                    btnExcelSum.visibility = View.GONE
                    btnPptNextSlide.visibility = View.VISIBLE
                    currentSlideIndex = 0
                    etDocument.setText(pptSlides[0])
                    btnPptNextSlide.text = "➕ สไลด์ถัดไป (1/${pptSlides.size})"
                    etDocument.hint = "พิมพ์หัวข้อและเนื้อหาสไลด์ที่ 1..."
                }
            }
        }

        btnOpenWord.setOnClickListener { openWorkspace("WORD") }
        btnOpenExcel.setOnClickListener { openWorkspace("EXCEL") }
        btnOpenPpt.setOnClickListener { openWorkspace("PPT") }

        btnBackHome.setOnClickListener {
            layoutEditorWorkspace.visibility = View.GONE
            layoutHomeHub.visibility = View.VISIBLE
        }

        fun updateTextStyle() {
            val style = when {
                isBold && isItalic -> Typeface.BOLD_ITALIC
                isBold -> Typeface.BOLD
                isItalic -> Typeface.ITALIC
                else -> Typeface.NORMAL
            }
            etDocument.setTypeface(null, style)
            etDocument.paintFlags = if (isUnderline) {
                etDocument.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            } else {
                etDocument.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
            }
        }

        btnBold.setOnClickListener {
            isBold = !isBold
            updateTextStyle()
        }
        btnItalic.setOnClickListener {
            isItalic = !isItalic
            updateTextStyle()
        }
        btnUnderline.setOnClickListener {
            isUnderline = !isUnderline
            updateTextStyle()
        }
        btnAlignCenter.setOnClickListener {
            isCentered = !isCentered
            etDocument.gravity = if (isCentered) Gravity.CENTER else (Gravity.TOP or Gravity.START)
            btnAlignCenter.text = if (isCentered) "ชิดซ้าย" else "จัดกึ่งกลาง"
        }
        btnFontSize.setOnClickListener {
            currentFontSize = when (currentFontSize) {
                18f -> 24f
                24f -> 32f
                32f -> 14f
                else -> 18f
            }
            etDocument.textSize = currentFontSize
            btnFontSize.text = "ขนาด: ${currentFontSize.toInt()}"
        }

        btnExcelSum.setOnClickListener {
            var total = 0.0
            for (cell in excelCells) {
                val value = cell.text.toString().toDoubleOrNull()
                if (value != null) total += value
            }
            Toast.makeText(this, "ผลรวมตัวเลขทั้งหมดในตาราง = $total", Toast.LENGTH_LONG).show()
        }

        btnPptNextSlide.setOnClickListener {
            pptSlides[currentSlideIndex] = etDocument.text.toString()
            currentSlideIndex++
            if (currentSlideIndex >= pptSlides.size) {
                pptSlides.add("")
            }
            etDocument.setText(pptSlides[currentSlideIndex])
            btnPptNextSlide.text = "➕ สไลด์ถัดไป (${currentSlideIndex + 1}/${pptSlides.size})"
            Toast.makeText(this, "สไลด์ที่ ${currentSlideIndex + 1}", Toast.LENGTH_SHORT).show()
        }

        btnModeType.setOnClickListener {
            stylusCanvas.isDrawingEnabled = false
            btnModeType.setBackgroundColor(Color.WHITE)
            btnModeType.setTextColor(Color.parseColor("#185ABD"))
            btnModeDraw.setBackgroundColor(Color.parseColor("#3B82F6"))
            btnModeDraw.setTextColor(Color.WHITE)
        }

        btnModeDraw.setOnClickListener {
            stylusCanvas.isDrawingEnabled = true
            etDocument.clearFocus()
            btnModeDraw.setBackgroundColor(Color.WHITE)
            btnModeDraw.setTextColor(Color.parseColor("#185ABD"))
            btnModeType.setBackgroundColor(Color.parseColor("#3B82F6"))
            btnModeType.setTextColor(Color.WHITE)
        }

        btnPenColor.setOnClickListener {
            colorIndex = (colorIndex + 1) % colors.size
            stylusCanvas.setPenColor(colors[colorIndex])
            btnPenColor.text = "🎨 ${colorNames[colorIndex]}"
        }

        btnClearDraw.setOnClickListener {
            stylusCanvas.clearCanvas()
        }

        btnExportPdf.setOnClickListener {
            exportToPdf(documentContainer)
        }
    }

    private fun buildExcelGrid(table: TableLayout) {
        val cols = listOf("", "A", "B", "C", "D", "E", "F")
        val headerRow = TableRow(this)
        for (col in cols) {
            val tv = TextView(this).apply {
                text = col
                gravity = Gravity.CENTER
                setPadding(24, 12, 24, 12)
                setBackgroundColor(Color.parseColor("#E5E7EB"))
                setTextColor(Color.BLACK)
                setTypeface(null, Typeface.BOLD)
            }
            headerRow.addView(tv)
        }
        table.addView(headerRow)

        for (r in 1..15) {
            val row = TableRow(this)
            val rowNum = TextView(this).apply {
                text = "$r"
                gravity = Gravity.CENTER
                setPadding(20, 12, 20, 12)
                setBackgroundColor(Color.parseColor("#E5E7EB"))
                setTextColor(Color.BLACK)
            }
            row.addView(rowNum)

            for (c in 1..6) {
                val cell = EditText(this).apply {
                    minWidth = 140
                    setPadding(12, 8, 12, 8)
                    setBackgroundResource(android.R.drawable.edit_text)
                    inputType = InputType.TYPE_CLASS_TEXT
                    textSize = 14f
                }
                excelCells.add(cell)
                row.addView(cell)
            }
            table.addView(row)
        }
    }

    private fun exportToPdf(view: FrameLayout) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(view.width, view.height, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        view.draw(page.canvas)
        pdfDocument.finishPage(page)

        val fileName = "UniOffice_${currentAppMode}_${System.currentTimeMillis()}.pdf"
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let {
                    contentResolver.openOutputStream(it)?.use { outputStream ->
                        pdfDocument.writeTo(outputStream)
                    }
                }
            } else {
                val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
                FileOutputStream(file).use { outputStream ->
                    pdfDocument.writeTo(outputStream)
                }
            }
            Toast.makeText(this, "บันทึก PDF ($currentAppMode) ลงโฟลเดอร์ Downloads สำเร็จ!", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "เกิดข้อผิดพลาด: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            pdfDocument.close()
        }
    }
}

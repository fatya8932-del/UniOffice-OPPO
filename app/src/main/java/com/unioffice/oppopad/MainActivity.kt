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
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private var penColorIndex = 0
    private val penColors = listOf(Color.BLUE, Color.RED, Color.BLACK, Color.parseColor("#107C41"))
    private val penColorNames = listOf("น้ำเงิน", "แดง", "ดำ", "เขียว")

    private var textColorIndex = 0
    private val textColors = listOf(Color.parseColor("#111827"), Color.parseColor("#185ABD"), Color.parseColor("#DC2626"), Color.parseColor("#107C41"))
    private val textColorNames = listOf("A สีดำ", "A น้ำเงิน", "A สีแดง", "A สีเขียว")

    private var paperColorIndex = 0
    private val paperColors = listOf(Color.WHITE, Color.parseColor("#FEF9C3"), Color.parseColor("#ECFDF5"), Color.parseColor("#EFF6FF"))

    private var fontIndex = 0
    private val fontTypes = listOf(Typeface.SERIF, Typeface.SANS_SERIF, Typeface.MONOSPACE)
    private val fontNames = listOf("TH Sarabun ▼", "Angsana ▼", "Cordia ▼")

    private var alignIndex = 0
    private var marginIndex = 0
    private var spacingIndex = 0
    private var isBold = false
    private var isItalic = false
    private var isUnderline = false
    private var isStrike = false
    private var currentFontSize = 18f

    private val excelCells = mutableListOf<EditText>()
    private var activeExcelCell: EditText? = null
    private var isSyncingFormula = false

    private val pptSlides = mutableListOf("")
    private var currentSlideIndex = 0
    private var currentAppMode = "WORD"
    private var themeColorHex = "#185ABD"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val layoutHomeHub = findViewById<LinearLayout>(R.id.layoutHomeHub)
        val layoutEditorWorkspace = findViewById<LinearLayout>(R.id.layoutEditorWorkspace)
        val ribbonTopScroll = findViewById<HorizontalScrollView>(R.id.ribbonTopScroll)
        val ribbonSubBar = findViewById<HorizontalScrollView>(R.id.ribbonSubBar)
        val tvAppTitle = findViewById<TextView>(R.id.tvAppTitle)

        val btnOpenWord = findViewById<Button>(R.id.btnOpenWord)
        val btnOpenExcel = findViewById<Button>(R.id.btnOpenExcel)
        val btnOpenPpt = findViewById<Button>(R.id.btnOpenPpt)
        val btnBackHome = findViewById<Button>(R.id.btnBackHome)

        val tabHome = findViewById<Button>(R.id.tabHome)
        val tabInsert = findViewById<Button>(R.id.tabInsert)
        val tabDraw = findViewById<Button>(R.id.tabDraw)
        val tabLayout = findViewById<Button>(R.id.tabLayout)
        val tabView = findViewById<Button>(R.id.tabView)

        val groupHomeTools = findViewById<LinearLayout>(R.id.groupHomeTools)
        val groupInsertTools = findViewById<LinearLayout>(R.id.groupInsertTools)
        val groupDrawTools = findViewById<LinearLayout>(R.id.groupDrawTools)
        val groupLayoutTools = findViewById<LinearLayout>(R.id.groupLayoutTools)
        val groupViewTools = findViewById<LinearLayout>(R.id.groupViewTools)

        val btnFontFamily = findViewById<Button>(R.id.btnFontFamily)
        val btnFontSize = findViewById<Button>(R.id.btnFontSize)
        val btnBold = findViewById<Button>(R.id.btnBold)
        val btnItalic = findViewById<Button>(R.id.btnItalic)
        val btnUnderline = findViewById<Button>(R.id.btnUnderline)
        val btnStrike = findViewById<Button>(R.id.btnStrike)
        val btnTextColor = findViewById<Button>(R.id.btnTextColor)
        val btnHighlightPaper = findViewById<Button>(R.id.btnHighlightPaper)
        val btnBulletList = findViewById<Button>(R.id.btnBulletList)
        val btnAlignCycle = findViewById<Button>(R.id.btnAlignCycle)

        val btnInsertTable = findViewById<Button>(R.id.btnInsertTable)
        val btnInsertTextBox = findViewById<Button>(R.id.btnInsertTextBox)
        val btnInsertDate = findViewById<Button>(R.id.btnInsertDate)
        val btnInsertDivider = findViewById<Button>(R.id.btnInsertDivider)
        val btnPptAddSlide = findViewById<Button>(R.id.btnPptAddSlide)

        val btnModeType = findViewById<Button>(R.id.btnModeType)
        val btnModeDraw = findViewById<Button>(R.id.btnModeDraw)
        val btnPenColor = findViewById<Button>(R.id.btnPenColor)
        val btnClearDraw = findViewById<Button>(R.id.btnClearDraw)

        val btnPageMargin = findViewById<Button>(R.id.btnPageMargin)
        val btnLineSpacing = findViewById<Button>(R.id.btnLineSpacing)

        val btnToggleRuler = findViewById<Button>(R.id.btnToggleRuler)
        val btnStartSlideshow = findViewById<Button>(R.id.btnStartSlideshow)
        val btnExitSlideshow = findViewById<Button>(R.id.btnExitSlideshow)
        val btnExportPdf = findViewById<Button>(R.id.btnExportPdf)

        val excelFormulaBar = findViewById<LinearLayout>(R.id.excelFormulaBar)
        val tvSelectedCell = findViewById<TextView>(R.id.tvSelectedCell)
        val etFormulaInput = findViewById<EditText>(R.id.etFormulaInput)
        val btnCalcSum = findViewById<Button>(R.id.btnCalcSum)
        val btnCalcAvg = findViewById<Button>(R.id.btnCalcAvg)
        val btnCalcMax = findViewById<Button>(R.id.btnCalcMax)

        val tvWordRuler = findViewById<TextView>(R.id.tvWordRuler)
        val pptSlideSidebar = findViewById<ScrollView>(R.id.pptSlideSidebar)
        val pptSlideListContainer = findViewById<LinearLayout>(R.id.pptSlideListContainer)

        val etDocument = findViewById<EditText>(R.id.etDocument)
        val scrollExcel = findViewById<ScrollView>(R.id.scrollExcel)
        val tableExcel = findViewById<TableLayout>(R.id.tableExcel)
        val stylusCanvas = findViewById<StylusCanvasView>(R.id.stylusCanvas)
        val documentContainer = findViewById<FrameLayout>(R.id.documentContainer)

        etDocument.typeface = fontTypes[0]
        stylusCanvas.isDrawingEnabled = false
        fun selectRibbonTab(tabIndex: Int) {
            groupHomeTools.visibility = if (tabIndex == 0) View.VISIBLE else View.GONE
            groupInsertTools.visibility = if (tabIndex == 1) View.VISIBLE else View.GONE
            groupDrawTools.visibility = if (tabIndex == 2) View.VISIBLE else View.GONE
            groupLayoutTools.visibility = if (tabIndex == 3) View.VISIBLE else View.GONE
            groupViewTools.visibility = if (tabIndex == 4) View.VISIBLE else View.GONE

            val tabs = listOf(tabHome, tabInsert, tabDraw, tabLayout, tabView)
            val themeColor = Color.parseColor(themeColorHex)
            for (i in tabs.indices) {
                if (i == tabIndex) {
                    tabs[i].setBackgroundColor(Color.parseColor("#F3F4F6"))
                    tabs[i].setTextColor(themeColor)
                } else {
                    tabs[i].setBackgroundColor(Color.argb(60, 255, 255, 255))
                    tabs[i].setTextColor(Color.WHITE)
                }
            }
        }

        tabHome.setOnClickListener { selectRibbonTab(0) }
        tabInsert.setOnClickListener { selectRibbonTab(1) }
        tabDraw.setOnClickListener { selectRibbonTab(2) }
        tabLayout.setOnClickListener { selectRibbonTab(3) }
        tabView.setOnClickListener { selectRibbonTab(4) }

        fun refreshPptSidebar() {
            pptSlideListContainer.removeAllViews()
            for (i in pptSlides.indices) {
                val slideBtn = Button(this).apply {
                    text = "สไลด์ ${i + 1}\n${pptSlides[i].take(12)}"
                    textSize = 12f
                    setPadding(8, 16, 8, 16)
                    setBackgroundColor(if (i == currentSlideIndex) Color.parseColor("#FED7AA") else Color.WHITE)
                    setTextColor(Color.BLACK)
                    setOnClickListener {
                        pptSlides[currentSlideIndex] = etDocument.text.toString()
                        currentSlideIndex = i
                        etDocument.setText(pptSlides[currentSlideIndex])
                        refreshPptSidebar()
                    }
                }
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                params.setMargins(0, 0, 0, 10)
                pptSlideListContainer.addView(slideBtn, params)
            }
        }

        buildExcelGrid(tableExcel, tvSelectedCell, etFormulaInput)

        etFormulaInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!isSyncingFormula && activeExcelCell != null) {
                    isSyncingFormula = true
                    activeExcelCell?.setText(s?.toString() ?: "")
                    isSyncingFormula = false
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        fun openWorkspace(mode: String) {
            currentAppMode = mode
            layoutHomeHub.visibility = View.GONE
            layoutEditorWorkspace.visibility = View.VISIBLE
            stylusCanvas.clearCanvas()

            when (mode) {
                "WORD" -> {
                    themeColorHex = "#185ABD"
                    tvAppTitle.text = "Word"
                    ribbonTopScroll.setBackgroundColor(Color.parseColor(themeColorHex))
                    btnBackHome.setTextColor(Color.parseColor(themeColorHex))
                    etDocument.visibility = View.VISIBLE
                    scrollExcel.visibility = View.GONE
                    excelFormulaBar.visibility = View.GONE
                    tvWordRuler.visibility = View.VISIBLE
                    pptSlideSidebar.visibility = View.GONE
                    btnPptAddSlide.visibility = View.GONE
                    btnStartSlideshow.visibility = View.GONE
                    etDocument.hint = "พิมพ์รายงานหรือเลกเชอร์ที่นี่..."
                }
                "EXCEL" -> {
                    themeColorHex = "#107C41"
                    tvAppTitle.text = "Excel"
                    ribbonTopScroll.setBackgroundColor(Color.parseColor(themeColorHex))
                    btnBackHome.setTextColor(Color.parseColor(themeColorHex))
                    etDocument.visibility = View.GONE
                    scrollExcel.visibility = View.VISIBLE
                    excelFormulaBar.visibility = View.VISIBLE
                    tvWordRuler.visibility = View.GONE
                    pptSlideSidebar.visibility = View.GONE
                    btnPptAddSlide.visibility = View.GONE
                    btnStartSlideshow.visibility = View.GONE
                }
                "PPT" -> {
                    themeColorHex = "#C43E1C"
                    tvAppTitle.text = "PowerPoint"
                    ribbonTopScroll.setBackgroundColor(Color.parseColor(themeColorHex))
                    btnBackHome.setTextColor(Color.parseColor(themeColorHex))
                    etDocument.visibility = View.VISIBLE
                    scrollExcel.visibility = View.GONE
                    excelFormulaBar.visibility = View.GONE
                    tvWordRuler.visibility = View.GONE
                    pptSlideSidebar.visibility = View.VISIBLE
                    btnPptAddSlide.visibility = View.VISIBLE
                    btnStartSlideshow.visibility = View.VISIBLE
                    currentSlideIndex = 0
                    etDocument.setText(pptSlides[0])
                    etDocument.hint = "คลิกเพื่อเพิ่มชื่อเรื่องและเนื้อหาสไลด์..."
                    refreshPptSidebar()
                }
            }
            selectRibbonTab(0)
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
            etDocument.setTypeface(fontTypes[fontIndex], style)
            var flags = etDocument.paintFlags
            flags = if (isUnderline) (flags or Paint.UNDERLINE_TEXT_FLAG) else (flags and Paint.UNDERLINE_TEXT_FLAG.inv())
            flags = if (isStrike) (flags or Paint.STRIKE_THRU_TEXT_FLAG) else (flags and Paint.STRIKE_THRU_TEXT_FLAG.inv())
            etDocument.paintFlags = flags
        }

        btnFontFamily.setOnClickListener {
            fontIndex = (fontIndex + 1) % fontTypes.size
            btnFontFamily.text = fontNames[fontIndex]
            updateTextStyle()
        }

        btnFontSize.setOnClickListener {
            currentFontSize = when (currentFontSize) {
                18f -> 22f
                22f -> 28f
                28f -> 36f
                36f -> 14f
                else -> 18f
            }
            etDocument.textSize = currentFontSize
            btnFontSize.text = "${currentFontSize.toInt()} ▼"
        }

        btnBold.setOnClickListener { isBold = !isBold; updateTextStyle() }
        btnItalic.setOnClickListener { isItalic = !isItalic; updateTextStyle() }
        btnUnderline.setOnClickListener { isUnderline = !isUnderline; updateTextStyle() }
        btnStrike.setOnClickListener { isStrike = !isStrike; updateTextStyle() }

        btnTextColor.setOnClickListener {
            textColorIndex = (textColorIndex + 1) % textColors.size
            etDocument.setTextColor(textColors[textColorIndex])
            btnTextColor.text = textColorNames[textColorIndex]
        }

        btnHighlightPaper.setOnClickListener {
            paperColorIndex = (paperColorIndex + 1) % paperColors.size
            documentContainer.setBackgroundColor(paperColors[paperColorIndex])
        }

        btnBulletList.setOnClickListener { etDocument.append("\n  • ") }

        btnAlignCycle.setOnClickListener {
            alignIndex = (alignIndex + 1) % 3
            when (alignIndex) {
                0 -> { etDocument.gravity = Gravity.TOP or Gravity.START; btnAlignCycle.text = "ชิดซ้าย" }
                1 -> { etDocument.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL; btnAlignCycle.text = "กึ่งกลาง" }
                2 -> { etDocument.gravity = Gravity.TOP or Gravity.END; btnAlignCycle.text = "ชิดขวา" }
            }
        }

        btnInsertTable.setOnClickListener {
            etDocument.append("\n┌──────────┬──────────┬──────────┐\n│ หัวข้อ 1   │ หัวข้อ 2   │ หัวข้อ 3   │\n├──────────┼──────────┼──────────┤\n│ ข้อมูล     │ ข้อมูล     │ ข้อมูล     │\n└──────────┴──────────┴──────────┘\n")
        }

        btnInsertTextBox.setOnClickListener {
            etDocument.append("\n╔════════════════════════════╗\n║  พิมพ์ข้อความสำคัญในกรอบนี้  ║\n╚════════════════════════════╝\n")
        }

        btnInsertDate.setOnClickListener {
            val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
            etDocument.append("\nวันที่บันทึก: $dateStr\n")
        }

        btnInsertDivider.setOnClickListener {
            etDocument.append("\n────────────────────────────────────────\n")
        }

        btnPptAddSlide.setOnClickListener {
            pptSlides[currentSlideIndex] = etDocument.text.toString()
            pptSlides.add("")
            currentSlideIndex = pptSlides.size - 1
            etDocument.setText("")
            refreshPptSidebar()
        }

        btnPageMargin.setOnClickListener {
            marginIndex = (marginIndex + 1) % 3
            when (marginIndex) {
                0 -> { etDocument.setPadding(28, 28, 28, 28); btnPageMargin.text = "📏 ระยะขอบ: ปกติ" }
                1 -> { etDocument.setPadding(12, 12, 12, 12); btnPageMargin.text = "📏 ระยะขอบ: แคบ" }
                2 -> { etDocument.setPadding(56, 40, 56, 40); btnPageMargin.text = "📏 ระยะขอบ: กว้าง" }
            }
        }

        btnLineSpacing.setOnClickListener {
            spacingIndex = (spacingIndex + 1) % 3
            when (spacingIndex) {
                0 -> { etDocument.setLineSpacing(0f, 1.25f); btnLineSpacing.text = "↕️ ระยะห่าง: 1.25" }
                1 -> { etDocument.setLineSpacing(0f, 1.5f); btnLineSpacing.text = "↕️ ระยะห่าง: 1.5" }
                2 -> { etDocument.setLineSpacing(0f, 2.0f); btnLineSpacing.text = "↕️ ระยะห่าง: 2.0" }
            }
        }

        btnToggleRuler.setOnClickListener {
            tvWordRuler.visibility = if (tvWordRuler.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        btnStartSlideshow.setOnClickListener {
            ribbonTopScroll.visibility = View.GONE
            ribbonSubBar.visibility = View.GONE
            pptSlideSidebar.visibility = View.GONE
            btnExitSlideshow.visibility = View.VISIBLE
            Toast.makeText(this, "เข้าสู่โหมดนำเสนอเต็มจอ", Toast.LENGTH_SHORT).show()
        }

        btnExitSlideshow.setOnClickListener {
            ribbonTopScroll.visibility = View.VISIBLE
            ribbonSubBar.visibility = View.VISIBLE
            if (currentAppMode == "PPT") pptSlideSidebar.visibility = View.VISIBLE
            btnExitSlideshow.visibility = View.GONE
        }

        btnCalcSum.setOnClickListener {
            val nums = excelCells.mapNotNull { it.text.toString().toDoubleOrNull() }
            Toast.makeText(this, "ผลรวม (SUM) = ${nums.sum()}", Toast.LENGTH_LONG).show()
        }

        btnCalcAvg.setOnClickListener {
            val nums = excelCells.mapNotNull { it.text.toString().toDoubleOrNull() }
            val avg = if (nums.isNotEmpty()) nums.average() else 0.0
            Toast.makeText(this, "ค่าเฉลี่ย (AVERAGE) = %.2f".format(avg), Toast.LENGTH_LONG).show()
        }

        btnCalcMax.setOnClickListener {
            val nums = excelCells.mapNotNull { it.text.toString().toDoubleOrNull() }
            val max = nums.maxOrNull() ?: 0.0
            Toast.makeText(this, "ค่าสูงสุด (MAX) = $max", Toast.LENGTH_LONG).show()
        }

        btnModeType.setOnClickListener {
            stylusCanvas.isDrawingEnabled = false
            Toast.makeText(this, "โหมดพิมพ์ข้อความ", Toast.LENGTH_SHORT).show()
        }

        btnModeDraw.setOnClickListener {
            stylusCanvas.isDrawingEnabled = true
            etDocument.clearFocus()
            Toast.makeText(this, "โหมดปากกา Stylus พร้อมวาดเขียน", Toast.LENGTH_SHORT).show()
        }

        btnPenColor.setOnClickListener {
            penColorIndex = (penColorIndex + 1) % penColors.size
            stylusCanvas.setPenColor(penColors[penColorIndex])
            btnPenColor.text = "🎨 หมึก: ${penColorNames[penColorIndex]}"
        }

        btnClearDraw.setOnClickListener { stylusCanvas.clearCanvas() }
        btnExportPdf.setOnClickListener { exportToPdf(documentContainer) }
    }

    private fun buildExcelGrid(table: TableLayout, tvCellLabel: TextView, etFormula: EditText) {
        val cols = listOf("", "A", "B", "C", "D", "E", "F")
        val headerRow = TableRow(this)
        for (col in cols) {
            val tv = TextView(this).apply {
                text = col
                gravity = Gravity.CENTER
                setPadding(24, 10, 24, 10)
                setBackgroundColor(Color.parseColor("#E5E7EB"))
                setTextColor(Color.BLACK)
                setTypeface(null, Typeface.BOLD)
            }
            headerRow.addView(tv)
        }
        table.addView(headerRow)

        for (r in 1..20) {
            val row = TableRow(this)
            val rowNum = TextView(this).apply {
                text = "$r"
                gravity = Gravity.CENTER
                setPadding(20, 10, 20, 10)
                setBackgroundColor(Color.parseColor("#E5E7EB"))
                setTextColor(Color.BLACK)
            }
            row.addView(rowNum)

            for (c in 1..6) {
                val cellName = "${cols[c]}$r"
                val cell = EditText(this).apply {
                    minWidth = 140
                    setPadding(12, 8, 12, 8)
                    setBackgroundResource(android.R.drawable.edit_text)
                    inputType = InputType.TYPE_CLASS_TEXT
                    textSize = 14f
                    setOnFocusChangeListener { _, hasFocus ->
                        if (hasFocus) {
                            activeExcelCell = this
                            tvCellLabel.text = cellName
                            isSyncingFormula = true
                            etFormula.setText(this.text.toString())
                            isSyncingFormula = false
                        }
                    }
                    addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                            if (!isSyncingFormula && activeExcelCell == this@apply) {
                                isSyncingFormula = true
                                etFormula.setText(s?.toString() ?: "")
                                isSyncingFormula = false
                            }
                        }
                        override fun afterTextChanged(s: Editable?) {}
                    })
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

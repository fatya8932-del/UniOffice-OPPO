package com.unioffice.oppopad

import android.content.ContentValues
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private var colorIndex = 0
    private val colors = listOf(Color.BLUE, Color.RED, Color.BLACK)
    private val colorNames = listOf("น้ำเงิน", "แดง", "ดำ")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnModeType = findViewById<Button>(R.id.btnModeType)
        val btnModeDraw = findViewById<Button>(R.id.btnModeDraw)
        val btnPenColor = findViewById<Button>(R.id.btnPenColor)
        val btnClearDraw = findViewById<Button>(R.id.btnClearDraw)
        val btnExportPdf = findViewById<Button>(R.id.btnExportPdf)
        val etDocument = findViewById<EditText>(R.id.etDocument)
        val stylusCanvas = findViewById<StylusCanvasView>(R.id.stylusCanvas)
        val documentContainer = findViewById<FrameLayout>(R.id.documentContainer)

        // เริ่มต้นที่โหมดพิมพ์งาน
        stylusCanvas.isDrawingEnabled = false

        btnModeType.setOnClickListener {
            stylusCanvas.isDrawingEnabled = false
            etDocument.isEnabled = true
            btnModeType.setBackgroundColor(Color.WHITE)
            btnModeType.setTextColor(Color.parseColor("#1A56DB"))
            btnModeDraw.setBackgroundColor(Color.parseColor("#3B82F6"))
            btnModeDraw.setTextColor(Color.WHITE)
            Toast.makeText(this, "สลับเป็นโหมดพิมพ์เอกสาร", Toast.LENGTH_SHORT).show()
        }

        btnModeDraw.setOnClickListener {
            stylusCanvas.isDrawingEnabled = true
            etDocument.clearFocus()
            btnModeDraw.setBackgroundColor(Color.WHITE)
            btnModeDraw.setTextColor(Color.parseColor("#1A56DB"))
            btnModeType.setBackgroundColor(Color.parseColor("#3B82F6"))
            btnModeType.setTextColor(Color.WHITE)
            Toast.makeText(this, "สลับเป็นโหมดปากกา OPPO Pencil", Toast.LENGTH_SHORT).show()
        }

        btnPenColor.setOnClickListener {
            colorIndex = (colorIndex + 1) % colors.size
            stylusCanvas.setPenColor(colors[colorIndex])
            btnPenColor.text = "🎨 สีหมึก: ${colorNames[colorIndex]}"
        }

        btnClearDraw.setOnClickListener {
            stylusCanvas.clearCanvas()
            Toast.makeText(this, "ลบเส้นปากกาทั้งหมดแล้ว", Toast.LENGTH_SHORT).show()
        }

        btnExportPdf.setOnClickListener {
            exportToPdf(documentContainer)
        }
    }

    private fun exportToPdf(view: FrameLayout) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(view.width, view.height, 1).create()
        val page = pdfDocument.startPage(pageInfo)

        view.draw(page.canvas)
        pdfDocument.finishPage(page)

        val fileName = "UniOffice_${System.currentTimeMillis()}.pdf"

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
            Toast.makeText(this, "บันทึกไฟล์ PDF สำเร็จในโฟลเดอร์ Downloads!", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "เกิดข้อผิดพลาด: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            pdfDocument.close()
        }
    }
}

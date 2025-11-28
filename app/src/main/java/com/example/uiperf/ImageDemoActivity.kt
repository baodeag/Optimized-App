package com.example.uiperf

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.SystemClock
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class ImageDemoActivity : AppCompatActivity() {

    private lateinit var ivUnoptimized: ImageView
    private lateinit var ivOptimized: ImageView
    private lateinit var tvImageInfo: TextView
    private lateinit var btnMeasureImage: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_demo)

        ivUnoptimized = findViewById(R.id.ivUnoptimized)
        ivOptimized = findViewById(R.id.ivOptimized)
        tvImageInfo = findViewById(R.id.tvImageInfo)
        btnMeasureImage = findViewById(R.id.btnRunImageTest)

        tvImageInfo.text = "So sánh load ảnh trước / sau tối ưu"

        btnMeasureImage.setOnClickListener {
            measureAndShow()
        }
    }

    private fun measureAndShow() {
        val resId = R.drawable.sample_large // ảnh bạn đã cho vào drawable-nodpi

        // 1. Decode chưa tối ưu (full size)
        val start1 = SystemClock.elapsedRealtimeNanos()
        val bmpUnopt = BitmapFactory.decodeResource(resources, resId)
        val timeUnoptMs = (SystemClock.elapsedRealtimeNanos() - start1) / 1_000_000.0
        ivUnoptimized.setImageBitmap(bmpUnopt)
        val memUnopt = bmpUnopt.byteCount

        // 2. Decode tối ưu với inSampleSize cố định (giảm mạnh kích thước)
        val start2 = SystemClock.elapsedRealtimeNanos()
        val bmpOpt = decodeSampledBitmapFromResource(
            resources,
            resId,
            inSampleSize = 8  // downscale mạnh để thấy khác biệt rõ
        )
        val timeOptMs = (SystemClock.elapsedRealtimeNanos() - start2) / 1_000_000.0
        ivOptimized.setImageBitmap(bmpOpt)
        val memOpt = bmpOpt.byteCount

        val percentTime =
            if (timeUnoptMs > 0 && timeOptMs > 0) ((timeUnoptMs - timeOptMs) / timeUnoptMs) * 100.0 else 0.0
        val percentMem =
            if (memUnopt > 0 && memOpt > 0) ((memUnopt - memOpt).toDouble() / memUnopt.toDouble()) * 100.0 else 0.0

        val timeSummary = if (timeUnoptMs > 0 && timeOptMs > 0) {
            if (percentTime > 0) {
                "Thời gian: tối ưu nhanh hơn ~${"%.1f".format(percentTime)}%."
            } else if (percentTime < 0) {
                "Thời gian: tối ưu chậm hơn ~${"%.1f".format(-percentTime)}%."
            } else {
                "Thời gian: gần như ngang nhau."
            }
        } else "Thời gian: cần đo lại."

        val memSummary = if (memUnopt > 0 && memOpt > 0) {
            "RAM giảm ~${"%.1f".format(percentMem)}%."
        } else "Không tính được % RAM."

        AlertDialog.Builder(this)
            .setTitle("Kết quả đo load ảnh")
            .setMessage(
                """
                    ⛔ Chưa tối ưu:
                    - Thời gian: ${"%.2f".format(timeUnoptMs)} ms
                    - Kích thước bitmap: ${memUnopt / 1024} KB

                    ✅ Đã tối ưu (inSampleSize = 8):
                    - Thời gian: ${"%.2f".format(timeOptMs)} ms
                    - Kích thước bitmap: ${memOpt / 1024} KB

                    $timeSummary
                    $memSummary
                """.trimIndent()
            )
            .setPositiveButton("OK", null)
            .show()

        // có thể recycle nếu muốn, nhưng đang hiển thị lên ImageView nên tạm giữ
        // bmpUnopt.recycle() // chỉ recycle nếu không hiển thị nữa
    }

    private fun decodeSampledBitmapFromResource(
        res: Resources,
        resId: Int,
        inSampleSize: Int
    ): Bitmap {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = false
            this.inSampleSize = inSampleSize
        }
        return BitmapFactory.decodeResource(res, resId, options)
    }
}

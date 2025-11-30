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
import android.util.LruCache

class ImageDemoActivity : AppCompatActivity() {

    private lateinit var ivUnoptimized: ImageView
    private lateinit var ivOptimized: ImageView
    private lateinit var tvImageInfo: TextView
    private lateinit var btnMeasureImage: Button

    companion object {
        // Dùng chung cache cho ảnh trong Activity này
        private const val SAMPLE_SIZE = 8

        private var bitmapCache: LruCache<Int, Bitmap>? = null

        private fun getBitmapCache(): LruCache<Int, Bitmap> {
            if (bitmapCache == null) {
                // cấp ~1/8 bộ nhớ cho cache
                val maxMemKb = (Runtime.getRuntime().maxMemory() / 1024).toInt()
                val cacheSizeKb = maxMemKb / 8
                bitmapCache = object : LruCache<Int, Bitmap>(cacheSizeKb) {
                    override fun sizeOf(key: Int, value: Bitmap): Int {
                        return value.byteCount / 1024
                    }
                }
            }
            return bitmapCache!!
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_demo)

        ivUnoptimized = findViewById(R.id.ivUnoptimized)
        ivOptimized = findViewById(R.id.ivOptimized)
        tvImageInfo = findViewById(R.id.tvImageInfo)
        btnMeasureImage = findViewById(R.id.btnRunImageTest)

        tvImageInfo.text =
            "So sánh load ảnh trước / sau tối ưu\n" +
                    "- Trước: decode full size trên UI thread (xấu)\n" +
                    "- Sau: decode có inSampleSize + cache + background thread"

        btnMeasureImage.setOnClickListener {
            measureAndShow()
        }
    }

    private fun measureAndShow() {
        val resId = R.drawable.sample_large // ảnh bạn đã cho vào drawable-nodpi

        // khoá nút để tránh bấm liên tục
        btnMeasureImage.isEnabled = false
        btnMeasureImage.text = "Đang đo..."

        //
        // 1. Decode CHƯA TỐI ƯU (full size, ngay trên UI thread)
        //
        val start1 = SystemClock.elapsedRealtimeNanos()
        val bmpUnopt = BitmapFactory.decodeResource(resources, resId)
        val timeUnoptMs = (SystemClock.elapsedRealtimeNanos() - start1) / 1_000_000.0
        ivUnoptimized.setImageBitmap(bmpUnopt)
        val memUnopt = bmpUnopt.byteCount

        //
        // 2. Decode TỐI ƯU trên background thread
        //    - Dùng inSampleSize
        //    - Sử dụng LruCache để không decode lại nếu đã có
        //
        Thread {
            val cache = getBitmapCache()

            var bmpOpt: Bitmap? = cache.get(resId)

            val start2 = SystemClock.elapsedRealtimeNanos()
            if (bmpOpt == null) {
                // Chưa có trong cache, decode mới
                bmpOpt = decodeSampledBitmapFromResource(
                    resources,
                    resId,
                    inSampleSize = SAMPLE_SIZE //sample_size=8
                )
                cache.put(resId, bmpOpt)
            }
            val timeOptMs =
                (SystemClock.elapsedRealtimeNanos() - start2) / 1_000_000.0
            val memOpt = bmpOpt.byteCount

            val percentTime =
                if (timeUnoptMs > 0 && timeOptMs > 0)
                    ((timeUnoptMs - timeOptMs) / timeUnoptMs) * 100.0
                else 0.0

            val percentMem =
                if (memUnopt > 0 && memOpt > 0)
                    ((memUnopt - memOpt).toDouble() / memUnopt.toDouble()) * 100.0
                else 0.0

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

            runOnUiThread {
                // cập nhật UI với bitmap tối ưu (decode trong background)
                ivOptimized.setImageBitmap(bmpOpt)

                AlertDialog.Builder(this)
                    .setTitle("Kết quả đo load ảnh")
                    .setMessage(
                        """
                        ⛔ Chưa tối ưu (UI thread, full size):
                        - Thời gian: ${"%.2f".format(timeUnoptMs)} ms
                        - Kích thước bitmap: ${memUnopt / 1024} KB

                        ✅ Đã tối ưu (inSampleSize=$SAMPLE_SIZE, background + cache):
                        - Thời gian decode (lần này): ${"%.2f".format(timeOptMs)} ms
                        - Kích thước bitmap: ${memOpt / 1024} KB

                        $timeSummary
                        $memSummary
                    """.trimIndent()
                    )
                    .setPositiveButton("OK", null)
                    .show()

                btnMeasureImage.isEnabled = true
                btnMeasureImage.text = "Đo và so sánh load ảnh"
            }
        }.start()
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

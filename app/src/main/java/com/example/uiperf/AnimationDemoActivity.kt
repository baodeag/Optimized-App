package com.example.uiperf

import android.os.Bundle
import android.os.SystemClock
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class AnimationDemoActivity : AppCompatActivity() {

    private lateinit var tvAnimInfo: TextView
    private lateinit var btnMeasureAnim: Button
    private lateinit var badViews: List<android.view.View>
    private lateinit var goodViews: List<android.view.View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_animation_demo)

        tvAnimInfo = findViewById(R.id.tvAnimInfo)
        btnMeasureAnim = findViewById(R.id.btnMeasureAnim)

        badViews = listOf(
            findViewById(R.id.badBox1),
            findViewById(R.id.badBox2),
            findViewById(R.id.badBox3),
            findViewById(R.id.badBox4),
            findViewById(R.id.badBox5),
            findViewById(R.id.badBox6),
            findViewById(R.id.badBox7),
            findViewById(R.id.badBox8)
        )

        goodViews = listOf(
            findViewById(R.id.goodBox1),
            findViewById(R.id.goodBox2),
            findViewById(R.id.goodBox3),
            findViewById(R.id.goodBox4)
        )

        tvAnimInfo.text = "So sánh animation trước / sau tối ưu"

        btnMeasureAnim.setOnClickListener {
            val badMs = runBadAnimation()
            val goodMs = runGoodAnimation()

            val percent =
                if (badMs > 0 && goodMs > 0) ((badMs - goodMs) / badMs) * 100.0 else 0.0

            val summary = if (badMs > 0 && goodMs > 0) {
                if (percent > 0) {
                    "Tối ưu nhanh hơn ~${"%.1f".format(percent)}%."
                } else if (percent < 0) {
                    "Tối ưu chậm hơn ~${"%.1f".format(-percent)}%."
                } else {
                    "Hai cách gần như giống nhau."
                }
            } else {
                "Cần đo lại để có kết quả rõ hơn."
            }

            AlertDialog.Builder(this)
                .setTitle("Kết quả đo animation")
                .setMessage(
                    """
                        Chưa tối ưu: ${"%.2f".format(badMs)} ms
                        Đã tối ưu: ${"%.2f".format(goodMs)} ms
                        
                        $summary
                    """.trimIndent()
                )
                .setPositiveButton("OK", null)
                .show()
        }
    }

    // Animation chưa tối ưu: nhiều view + API cũ
    private fun runBadAnimation(): Double {
        val distance = resources.displayMetrics.density * 80 // 80dp

        val startNs = SystemClock.elapsedRealtimeNanos()

        // giả lập việc nặng trên UI thread để thấy khác biệt rõ
        var dummy = 0L
        for (i in 0 until 2_000_000) {
            dummy += i
        }

        badViews.forEach { view ->
            view.clearAnimation()
            val anim = TranslateAnimation(
                0f, 0f,
                0f, -distance
            ).apply {
                duration = 600
                repeatCount = 1
                repeatMode = Animation.REVERSE
                fillAfter = true
            }
            view.startAnimation(anim)
        }

        val elapsedMs = (SystemClock.elapsedRealtimeNanos() - startNs) / 1_000_000.0
        return elapsedMs
    }

    // Animation tối ưu: ít view hơn + API hiện đại
    private fun runGoodAnimation(): Double {
        val distance = resources.displayMetrics.density * 80 // 80dp

        val startNs = SystemClock.elapsedRealtimeNanos()

        // công việc nhẹ hơn trên UI thread
        var dummy = 0L
        for (i in 0 until 500_000) {
            dummy += i
        }

        goodViews.forEach { view ->
            view.animate().cancel()
            view.translationY = 0f
            view.animate()
                .translationY(-distance)
                .setDuration(400)
                .withEndAction {
                    view.animate()
                        .translationY(0f)
                        .setDuration(400)
                        .start()
                }
                .start()
        }

        val elapsedMs = (SystemClock.elapsedRealtimeNanos() - startNs) / 1_000_000.0
        return elapsedMs
    }
}

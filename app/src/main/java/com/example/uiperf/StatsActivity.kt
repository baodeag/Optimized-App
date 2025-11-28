package com.example.uiperf

import android.os.Bundle
import android.widget.TextView
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class StatsActivity : AppCompatActivity() {

    private lateinit var tvSummary: TextView
    private lateinit var barBefore: View
    private lateinit var barAfter: View
    private lateinit var tvBeforeLabel: TextView
    private lateinit var tvAfterLabel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        tvSummary = findViewById(R.id.tvSummary)
        barBefore = findViewById(R.id.barBefore)
        barAfter = findViewById(R.id.barAfter)
        tvBeforeLabel = findViewById(R.id.tvBeforeLabel)
        tvAfterLabel = findViewById(R.id.tvAfterLabel)

        val countBefore = intent.getIntExtra("countBefore", 0)
        val countAfter = intent.getIntExtra("countAfter", 0)
        val avgBefore = intent.getFloatExtra("avgBefore", 0f)
        val avgAfter = intent.getFloatExtra("avgAfter", 0f)

        val summary = buildString {
            appendLine("TRƯỚC TỐI ƯU:")
            appendLine("- Số lần đo: $countBefore")
            appendLine("- Trung bình: ${"%.2f".format(avgBefore)} ms")
            appendLine()
            appendLine("SAU TỐI ƯU:")
            appendLine("- Số lần đo: $countAfter")
            appendLine("- Trung bình: ${"%.2f".format(avgAfter)} ms")
        }
        tvSummary.text = summary

        tvBeforeLabel.text = "Trước\n${"%.1f".format(avgBefore)} ms"
        tvAfterLabel.text = "Sau\n${"%.1f".format(avgAfter)} ms"

        // vẽ "biểu đồ cột" đơn giản bằng chiều cao view
        val maxMs = maxOf(avgBefore, avgAfter, 1f)
        val maxHeightPx = resources.displayMetrics.density * 160 // ~160dp

        val beforeHeight = (maxHeightPx * (avgBefore / maxMs))
        val afterHeight = (maxHeightPx * (avgAfter / maxMs))

        barBefore.layoutParams = barBefore.layoutParams.apply {
            height = beforeHeight.toInt().coerceAtLeast(1)
        }
        barAfter.layoutParams = barAfter.layoutParams.apply {
            height = afterHeight.toInt().coerceAtLeast(1)
        }
    }
}

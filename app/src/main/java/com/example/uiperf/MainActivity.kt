package com.example.uiperf

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnToggleMode: Button
    private lateinit var btnShowStats: Button
    private lateinit var btnAnimationDemo: Button
    private lateinit var btnImageDemo: Button
    private lateinit var tvMode: TextView
    private lateinit var tvMetrics: TextView

    // false = trước tối ưu, true = sau tối ưu
    private var optimized = false

    // dữ liệu demo
    private val data = List(100) { "Item #$it" }

    // thống kê
    private var totalBeforeMs = 0.0
    private var totalAfterMs = 0.0
    private var countBefore = 0
    private var countAfter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        btnToggleMode = findViewById(R.id.btnToggleMode)
        btnShowStats = findViewById(R.id.btnShowStats)
        btnAnimationDemo = findViewById(R.id.btnAnimationDemo)
        btnImageDemo = findViewById(R.id.btnImageDemo)
        tvMode = findViewById(R.id.tvMode)
        tvMetrics = findViewById(R.id.tvMetrics)

        // LayoutManager với prefetch
        val layoutManager = LinearLayoutManager(this).apply {
            // Prefetch một số item sắp tới để scroll mượt hơn
            initialPrefetchItemCount = 10
        }
        recyclerView.layoutManager = layoutManager

        // RecyclerView tối ưu hơn nếu size không đổi
        recyclerView.setHasFixedSize(true)

        // Giữ cache sẵn một số ViewHolder đã bind
        recyclerView.setItemViewCacheSize(20)

        // đổi chế độ trước/sau tối ưu layout
        btnToggleMode.setOnClickListener {
            optimized = !optimized
            reloadAndMeasure()
        }

        // xem thống kê + biểu đồ
        btnShowStats.setOnClickListener {
            showStatsScreen()
        }

        // mở demo animation
        btnAnimationDemo.setOnClickListener {
            startActivity(Intent(this, AnimationDemoActivity::class.java))
        }

        // mở demo image
        btnImageDemo.setOnClickListener {
            startActivity(Intent(this, ImageDemoActivity::class.java))
        }

        // chạy lần đầu với "trước tối ưu"
        reloadAndMeasure()
    }

    private fun reloadAndMeasure() {
        tvMode.text = if (optimized) {
            getString(R.string.mode_optimized)
        } else {
            getString(R.string.mode_unoptimized)
        }

        val startNs = SystemClock.elapsedRealtimeNanos()

        recyclerView.adapter = DemoAdapter(optimized, data)

        // đo thời gian dựng UI lần đầu của RecyclerView
        recyclerView.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    recyclerView.viewTreeObserver.removeOnPreDrawListener(this)

                    val endNs = SystemClock.elapsedRealtimeNanos()
                    val elapsedMs = (endNs - startNs) / 1_000_000.0

                    // lưu số liệu thống kê
                    if (optimized) {
                        totalAfterMs += elapsedMs
                        countAfter++
                    } else {
                        totalBeforeMs += elapsedMs
                        countBefore++
                    }

                    tvMetrics.text = getString(
                        R.string.metrics_format,
                        if (optimized) getString(R.string.mode_optimized) else getString(R.string.mode_unoptimized),
                        String.format("%.2f", elapsedMs)
                    )

                    Snackbar.make(
                        recyclerView,
                        "Thời gian dựng UI: %.2f ms".format(elapsedMs),
                        Snackbar.LENGTH_SHORT
                    ).show()

                    return true
                }
            }
        )
    }

    private fun showStatsScreen() {
        val avgBefore =
            if (countBefore == 0) 0.0 else totalBeforeMs / countBefore.toDouble()
        val avgAfter =
            if (countAfter == 0) 0.0 else totalAfterMs / countAfter.toDouble()

        val intent = Intent(this, StatsActivity::class.java).apply {
            putExtra("countBefore", countBefore)
            putExtra("countAfter", countAfter)
            putExtra("avgBefore", avgBefore.toFloat())
            putExtra("avgAfter", avgAfter.toFloat())
        }
        startActivity(intent)
    }
}

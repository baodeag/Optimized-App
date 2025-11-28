package com.example.uiperf

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DemoAdapter(
    private val optimized: Boolean,
    private val items: List<String>
) : RecyclerView.Adapter<DemoAdapter.DemoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DemoViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val layoutRes = if (optimized) {
            R.layout.item_optimized
        } else {
            R.layout.item_unoptimized
        }
        val view = inflater.inflate(layoutRes, parent, false)
        return DemoViewHolder(view, optimized)
    }

    override fun onBindViewHolder(holder: DemoViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class DemoViewHolder(
        itemView: View,
        private val optimized: Boolean
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvSubtitle: TextView = itemView.findViewById(R.id.tvSubtitle)

        // TRƯỚC TỐI ƯU: detail luôn inflate, layout nặng
        private val detailContainerAlways: View? =
            if (!optimized) itemView.findViewById(R.id.layoutDetailAlways) else null
        private val tvDetail1Always: TextView? =
            if (!optimized) itemView.findViewById(R.id.tvDetail1) else null
        private val tvDetail2Always: TextView? =
            if (!optimized) itemView.findViewById(R.id.tvDetail2) else null
        private val tvDetail3Always: TextView? =
            if (!optimized) itemView.findViewById(R.id.tvDetail3) else null
        private val imgIconAlways: ImageView? =
            if (!optimized) itemView.findViewById(R.id.imgDetailIcon) else null

        // SAU TỐI ƯU: dùng ViewStub, chỉ inflate khi cần
        private val tvShowDetail: TextView? =
            if (optimized) itemView.findViewById(R.id.tvShowDetail) else null
        private val stubDetail: ViewStub? =
            if (optimized) itemView.findViewById(R.id.stubDetail) else null

        private var stubInflated = false
        private var detailContainerLazy: View? = null
        private var tvDetail1Lazy: TextView? = null
        private var tvDetail2Lazy: TextView? = null
        private var tvDetail3Lazy: TextView? = null
        private var imgIconLazy: ImageView? = null

        init {
            if (optimized && tvShowDetail != null && stubDetail != null) {
                tvShowDetail.setOnClickListener {
                    if (!stubInflated) {
                        val inflated = stubDetail.inflate()
                        detailContainerLazy = inflated
                        tvDetail1Lazy = inflated.findViewById(R.id.tvDetail1)
                        tvDetail2Lazy = inflated.findViewById(R.id.tvDetail2)
                        tvDetail3Lazy = inflated.findViewById(R.id.tvDetail3)
                        imgIconLazy = inflated.findViewById(R.id.imgDetailIcon)
                        stubInflated = true
                    }
                    detailContainerLazy?.let { container ->
                        container.visibility =
                            if (container.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                    }
                }
            }
        }

        fun bind(text: String) {
            tvTitle.text = text
            tvSubtitle.text = "Subtitle for $text"

            if (!optimized) {
                // bản chưa tối ưu: luôn setup detail nặng
                detailContainerAlways?.visibility = View.VISIBLE
                tvDetail1Always?.text = "Detail line 1 for $text"
                tvDetail2Always?.text = "Detail line 2 for $text"
                tvDetail3Always?.text = "Detail line 3 for $text"
                // imgIconAlways: chỉ để có ImageView cho layout nặng hơn
                imgIconAlways?.setBackgroundResource(R.drawable.bg_detail_icon)
            } else {
                // bản tối ưu: chi tiết chỉ hiện nếu user bấm
                if (stubInflated) {
                    tvDetail1Lazy?.text = "Detail line 1 for $text"
                    tvDetail2Lazy?.text = "Detail line 2 for $text"
                    tvDetail3Lazy?.text = "Detail line 3 for $text"
                    imgIconLazy?.setBackgroundResource(R.drawable.bg_detail_icon)
                }
            }
        }
    }
}

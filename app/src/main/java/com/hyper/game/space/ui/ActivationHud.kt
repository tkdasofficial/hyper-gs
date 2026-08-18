package com.hyper.game.space.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class HudItem(val label: String, var state: SlotState = SlotState.WAITING)

enum class SlotState { WAITING, LOADING, DONE }

class HudAdapter(private val items: List<HudItem>) : RecyclerView.Adapter<HudAdapter.HudViewHolder>() {

    class HudViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val labelView: TextView = view.findViewWithTag("label")
        val progressView: ProgressBar = view.findViewWithTag("progress")
        val checkView: ImageView = view.findViewWithTag("check")
        val container: LinearLayout = view as LinearLayout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HudViewHolder {
        val context = parent.context
        
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = ViewGroup.MarginLayoutParams(
                (260 * context.resources.displayMetrics.density).toInt(),
                (56 * context.resources.displayMetrics.density).toInt()
            ).apply {
                setMargins(0, (4 * context.resources.displayMetrics.density).toInt(), 0, (4 * context.resources.displayMetrics.density).toInt())
            }
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#1E1E1E"))
                cornerRadius = 12f * context.resources.displayMetrics.density
            }
            setPadding(
                (20 * context.resources.displayMetrics.density).toInt(), 0,
                (20 * context.resources.displayMetrics.density).toInt(), 0
            )
        }

        val label = TextView(context).apply {
            tag = "label"
            setTextColor(Color.parseColor("#EEEEEE"))
            textSize = 14f
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        
        val progress = ProgressBar(context).apply {
            tag = "progress"
            layoutParams = LinearLayout.LayoutParams(
                (20 * context.resources.displayMetrics.density).toInt(),
                (20 * context.resources.displayMetrics.density).toInt()
            )
            visibility = View.GONE
        }
        
        val check = ImageView(context).apply {
            tag = "check"
            setImageResource(android.R.drawable.checkbox_on_background) // standard check
            layoutParams = LinearLayout.LayoutParams(
                (24 * context.resources.displayMetrics.density).toInt(),
                (24 * context.resources.displayMetrics.density).toInt()
            )
            visibility = View.GONE
        }

        container.addView(label)
        container.addView(progress)
        container.addView(check)

        return HudViewHolder(container)
    }

    override fun onBindViewHolder(holder: HudViewHolder, position: Int) {
        val item = items[position]
        holder.labelView.text = item.label
        
        when (item.state) {
            SlotState.WAITING -> {
                holder.progressView.visibility = View.GONE
                holder.checkView.visibility = View.GONE
                holder.container.alpha = 0.5f
            }
            SlotState.LOADING -> {
                holder.progressView.visibility = View.VISIBLE
                holder.checkView.visibility = View.GONE
                holder.container.alpha = 1.0f
            }
            SlotState.DONE -> {
                holder.progressView.visibility = View.GONE
                holder.checkView.visibility = View.VISIBLE
                holder.container.alpha = 1.0f
            }
        }
    }

    override fun getItemCount() = items.size
}

class ActivationHudView(context: Context, private val onFinish: () -> Unit) : FrameLayout(context) {

    private val recyclerView = RecyclerView(context)
    private val items = listOf(
        HudItem("Memory Boost"),
        HudItem("Network QoS Policy"),
        HudItem("Overload Shield"),
        HudItem("DND Rules Engine"),
        HudItem("V-Sens Touch Hook")
    )
    private val adapter = HudAdapter(items)

    init {
        recyclerView.layoutParams = LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        addView(recyclerView)
        
        alpha = 0f
        
        val fadeIn = ObjectAnimator.ofFloat(this, "alpha", 0f, 1f).apply {
            duration = 300
            interpolator = DecelerateInterpolator()
        }
        fadeIn.start()

        runSequence()
    }

    private fun runSequence() {
        CoroutineScope(Dispatchers.Main).launch {
            for (i in items.indices) {
                items[i].state = SlotState.LOADING
                adapter.notifyItemChanged(i)
                
                // Animate slide up
                val child = recyclerView.layoutManager?.findViewByPosition(i)
                child?.let { view ->
                    ObjectAnimator.ofFloat(view, "translationY", 50f, 0f).apply {
                        duration = 150
                        start()
                    }
                }
                
                delay(200) // spinner time
                items[i].state = SlotState.DONE
                adapter.notifyItemChanged(i)
                delay(200) // tick time
            }
            
            delay(400)
            val fadeOut = ObjectAnimator.ofFloat(this@ActivationHudView, "alpha", 1f, 0f).apply {
                duration = 300
            }
            fadeOut.start()
            delay(300)
            onFinish()
        }
    }
}


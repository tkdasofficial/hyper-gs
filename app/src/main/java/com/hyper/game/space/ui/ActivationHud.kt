package com.hyper.game.space.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
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

data class HudItem(val label: String, val isActive: Boolean, var state: SlotState = SlotState.WAITING)

enum class SlotState { WAITING, LOADING, DONE }

class HudAdapter(private val items: List<HudItem>) : RecyclerView.Adapter<HudAdapter.HudViewHolder>() {

    class HudViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val labelView: TextView = view.findViewWithTag("label")
        val statusView: TextView = view.findViewWithTag("status")
        val progressView: ProgressBar = view.findViewWithTag("progress")
        val container: LinearLayout = view as LinearLayout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HudViewHolder {
        val context = parent.context
        
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = ViewGroup.MarginLayoutParams(
                (280 * context.resources.displayMetrics.density).toInt(),
                (60 * context.resources.displayMetrics.density).toInt()
            ).apply {
                setMargins(0, (4 * context.resources.displayMetrics.density).toInt(), 0, (4 * context.resources.displayMetrics.density).toInt())
            }
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#1E1E1E"))
                cornerRadius = 12f * context.resources.displayMetrics.density
                setStroke((1 * context.resources.displayMetrics.density).toInt(), Color.parseColor("#333333"))
            }
            setPadding(
                (20 * context.resources.displayMetrics.density).toInt(), 0,
                (20 * context.resources.displayMetrics.density).toInt(), 0
            )
        }

        val textContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        val label = TextView(context).apply {
            tag = "label"
            setTextColor(Color.parseColor("#EEEEEE"))
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val status = TextView(context).apply {
            tag = "status"
            textSize = 12f
        }

        textContainer.addView(label)
        textContainer.addView(status)

        val progress = ProgressBar(context).apply {
            tag = "progress"
            layoutParams = LinearLayout.LayoutParams(
                (20 * context.resources.displayMetrics.density).toInt(),
                (20 * context.resources.displayMetrics.density).toInt()
            )
            visibility = View.GONE
        }
        
        container.addView(textContainer)
        container.addView(progress)

        return HudViewHolder(container)
    }

    override fun onBindViewHolder(holder: HudViewHolder, position: Int) {
        val item = items[position]
        holder.labelView.text = item.label
        
        when (item.state) {
            SlotState.WAITING -> {
                holder.progressView.visibility = View.GONE
                holder.statusView.text = "Pending..."
                holder.statusView.setTextColor(Color.GRAY)
                holder.container.alpha = 0.5f
                (holder.container.background as GradientDrawable).setStroke((1 * holder.container.context.resources.displayMetrics.density).toInt(), Color.parseColor("#333333"))
            }
            SlotState.LOADING -> {
                holder.progressView.visibility = View.VISIBLE
                holder.statusView.text = "Initializing..."
                holder.statusView.setTextColor(Color.LTGRAY)
                holder.container.alpha = 1.0f
                (holder.container.background as GradientDrawable).setStroke((1 * holder.container.context.resources.displayMetrics.density).toInt(), Color.CYAN)
            }
            SlotState.DONE -> {
                holder.progressView.visibility = View.GONE
                holder.container.alpha = 1.0f
                if (item.isActive) {
                    holder.statusView.text = "ACTIVE"
                    holder.statusView.setTextColor(Color.CYAN)
                    (holder.container.background as GradientDrawable).setStroke((1 * holder.container.context.resources.displayMetrics.density).toInt(), Color.CYAN)
                } else {
                    holder.statusView.text = "DISABLED"
                    holder.statusView.setTextColor(Color.GRAY)
                    (holder.container.background as GradientDrawable).setStroke((1 * holder.container.context.resources.displayMetrics.density).toInt(), Color.parseColor("#333333"))
                }
            }
        }
    }

    override fun getItemCount() = items.size
}

class ActivationHudView(context: Context, activeFeatures: List<Pair<String, Boolean>>, private val onFinish: () -> Unit) : FrameLayout(context) {
    private val recyclerView = RecyclerView(context)
    private val items = activeFeatures.map { HudItem(it.first, it.second) }
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
                
                delay(150) // spinner time
                items[i].state = SlotState.DONE
                adapter.notifyItemChanged(i)
                delay(150) // tick time
            }
            
            delay(1200)

            val fadeOut = ObjectAnimator.ofFloat(this@ActivationHudView, "alpha", 1f, 0f).apply {
                duration = 400
            }
            fadeOut.start()

            delay(400)
            onFinish()
        }
    }
}


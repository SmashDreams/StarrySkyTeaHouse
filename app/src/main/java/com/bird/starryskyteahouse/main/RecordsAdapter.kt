package com.bird.starryskyteahouse.main

import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bird.starryskyteahouse.R
import com.bird.starryskyteahouse.records.GameRecord
import com.bird.starryskyteahouse.records.GameRecordSummary

class RecordsAdapter(
    private val mContext: Context
) : ListAdapter<RecordsListItem, RecyclerView.ViewHolder>(RecordsListDiffCallback()) {

    fun render(state: RecordsUiState) {
        submitList(
            when (state) {
                RecordsUiState.LoginRequired -> listOf(RecordsListItem.Message(R.string.records_login_first))
                RecordsUiState.Loading -> listOf(RecordsListItem.Message(R.string.records_loading))
                RecordsUiState.Empty -> listOf(RecordsListItem.Message(R.string.records_empty))
                RecordsUiState.Unavailable -> listOf(RecordsListItem.Message(R.string.records_missing_game))
                is RecordsUiState.Content -> buildContentItems(state.records, state.summary)
            }
        )
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is RecordsListItem.Message -> VIEW_TYPE_MESSAGE
            is RecordsListItem.Summary -> VIEW_TYPE_SUMMARY
            is RecordsListItem.Record -> VIEW_TYPE_RECORD
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_MESSAGE -> MessageViewHolder(mContext)
            VIEW_TYPE_SUMMARY -> SummaryViewHolder(mContext)
            else -> RecordViewHolder(mContext)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is RecordsListItem.Message -> (holder as MessageViewHolder).bind(item)
            is RecordsListItem.Summary -> (holder as SummaryViewHolder).bind(item)
            is RecordsListItem.Record -> (holder as RecordViewHolder).bind(item)
        }
    }

    private fun buildContentItems(
        records: List<GameRecord>,
        summary: GameRecordSummary
    ): List<RecordsListItem> {
        return buildList {
            add(RecordsListItem.Summary(summary))
            records.forEachIndexed { index, record ->
                add(RecordsListItem.Record(record, index > 0))
            }
        }
    }

    private companion object {
        const val VIEW_TYPE_MESSAGE = 1
        const val VIEW_TYPE_SUMMARY = 2
        const val VIEW_TYPE_RECORD = 3
    }
}

sealed class RecordsListItem {
    data class Message(val messageRes: Int) : RecordsListItem()
    data class Summary(val summary: GameRecordSummary) : RecordsListItem()
    data class Record(val record: GameRecord, val hasTopMargin: Boolean) : RecordsListItem()
}

private class RecordsListDiffCallback : DiffUtil.ItemCallback<RecordsListItem>() {
    override fun areItemsTheSame(oldItem: RecordsListItem, newItem: RecordsListItem): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: RecordsListItem, newItem: RecordsListItem): Boolean {
        return oldItem == newItem
    }
}

private class MessageViewHolder(context: Context) : RecyclerView.ViewHolder(
    createText(context, context.getString(R.string.records_loading), R.color.tea_mist, 14f).apply {
        setBackgroundResource(R.drawable.bg_records_panel)
        gravity = Gravity.CENTER
        minHeight = dp(context, 104)
        setPadding(dp(context, 16), dp(context, 16), dp(context, 16), dp(context, 16))
        applyFullWidthRecordItemLayout()
    }
) {
    private val mTextView = itemView as TextView

    fun bind(item: RecordsListItem.Message) {
        mTextView.setText(item.messageRes)
    }
}

private class SummaryViewHolder(context: Context) : RecyclerView.ViewHolder(
    LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setBackgroundResource(R.drawable.bg_records_panel_solid)
        setPadding(dp(context, 16), dp(context, 14), dp(context, 16), dp(context, 14))
        layoutParams = RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.MATCH_PARENT,
            RecyclerView.LayoutParams.WRAP_CONTENT
        )
    }
) {
    private val mContext = context
    private val mContainer = itemView as LinearLayout

    fun bind(item: RecordsListItem.Summary) {
        mContainer.removeAllViews()
        mContainer.addView(createSummaryPanel(item.summary))
    }

    private fun createSummaryPanel(summary: GameRecordSummary): LinearLayout {
        return LinearLayout(mContext).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val completedBlock = LinearLayout(mContext).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setBackgroundResource(R.drawable.bg_record_summary_primary)
                setPadding(dp(mContext, 12), dp(mContext, 12), dp(mContext, 12), dp(mContext, 12))
                addView(createText(mContext, summary.completedCount.toString(), R.color.tea_gold, 24f, bold = true).apply {
                    gravity = Gravity.CENTER
                    setSingleLine(true)
                })
                addView(createText(mContext, mContext.getString(R.string.records_summary_completed), R.color.tea_mist, 11f).apply {
                    gravity = Gravity.CENTER
                    setSingleLine(true)
                })
            }
            addView(completedBlock, LinearLayout.LayoutParams(dp(mContext, 78), dp(mContext, 72)))

            val details = LinearLayout(mContext).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(mContext, 12), 0, 0, 0)
                addView(
                    createSummaryDetail(
                        mContext.getString(R.string.records_summary_highest),
                        mContext.getString(R.string.records_level_badge, summary.highestLevel),
                        R.color.tea_leaf
                    )
                )
                addView(
                    createSummaryDetail(
                        mContext.getString(R.string.records_summary_latest),
                        mContext.getString(R.string.records_level_badge, summary.latestLevel),
                        R.color.tea_gold
                    ),
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { topMargin = dp(mContext, 8) }
                )
            }
            addView(details, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        }
    }

    private fun createSummaryDetail(label: String, value: String, valueColorRes: Int): LinearLayout {
        return LinearLayout(mContext).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundResource(R.drawable.bg_record_summary)
            setPadding(dp(mContext, 12), dp(mContext, 7), dp(mContext, 12), dp(mContext, 7))
            addView(createText(mContext, label, R.color.tea_muted, 11f).apply { setSingleLine(true) })
            addView(
                createText(mContext, value, valueColorRes, 13f, bold = true).apply {
                    gravity = Gravity.END
                    setSingleLine(true)
                },
                LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            )
        }
    }
}

private class RecordViewHolder(context: Context) : RecyclerView.ViewHolder(
    LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setBackgroundResource(R.drawable.bg_record_item)
        setPadding(dp(context, 14), dp(context, 12), dp(context, 14), dp(context, 12))
    }
) {
    private val mContext = context
    private val mContainer = itemView as LinearLayout

    fun bind(item: RecordsListItem.Record) {
        mContainer.removeAllViews()
        val record = item.record
        val header = LinearLayout(mContext).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        header.addView(
            createText(
                mContext,
                mContext.getString(R.string.records_level_badge, record.level),
                R.color.tea_gold,
                14f,
                bold = true
            ).apply { setSingleLine(true) },
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        )

        val statusText = if (record.completed) {
            mContext.getString(R.string.record_completed)
        } else {
            mContext.getString(R.string.record_uncompleted)
        }
        val statusBackground = if (record.completed) {
            R.drawable.bg_record_status_complete
        } else {
            R.drawable.bg_record_status_pending
        }
        header.addView(
            createText(mContext, statusText, R.color.tea_mist, 11f, bold = true).apply {
                setBackgroundResource(statusBackground)
                setPadding(dp(mContext, 10), dp(mContext, 4), dp(mContext, 10), dp(mContext, 4))
                setSingleLine(true)
            }
        )
        mContainer.addView(header)
        mContainer.addView(
            createRecordInfoLine(record),
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(mContext, 8) }
        )
        mContainer.layoutParams = RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.MATCH_PARENT,
            RecyclerView.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = if (item.hasTopMargin) dp(mContext, 8) else dp(mContext, 12)
        }
    }

    private fun createRecordInfoLine(record: GameRecord): LinearLayout {
        return LinearLayout(mContext).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundResource(R.drawable.bg_record_metric)
            setPadding(dp(mContext, 12), dp(mContext, 9), dp(mContext, 12), dp(mContext, 9))
            addView(
                createText(
                    mContext,
                    "${mContext.getString(R.string.records_elapsed_label)} ${mContext.getString(R.string.records_seconds, record.elapsedSeconds)}",
                    R.color.tea_mist,
                    12.5f,
                    bold = true
                ).apply { setSingleLine(true) },
                LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            )
            addView(
                createText(
                    mContext,
                    "${mContext.getString(R.string.records_remaining_label)} ${mContext.getString(R.string.records_seconds, record.remainingSeconds)}",
                    R.color.tea_muted,
                    12f
                ).apply {
                    gravity = Gravity.END
                    setSingleLine(true)
                },
                LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            )
        }
    }
}

private fun createText(
    context: Context,
    text: String,
    colorRes: Int,
    sizeSp: Float,
    bold: Boolean = false
): TextView {
    return TextView(context).apply {
        this.text = text
        setTextColor(ContextCompat.getColor(context, colorRes))
        textSize = sizeSp
        includeFontPadding = true
        if (bold) setTypeface(typeface, Typeface.BOLD)
    }
}

private fun TextView.applyFullWidthRecordItemLayout() {
    layoutParams = RecyclerView.LayoutParams(
        RecyclerView.LayoutParams.MATCH_PARENT,
        RecyclerView.LayoutParams.WRAP_CONTENT
    )
}

private fun dp(context: Context, value: Int): Int {
    return (value * context.resources.displayMetrics.density).toInt()
}

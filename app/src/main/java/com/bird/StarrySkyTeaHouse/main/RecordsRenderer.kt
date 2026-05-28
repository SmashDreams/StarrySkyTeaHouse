package com.bird.StarrySkyTeaHouse.main

import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bird.StarrySkyTeaHouse.R
import com.bird.StarrySkyTeaHouse.records.GameRecord
import com.bird.StarrySkyTeaHouse.records.GameRecordSummary

class RecordsRenderer(
    private val mContext: Context,
    private val mContainer: LinearLayout
) {
    fun render(state: RecordsUiState) {
        when (state) {
            RecordsUiState.LoginRequired -> showMessage(R.string.records_login_first)
            RecordsUiState.Loading -> showMessage(R.string.records_loading)
            RecordsUiState.Empty -> showMessage(R.string.records_empty)
            RecordsUiState.Unavailable -> showMessage(R.string.records_missing_game)
            is RecordsUiState.Content -> showRecordCards(state.records, state.summary)
        }
    }

    private fun showMessage(messageRes: Int) {
        mContainer.removeAllViews()
        mContainer.addView(
            createText(mContext.getString(messageRes), R.color.tea_mist, 14f).apply {
                setBackgroundResource(R.drawable.bg_records_panel)
                gravity = Gravity.CENTER
                minHeight = dp(104)
                setPadding(dp(16), dp(16), dp(16), dp(16))
            },
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
    }

    private fun showRecordCards(records: List<GameRecord>, summary: GameRecordSummary) {
        mContainer.removeAllViews()
        addSummaryRow(summary)
        records.forEachIndexed { index, record -> addRecordCard(record, index > 0) }
    }

    private fun addSummaryRow(summary: GameRecordSummary) {
        val summaryRow = LinearLayout(mContext).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        mContainer.addView(
            summaryRow,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
        addSummaryItem(summaryRow, mContext.getString(R.string.records_summary_completed), summary.completedCount.toString())
        addSummaryItem(summaryRow, mContext.getString(R.string.records_summary_highest), mContext.getString(R.string.records_level_badge, summary.highestLevel))
        addSummaryItem(summaryRow, mContext.getString(R.string.records_summary_latest), mContext.getString(R.string.records_level_badge, summary.latestLevel))
    }

    private fun addSummaryItem(parent: LinearLayout, label: String, value: String) {
        val item = LinearLayout(mContext).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundResource(R.drawable.bg_record_summary)
            setPadding(dp(8), dp(10), dp(8), dp(10))
            addView(createText(value, R.color.tea_gold, 15f, bold = true))
            addView(createText(label, R.color.tea_muted, 11f))
        }
        parent.addView(
            item,
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginEnd = dp(8)
            }
        )
    }

    private fun addRecordCard(record: GameRecord, hasTopMargin: Boolean) {
        val card = LinearLayout(mContext).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundResource(R.drawable.bg_record_item)
            setPadding(dp(14), dp(14), dp(14), dp(14))
        }

        val badge = createText(mContext.getString(R.string.records_level_badge, record.level), R.color.tea_gold, 15f, bold = true).apply {
            gravity = Gravity.CENTER
            setBackgroundResource(R.drawable.bg_record_badge)
        }
        card.addView(badge, LinearLayout.LayoutParams(dp(72), dp(58)))

        val content = LinearLayout(mContext).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, 0, 0)
        }
        val statusText = if (record.completed) mContext.getString(R.string.record_completed) else mContext.getString(R.string.record_uncompleted)
        val statusBackground = if (record.completed) R.drawable.bg_record_status_complete else R.drawable.bg_record_status_pending
        content.addView(
            createText(statusText, R.color.tea_mist, 13f, bold = true).apply {
                setBackgroundResource(statusBackground)
                setPadding(dp(10), dp(5), dp(10), dp(5))
            },
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        )

        val metrics = LinearLayout(mContext).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        metrics.addView(createMetric(mContext.getString(R.string.records_elapsed_label), mContext.getString(R.string.records_seconds, record.elapsedSeconds)))
        metrics.addView(createMetric(mContext.getString(R.string.records_remaining_label), mContext.getString(R.string.records_seconds, record.remainingSeconds)))
        content.addView(
            metrics,
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = dp(10)
            }
        )

        card.addView(content, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        mContainer.addView(
            card,
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = if (hasTopMargin) dp(10) else dp(14)
            }
        )
    }

    private fun createMetric(label: String, value: String): LinearLayout {
        return LinearLayout(mContext).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.bg_record_metric)
            setPadding(dp(10), dp(8), dp(10), dp(8))
            addView(createText(label, R.color.tea_muted, 11f))
            addView(createText(value, R.color.tea_mist, 13f, bold = true))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginEnd = dp(8)
            }
        }
    }

    private fun createText(text: String, colorRes: Int, sizeSp: Float, bold: Boolean = false): TextView {
        return TextView(mContext).apply {
            this.text = text
            setTextColor(ContextCompat.getColor(mContext, colorRes))
            textSize = sizeSp
            includeFontPadding = true
            if (bold) setTypeface(typeface, Typeface.BOLD)
        }
    }

    private fun dp(value: Int): Int = (value * mContext.resources.displayMetrics.density).toInt()
}

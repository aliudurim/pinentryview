package com.da.pinentryview

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.ActionMode
import java.lang.RuntimeException
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem

class PinEntryEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs) {

    companion object {
        private const val XML_NAMESPACE_ANDROID = "http://schemas.android.com/apk/res/android"
    }


    private var mSpace = 24f //24 dp by default, space between the lines

    private var mCharSize = 0f
    private var mNumChars = 4f
    private var mLineSpacing = 8f //8dp by default, height of the text from our lines

    private var mMaxLength = 4

    private var mClickListener: OnClickListener? = null

    private var mLineStroke = 1f //1dp by default

    private var mLineStrokeSelected = 2f //2dp by default

    private var mLinesPaint: Paint

    private var mStates = arrayOf(
        intArrayOf(android.R.attr.state_selected),
        intArrayOf(android.R.attr.state_focused),
        intArrayOf(-android.R.attr.state_focused)
    )

    private var mColors = intArrayOf(
        Color.GREEN,
        Color.BLACK,
        Color.GRAY
    )

    var mColorStates = ColorStateList(mStates, mColors)

    init {
        val multi = context.resources.displayMetrics.density
        mLineStroke *= multi
        mLineStrokeSelected *= multi
        mLinesPaint = Paint(paint)
        mLinesPaint.strokeWidth = mLineStroke
        if (!isInEditMode) {
            val outValue = TypedValue()
            context.theme.resolveAttribute(
                R.attr.colorControlActivated,
                outValue, true
            )
            val colorActivated = outValue.data
            mColors[0] = colorActivated
            context.theme.resolveAttribute(
                R.attr.colorPrimaryDark,
                outValue, true
            )
            val colorDark = outValue.data
            mColors[1] = colorDark
            context.theme.resolveAttribute(
                R.attr.colorControlHighlight,
                outValue, true
            )
            val colorHighlight = outValue.data
            mColors[2] = colorHighlight
        }
        setBackgroundResource(0)
        mSpace *= multi //convert to pixels for our density

        mLineSpacing *= multi //convert to pixels for our density

        mMaxLength = attrs!!.getAttributeIntValue(XML_NAMESPACE_ANDROID, "maxLength", 4)
        mNumChars = mMaxLength.toFloat()

        //Disable copy paste
        super.setCustomSelectionActionModeCallback(object : ActionMode.Callback {
            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false
            }

            override fun onDestroyActionMode(mode: ActionMode) {}
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                return false
            }
        })

        // When tapped, move cursor to end of text.
        super.setOnClickListener { v ->
            setSelection(text!!.length)
            if (mClickListener != null) {
                mClickListener!!.onClick(v)
            }
        }
    }


    override fun setOnClickListener(l: OnClickListener?) {
        mClickListener = l
    }

    override fun setCustomSelectionActionModeCallback(actionModeCallback: ActionMode.Callback?) {
        throw RuntimeException("setCustomSelectionActionModeCallback() not supported.")
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        //super.onDraw(canvas);
        val availableWidth = width - paddingRight - paddingLeft
        mCharSize = if (mSpace < 0) {
            availableWidth / (mNumChars * 2 - 1)
        } else {
            (availableWidth - mSpace * (mNumChars - 1)) / mNumChars
        }
        var startX = paddingLeft
        val bottom = height - paddingBottom

        //Text Width
        val text = text
        val textLength = text!!.length
        val textWidths = FloatArray(textLength)
        paint.getTextWidths(getText(), 0, textLength, textWidths)
        for (i in 0 until mNumChars.toInt()) {
            updateColorForLines(i == textLength)
            canvas.drawLine(
                startX.toFloat(),
                bottom.toFloat(), startX + mCharSize, bottom.toFloat(), mLinesPaint
            )
            if (getText()!!.length > i) {
                val middle = startX + mCharSize / 2
                canvas.drawText(
                    text, i, i + 1, middle - textWidths[0] / 2, bottom - mLineSpacing,
                    paint
                )
            }
            startX += if (mSpace < 0) {
                (mCharSize * 2).toInt()
            } else {
                (mCharSize + mSpace).toInt()
            }
        }
    }

    private fun getColorForState(vararg states: Int): Int {
        return mColorStates.getColorForState(states, Color.GRAY)
    }

    /**
     * @param next Is the current char the next character to be input?
     */
    private fun updateColorForLines(next: Boolean) {
        if (isFocused) {
            mLinesPaint.strokeWidth = mLineStrokeSelected
            mLinesPaint.color = getColorForState(android.R.attr.state_focused)
            if (next) {
                mLinesPaint.color = getColorForState(android.R.attr.state_selected)
            }
        } else {
            mLinesPaint.strokeWidth = mLineStroke
            mLinesPaint.color = getColorForState(-android.R.attr.state_focused)
        }
    }
}
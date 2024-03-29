package com.penguodev.smartmd.ui.editor.toolbar

import android.view.LayoutInflater
import android.view.View
import androidx.databinding.DataBindingUtil
import com.penguodev.smartmd.R
import com.penguodev.smartmd.databinding.ToolbarExpertBinding

class ToolbarExpert(private val tManager: ToolbarManager) :
    ToolbarBase(tManager.viewGroup) {

    override val type: ToolbarType = ToolbarType.EXPERT

    override val binding = DataBindingUtil.inflate<ToolbarExpertBinding>(
        LayoutInflater.from(tManager.viewGroup.context),
        R.layout.toolbar_expert,
        tManager.viewGroup,
        false
    ).apply {
        lifecycleOwner = tManager.lifecycleOwner
        clickHandler = ClickHandler()
    }

    inner class ClickHandler {
        fun onClickPound(view: View) {
            tManager.mdEditor.adapter?.addCurrentItemText("#")
        }

        fun onClickStar(view: View) {
            tManager.mdEditor.adapter?.addCurrentItemText("*")
        }

        fun onClickTilde(view: View) {
            tManager.mdEditor.adapter?.addCurrentItemText("~")
        }

        fun onClickSB(view: View) {
            tManager.mdEditor.adapter?.addCurrentItemText("[", "]")
        }

        fun onClickParentheses(view: View) {
            tManager.mdEditor.adapter?.addCurrentItemText("(", ")")
        }

        fun onClickPB(view: View) {
            tManager.mdEditor.adapter?.addCurrentItemText(">")
        }

        fun onClickSwap(view: View) {
            tManager.swapToolbar()
        }

        fun onClickImage(view: View){
            tManager.onClickImage()
        }
    }
}
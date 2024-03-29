package com.penguodev.smartmd.ui.editor

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.penguodev.smartmd.MDApplication.Companion.RC_PICK_IMAGE
import com.penguodev.smartmd.R
import com.penguodev.smartmd.databinding.ActivityEditorBinding
import com.penguodev.smartmd.model.ItemDocument
import com.penguodev.smartmd.repository.MDDatabase
import com.penguodev.smartmd.ui.editor.toolbar.ToolbarManager
import com.penguodev.smartmd.ui.editor.toolbar.ToolbarType
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.penguodev.smartmd.common.ComfyUtil
import com.penguodev.smartmd.repository.PrefManager
import kotlinx.coroutines.*
import timber.log.Timber


class EditorActivity : AppCompatActivity() {
    companion object {
        fun createActivityIntent(context: Context, documentId: Long?): Intent {
            return Intent(context, EditorActivity::class.java).apply {
                putExtra("documentId", documentId)
            }
        }
    }

    private lateinit var binding: ActivityEditorBinding
    private var documentId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_editor)
        binding.lifecycleOwner = this
        binding.clickHandler = ClickHandler()
        binding.mdEditor.setLifecycleOwner(this)

        ToolbarManager(this, binding.mdEditor, binding.editorSectionToolbar).apply {
            attachToolbar(ToolbarType.FAST)
            pickImageFunction = ::pickImage
        }

        documentId = intent.getLongExtra("documentId", -1).let {
            if (it != -1L) it
            else null
        }
        loadData()
    }

    private fun checkTemporarySavedData() {
        if (documentId == PrefManager.temporarySavedId && PrefManager.temporarySavedText != null && PrefManager.temporarySavedText?.length != 0) {
            AlertDialog.Builder(this)
                .setMessage("임시저장된 내용이 있습니다.")
                .setPositiveButton("불러오기") { dialog, which ->
                    binding.mdEditor.setContent(PrefManager.temporarySavedText ?: "")
                    dialog.dismiss()
                }
                .setNegativeButton("취소") { dialog, which ->
                    dialog.dismiss()
                }
                .setCancelable(false)
                .show()
        }
    }

    private fun loadData() {
        documentId?.let { documentId ->
            GlobalScope.launch(Dispatchers.Main) {
                withContext(Dispatchers.Default) {
                    MDDatabase.instance.documentDao.getItem(documentId)
                }.let {
                    if (it == null) {
                        this@EditorActivity.documentId = null
                        binding.mdEditor.notifyDataSetChanged(true)
                    } else {
                        this@EditorActivity.documentId = it.id
                        binding.mdEditor.setContent(it.text)
                    }
                }
            }
        } ?: run {
            binding.mdEditor.notifyDataSetChanged(true)
        }
        checkTemporarySavedData()
    }

    override fun onPause() {
        super.onPause()
        PrefManager.saveTemporary(documentId, binding.mdEditor.getContent())
    }

    inner class ClickHandler {

        fun onClickSave(view: View) {
            saveAndFinish()
        }
    }

    private fun saveAndFinish() {
        GlobalScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.Default) {
                val currentTime = System.currentTimeMillis()
                ItemDocument(
                    documentId,
                    binding.mdEditor.adapter?.getHeader() ?: "",
                    binding.mdEditor.getContent(),
                    currentTime,
                    currentTime
                ).let {
                    MDDatabase.instance.documentDao.submit(it)
                }
            }
            setResult(Activity.RESULT_OK)
            finish()
            PrefManager.saveTemporary(null, null)
        }
    }

    fun pickImage() {
        Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }.let {
            startActivityForResult(Intent.createChooser(it, "Select Picture"), RC_PICK_IMAGE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                RC_PICK_IMAGE -> {
                    if (data == null || data.data == null) {
                        Toast.makeText(this@EditorActivity, "오류가 발생했습니다", Toast.LENGTH_SHORT).show()
                        Timber.e("error occur.")
                        return
                    } else {
                        val uri: Uri = data.data!!
                        val fileName = uri.toString().split("/").last()
                        ComfyUtil.savefile(this@EditorActivity, uri).also {
                            binding.mdEditor.adapter?.addCurrentItemText("![$fileName]($it)")
                        }
                    }
                }
            }
        }
    }
}
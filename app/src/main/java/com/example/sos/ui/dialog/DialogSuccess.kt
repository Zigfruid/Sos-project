package com.example.sos.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.example.sos.databinding.DialogSuccessBinding

class DialogSuccess(context: Context):Dialog(context) {
    private lateinit var binding:DialogSuccessBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
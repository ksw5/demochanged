package com.example.dididemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    lateinit var viewModel: BotChatViewModel
    var ddResponseList: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = ViewModelProvider(this)[BotChatViewModel::class.java]
        viewModel.responseLiveData.observe(this, OnResponseReceived())
        init_engine.setOnClickListener {
            viewModel.initBotChat()
            showSendStatement()
        }
        send_to_engine_button.setOnClickListener(OnSendStatementClick())
    }

    private fun showSendStatement() {
        send_to_engine_et.visibility = View.VISIBLE
        send_to_engine_button.visibility = View.VISIBLE
    }

    inner class OnSendStatementClick : View.OnClickListener {
        override fun onClick(p0: View?) {
            viewModel.sendStatementToEngine(send_to_engine_et.text.toString())
            send_to_engine_et.text.clear()
            send_to_engine_et.hint = "Enter a statement"
        }
    }

    inner class OnResponseReceived : Observer<String> {
        override fun onChanged(t: String?) {
            t?.apply {
                ddResponseList.add(t)
            }
            response_value.text = t?:"No response"
        }
    }
}
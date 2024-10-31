package com.aos.beacondistance

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.aos.beacondistance.databinding.ActivityLoginBinding
import com.aos.beacondistance.http.HttpRetrofitService
import com.aos.beacondistance.http.LoginData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.UnknownHostException


class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    private var canGoNext = false
    private var url: String? = null
    private var vtype: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        initBinding()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding.activity = this
        binding.lifecycleOwner = this
    }

    private suspend fun doLogin() {
        try {
            val service = HttpRetrofitService.createHttpService()
            val data = LoginData("beaconuser", "beaconpw")

            service.checkLogin(data)
                .also {
                    val response = it.body()
                    val jsonObject = JSONObject(response)
                    val updateType = jsonObject.optString("update")
                    goMain()
                }
        } catch (e: UnknownHostException) {
            goMain()
        } catch (e: Exception) {
            Log.e(TAG, "${e.printStackTrace()}")
            goMain()
        }
    }

    private fun goMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("url", url)
        intent.putExtra("vtype", vtype)
        startActivity(intent)
        finish()
    }

    fun onClickLoginButton(view: View) {
        CoroutineScope(Dispatchers.Default).launch {
            doLogin()
        }
    }
}
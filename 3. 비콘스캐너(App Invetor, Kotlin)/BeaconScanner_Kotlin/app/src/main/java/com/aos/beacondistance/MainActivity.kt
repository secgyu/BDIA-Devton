package com.aos.beacondistance

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.aos.beacondistance.databinding.ActivityMainBinding
import com.aos.beacondistance.http.CircleData
import com.aos.beacondistance.http.HttpRetrofitService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.json.JSONObject


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private var isScanning = false
    private val httpCoroutine = CoroutineScope(Dispatchers.IO)
    private val gson = Gson()

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
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.activity = this
        binding.lifecycleOwner = this
    }

    private suspend fun getUserData() {
        try {
            val service = HttpRetrofitService.createHttpService()
            service.getUserData()
                .also {

                    val circleDataType = object : TypeToken<List<CircleData>>() {}.type
                    val circleDataList: ArrayList<CircleData> = gson.fromJson(it.body(), circleDataType)
                    binding.circleView.setCircleList(circleDataList)
                }
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "${e.printStackTrace()}")
        }
    }


    /****** test function ************/
    fun setListlist01(view : View) {
        // TODO : 테스트 기능 삭제 후 버튼 원래 기능 활성화(거리 정보 받아오는 쓰레드 시작/종료)
//        if (isScanning) {
//            httpCoroutine.cancel()
//            return
//        }
//        httpCoroutine.launch {
//            isScanning = true
//            getUserData()
//            delay(5000)
//        }

        // test
        val list: ArrayList<CircleData> = arrayListOf(
            CircleData("nna0", 86),
            CircleData("nna1", 86),
            CircleData("nna2", 90),
            CircleData("nna3", 40),
            CircleData("nna4", 36),
            CircleData("nna5", 70),
            CircleData("nna6", 80),
        )
        binding.circleView.setCircleList(list)
    }

    fun setListlist02(view : View) {
        val list: ArrayList<CircleData> = arrayListOf(
            CircleData("nna0", 46),
            CircleData("nna1", 60),
            CircleData("nna2", 80),
            CircleData("nna3", 96),
            CircleData("nna4", 40),
            CircleData("nna5", 70),
            CircleData("nna6", 70),
            CircleData("nna6", 70),
            CircleData("nna7", 70),
            CircleData("nna8", 70),
            CircleData("nna9", 70),
            CircleData("nna10", 70),
            CircleData("nna11", 70),
            CircleData("nna12", 70),
        )
        binding.circleView.setCircleList(list)
    }

    fun setListlist03(view : View) {
        val list: ArrayList<CircleData> = arrayListOf(
            CircleData("nna0", 66),
            CircleData("nna1", 76),
            CircleData("nna2", 88),
            CircleData("nna3", 90),
            CircleData("nna4", 96),
            CircleData("nna5", 90),
            CircleData("nna6", 70),
            CircleData("nna7", 87),
            CircleData("nna8", 80),
            CircleData("nna9", 46),
            CircleData("nna10", 89),
        )
        binding.circleView.setCircleList(list)
    }

}
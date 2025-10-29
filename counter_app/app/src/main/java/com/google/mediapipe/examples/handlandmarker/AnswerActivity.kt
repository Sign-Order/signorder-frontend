package com.google.mediapipe.examples.handlandmarker

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class AnswerActivity : AppCompatActivity() {

    private lateinit var signVideo: SignVideo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_answer)

        signVideo = findViewById(R.id.signVideo)

        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            if (!isFinishing) {
                val intent = Intent(this, HomeActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                startActivity(intent)
                finish()
            }
        }

        val urls = intent.getStringArrayListExtra("sign_urls")
        val fastMessage = intent.getStringExtra("fast_message")
        val videoType = intent.getStringExtra("videoType")

        // 결제해드릴게요, 예, 아니오, 잠시만 기다려주세요 부분 영상으로 추가하기
        //        signVideo.setup(listOf(R.raw.video12_yes))
        //        signVideo.setup(listOf(R.raw.video13_no))
        //        signVideo.setup(listOf(R.raw.video14_pay))
        when {
            urls != null -> {
                signVideo.setupWithUrls(urls)
            }
            fastMessage != null -> {
                when (fastMessage) {
                    "네" -> signVideo.setup(listOf(R.raw.video13_yes))
                    "아니요" -> signVideo.setup(listOf(R.raw.video14_no))
                    "잠시만 기다려주세요" -> signVideo.setup(listOf(R.raw.video15_wait))
                    "결제해드릴게요" -> signVideo.setup(listOf(R.raw.video16_pay))
                }
            }
            videoType != null -> {
                when (videoType) {
                    "restroom" -> signVideo.setup(listOf(R.raw.video10_quickqna_restroom))
                    "wifi" -> signVideo.setup(listOf(R.raw.video11_quickqna_wifi))
                    else -> null
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        WebSocketService.onSignMessageReceived = null
    }
}

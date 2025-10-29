package com.google.mediapipe.examples.handlandmarker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout

class HomeActivity : AppCompatActivity() {
    private lateinit var signVideo: SignVideo
    private lateinit var inquiryButton: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        signVideo = findViewById(R.id.signVideo)
        inquiryButton = findViewById(R.id.inquiryButton)

        signVideo.setup(
            listOf(
                R.raw.video5_guide_staff_button,
                R.raw.video6_guide_additional_mainqna,
                R.raw.video7_guide_kor_signlang_mainqna
            )
        )

        // 수어 영상 수신 -> LoadActivity 에서 AnswerActivity 로 연결
        WebSocketService.onSignUrlsReceived = {urls ->
            runOnUiThread{
                val intent = Intent(this, LoadingActivity::class.java)
                intent.putStringArrayListExtra("sign_urls", ArrayList(urls))
                startActivity(intent)
            }
        }

        // "문의사항 있으신가요?" 알림 수신 시 OX 선택 화면으로 전환
        WebSocketService.onSignOrderReceived = { title, number ->
            runOnUiThread {
                val layoutType = when (title) {
                    "order" -> "order"
                    "inquiry" -> "inquiry"
                    else -> null
                }

                if (layoutType != null) {
                    val intent = Intent(this, OxSelectionAnswerActivity::class.java)
                    intent.putExtra("layoutType", layoutType)
                    startActivity(intent)
                } else {
                    // 예외 처리 (로그만 찍고 화면 전환 안 함)
                    Log.e("HomeActivity", "WebSocket 수신 오류: 알 수 없는 title = $title")
                }
            }
        }

        // WebSocket 연결
        WebSocketService.connect()

        inquiryButton.setOnClickListener {
            val intent = Intent(this, QuestionActivity::class.java)
            intent.putExtra("layoutType", "inquiry")
            startActivity(intent)
        }
    }
}

package com.google.mediapipe.examples.handlandmarker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import com.example.grpc.GrpcClient
import android.util.Log

class OxSelectionAnswerActivity : AppCompatActivity() {
    private val grpcClient = GrpcClient()
    private lateinit var signVideo: SignVideo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ox_selection_answer)

        signVideo = findViewById(R.id.signVideo)

        signVideo.setup(
            listOf(
                R.raw.video1_qna_additional,
                R.raw.video2_guide_additional_qna,
                R.raw.video3_guide_korean_signlang,
                R.raw.video4_guide_signlang_complete
            )
        )

        val inquiryNumber = WebSocketService.currentInquiryNum ?: -1
        val inquiryType = WebSocketService.currentInquiryType ?: "inquiry"

        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener{
            val intent = Intent(this, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }

        val yesButton = findViewById<LinearLayout>(R.id.yesButton)
        val noButton = findViewById<LinearLayout>(R.id.noButton)

        yesButton.setOnClickListener{
            val intent = Intent(this, QuestionActivity::class.java)
            Log.d("OXDebug", "yesButton 눌림, inquiryType=$inquiryType")
            intent.putExtra("layoutType", inquiryType)
            startActivity(intent)
        }

        noButton.setOnClickListener{
            grpcClient.sendFastInquiry(title = inquiryType, num = inquiryNumber) { success ->
                runOnUiThread {
                    if (success) {
                        println("FastInquiry 성공")
                    } else {
                        println("FastInquiry 오류")
                    }
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

}
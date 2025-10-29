package com.google.mediapipe.examples.handlandmarker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.example.grpc.GrpcClient
import com.google.mediapipe.examples.handlandmarker.databinding.ActivityQuestionBinding
import com.google.mediapipe.examples.handlandmarker.databinding.ActivityGeneralQuestionBinding
import com.google.mediapipe.examples.handlandmarker.fragment.CameraFragment

class QuestionActivity : AppCompatActivity() {
    private var bindingOrder: ActivityQuestionBinding? = null
    private var bindingInquiry: ActivityGeneralQuestionBinding? = null

    private var countdownOverlay: FrameLayout? = null
    private var countdownText: android.widget.TextView? = null
    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1001
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layoutType = intent.getStringExtra("layoutType")
        when (layoutType) {
            "order" -> {
                bindingOrder = ActivityQuestionBinding.inflate(layoutInflater)
                setContentView(bindingOrder!!.root)
                initOrderUI()
            }

            "inquiry" -> {
                bindingInquiry = ActivityGeneralQuestionBinding.inflate(layoutInflater)
                setContentView(bindingInquiry!!.root)
                initInquiryUI()
            }

            else -> {
                Log.e("QuestionActivity", "layoutType이 지정되지 않았습니다.")
                finish()
            }
        }
    }

    private fun initOrderUI() {
        bindingOrder?.apply {
            countdownOverlay = root.findViewById(R.id.countDownOverlay)
            countdownText = root.findViewById(R.id.countDownText)
            startCountdown()

            backButton.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

            setupSendButton(
                sendButton = sendButton,
                containerId = R.id.order_fragment_container,
            )

            val restRoomButton = findViewById<ImageButton>(R.id.restRoomButton)
            val wifiButton = findViewById<ImageButton>(R.id.wifiButton)

            restRoomButton.setOnClickListener {
                val intent = Intent(this@QuestionActivity, AnswerActivity::class.java)
                intent.putExtra("videoType", "restroom")
                startActivity(intent)
            }

            wifiButton.setOnClickListener {
                val intent = Intent(this@QuestionActivity, AnswerActivity::class.java)
                intent.putExtra("videoType", "wifi")
                startActivity(intent)
            }
        }
    }

    private fun initInquiryUI() {
        bindingInquiry?.apply {
            countdownOverlay = root.findViewById(R.id.countDownOverlay)
            countdownText = root.findViewById(R.id.countDownText)
            startCountdown()

            backButton.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

            setupSendButton(
                sendButton = sendButton,
                containerId = R.id.inquiry_fragment_container,
            )

            val restRoomButton = findViewById<ImageButton>(R.id.restRoomButton)
            val wifiButton = findViewById<ImageButton>(R.id.wifiButton)

            restRoomButton.setOnClickListener {
                val intent = Intent(this@QuestionActivity, AnswerActivity::class.java)
                intent.putExtra("videoType", "restroom")
                startActivity(intent)
            }

            wifiButton.setOnClickListener {
                val intent = Intent(this@QuestionActivity, AnswerActivity::class.java)
                intent.putExtra("videoType", "wifi")
                startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkCameraPermissionAndInit()
    }

    override fun onDestroy() {
        super.onDestroy()
        bindingOrder = null
        bindingInquiry = null
    }

    private fun checkCameraPermissionAndInit() {
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // NavHostFragment 내부 CameraFragment 찾기
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.order_fragment_container) as? NavHostFragment
                    ?: supportFragmentManager.findFragmentById(R.id.inquiry_fragment_container) as? NavHostFragment

            val cameraFragment = navHostFragment?.childFragmentManager?.fragments
                ?.firstOrNull { it is CameraFragment } as? CameraFragment

            // 찾은 CameraFragment의 initializeCamera() 호출
            cameraFragment?.initializeCamera()
        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        }
    }

    private fun startCountdown(onFinish: () -> Unit = {}) {
        countdownOverlay?.visibility = View.VISIBLE
        var count = 3
        countdownText?.text = count.toString()
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                count--
                if (count > 0) {
                    countdownText?.text = count.toString()
                    handler.postDelayed(this, 1000)
                } else {
                    countdownOverlay?.visibility = View.GONE
                    onFinish() 
                }
            }
        }

        handler.postDelayed(runnable, 1000)
    }

    private fun setupSendButton(sendButton: View, containerId: Int) {
        val inquiryNumber = WebSocketService.currentInquiryNum ?: -1
        val inquiryType = WebSocketService.currentInquiryType ?: "inquiry"

        startCountdown{
            sendButton.setOnClickListener {
                Log.d("QuestionActivity", "전송 버튼 클릭됨")
                
                val navHostFragment =
                    supportFragmentManager.findFragmentById(containerId) as? NavHostFragment

                if (navHostFragment == null) {
                    Log.e("QuestionActivity", "NavHostFragment를 찾을 수 없습니다.")
                    return@setOnClickListener
                }

                val cameraFragment = navHostFragment
                    ?.childFragmentManager
                    ?.fragments
                    ?.firstOrNull { it is CameraFragment } as? CameraFragment

                if (cameraFragment != null) {
                    Log.d("QuestionActivity", "CameraFragment 찾음")
                    
                    // 카메라 상태 확인
                    val cameraStatus = cameraFragment.getCameraStatus()
                    Log.d("QuestionActivity", "카메라 상태: $cameraStatus")
                    
                    if (!cameraFragment.isCameraInitialized()) {
                        Log.w("QuestionActivity", "카메라가 초기화되지 않았습니다: $cameraStatus")
                        Toast.makeText(this@QuestionActivity, "카메라를 초기화하는 중입니다. 잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    
                    val frameData = cameraFragment.getAllFrameVectors()
                    if (frameData.isNotEmpty()) {
                        Log.d("QuestionActivity", "프레임 데이터 크기: ${frameData.size}")
                        val grpcClient = GrpcClient()
                        Log.d("GrpcLog", "gRPC 요청 시작")
                        Log.d("GrpcLog", "frameData 길이 = ${frameData.size}")
                        Log.d("GrpcLog", "frameData 일부 = ${frameData.take(10)}")
                        Log.d("GrpcLog", "inquiryType = $inquiryType")
                        Log.d("GrpcLog", "보내는 채팅방 번호: $inquiryNumber")
                        grpcClient.sendAllFrameData(
                            frameData = frameData,
                            inquiryType = inquiryType,
                            num = inquiryNumber,
                            onLog = { message -> Log.d("GrpcLog", message) }
                        )
                        grpcClient.shutdown()
                        
                        // 전송 후 LoadingActivity로 이동
                        val intent = Intent(this@QuestionActivity, LoadingActivity::class.java)
                        startActivity(intent)
                    } else {
                        Log.w("QuestionActivity", "아직 누적된 데이터가 없습니다.")
                        Toast.makeText(this@QuestionActivity, "카메라 데이터를 수집 중입니다. 잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("QuestionActivity", "CameraFragment를 찾을 수 없습니다.")
                    Toast.makeText(this@QuestionActivity, "카메라를 초기화할 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

package com.capstone.whereigo

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.capstone.whereigo.databinding.DialogVoiceRecordBinding
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.*

class VoiceRecordDialog : DialogFragment() {
    private var _binding: DialogVoiceRecordBinding? = null
    private val binding get() = _binding!!

    private lateinit var tts: TextToSpeech
    private var speechRecognizer: SpeechRecognizer? = null
    private lateinit var recognizerIntent: Intent
    private var isListening = false
    private val openAI = OpenAI("")


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DialogVoiceRecordBinding.inflate(inflater, container, false)
        initSpeechRecognizer()

        tts = TextToSpeech(context ){
            val result = tts.setLanguage(Locale.KOREAN)
        }

        // UI 뜨고 500ms 후 음성 인식 시작
        binding.root.postDelayed({
            if (!isListening) {
                isListening = true
                speechRecognizer?.startListening(recognizerIntent)
            }
        }, 500)

        return binding.root
    }

    private fun initSpeechRecognizer() {
        speechRecognizer?.destroy()

        if (!SpeechRecognizer.isRecognitionAvailable(requireContext())) {
            dismissAllowingStateLoss()
            return
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())
        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.KOREAN)
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                binding.txtRecording.text = "말씀하세요..."
            }

            override fun onBeginningOfSpeech() {
                binding.txtRecording.text = "듣고 있어요..."
            }

            override fun onEndOfSpeech() {
                binding.txtRecording.text = "인식 중..."
            }

            override fun onResults(results: Bundle?) {
                isListening = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                //결과 값 출력하는 부분
                val resultText = matches?.joinToString(" ") ?: "결과 없음"
                Log.d("VoiceDialog", "최종 인식 결과: $resultText")
                if (resultText != "결과 없음"){
                    lifecycleScope.launch {
                        val chatCompletionRequest = ChatCompletionRequest(
                            model = ModelId("gpt-4o-mini"),
                            messages = listOf(
                                ChatMessage(
                                    role = ChatRole.System,
                                    content = """
                                        너는 사회적 약자를 위한 실내 네비게이션 앱 'wigo'야
                                        질문에 대해 다음 시퀀스를 따라가
                                        1. 짧고 간결하게 공손하게 답해
                                        2. 답변의 형식은 { 'command': ~명령~, 'context': ~명령 내용~} 으로 나오게
                                        3. 길 찾는 것과 관련 된 명령 일 시, 반환은 'navigate', 목적지
                                        4. 설정 변경에 관련 된 명령 일 시, 반환은 'settings', 설정내용
                                        5. 그 외 답변 일 시, 반환은 'except', 자율적인 답변
                                        6. 답변이 이상하면 너가 약간의 추론을 통해서 올바른 답변이 나오게 해
                                    """.trimIndent()
                                ),
                                ChatMessage(
                                    role = ChatRole.User,
                                    content = resultText  // 여기 문자열로!
                                )
                            )
                        )

                        try {
                            val completion: ChatCompletion = openAI.chatCompletion(chatCompletionRequest)
                            //Log.d("VoiceDialog", completion.choices.firstOrNull()?.message?.content.toString())
                            val jsonAnswer = JSONObject(completion.choices.firstOrNull()?.message?.content)
                            val command = jsonAnswer["command"].toString()
                            val reply = jsonAnswer["context"].toString()
                            Log.d("VoiceDialog", "응답: ${jsonAnswer}")
                            //Toast.makeText(requireContext(), "응답: ${completion.choices.firstOrNull()?.message?.content}", Toast.LENGTH_SHORT).show()
                            if(command == "navigate"){

                            }
                            else if(command == "settings"){
                                if(reply == "volume"){
                                    val value = jsonAnswer["value"] as Int

                                }
                            }
                            else if(command == "except"){
                                tts.speak("${reply}", TextToSpeech.QUEUE_FLUSH, null, null)
                            }
                        } catch (e: Exception) {
                            Log.e("VoiceDialog", "OpenAI 호출 실패: ${e.message}")
                        }

                        if (isAdded) dismissAllowingStateLoss()
                    }
                } else{
                    if (isAdded) dismissAllowingStateLoss()
                }
            }

            override fun onError(error: Int) {
                isListening = false
                Log.e("VoiceDialog", "음성 인식 에러: $error")
                if (isAdded) dismissAllowingStateLoss()
            }

            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onPartialResults(partialResults: Bundle?) {}
        })
    }



    override fun onDestroyView() {
        speechRecognizer?.destroy()
        _binding = null
        super.onDestroyView()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog?.window?.setGravity(Gravity.CENTER)
    }
}
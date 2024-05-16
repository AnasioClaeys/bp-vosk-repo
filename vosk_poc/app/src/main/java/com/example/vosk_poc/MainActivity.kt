package com.example.vosk_poc

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vosk_poc.ui.theme.Vosk_pocTheme
import kotlinx.serialization.json.Json
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import org.vosk.android.SpeechStreamService
import org.vosk.android.StorageService
import java.io.IOException

class MainActivity : ComponentActivity(), RecognitionListener {

    private var model: Model? = null
    private lateinit var speechService: SpeechService
    private var speechStreamService: SpeechStreamService? = null
    private var stringSpeech by mutableStateOf("")
    private var finalSpeechResult = ArrayList<String>()
    private var checkedStates by mutableStateOf(mapOf<Int, Boolean>())
    private var selectedModel by mutableStateOf("vosk-model-small-en-us-0.15")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initModel(selectedModel)

        setContent {
            Vosk_pocTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting(
                        stringSpeech,
                        finalSpeechResult,
                        startSpeechRecognition = { startSpeechRecognition() },
                        stopRecognition = { onDestroy() },
                        addToList = { addToList() },
                        removeFromList = { index -> removeFromList(index) },
                        checkedStates = checkedStates,
                        updateCheckState = { index, isChecked ->
                            updateCheckState(
                                index,
                                isChecked
                            )
                        },
                        setSelectedModel = { model ->
                            selectedModel = model
                            initModel(model)
                        }
                    )
                }
            }
        }
    }


    private fun parseJsonText(a: String): String {
        return Json.decodeFromString<DecodeJsonText>(a).partial
    }

    private fun parseJsonResult(a: String): String {
        return Json.decodeFromString<DecodeJsonResult>(a).text
    }

    private fun initModel(modelName: String) {
        StorageService.unpack(this, modelName, "model",
            { model: Model? ->
                this.model = model
            }
        ) { exception: IOException ->
            exception.printStackTrace()
        }
    }

    override fun onPartialResult(hypothesis: String?) {
    }

    override fun onResult(hypothesis: String?) {
        val result = parseJsonResult(hypothesis!!)
        stringSpeech += " $result"
    }

    override fun onFinalResult(hypothesis: String?) {
        if (speechStreamService != null) {
            speechStreamService = null
        }
        speechService.stop()
    }

    override fun onError(exception: Exception?) {
        exception?.printStackTrace()
    }

    override fun onTimeout() {
        TODO("Not yet implemented")
    }

    private fun startSpeechRecognition() {
        try {
            val rec = Recognizer(model, 16000.0f)
            speechService = SpeechService(rec, 16000.0f)
            speechService.startListening(this)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (speechService != null) {
            speechService.stop()
            speechService.shutdown()
        }
        if (speechStreamService != null) {
            speechStreamService!!.stop()
        }
    }

    private fun addToList() {
        if (stringSpeech.isEmpty()) return
        finalSpeechResult.add(stringSpeech.trim())
        stringSpeech = ""
        onDestroy()
    }

    private fun updateCheckState(index: Int, isChecked: Boolean) {
        checkedStates = checkedStates.toMutableMap().apply {
            this[index] = isChecked
        }
    }

    private fun removeFromList(index: Int) {
        finalSpeechResult = ArrayList(finalSpeechResult.filterIndexed { i, _ -> i != index })
        checkedStates = checkedStates.filterKeys { it != index }
    }
}

@Composable
fun Greeting(
    recognizedText: String,
    final: ArrayList<String>,
    startSpeechRecognition: () -> Unit,
    modifier: Modifier = Modifier,
    stopRecognition: () -> Unit,
    addToList: () -> Unit,
    removeFromList: (Int) -> Unit,
    checkedStates: Map<Int, Boolean>,
    updateCheckState: (Int, Boolean) -> Unit,
    setSelectedModel: (String) -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { setSelectedModel("vosk-model-small-en-us-0.15") },
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f)

            ) {
                Text(
                    text = "en-us",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 16.sp
                )
            }

            Button(
                onClick = { setSelectedModel("vosk-model-small-nl-0.22") },
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f)

            ) {
                Text(
                    text = "nl-nl",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 16.sp
                )
            }
        }



        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = startSpeechRecognition,
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f)

            ) {
                Text(
                    text = "Start spraakherkenning",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 16.sp
                )
            }

            Button(
                onClick = stopRecognition,
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f)

            ) {
                Text(
                    text = "Stop spraakherkenning",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 16.sp
                )
            }
        }


        Text(
            text = "Herkende spraak:",
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.padding(16.dp)
        )
        Row {
            Text(
                text = recognizedText,
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            )

            Button(onClick = addToList, modifier = Modifier.padding(8.dp)) {
                Icon(Icons.Outlined.Add, contentDescription = "Add")
            }
        }
        Divider()


        final.forEachIndexed { index, item ->
            Row {
                Checkbox(
                    checked = checkedStates[index] ?: false,
                    onCheckedChange = { isChecked ->
                        updateCheckState(index, isChecked)
                        if (isChecked) {
                            removeFromList(index)
                        }
                    }
                )
                Text(
                    text = item,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }
    }
}

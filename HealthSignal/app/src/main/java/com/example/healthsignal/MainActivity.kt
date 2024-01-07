package com.example.healthsignal

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.healthsignal.ui.theme.Green1
import com.example.healthsignal.ui.theme.HealthSignalTheme
import com.example.healthsignal.ui.theme.PoppinsFamily

import android.os.Bundle
import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.healthsignal.ml.DrinkingModelTf
import com.example.healthsignal.ml.SmokingModelTf
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

var drinking by mutableStateOf("")
var smoking by mutableStateOf("")

@Composable
fun SplashScreen() {
    var drinking by remember { mutableStateOf("") }
    var smoking by remember { mutableStateOf("") }
    val context = LocalContext.current
    val properties = listOf(
        "Sex", "Age", "Height", "Waistline",
        "Sight Left", "Sight Right", "Hear Left", "Hear Right",
        "SBP", "DBP", "BLDS", "Tot Cholesterol", "HDL Cholesterol",
        "LDL Cholesterol", "Urine Protein",
        "Serum Creatinine", "SGOT AST", "SGOT ALT", "Gamma GTP"
    )

    val propertyStates: Map<String, MutableState<String>> = remember {
        properties.associateWith { property ->
            mutableStateOf("")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 22.dp, 16.dp, 6.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Logo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.healthsignallogo_1),
                contentDescription = null,
                modifier = Modifier
                    .size(150.dp)
            )
        }

        // Title
        Text(
            text = "Predict Health From BodySignal",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = PoppinsFamily,
            color = Color.Black,
            modifier = Modifier.padding(16.dp, 0.dp),
        )

        // Description
        Text(
            text = "With this app you can predict whether a person is a drinker and/or a smoker",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = PoppinsFamily,
            color = Green1,
            modifier = Modifier.padding(16.dp, 0.dp),
        )

        // Text Fields
        Column(modifier = Modifier.padding(16.dp)) {
            for (property in properties) {
                healthInfoTextField(property, propertyStates[property]!!)
            }
        }

        // Predict Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    // Get values from your input fields or states
                    val values: Map<String, String> = properties.associateWith { property ->
                        propertyStates[property]?.value.orEmpty()
                    }
                    Log.d("InputValues", values.toString())
                    // Use the TFLiteManager to make predictions
                    val tfliteManager = TFLiteManager(context)
                    drinking = tfliteManager.makePredictionsDrinking(values).toString()
                    smoking = tfliteManager.makePredictionsSmoking(values).toString()

                    drinking = if(drinking.toInt() == 1) "Yes" else "No"
                    smoking = when (smoking.toInt()) {
                        0 -> "Never smoked"
                        1 -> "Used to smoke"
                        2 -> "Still smoking"
                        else -> "Unknown"
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Green1),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Predict",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontFamily = PoppinsFamily,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        // Display predictions
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Drinker prediction
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp, 8.dp),
            ) {
                Text(
                    text = "Drinker (Yes/No)",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontFamily = PoppinsFamily,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Prediction: $drinking",
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontFamily = PoppinsFamily,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Smoker prediction
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp, 8.dp),
            ) {
                Text(
                    text = "Smoker (Never Smoked/Used to Smoke/Still Smoking)",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontFamily = PoppinsFamily,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Prediction: $smoking",
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontFamily = PoppinsFamily,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}


class TFLiteManager(private val context: Context) {
    fun makePredictionsDrinking(values: Map<String, String>): Int {
        // Ensure that the 'context' variable is defined and accessible

        val model = DrinkingModelTf.newInstance(context)

        // Check if the input features match the model's expectations
        if (values.size == 19) {
            val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 19), DataType.FLOAT32)

            // Convert values to float array
            val floatValues = values.values.map { it.toFloat() }.toFloatArray()

            // Convert float array to ByteBuffer
            val byteBuffer = ByteBuffer.allocateDirect(floatValues.size * 4)
            byteBuffer.order(ByteOrder.nativeOrder())
            for (value in floatValues) {
                byteBuffer.putFloat(value)
            }
            byteBuffer.rewind()

            // Load ByteBuffer into TensorBuffer
            inputFeature0.loadBuffer(byteBuffer)

            // Run model inference and get the result
            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer

            // Decide the class based on a threshold value (e.g., 0.5)
            val threshold = 0.5
            val prediction = if (outputFeature0.getFloatValue(0) >= threshold) 1 else 0

            // Log the result
            Log.d("TFLiteManager", "Drinker Prediction: $prediction")

            // Release model resources
            model.close()

            return prediction
        } else {
            Log.e("TFLiteManager", "Incorrect number of input features.")
        }

        return -1 // Return -1 to indicate an error or unknown prediction
    }



    fun makePredictionsSmoking(values: Map<String, String>): Int {
        try {
            val model = SmokingModelTf.newInstance(context)
            val inputTensorShape = intArrayOf(1, 19)
            val inputFeature0 = TensorBuffer.createFixedSize(inputTensorShape, DataType.FLOAT32)

            // Replace the following with your actual input data
            val inputFloatArray: FloatArray = values.values.map { it.toFloatOrNull() ?: 0.0f }.toFloatArray()

            if (inputFloatArray.size != inputTensorShape[1]) {
                println("Error: Input size mismatch. Expected size: ${inputTensorShape[1]}, Actual size: ${inputFloatArray.size}")
                return -1 // Return -1 to indicate an error or unknown prediction
            }

            // Initialize ByteBuffer
            val byteBuffer = ByteBuffer.allocateDirect(inputFloatArray.size * 4)
            byteBuffer.order(ByteOrder.nativeOrder())

            // Load input data into ByteBuffer
            for (value in inputFloatArray) {
                byteBuffer.putFloat(value)
            }

            // Load ByteBuffer into TensorBuffer
            inputFeature0.loadBuffer(byteBuffer)

            // Runs model inference and gets result
            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer

            // Decide the class based on logic or threshold values
            val prediction: Int = when {
                outputFeature0.getFloatValue(0) < 0.5 -> 0 // Class 0: Tidak pernah smoking
                outputFeature0.getFloatValue(0) < 1.5 -> 1 // Class 1: Pernah smoking
                else -> 2 // Class 2: Still smoking
            }

            // Releases model resources if no longer used
            model.close()

            return prediction
        } catch (e: Exception) {
            Log.e("TFLiteManager", "Error during smoking inference: $e")
        }

        return -1 // Return -1 to indicate an error or unknown prediction
    }

}


@Composable
fun healthInfoTextField(label: String, textState: MutableState<String>) {
    if (label == "Sex") {
        // Dropdown for Sex
        var expanded by remember { mutableStateOf(false) }

        OutlinedTextField(
            value = textState.value,
            onValueChange = {
                textState.value = it
            },
            label = {
                Text(text = "Sex (Male or Female)")
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        )
    }
    else {
        // Normal TextField for other properties
        OutlinedTextField(
            value = textState.value,
            onValueChange = {
                textState.value = it
            },
            label = {
                Text(text = label)
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

fun DropdownMenuItem(onClick: () -> Unit, interactionSource: () -> Unit) {

}


@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    HealthSignalTheme {
        SplashScreen()
    }
}

class MainActivity : ComponentActivity() {
    private lateinit var tfliteManager: TFLiteManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        // Initialize TFLiteManager
        tfliteManager = TFLiteManager(this)
        setContent {
            HealthSignalTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    SplashScreen()
                }
            }
        }
    }
}




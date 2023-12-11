package com.example.healthsignal

import android.content.Context
import android.os.Bundle
import org. tensorflow.lite.Interpreter
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.healthsignal.ml.DrinkingModelTf
import com.example.healthsignal.ml.SmokingModelTf
import com.example.healthsignal.ui.theme.Blue1
import com.example.healthsignal.ui.theme.Green1
import com.example.healthsignal.ui.theme.Green2
import com.example.healthsignal.ui.theme.HealthSignalTheme
import com.example.healthsignal.ui.theme.PoppinsFamily
import com.example.healthsignal.ui.theme.Silver
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.model.Model
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MainActivity : ComponentActivity() {
//    private lateinit var tfliteManager: TFLiteManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        // Initialize TFLiteManager
//        tfliteManager = TFLiteManager(this)
        setContent {
            HealthSignalTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    SplashScreen()
//                    SplashScreen(tfliteManager)
                }
            }
        }
    }
}
//tfliteManager: TFLiteManager,
@Composable
fun SplashScreen(modifier: Modifier = Modifier) {
    var drinking = remember { mutableStateOf("") }
    var smoking = remember { mutableStateOf("") }
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 22.dp, 16.dp, 6.dp)
            .verticalScroll(rememberScrollState()),
    ) {

        Row( modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(R.drawable.healthsignallogo_1),
            contentDescription = null,
            modifier = Modifier
                .size(150.dp)
        )
    }
        Text(
            text = "Predict Health From BodySignal",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = PoppinsFamily,
            color = Color.Black,
            modifier = Modifier.padding(16.dp, 0.dp),
        )
        Text(
            text = "With this app you can predict whether a person is a drinker and/or a smoker",
            fontSize =18.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = PoppinsFamily,
            color = Green1,
            modifier = Modifier.padding(16.dp, 0.dp),
        )
        val properties = listOf(
            "Sex", "Age", "Height", "Weight", "Waistline",
            "Sight Left", "Sight Right", "Hear Left", "Hear Right",
            "SBP", "DBP", "BLDS", "Tot Cholesterol", "HDL Cholesterol",
            "LDL Cholesterol", "Triglyceride", "Hemoglobin", "Urine Protein",
            "Serum Creatinine", "SGOT AST", "SGOT ALT", "Gamma GTP", "BMI",
                    "BMI Category",
                    "MAP",
                    "Liver Enzyme_Ratio",
                    "Anemia Indicator"
        )

        val propertyStates: Map<String, MutableState<String>> = remember {
            properties.associateWith { property ->
                mutableStateOf("")
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            for (property in properties) {
                healthInfoTextField(property, propertyStates[property]!!)
            }
        }
        Row( modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = {
                    println("Drinking Prediction: ${drinking.value}")
                    println("Smoking Prediction: ${smoking.value}")
                    // Get values from your input fields or states
                    val values: Map<String, String> = properties.associateWith { property ->
                        propertyStates[property]?.value.orEmpty()
                    }
                    println("values: ${values}")
                    println("values: ${values.values.map { it.toFloat() }.toFloatArray()}")
                    // Use the TFLiteManager to make predictions
                    val tfliteManager = TFLiteManager(context)
                    drinking.value = tfliteManager.makePredictionsDrinking(values)
                    smoking.value = tfliteManager.makePredictionsSmoking(values)

                    // Log predictions
                    println("Drinking Prediction: ${drinking.value}")
                    println("Smoking Prediction: ${smoking.value}")
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
        Column( modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
            ) {
            Column( modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp, 8.dp),
            ) {
                Text(text = "Drinker (Yes/No)",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontFamily = PoppinsFamily,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(text = "Prediction: ${drinking.value}",
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontFamily = PoppinsFamily,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Column( modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp, 8.dp),
            ) {
                Text(text = "Smoker (Never Smoked/Used to Smoke/Still Smoking)",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontFamily = PoppinsFamily,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(text = "Prediction: ${smoking.value}",
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
    fun makePredictionsDrinking(values: Map<String, String>): String {
        try {
            val model = DrinkingModelTf.newInstance(context)
//            val inputTensorShape = intArrayOf(1, 27)
//
//            // Replace the following with your actual input data
//val inputFloatArray: FloatArray = values.values.map { it.toFloatOrNull() ?: 0.0f }.toFloatArray()
//
//            // Check if the size of the inputFloatArray matches the expected size
//            if (inputFloatArray.size != inputTensorShape[1]) {
//                println("Error: Input size mismatch. Expected size: ${inputTensorShape[1]}, Actual size: ${inputFloatArray.size}")
//                return "Drinker Prediction: -"
//            }
//
//            val inputFeature0 = TensorBuffer.createFixedSize(inputTensorShape, DataType.FLOAT32)
//            val byteBuffer = ByteBuffer.allocateDirect(inputFloatArray.size * 4)
//            byteBuffer.order(ByteOrder.nativeOrder())
//            for (value in inputFloatArray) {
//                byteBuffer.putFloat(value)
//            }
//            inputFeature0.loadBuffer(byteBuffer)
            val inputTensorShape = intArrayOf(1, 27)
            val inputFeature0 = TensorBuffer.createFixedSize(inputTensorShape, DataType.FLOAT32)

// Replace the following with your actual input data
            val inputFloatArray: FloatArray =  values.values.map { it.toFloatOrNull() ?: 0.0f }.toFloatArray()
print("Error during drinker: $inputFloatArray.values")
            if (inputFloatArray.size != inputTensorShape[1]) {
                println("Error: Input size mismatch. Expected size: ${inputTensorShape[1]}, Actual size: ${inputFloatArray.size}")
                return "Drinker Prediction: -"
            }
// Load the input data into the TensorBuffer
                inputFeature0.loadArray(inputFloatArray, inputTensorShape)


            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer
            model.close()

            // Process the outputFeature0 and return the prediction as a String
            return "Drinker Prediction: ${outputFeature0.getFloatValue(0)}"
        } catch (e: Exception) {
            println("Error during inference: $e")
        }

        return "-"
    }

    fun makePredictionsSmoking(values: Map<String, String>): String {
        try {
            val model = SmokingModelTf.newInstance(context)
            val inputTensorShape = intArrayOf(1, 27)

//            // Replace the following with your actual input data
//            val inputFloatArray: FloatArray = values.values.map { it.toFloatOrNull() ?: 0.0f }.toFloatArray()
//
//            // Check if the size of the inputFloatArray matches the expected size
//            if (inputFloatArray.size != inputTensorShape[1]) {
//                println("Error: Input size mismatch. Expected size: ${inputTensorShape[1]}, Actual size: ${inputFloatArray.size}")
//                return "Smoker Prediction: -"
//            }
//
//            val inputFeature0 = TensorBuffer.createFixedSize(inputTensorShape, DataType.FLOAT32)
//            val byteBuffer = ByteBuffer.allocateDirect(inputFloatArray.size * 4)
//            byteBuffer.order(ByteOrder.nativeOrder())
//            for (value in inputFloatArray) {
//                byteBuffer.putFloat(value)
//            }
//            inputFeature0.loadBuffer(byteBuffer)
            val inputFeature0 = TensorBuffer.createFixedSize(inputTensorShape, DataType.FLOAT32)
            print("Error during smoker1: $inputFeature0")
// Replace the following with your actual input data
            val inputFloatArray: FloatArray =  values.values.map { it.toFloatOrNull() ?: 0.0f }.toFloatArray()
            print("Error during smoker: $inputFloatArray")
            if (inputFloatArray.size != inputTensorShape[1]) {
                println("Error: Input size mismatch. Expected size: ${inputTensorShape[1]}, Actual size: ${inputFloatArray.size}")
                return "Drinker Prediction: -"
            }
// Load the input data into the TensorBuffer
            inputFeature0.loadArray(inputFloatArray, inputTensorShape)


            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer
            model.close()

            // Process the outputFeature0 and return the prediction as a String
            return "Smoker Prediction: ${outputFeature0.getFloatValue(0)}"
        } catch (e: Exception) {
            println("Error during inference: $e")
        }

        return "-"
    }
}


@Composable
fun healthInfoTextField(label: String, textState: MutableState<String>){
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
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    // Assuming you are using the default PreviewConfiguration
//    val context = LocalContext.current
//    val tfliteManager = remember { TFLiteManager(context) }

    HealthSignalTheme {
        SplashScreen()
    }
}



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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.healthsignal.ml.DrinkingModelTf
import com.example.healthsignal.ml.SmokingModelTf
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

@Composable
fun SplashScreen() {
    var drinking by remember { mutableStateOf("") }
    var smoking by remember { mutableStateOf("") }
    var selectedsexCategory by rememberSaveable { mutableStateOf("Male") }
    var selectedhearlCategory by rememberSaveable { mutableStateOf("Normal") }
    var selectedhearrCategory by rememberSaveable { mutableStateOf("Normal") }

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

        Column(modifier = Modifier.padding(16.dp)) {
            for (property in properties) {
                healthInfoTextField(
                    label = property,
                    textState = propertyStates[property]!!,
                    categories = listOf("Male", "Female"),
                    categorieshear = listOf("Normal", "Abnormal"),
                    selectedsexCategory = selectedsexCategory,
                    selectedhearlCategory = selectedhearlCategory,
                    selectedhearrCategory = selectedhearrCategory,
                    onCategorysexSelected = { category ->
                        selectedsexCategory = category // Update the value property
                    },
                    onCategoryhearlSelected = { category ->
                        selectedhearlCategory = category // Update the value property
                    },
                    onCategoryhearrSelected = { category ->
                        selectedhearrCategory = category // Update the value property
                    }
                )
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
fun healthInfoTextField(
    label: String,
    textState: MutableState<String>,
    categories: List<String>,
    categorieshear: List<String>,
    selectedsexCategory: String, // Use MutableState<String> for tracking selected category
    onCategorysexSelected: (String) -> Unit,
    selectedhearlCategory: String, // Use MutableState<String> for tracking selected category
    onCategoryhearlSelected: (String) -> Unit,
    selectedhearrCategory: String, // Use MutableState<String> for tracking selected category
    onCategoryhearrSelected: (String) -> Unit,
    modifier: Modifier = Modifier
): Pair<MutableState<String>, String> {
    var sex by remember { mutableStateOf(0) }
    var hearl by remember { mutableStateOf(1) }
    var hearr by remember { mutableStateOf(1) }


    if (label == "Sex") {
        // Dropdown for Sex
        var expanded by remember { mutableStateOf(false) }
        val onCategorySelectedState by rememberUpdatedState(onCategorysexSelected)
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .background(color = Color.White, shape = RoundedCornerShape(16.dp))
                .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(16.dp))
                .padding(vertical = 3.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedsexCategory,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
            }

            if (expanded) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(16.dp))
                ) {
                    Column {
                        categories.forEach { category ->
                            Text(
                                text = category,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        expanded = false
                                        onCategorySelectedState(category)
                                        sex = if (category == "Male") 0 else 1
                                        textState.value = sex.toString() // Update textState with encoded value
                                    }
                                    .padding(16.dp)
                            )
                            Divider(color = Color.Gray, thickness = 1.dp)
                        }
                    }
                }
            }
        }
        return textState to sex.toString()
    } else if (label == "Hear Left") {
        // Dropdown for Sex
        var expanded by remember { mutableStateOf(false) }
        val onCategorySelectedState by rememberUpdatedState(onCategoryhearlSelected)
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .background(color = Color.White, shape = RoundedCornerShape(16.dp))
                .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(16.dp))
                .padding(vertical = 3.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedhearlCategory,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
            }

            if (expanded) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(16.dp))
                ) {
                    Column {
                        categorieshear.forEach { category ->
                            Text(
                                text = category,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        expanded = false
                                        onCategorySelectedState(category)
                                        hearl = if (category == "Normal") 1 else 2
                                        textState.value = hearl.toString() // Update textState with encoded value
                                    }
                                    .padding(16.dp)
                            )
                            Divider(color = Color.Gray, thickness = 1.dp)
                        }
                    }
                }
            }
        }
        return textState to hearl.toString()
    }
    else if (label == "Hear Right") {
        // Dropdown for Sex
        var expanded by remember { mutableStateOf(false) }
        val onCategorySelectedState by rememberUpdatedState(onCategoryhearrSelected)
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .background(color = Color.White, shape = RoundedCornerShape(16.dp))
                .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(16.dp))
                .padding(vertical = 3.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedhearrCategory,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
            }

            if (expanded) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(16.dp))
                ) {
                    Column {
                        categorieshear.forEach { category ->
                            Text(
                                text = category,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        expanded = false
                                        onCategorySelectedState(category)
                                        hearr = if (category == "Normal") 1 else 2
                                        textState.value = hearr.toString() // Update textState with encoded value
                                    }
                                    .padding(16.dp)
                            )
                            Divider(color = Color.Gray, thickness = 1.dp)
                        }
                    }
                }
            }
        }
        return textState to hearr.toString()
    }else {
        // Normal TextField for other properties
        OutlinedTextField(
            value = textState.value,
            onValueChange = {
                textState.value = it
                // Optionally, you can perform additional actions here if needed
            },
            label = { Text(text = label) },
            modifier = Modifier.fillMaxWidth()
        )
        return textState to textState.value
    }
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




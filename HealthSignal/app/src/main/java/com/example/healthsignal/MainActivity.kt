package com.example.healthsignal

import android.os.Bundle
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.healthsignal.ui.theme.Blue1
import com.example.healthsignal.ui.theme.Green1
import com.example.healthsignal.ui.theme.Green2
import com.example.healthsignal.ui.theme.HealthSignalTheme
import com.example.healthsignal.ui.theme.PoppinsFamily
import com.example.healthsignal.ui.theme.Silver

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
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

@Composable
fun SplashScreen(modifier: Modifier = Modifier) {
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
        val drinker = "Yes"
        val smoker = "Never Smoked"
        val properties = listOf(
            "Sex", "Age", "Height", "Weight", "Waistline",
            "Sight Left", "Sight Right", "Hear Left", "Hear Right",
            "SBP", "DBP", "BLDS", "Tot Cholesterol", "HDL Cholesterol",
            "LDL Cholesterol", "Triglyceride", "Hemoglobin", "Urine Protein",
            "Serum Creatinine", "SGOT AST", "SGOT ALT", "Gamma GTP", "BMI",
            "BMI Category", "MAP", "Liver Enzyme Ratio", "Anemia Indicator"
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
                    val values: Map<String, String> = propertyStates.mapValues { it.value.value }

// Now 'values' contains the current values for each property
                    for ((property, value) in values) {
                        println("$property: $value")
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
                Text(text = "Prediction: $drinker",
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
                Text(text = "Prediction: $smoker",
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontFamily = PoppinsFamily,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
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
    HealthSignalTheme {
        SplashScreen()
    }
}


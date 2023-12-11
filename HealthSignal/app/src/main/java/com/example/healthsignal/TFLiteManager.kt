package com.example.healthsignal

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class TFLiteManager3(private val context: Context) {
    private var drinkingInterpreter: Interpreter? = null
    private var smokingInterpreter: Interpreter? = null

    init {
        drinkingInterpreter = Interpreter(loadModelFile("drinking_model_tf.tflite"))
        smokingInterpreter = Interpreter(loadModelFile("smoking_model_tf.tflite"))
    }

    @Throws(IOException::class)
    private fun loadModelFile(fileName: String): ByteBuffer {
        val assetManager = context.assets
        val inputStream = assetManager.open(fileName)
        val fileSize = inputStream.available()

        val buffer = ByteBuffer.allocateDirect(fileSize)
        buffer.order(ByteOrder.nativeOrder())

        inputStream.read(buffer.array()) // Directly read into ByteBuffer's array

        inputStream.close()

        return buffer
    }
    fun predictDrinking(inputValues: Map<String, String>): String {
        val inputArray = preprocessInput(inputValues)
        val outputArray = Array(1) { FloatArray(2) } // Assuming two classes (0: No, 1: Yes)
        drinkingInterpreter?.run(inputArray, outputArray)

        // Interpret the outputArray
        val prediction = if (outputArray[0][0] > outputArray[0][1]) 0 else 1
        return if (prediction == 0) "No" else "Yes"
    }

    fun predictSmoking(inputValues: Map<String, String>): String {
        val inputArray = preprocessInput(inputValues)
        val outputArray = FloatArray(3) // Assuming three classes (0: Never smoked, 1: Used to smoke, 2: Still smoking)
        smokingInterpreter?.run(inputArray, outputArray)

        // Interpret the outputArray
        val prediction = outputArray.indexOfMax()
        return when (prediction) {
            0 -> "Never Smoked"
            1 -> "Used to Smoke"
            2 -> "Still Smoking"
            else -> "Unknown" // Handle unexpected cases
        }
    }

    fun FloatArray.indexOfMax(): Int {
        var maxIndex = 0
        var maxValue = this[0]

        for (i in 1 until this.size) {
            if (this[i] > maxValue) {
                maxIndex = i
                maxValue = this[i]
            }
        }

        return maxIndex
    }


    private fun preprocessInput(inputValues: Map<String, String>): Array<ByteArray> {
        // Implement logic to convert inputValues to a format suitable for inputArray
        // You need to convert the input data (strings) to the appropriate data type (e.g., float)
        // and arrange it in the required input format for the model.
        // This might involve normalizing values, encoding categorical variables, etc.

        // For illustration purposes, assuming all input features are floats:
        val inputArray = Array(1) { ByteArray(inputValues.size * 4) }
        var offset = 0

        for ((property, value) in inputValues) {
            // Convert the string value to float and copy it to the input array
            val floatValue = value.toFloat()
            ByteBuffer.wrap(inputArray[0], offset, 4).order(ByteOrder.nativeOrder()).putFloat(floatValue)

            offset += 4
        }

        return inputArray
    }
}

package com.seenu.mlactivity

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class Analysis(private val listener: BarCodeImageListener) :
    ImageAnalysis.Analyzer {

    var isDone = false
    override fun analyze(image: ImageProxy) {
        scanBarCode(image)
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun scanBarCode(image: ImageProxy) {

        val image1 = image.image

        val inputImage =
            image1?.let { InputImage.fromMediaImage(it, image.imageInfo.rotationDegrees) }

        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_AZTEC
            )
            .build()


        val scanner = BarcodeScanning.getClient(options)

        inputImage?.let { it ->
            scanner.process(it)
                .addOnSuccessListener { barcodes ->
                    if (!isDone && barcodes.isNotEmpty()) {
                        readBarCodeData(barcodes)
                        isDone = true

                        scanner.close()
                    }
                }
                .addOnFailureListener {exception->
                   listener. barCodeFailure(exception)
                }.addOnCompleteListener {
                    image.close()
                }
        }

    }

    private fun readBarCodeData(barcodes: MutableList<Barcode>) {

        for (barcode in barcodes) {
//                val bounds = barcode.boundingBox
//                val corners = barcode.cornerPoints

            val rawValue = barcode.rawValue
            Log.d("rawValue", "readBarCodeData:${rawValue} ")
//            Toast.makeText(context, rawValue.toString(), Toast.LENGTH_SHORT).show()
            if (rawValue != null) {
                listener.barCodeData(rawValue)
            }

            // See API reference for complete list of supported types
            when (barcode.valueType) {
                Barcode.TYPE_WIFI -> {
                    val ssid = barcode.wifi!!.ssid
//                    val password = barcode.wifi!!.password
//                    val type = barcode.wifi!!.encryptionType
                    Log.d("test", "readBarCodeData:${ssid} ")
                }
                Barcode.TYPE_URL -> {
                    val title = barcode.url!!.title
//                    val url = barcode.url!!.url
                    Log.d("test", "readBarCodeData:${title} ")
                }
            }
        }
    }
}

interface BarCodeImageListener {
    fun barCodeData(data: String)
    fun barCodeFailure(E:Exception)
}

package com.seenu.mlactivity

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Size
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity(), BarCodeImageListener {

    private var cameraPermission = false
    private lateinit var preview: Preview
    private lateinit var cameraProviderFeature: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var previewView: PreviewView
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                Toast.makeText(this, "granted", Toast.LENGTH_SHORT).show()
                cameraExecutor = Executors.newSingleThreadExecutor()
                cameraProviderFeature = ProcessCameraProvider.getInstance(this)
                cameraProviderFeature.addListener({
                    try {
                        val processCameraProvider = cameraProviderFeature.get()
                        bindPreview(processCameraProvider)
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }

                }, ContextCompat.getMainExecutor(this))

            } else {
                Toast.makeText(this, "denied", Toast.LENGTH_SHORT).show()

            }
        }

        checkPermission()
        previewView = findViewById(R.id.camera_preview)

        if (cameraPermission) {

            cameraExecutor = Executors.newSingleThreadExecutor()
            cameraProviderFeature = ProcessCameraProvider.getInstance(this)
            cameraProviderFeature.addListener({
                try {
                    val processCameraProvider = cameraProviderFeature.get()
                    bindPreview(processCameraProvider)
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }

            }, ContextCompat.getMainExecutor(this))
        }

        val scanAgain = findViewById<Button>(R.id.scan)

        scanAgain.setOnClickListener {
//            val intentIntegrator = IntentIntegrator(this)
//            intentIntegrator.setPrompt("Scan a barcode or QR Code")
//            intentIntegrator.setOrientationLocked(true)
//            intentIntegrator.initiateScan()


        }

        fun encodeAsBitmap(str: String?): Bitmap? {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(str, BarcodeFormat.QR_CODE, 400, 400)
            val w = bitMatrix.width
            val h = bitMatrix.height
            val pixels = IntArray(w * h)
            for (y in 0 until h) {
                for (x in 0 until w) {
                    pixels[y * w + x] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                }
            }
            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, w, 0, 0, w, h)
            return bitmap
        }

        val image = findViewById<ImageView>(R.id.imageView)

        image.setImageBitmap(encodeAsBitmap("seenu"))

    }

    private fun bindPreview(processCameraProvider: ProcessCameraProvider?) {

        preview = Preview.Builder().build()

        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
        preview.setSurfaceProvider(previewView.surfaceProvider)
        val imageCapture = ImageCapture.Builder().build()
        processCameraProvider?.unbindAll()
        val analyser = Analysis(this)
        val imageAnalyser = ImageAnalysis.Builder().setTargetResolution(Size(1200, 1200))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()

        imageAnalyser.setAnalyzer(cameraExecutor, analyser)

        processCameraProvider?.bindToLifecycle(
            this, cameraSelector, preview, imageCapture, imageAnalyser
        )

    }

    private fun checkPermission() {

        cameraPermission = ContextCompat.checkSelfPermission(
            this, android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED


        if (!cameraPermission) {
            requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    override fun barCodeData(data: String) {
        Toast.makeText(this, data, Toast.LENGTH_SHORT).show()
    }

    override fun barCodeFailure(E: Exception) {
        E.printStackTrace()
    }

}

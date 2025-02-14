package ovh.gabrielhuav.sensores_escom_v2

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.view.GestureDetectorCompat

class MapView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val paintGrid = Paint().apply {
        color = Color.GRAY
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }
    private val paintLocalPlayer = Paint().apply {
        style = Paint.Style.FILL
    }
    private val paintRemotePlayer = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
    }

    private var offsetX = 0f
    private var offsetY = 0f
    var scaleFactor: Float = 1.0f
    private var localPlayerPosition: Pair<Int, Int>? = null
    private var remotePlayerPosition: Pair<Int, Int>? = null

    private var playerColor: Int = Color.BLUE
    private var playerShape: String = "Cuadrado"

    private val backgroundBitmap: Bitmap? = try {
        BitmapFactory.decodeResource(resources, R.drawable.escom_mapa)
    } catch (e: Exception) {
        null
    }

    private lateinit var gestureDetector: GestureDetectorCompat
    private lateinit var scaleGestureDetector: ScaleGestureDetector

    init {
        initializeDetectors()
        loadPlayerPreferences()
    }

    private fun initializeDetectors() {
        gestureDetector = GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                if (!::scaleGestureDetector.isInitialized || !scaleGestureDetector.isInProgress) {
                    offsetX -= distanceX / scaleFactor
                    offsetY -= distanceY / scaleFactor
                    constrainOffset()
                    invalidate()
                }
                return true
            }
        })

        scaleGestureDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                scaleFactor *= detector.scaleFactor
                scaleFactor = scaleFactor.coerceIn(0.5f, 3.0f)

                val focusX = detector.focusX
                val focusY = detector.focusY

                offsetX += (focusX - offsetX) * (1 - detector.scaleFactor)
                offsetY += (focusY - offsetY) * (1 - detector.scaleFactor)

                constrainOffset()
                invalidate()
                return true
            }
        })
    }

    private fun constrainOffset() {
        if (backgroundBitmap == null) return

        val maxOffsetX = -(backgroundBitmap.width * scaleFactor - width)
        val maxOffsetY = -(backgroundBitmap.height * scaleFactor - height)

        offsetX = offsetX.coerceIn(maxOffsetX, 0f)
        offsetY = offsetY.coerceIn(maxOffsetY, 0f)
    }

    private fun loadPlayerPreferences() {
        val preferences = context.getSharedPreferences("PlayerSettings", Context.MODE_PRIVATE)
        val color = preferences.getString("color", "Azul")
        val shape = preferences.getString("shape", "Cuadrado")

        playerColor = when (color) {
            "Rojo" -> Color.RED
            "Verde" -> Color.GREEN
            else -> Color.BLUE
        }
        playerShape = shape ?: "Cuadrado"
        paintLocalPlayer.color = playerColor
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (backgroundBitmap == null) {
            canvas.drawColor(Color.RED)
            val errorPaint = Paint().apply {
                color = Color.WHITE
                textSize = 32f
            }
            canvas.drawText(
                "Error: Mapa no encontrado",
                50f,
                50f,
                errorPaint
            )
            return
        }

        canvas.save()
        canvas.scale(scaleFactor, scaleFactor)
        canvas.translate(offsetX / scaleFactor, offsetY / scaleFactor)

        canvas.drawBitmap(backgroundBitmap, 0f, 0f, null)

        val cellWidth = backgroundBitmap.width / 10f
        val cellHeight = backgroundBitmap.height / 10f
        for (i in 0..10) {
            canvas.drawLine(i * cellWidth, 0f, i * cellWidth, backgroundBitmap.height.toFloat(), paintGrid)
            canvas.drawLine(0f, i * cellHeight, backgroundBitmap.width.toFloat(), i * cellHeight, paintGrid)
        }

        // Dibujar jugador local
        localPlayerPosition?.let {
            val playerX = it.first * cellWidth + cellWidth / 2
            val playerY = it.second * cellHeight + cellHeight / 2
            when (playerShape) {
                "Triángulo" -> drawTriangle(canvas, playerX, playerY, cellWidth / 2)
                "Círculo" -> canvas.drawCircle(playerX, playerY, cellWidth / 4f, paintLocalPlayer)
                else -> canvas.drawRect(playerX - cellWidth / 4, playerY - cellHeight / 4, playerX + cellWidth / 4, playerY + cellHeight / 4, paintLocalPlayer)
            }
        }

        // Dibujar jugador remoto (rojo)
        remotePlayerPosition?.let {
            val playerX = it.first * cellWidth + cellWidth / 2
            val playerY = it.second * cellHeight + cellHeight / 2
            canvas.drawCircle(playerX, playerY, cellWidth / 4f, paintRemotePlayer)
        }

        canvas.restore()
    }

    private fun drawTriangle(canvas: Canvas, x: Float, y: Float, size: Float) {
        val path = Path()
        path.moveTo(x, y - size)
        path.lineTo(x - size, y + size)
        path.lineTo(x + size, y + size)
        path.close()
        canvas.drawPath(path, paintLocalPlayer)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var handled = scaleGestureDetector.onTouchEvent(event)
        handled = gestureDetector.onTouchEvent(event) || handled
        return handled || super.onTouchEvent(event)
    }

    fun updateLocalPlayerPosition(position: Pair<Int, Int>?) {
        localPlayerPosition = position
        invalidate()
    }

    fun updateRemotePlayerPosition(position: Pair<Int, Int>?) {
        remotePlayerPosition = position
        invalidate()
    }

    fun updateScaleFactor(scale: Float) {
        scaleFactor = scale.coerceIn(0.5f, 3.0f)
        constrainOffset()
        invalidate()
    }
}
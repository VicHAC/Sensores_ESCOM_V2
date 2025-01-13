package ovh.gabrielhuav.sensores_escom_v2

import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var btnSave: Button
    private lateinit var colorGroup: RadioGroup
    private lateinit var shapeGroup: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        btnSave = findViewById(R.id.btnSave)
        colorGroup = findViewById(R.id.colorGroup)
        shapeGroup = findViewById(R.id.shapeGroup)

        btnSave.setOnClickListener {
            val selectedColorId = colorGroup.checkedRadioButtonId
            val selectedShapeId = shapeGroup.checkedRadioButtonId

            val selectedColor = findViewById<RadioButton>(selectedColorId).text.toString()
            val selectedShape = findViewById<RadioButton>(selectedShapeId).text.toString()

            // Guardar las preferencias en SharedPreferences
            val preferences = getSharedPreferences("PlayerSettings", MODE_PRIVATE)
            with(preferences.edit()) {
                putString("color", selectedColor)
                putString("shape", selectedShape)
                apply()
            }

            finish()
        }
    }
}
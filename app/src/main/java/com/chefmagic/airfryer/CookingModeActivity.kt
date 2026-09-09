package com.chefmagic.airfryer

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.widget.NestedScrollView
import com.google.android.material.button.MaterialButton

class CookingModeActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_FILE = "extra_file"
    }

    private var steps: List<Step> = emptyList()
    private var currentIndex = 0
    private var activeTimer: CountDownTimer? = null

    private lateinit var stepProgressText: android.widget.TextView
    private lateinit var stepText: android.widget.TextView
    private lateinit var timerCard: CardView
    private lateinit var timerText: android.widget.TextView
    private lateinit var timerButton: MaterialButton
    private lateinit var prevButton: MaterialButton
    private lateinit var nextButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cooking_mode)

        val title = intent.getStringExtra(EXTRA_TITLE) ?: getString(R.string.app_name)
        val file = intent.getStringExtra(EXTRA_FILE)

        val toolbar: Toolbar = findViewById(R.id.cookingToolbar)
        toolbar.title = title
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        stepProgressText = findViewById(R.id.stepProgressText)
        stepText = findViewById(R.id.stepText)
        timerCard = findViewById(R.id.timerCard)
        timerText = findViewById(R.id.timerText)
        timerButton = findViewById(R.id.timerButton)
        prevButton = findViewById(R.id.prevButton)
        nextButton = findViewById(R.id.nextButton)

        val recipe = RecipeRepository.loadSections(this)
            .flatMap { it.recipes }
            .firstOrNull { it.file == file }

        steps = recipe?.steps ?: emptyList()

        if (steps.isEmpty()) {
            stepText.text = "No steps found for this recipe."
            prevButton.isEnabled = false
            nextButton.isEnabled = false
            return
        }

        prevButton.setOnClickListener { goToStep(currentIndex - 1) }
        nextButton.setOnClickListener {
            if (currentIndex == steps.lastIndex) {
                finish()
            } else {
                goToStep(currentIndex + 1)
            }
        }

        showStep(0)
    }

    private fun goToStep(index: Int) {
        if (index < 0 || index >= steps.size) return
        cancelActiveTimer()
        showStep(index)
    }

    private fun showStep(index: Int) {
        currentIndex = index
        val step = steps[index]

        stepProgressText.text = "STEP ${index + 1} OF ${steps.size}"
        stepText.text = step.text

        prevButton.isEnabled = index > 0
        nextButton.text = if (index == steps.lastIndex) "Finish" else "Next →"

        if (step.durationSeconds != null && step.durationSeconds > 0) {
            timerCard.visibility = android.view.View.VISIBLE
            resetTimerUi(step.durationSeconds)
        } else {
            timerCard.visibility = android.view.View.GONE
        }
    }

    private fun resetTimerUi(totalSeconds: Int) {
        timerText.text = formatTime(totalSeconds)
        timerButton.text = "Start Timer"
        timerButton.setOnClickListener { startTimer(totalSeconds) }
    }

    private fun startTimer(totalSeconds: Int) {
        cancelActiveTimer()
        timerButton.text = "Cancel"
        timerButton.setOnClickListener {
            cancelActiveTimer()
            resetTimerUi(totalSeconds)
        }

        activeTimer = object : CountDownTimer(totalSeconds * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                timerText.text = formatTime((millisUntilFinished / 1000).toInt())
            }

            override fun onFinish() {
                timerText.text = "Done!"
                vibrateDevice()
                resetTimerUi(totalSeconds)
            }
        }.start()
    }

    private fun cancelActiveTimer() {
        activeTimer?.cancel()
        activeTimer = null
    }

    private fun formatTime(totalSeconds: Int): String {
        val m = totalSeconds / 60
        val s = totalSeconds % 60
        return String.format("%d:%02d", m, s)
    }

    private fun vibrateDevice() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(400)
        }
    }

    override fun onDestroy() {
        cancelActiveTimer()
        super.onDestroy()
    }
}

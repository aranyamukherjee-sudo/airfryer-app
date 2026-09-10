package com.chefmagic.airfryer

import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import com.google.android.material.button.MaterialButton

class CookingModeActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_FILE = "extra_file"
        const val EXTRA_SERVINGS = "extra_servings"
        private const val ALARM_AUTO_STOP_MS = 8000L
    }

    private var steps: List<Step> = emptyList()
    private var currentIndex = 0
    private var activeTimer: CountDownTimer? = null
    private var activeAlarm: Ringtone? = null
    private val alarmStopHandler = Handler(Looper.getMainLooper())

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
        val servings = intent.getIntExtra(EXTRA_SERVINGS, -1)

        val recipe = RecipeRepository.loadSections(this)
            .flatMap { it.recipes }
            .firstOrNull { it.file == file }

        steps = recipe?.steps ?: emptyList()

        val toolbar: Toolbar = findViewById(R.id.cookingToolbar)
        val subtitleParts = mutableListOf<String>()
        if (servings > 0) subtitleParts.add("Cooking for $servings")
        if (recipe?.kcalPerServing != null && servings > 0) {
            subtitleParts.add("≈${recipe.kcalPerServing} kcal/serving · ≈${recipe.kcalPerServing * servings} kcal total")
        }
        toolbar.title = if (subtitleParts.isNotEmpty()) "$title  ·  ${subtitleParts.joinToString("  ·  ")}" else title
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        stepProgressText = findViewById(R.id.stepProgressText)
        stepText = findViewById(R.id.stepText)
        timerCard = findViewById(R.id.timerCard)
        timerText = findViewById(R.id.timerText)
        timerButton = findViewById(R.id.timerButton)
        prevButton = findViewById(R.id.prevButton)
        nextButton = findViewById(R.id.nextButton)

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
        stopAlarmSound()
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
        stopAlarmSound()
        timerButton.text = "Cancel"
        timerButton.setOnClickListener {
            cancelActiveTimer()
            stopAlarmSound()
            resetTimerUi(totalSeconds)
        }

        activeTimer = object : CountDownTimer(totalSeconds * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                timerText.text = formatTime((millisUntilFinished / 1000).toInt())
            }

            override fun onFinish() {
                timerText.text = "Done!"
                vibrateDevice()
                playAlarmSound()
                timerButton.text = "Stop Alarm"
                timerButton.setOnClickListener {
                    stopAlarmSound()
                    resetTimerUi(totalSeconds)
                }
            }
        }.start()
    }

    private fun cancelActiveTimer() {
        activeTimer?.cancel()
        activeTimer = null
    }

    private fun playAlarmSound() {
        try {
            val alarmUri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(this, alarmUri)
            ringtone?.play()
            activeAlarm = ringtone

            // Failsafe: never let it ring longer than ALARM_AUTO_STOP_MS unattended.
            alarmStopHandler.postDelayed({ stopAlarmSound() }, ALARM_AUTO_STOP_MS)
        } catch (e: Exception) {
            // If no alarm sound is available on the device, the vibration alone still signals completion.
        }
    }

    private fun stopAlarmSound() {
        alarmStopHandler.removeCallbacksAndMessages(null)
        activeAlarm?.let { if (it.isPlaying) it.stop() }
        activeAlarm = null
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
        stopAlarmSound()
        super.onDestroy()
    }
}

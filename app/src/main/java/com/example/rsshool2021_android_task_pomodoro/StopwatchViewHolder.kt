package com.example.rsshool2021_android_task_pomodoro

import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.CountDownTimer
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.example.rsshool2021_android_task_pomodoro.databinding.TimerItemBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class StopwatchViewHolder(
    private val binding: TimerItemBinding,
    private val listener: StopwatchListener,
    private val resources: Resources
): RecyclerView.ViewHolder(binding.root) {

    private var timer: CountDownTimer? = null

    fun bind(stopwatch: Stopwatch) {
        binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()

        if (stopwatch.isStarted) {
            startTimer(stopwatch)
        } else {
            stopTimer(stopwatch)
        }

        initButtonsListeners(stopwatch)
    }

    private fun initButtonsListeners(stopwatch: Stopwatch) {
        binding.startPauseButton.setOnClickListener {
            if (stopwatch.isStarted) {
                listener.stop(stopwatch.id, stopwatch.currentMs)
            } else {
                listener.start(stopwatch.id)
            }
        }

        binding.deleteButton.setOnClickListener { listener.delete(stopwatch.id) }
    }

    private fun startTimer(stopwatch: Stopwatch) {

        binding.startPauseButton.text = "Stop"
        binding.customView.setPeriod(stopwatch.startMs)

        timer?.cancel()
        timer = getCountDownTimer(stopwatch)
        timer?.start()

        binding.blinkingIndicator.isInvisible = false
        (binding.blinkingIndicator.background as? AnimationDrawable)?.start()

        binding.root.setCardBackgroundColor(Color.WHITE)
    }

    private fun stopTimer(stopwatch: Stopwatch) {
        binding.startPauseButton.text = "Start"

        timer?.cancel()

        binding.blinkingIndicator.isInvisible = true
        (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
    }

    private fun getCountDownTimer(stopwatch: Stopwatch): CountDownTimer {
        return object : CountDownTimer(stopwatch.currentMs, UNIT_TEN_MS) {
            val interval = UNIT_TEN_MS

            override fun onTick(millisUntilFinished: Long) {
                stopwatch.currentMs -= interval
                binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
                binding.customView.setCurrent(stopwatch.startMs - stopwatch.currentMs)
            }

            override fun onFinish() {

                stopwatch.isStarted = false
                stopwatch.currentMs = stopwatch.startMs

                binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()

                binding.startPauseButton.text = "Start"

                binding.root.setCardBackgroundColor(resources.getColor(R.color.red_dark))

                val mediaPlayer = MediaPlayer.create(binding.root.context, R.raw.call_timer)
                mediaPlayer.start()

                val toast = Toast(binding.root.context)
                toast.let {
                    it.duration = Toast.LENGTH_SHORT
                    it.setText("Timer finished")
                    it.show()
                }

                toast.setText("Timer finished")
                toast.duration = Toast.LENGTH_SHORT


                binding.blinkingIndicator.isInvisible = true
                (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
            }
        }
    }

    private fun Long.displayTime(): String {

        val h = this / 1000 / 3600
        val m = this / 1000 % 3600 / 60
        val s = this / 1000 % 60
        val ms = this % 1000 / 10

        return "${displaySlot(h)}:${displaySlot(m)}:${displaySlot(s)}:${displaySlot(ms)}"
    }

    private fun displaySlot(count: Long): String {
        return if (count / 10L > 0) {
            "$count"
        } else {
            "0$count"
        }
    }

    private companion object {

        private const val UNIT_TEN_MS = 1000L

    }
}
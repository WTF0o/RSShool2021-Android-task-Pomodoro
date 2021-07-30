package com.example.rsshool2021_android_task_pomodoro

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rsshool2021_android_task_pomodoro.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), StopwatchListener, LifecycleObserver {

    private lateinit var binding: ActivityMainBinding
    private lateinit var minutes: EditText

    private val stopwatchAdapter = StopwatchAdapter(this)
    private val stopwatches = mutableListOf<Stopwatch>()

    private var nextId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        minutes = binding.minutes

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = stopwatchAdapter
        }


        binding.addNewStopwatchButton.setOnClickListener {
            if (stopwatches.size < 10) {

                var watchMinutes = binding.minutes.text.toString().toLongOrNull()
                if (watchMinutes != null && watchMinutes != 0L && watchMinutes <= 10080) {

                    watchMinutes *= 60 * 1000
                    val finishMs = System.currentTimeMillis() + watchMinutes

                    stopwatches.add(
                        Stopwatch(
                            nextId++,
                            watchMinutes,
                            finishMs,
                            watchMinutes,
                            false
                        )
                    )
                    stopwatchAdapter.submitList(stopwatches.toList())
                } else {
                    val toast = Toast(this)
                    toast.duration = Toast.LENGTH_SHORT
                    toast.setText("Incorrect number format")
                    toast.show()
                }

            }
        }

    }

    override fun start(id: Int) {
        changeStopwatch(id, true)
    }

    override fun stop(id: Int, currentMs: Long) {
        changeStopwatch(id, false)
    }

    override fun delete(id: Int) {
        stopwatches.remove(stopwatches.find { it.id == id })
        stopwatchAdapter.submitList(stopwatches.toList())
    }

    private fun changeStopwatch(id: Int, isStarted: Boolean) {
        val newTimers = mutableListOf<Stopwatch>()
        stopwatches.forEach {
            if (it.id == id) {
                val newFinishMs = System.currentTimeMillis() + it.currentMs
                newTimers.add(Stopwatch(it.id, it.startMs, newFinishMs, it.currentMs, isStarted))
            } else if (it.id != id && it.isStarted) {
                newTimers.add(Stopwatch(it.id, it.startMs, it.finishMs, it.currentMs, false))
            } else {
                newTimers.add(it)
            }
        }
        stopwatchAdapter.submitList(newTimers)
        stopwatches.clear()
        stopwatches.addAll(newTimers)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        val finishTime = stopwatches.find { it.isStarted }
            if(finishTime != null){
                val startIntent = Intent(this, ForegroundService::class.java)
                startIntent.putExtra(COMMAND_ID, COMMAND_START)
                startIntent.putExtra(STARTED_TIMER_TIME_MS, finishTime.finishMs)
                startService(startIntent)
            }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(stopIntent)
    }
}
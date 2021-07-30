package com.example.rsshool2021_android_task_pomodoro

data class Stopwatch(
    val id: Int,
    var startMs: Long,
    var finishMs: Long,
    var currentMs: Long,
    var isStarted: Boolean)
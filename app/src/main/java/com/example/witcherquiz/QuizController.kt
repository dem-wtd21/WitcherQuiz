package com.example.witcherquiz

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray

enum class ScreenState {
    START, QUIZ, RESULT, ERROR
}

class QuizController(
    private val context: Context,
    private val scope: CoroutineScope
) {
    var screenState by mutableStateOf(ScreenState.START)
    var questions by mutableStateOf<List<Question>>(emptyList())
    var currentIndex by mutableIntStateOf(0)
    var score by mutableIntStateOf(0)
    var questionDuration by mutableIntStateOf(20) // длительность вопроса зависит от сложности
    var timeLeft by mutableIntStateOf(20)
    var selectedAnswerIndex by mutableStateOf<Int?>(null)

    private var timerJob: Job? = null

    init {
        loadQuestions()
    }

    private fun loadQuestions() {
        try {
            val jsonString = context.assets.open("questions.json")
                .bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            val loadedList = mutableListOf<Question>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val optionsArray = obj.getJSONArray("options")
                val optionsList = mutableListOf<String>()
                for (j in 0 until optionsArray.length()) {
                    optionsList.add(optionsArray.getString(j))
                }
                loadedList.add(
                    Question(
                        question = obj.getString("question"),
                        options = optionsList,
                        correctIndex = obj.getInt("correctIndex")
                    )
                )
            }

            if (loadedList.isEmpty()) {
                screenState = ScreenState.ERROR
            } else {
                questions = loadedList
            }
        } catch (e: Exception) {
            screenState = ScreenState.ERROR
        }
    }

    fun startQuiz(seconds: Int = questionDuration) {
        if (questions.isEmpty()) {
            screenState = ScreenState.ERROR
            return
        }
        questionDuration = seconds
        questions = questions.shuffled()
        currentIndex = 0
        score = 0
        selectedAnswerIndex = null
        screenState = ScreenState.QUIZ
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        if (questionDuration <= 0) return // Режим «Только сюжет» — таймера нет
        timeLeft = questionDuration
        timerJob = scope.launch {
            while (timeLeft > 0) {
                delay(1000L)
                timeLeft--
            }
            if (selectedAnswerIndex == null) {
                moveToNext()
            }
        }
    }

    fun selectAnswer(index: Int) {
        if (selectedAnswerIndex != null) return
        timerJob?.cancel()
        selectedAnswerIndex = index

        if (index == questions[currentIndex].correctIndex) {
            score++
        }

        scope.launch {
            delay(1000L)
            moveToNext()
        }
    }

    private fun moveToNext() {
        selectedAnswerIndex = null
        if (currentIndex + 1 < questions.size) {
            currentIndex++
            startTimer()
        } else {
            screenState = ScreenState.RESULT
        }
    }
}

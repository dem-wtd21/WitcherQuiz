package com.example.witcherquiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val WitcherBg = Color(0xFF161618)
val WitcherCard = Color(0xFF232327)
val WitcherGold = Color(0xFFD4AF37)
val WitcherText = Color(0xFFEDEDED)
val WitcherGreen = Color(0xFF2E7D32)
val WitcherRed = Color(0xFFC62828)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            val controller = remember { QuizController(context, scope) }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = WitcherBg
            ) {
                when (controller.screenState) {
                    ScreenState.START -> StartScreen(onStartClick = { seconds -> controller.startQuiz(seconds) })
                    ScreenState.QUIZ -> QuizScreen(controller = controller)
                    ScreenState.RESULT -> ResultScreen(
                        score = controller.score,
                        total = controller.questions.size,
                        onRestartClick = { controller.screenState = ScreenState.START }
                    )
                    ScreenState.ERROR -> ErrorScreen()
                }
            }
        }
    }
}

@Composable
fun StartScreen(onStartClick: (Int) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "ВЗЯТЬ ЗАКАЗ",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = WitcherGold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Испытание на знание книг Анджея Сапковского и игр о Геральте из Ривии.\n\n Выбери сложность заказа:",
            fontSize = 16.sp,
            color = WitcherText,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { onStartClick(0) },
            colors = ButtonDefaults.buttonColors(
                containerColor = WitcherGold,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Text(
                text = "ТОЛЬКО СЮЖЕТ",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Button(
            onClick = { onStartClick(10) },
            colors = ButtonDefaults.buttonColors(
                containerColor = WitcherRed,
                contentColor = WitcherText
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Text(
                text = "НА СМЕРТЬ! (10 сек)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun QuizScreen(controller: QuizController) {
    val currentQuestion = controller.questions[controller.currentIndex]
    val total = controller.questions.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Вопрос ${controller.currentIndex + 1} из $total",
                    color = WitcherGold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (controller.questionDuration > 0) {
                    Text(
                        text = "Осталось: ${controller.timeLeft} сек",
                        color = if (controller.timeLeft <= 4) WitcherRed else WitcherText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "",
                        color = WitcherText.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                }
            }
            if (controller.questionDuration > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { controller.timeLeft / controller.questionDuration.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = if (controller.timeLeft <= 4) WitcherRed else WitcherGold,
                    trackColor = WitcherCard,
                )
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = WitcherCard),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = currentQuestion.question,
                color = WitcherText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 28.sp,
                modifier = Modifier.padding(20.dp)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            currentQuestion.options.forEachIndexed { index, optionText ->
                val selected = controller.selectedAnswerIndex
                val buttonColor = when {
                    selected == null -> WitcherCard
                    index == currentQuestion.correctIndex -> WitcherGreen
                    index == selected -> WitcherRed
                    else -> WitcherCard
                }

                Button(
                    onClick = { controller.selectAnswer(index) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonColor,
                        contentColor = WitcherText
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = optionText,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun ResultScreen(score: Int, total: Int, onRestartClick: () -> Unit) {
    val (rankTitle, rankDesc) = when {
        score <= 3 -> "Лютик без лютни" to "Кажется, ты слишком много времени провёл в таверне. Пора перечитать сагу!"
        score <= 7 -> "Ученик из Каэр Морхена" to "Мечом махать умеешь, но бестиарий стоит ещё подучить."
        else -> "Белый Волк" to "Ты пережил Испытание Травами и знаешь лор не хуже чародеек Ложи."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "ИТОГИ КОНТРАКТА",
            fontSize = 22.sp,
            color = WitcherText,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "$score из $total",
            fontSize = 48.sp,
            color = WitcherGold,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = rankTitle,
            fontSize = 24.sp,
            color = WitcherGold,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = rankDesc,
            fontSize = 16.sp,
            color = WitcherText,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        Spacer(modifier = Modifier.height(36.dp))
        Button(
            onClick = onRestartClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = WitcherGold,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Text(
                text = "ВЗЯТЬ НОВЫЙ ЗАКАЗ",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ErrorScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Заказов пока нет — не удалось загрузить свиток с вопросами.",
            color = WitcherRed,
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
    }
}
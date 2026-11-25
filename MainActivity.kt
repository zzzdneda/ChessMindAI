package com.chessmind.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val Bg = Color(0xFF101820)
private val Panel = Color(0xFF1B2835)
private val Accent = Color(0xFF63D6A3)
private val Muted = Color(0xFFA9B7C6)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ChessMindApp() }
    }
}

@Composable
fun ChessMindApp() {
    var fen by remember { mutableStateOf("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1") }
    var depth by remember { mutableFloatStateOf(16f) }
    var status by remember { mutableStateOf("Připraveno k analýze") }
    var result by remember { mutableStateOf("Zadej FEN pozice a spusť analýzu. Engine Stockfish je potřeba přidat zvlášť.") }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    MaterialTheme(colorScheme = darkColorScheme(
        background = Bg, surface = Panel, primary = Accent,
        onBackground = Color.White, onSurface = Color.White
    )) {
        Column(
            Modifier.fillMaxSize().background(Bg).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("CHESSMIND AI", color = Accent, fontSize = 25.sp, fontWeight = FontWeight.Black)
            Text("Offline chess analysis", color = Muted, fontSize = 14.sp)
            Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("POZICE", color = Accent, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = fen, onValueChange = { fen = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("FEN") },
                        minLines = 3,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None)
                    )
                    Text("FEN je textový zápis pozice. Vlož ho sem z vlastní analýzy nebo tréninkové partie.", color = Muted, fontSize = 12.sp)
                }
            }
            Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("VÝKON ENGINE", color = Accent, fontWeight = FontWeight.Bold)
                    Text("Hloubka: ${depth.toInt()}", color = Color.White)
                    Slider(value = depth, onValueChange = { depth = it }, valueRange = 8f..24f, steps = 15)
                    Text("Vyšší hloubka může výrazně zpomalit analýzu a zahřívat telefon.", color = Muted, fontSize = 12.sp)
                }
            }
            Button(
                onClick = {
                    scope.launch {
                        busy = true
                        status = "Kontrola enginu…"
                        result = withContext(Dispatchers.IO) { StockfishEngine.analyze(applicationContext, fen.trim(), depth.toInt()) }
                        status = if (result.startsWith("CHYBA")) "Engine není připraven" else "Analýza dokončena"
                        busy = false
                    }
                },
                enabled = !busy,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Bg)
            ) { Text(if (busy) "ANALYZUJI…" else "SPUSTIT ANALÝZU", fontWeight = FontWeight.Bold) }
            Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(status.uppercase(), color = Accent, fontWeight = FontWeight.Bold)
                    Text(result, color = Color.White, fontSize = 14.sp)
                }
            }
            Text("ChessMind AI je samostatný tréninkový analyzátor. Nepřipojuje se k živým partiím Chess.com ani za hráče neprovádí tahy.", color = Muted, fontSize = 12.sp)
        }
    }
}

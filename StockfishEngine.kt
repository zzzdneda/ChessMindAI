package com.chessmind.ai

import android.content.Context
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * UCI bridge. Place a compatible Android ARM64 Stockfish executable at:
 * app/src/main/assets/stockfish/stockfish
 *
 * The binary is not bundled in this starter project. Obtain it from the official
 * Stockfish project and comply with its GPL license and source distribution terms.
 */
object StockfishEngine {
    fun analyze(context: Context, fen: String, depth: Int): String {
        if (!fenLooksValid(fen)) return "CHYBA: FEN nemá očekávaný formát."
        return try {
            val engine = File(context.filesDir, "stockfish")
            if (!engine.exists()) {
                val asset = runCatching { context.assets.open("stockfish/stockfish") }.getOrNull()
                    ?: return "CHYBA: Chybí Stockfish binárka. Přidej ARM64 Android build do app/src/main/assets/stockfish/stockfish."
                asset.use { input -> engine.outputStream().use { input.copyTo(it) } }
                engine.setExecutable(true)
            }
            val process = ProcessBuilder(engine.absolutePath).redirectErrorStream(true).start()
            val writer = process.outputStream.bufferedWriter()
            val reader = process.inputStream.bufferedReader()
            fun send(command: String) { writer.write(command); writer.newLine(); writer.flush() }
            send("uci")
            readUntil(reader, "uciok", 5)
            send("isready")
            readUntil(reader, "readyok", 5)
            send("setoption name MultiPV value 3")
            send("position fen $fen")
            send("go depth ${depth.coerceIn(8, 24)}")
            val lines = mutableListOf<String>()
            val deadline = System.currentTimeMillis() + 45_000
            while (System.currentTimeMillis() < deadline) {
                val line = reader.readLine() ?: break
                if (line.startsWith("info") && line.contains(" pv ")) {
                    val multipv = Regex("multipv (\\d+)").find(line)?.groupValues?.get(1) ?: "1"
                    val score = Regex("score (cp|mate) (-?\\d+)").find(line)?.value ?: "score unavailable"
                    val pv = line.substringAfter(" pv ", "")
                    if (pv.isNotBlank()) lines.add("Varianta $multipv • $score • $pv")
                }
                if (line.startsWith("bestmove")) {
                    lines.add(0, "Nejlepší tah: " + line.substringAfter("bestmove ").substringBefore(" "))
                    break
                }
            }
            send("quit")
            process.waitFor(2, TimeUnit.SECONDS)
            if (lines.isEmpty()) "CHYBA: Engine nevrátil analýzu." else lines.distinct().joinToString("\n")
        } catch (e: Exception) {
            "CHYBA: ${e.message ?: "Nepodařilo se spustit Stockfish."}"
        }
    }

    private fun readUntil(reader: java.io.BufferedReader, token: String, seconds: Long) {
        val end = System.currentTimeMillis() + seconds * 1000
        while (System.currentTimeMillis() < end) {
            val line = reader.readLine() ?: break
            if (line.contains(token)) return
        }
        throw IllegalStateException("Engine timeout při čekání na $token")
    }

    private fun fenLooksValid(fen: String): Boolean =
        fen.split(Regex("\\s+")).size == 6 && fen.contains("/") &&
            (fen.split(Regex("\\s+"))[1] == "w" || fen.split(Regex("\\s+"))[1] == "b")
}

# ChessMind AI (Android starter)

Starter project for Xiaomi / Android 15+ using Kotlin, Jetpack Compose and a local Stockfish UCI engine.

## Build
1. Open this folder in Android Studio (Ladybug or newer).
2. Let Gradle sync.
3. Add a compatible **Android ARM64** Stockfish executable at:
   `app/src/main/assets/stockfish/stockfish`
4. Build > Build Bundle(s) / APK(s) > Build APK(s).
5. Install the debug APK on your phone.

## Stockfish
The engine binary is intentionally not bundled. Download/build an Android ARM64 binary from the official Stockfish project:
https://stockfishchess.org/
Stockfish is GPL-licensed. If you distribute an app containing it, comply with the applicable GPL requirements, including providing corresponding source and license notices.

## Current scope
- FEN position input
- Local UCI engine integration
- MultiPV candidate lines (three principal variations)
- Adjustable search depth

The displayed lines are candidate continuations, not calibrated probabilities of what an opponent will play. The app does not connect to Chess.com, read live boards, or make moves for a player. Use it for study and analysis outside live rated games.

## Notes
This is a starter project, not a signed production APK. A full chessboard editor, robust FEN validation, engine lifecycle management, and device-specific performance testing are still needed.

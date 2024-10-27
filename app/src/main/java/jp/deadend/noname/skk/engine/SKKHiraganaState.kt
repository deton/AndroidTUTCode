package jp.deadend.noname.skk.engine

import android.os.Build
import jp.deadend.noname.skk.R

// ひらがなモード
object SKKHiraganaState : SKKState {
    override val isTransient = false
    override val icon = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        R.drawable.ic_hiragana
    } else {
        R.drawable.immodeic_hiragana
    }

    override fun handleKanaKey(context: SKKEngine) {
        if (context.toggleKanaKey) context.changeState(SKKASCIIState)
    }

    override fun handleEisuKey(context: SKKEngine) = context.changeState(SKKASCIIState)

    internal fun processKana(
            context: SKKEngine,
            pcode: Int, commitFunc:
            (SKKEngine, String) -> Unit
    ) {
        val composing = context.mComposing
        composing.append(pcode.toChar())
        val hchr = context.romaji2kana(composing.toString())
        when (hchr) {
            null -> { // ローマ字シーケンス外の文字が来た場合はそのまま確定
                context.commitTextSKK(composing)
                composing.setLength(0)
            }
            "@cont" -> { // ローマ字内の文字ならComposingに積む
                context.setComposingTextSKK(composing, 1)
            }
            "@tglkata" -> { // カタカナひらがなモードトグル
                composing.setLength(0)
                if (context.state === SKKHiraganaState) {
                    context.changeState(SKKKatakanaState)
                } else if (context.state === SKKKatakanaState) {
                    context.changeState(SKKHiraganaState)
                }
            }
            "@maze" -> { // 漢字変換候補入力の開始。KanjiModeへの移行
                composing.setLength(0)
                context.changeState(SKKKanjiState)
                context.setComposingTextSKK("", 1);
            }
            "@Bushu" -> { // 後置型部首合成変換
                context.setComposingTextSKK("", 1);
                context.changeLastCharsByBushuConv()
            }
            "@Kata1" -> { // 後置型カタカナ置換。カタカナを1文字伸ばす
                context.setComposingTextSKK("", 1);
                context.changeLastCharsToKatakana(1)
            }
            "@Kata2" -> { // 後置型カタカナ置換。カタカナを2文字伸ばす
                context.setComposingTextSKK("", 1);
                context.changeLastCharsToKatakana(2)
            }
            "@Kata3" -> { // 後置型カタカナ置換。カタカナを3文字伸ばす
                context.setComposingTextSKK("", 1);
                context.changeLastCharsToKatakana(3)
            }
            "@Kata4" -> { // 後置型カタカナ置換。カタカナを4文字伸ばす
                context.setComposingTextSKK("", 1);
                context.changeLastCharsToKatakana(4)
            }
            "@Kata5" -> { // 後置型カタカナ置換。カタカナを5文字伸ばす
                context.setComposingTextSKK("", 1);
                context.changeLastCharsToKatakana(5)
            }
            "@Kata6" -> { // 後置型カタカナ置換。カタカナを6文字伸ばす
                context.setComposingTextSKK("", 1);
                context.changeLastCharsToKatakana(6)
            }
            "@Kata7" -> { // 後置型カタカナ置換。カタカナを7文字伸ばす
                context.setComposingTextSKK("", 1);
                context.changeLastCharsToKatakana(7)
            }
            "@Kata8" -> { // 後置型カタカナ置換。カタカナを8文字伸ばす
                context.setComposingTextSKK("", 1);
                context.changeLastCharsToKatakana(8)
            }
            "@Kata9" -> { // 後置型カタカナ置換。カタカナを9文字伸ばす
                context.setComposingTextSKK("", 1);
                context.changeLastCharsToKatakana(9)
            }
            "@Kata0" -> { // 後置型カタカナ置換。カタカナを伸ばす。連続するひらがな
                context.setComposingTextSKK("", 1);
                context.changeLastCharsToKatakana(0)
            }
            "@Kata-1" -> { // 後置型カタカナ置換。カタカナを伸ばす。連続するひらがな(1文字残す)
                context.setComposingTextSKK("", 1);
                context.changeLastCharsToKatakana(-1)
            }
            "@Kata-2" -> { // 後置型カタカナ置換。カタカナを伸ばす。連続するひらがな(2文字残す)
                context.setComposingTextSKK("", 1);
                context.changeLastCharsToKatakana(-2)
            }
            "@Kata-3" -> { // 後置型カタカナ置換。カタカナを伸ばす。連続するひらがな(3文字残す)
                context.setComposingTextSKK("", 1);
                context.changeLastCharsToKatakana(-3)
            }
            "@Kata-4" -> { // 後置型カタカナ置換。カタカナを伸ばす。連続するひらがな(4文字残す)
                context.setComposingTextSKK("", 1);
                context.changeLastCharsToKatakana(-4)
            }
            "@Kata-5" -> { // 後置型カタカナ置換。カタカナを伸ばす。連続するひらがな(5文字残す)
                context.setComposingTextSKK("", 1);
                context.changeLastCharsToKatakana(-5)
            }
            "@Kata-6" -> { // 後置型カタカナ置換。カタカナを伸ばす。連続するひらがな(6文字残す)
                context.setComposingTextSKK("", 1);
                context.changeLastCharsToKatakana(-6)
            }
            "@Maze1" -> { // 後置型交ぜ書き変換開始。読み1文字
                context.startPostMaze(1, false)
            }
            "@Maze2" -> { // 後置型交ぜ書き変換開始。読み2文字
                context.startPostMaze(2, false)
            }
            "@Maze3" -> { // 後置型交ぜ書き変換開始。読み3文字
                context.startPostMaze(3, false)
            }
            "@Maze4" -> { // 後置型交ぜ書き変換開始。読み4文字
                context.startPostMaze(4, false)
            }
            "@Maze5" -> { // 後置型交ぜ書き変換開始。読み5文字
                context.startPostMaze(5, false)
            }
            "@Maze6" -> { // 後置型交ぜ書き変換開始。読み6文字
                context.startPostMaze(6, false)
            }
            "@Maze7" -> { // 後置型交ぜ書き変換開始。読み7文字
                context.startPostMaze(7, false)
            }
            "@Maze8" -> { // 後置型交ぜ書き変換開始。読み8文字
                context.startPostMaze(8, false)
            }
            "@Maze9" -> { // 後置型交ぜ書き変換開始。読み9文字
                context.startPostMaze(9, false)
            }
            "@MazeK1" -> { // 後置型交ぜ書き変換開始(活用する語)。読み1文字
                context.startPostMaze(1, true)
            }
            "@MazeK2" -> { // 後置型交ぜ書き変換開始(活用する語)。読み2文字
                context.startPostMaze(2, true)
            }
            "@MazeK3" -> { // 後置型交ぜ書き変換開始(活用する語)。読み3文字
                context.startPostMaze(3, true)
            }
            "@MazeK4" -> { // 後置型交ぜ書き変換開始(活用する語)。読み4文字
                context.startPostMaze(4, true)
            }
            "@MazeK5" -> { // 後置型交ぜ書き変換開始(活用する語)。読み5文字
                context.startPostMaze(5, true)
            }
            "@MazeK6" -> { // 後置型交ぜ書き変換開始(活用する語)。読み6文字
                context.startPostMaze(6, true)
            }
            "@MazeK7" -> { // 後置型交ぜ書き変換開始(活用する語)。読み7文字
                context.startPostMaze(7, true)
            }
            "@MazeK8" -> { // 後置型交ぜ書き変換開始(活用する語)。読み8文字
                context.startPostMaze(8, true)
            }
            "@MazeK9" -> { // 後置型交ぜ書き変換開始(活用する語)。読み9文字
                context.startPostMaze(9, true)
            }
            else -> commitFunc(context, hchr) // 確定できるものがあれば確定
        }
    }

    override fun processKey(context: SKKEngine, pcode: Int) {
        if (context.changeInputMode(pcode, true)) return
        processKana(context, pcode) { engine, hchr ->
            engine.commitTextSKK(hchr)
            engine.mComposing.setLength(0)
        }
    }

    override fun afterBackspace(context: SKKEngine) {
        context.setComposingTextSKK(context.mComposing, 1)
    }

    override fun handleCancel(context: SKKEngine): Boolean {
        if (context.isRegistering) {
            context.cancelRegister()
            return true
        } else {
            return context.reConversion()
        }
    }
}

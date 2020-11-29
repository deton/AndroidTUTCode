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
                context.commitTextSKK(composing, 1)
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
            "@kata1" -> { // 後置型カタカナ置換。カタカナを1文字伸ばす
                context.setComposingTextSKK("", 1);
                context.changeLastCharsToKatakana(1)
            }
            "@kata2" -> { // 後置型カタカナ置換。カタカナを2文字伸ばす
                context.setComposingTextSKK("", 1);
                context.changeLastCharsToKatakana(2)
            }
            "@kata3" -> { // 後置型カタカナ置換。カタカナを3文字伸ばす
                context.setComposingTextSKK("", 1);
                context.changeLastCharsToKatakana(3)
            }
            "@kata4" -> { // 後置型カタカナ置換。カタカナを4文字伸ばす
                context.setComposingTextSKK("", 1);
                context.changeLastCharsToKatakana(4)
            }
            "@kata5" -> { // 後置型カタカナ置換。カタカナを5文字伸ばす
                context.setComposingTextSKK("", 1);
                context.changeLastCharsToKatakana(5)
            }
            "@kata6" -> { // 後置型カタカナ置換。カタカナを6文字伸ばす
                context.setComposingTextSKK("", 1);
                context.changeLastCharsToKatakana(6)
            }
            "@kata7" -> { // 後置型カタカナ置換。カタカナを7文字伸ばす
                context.setComposingTextSKK("", 1);
                context.changeLastCharsToKatakana(7)
            }
            "@kata8" -> { // 後置型カタカナ置換。カタカナを8文字伸ばす
                context.setComposingTextSKK("", 1);
                context.changeLastCharsToKatakana(8)
            }
            "@kata9" -> { // 後置型カタカナ置換。カタカナを9文字伸ばす
                context.setComposingTextSKK("", 1);
                context.changeLastCharsToKatakana(9)
            }
            "@kata0" -> { // 後置型カタカナ置換。カタカナを伸ばす。連続するひらがな
                context.setComposingTextSKK("", 1);
                context.changeLastCharsToKatakana(0)
            }
            "@kata-1" -> { // 後置型カタカナ置換。カタカナを伸ばす。連続するひらがな(1文字残す)
                context.setComposingTextSKK("", 1);
                context.changeLastCharsToKatakana(-1)
            }
            "@kata-2" -> { // 後置型カタカナ置換。カタカナを伸ばす。連続するひらがな(2文字残す)
                context.setComposingTextSKK("", 1);
                context.changeLastCharsToKatakana(-2)
            }
            "@kata-3" -> { // 後置型カタカナ置換。カタカナを伸ばす。連続するひらがな(3文字残す)
                context.setComposingTextSKK("", 1);
                context.changeLastCharsToKatakana(-3)
            }
            "@kata-4" -> { // 後置型カタカナ置換。カタカナを伸ばす。連続するひらがな(4文字残す)
                context.setComposingTextSKK("", 1);
                context.changeLastCharsToKatakana(-4)
            }
            "@kata-5" -> { // 後置型カタカナ置換。カタカナを伸ばす。連続するひらがな(5文字残す)
                context.setComposingTextSKK("", 1);
                context.changeLastCharsToKatakana(-5)
            }
            "@kata-6" -> { // 後置型カタカナ置換。カタカナを伸ばす。連続するひらがな(6文字残す)
                context.setComposingTextSKK("", 1);
                context.changeLastCharsToKatakana(-6)
            }
            else -> commitFunc(context, hchr) // 確定できるものがあれば確定
        }
    }

    override fun processKey(context: SKKEngine, pcode: Int) {
        if (context.changeInputMode(pcode, true)) return
        processKana(context, pcode) { engine, hchr ->
            engine.commitTextSKK(hchr, 1)
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

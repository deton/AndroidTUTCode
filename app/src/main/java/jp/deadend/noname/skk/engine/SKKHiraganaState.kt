package jp.deadend.noname.skk.engine

import android.os.Build
import jp.deadend.noname.skk.R
import jp.deadend.noname.skk.isAlphabet

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

    internal fun processKana(
            context: SKKEngine,
            pcode: Int, commitFunc:
            (SKKEngine, String) -> Unit
    ) {
        val composing = context.mComposing
        composing.append(pcode.toChar())
        val hchr = RomajiConverter.convert(composing.toString())
        when (hchr) {
            null -> {
                if (isAlphabet(pcode) || pcode == ';'.toInt() || pcode == ','.toInt() || pcode == '.'.toInt() || pcode == '/'.toInt()) {
                    // ローマ字内の文字ならComposingに積む
                    // TODO: pcode is in Romaji
                    context.setComposingTextSKK(composing, 1)
                } else {
                    context.commitTextSKK(composing, 1)
                    composing.setLength(0)
                }
            }
            "@maze" -> { // 漢字変換候補入力の開始。KanjiModeへの移行
                composing.setLength(0)
                context.changeState(SKKKanjiState)
                context.setComposingTextSKK("", 1);
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

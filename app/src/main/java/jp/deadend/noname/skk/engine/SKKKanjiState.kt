package jp.deadend.noname.skk.engine

import jp.deadend.noname.skk.createTrimmedBuilder
import jp.deadend.noname.skk.hirakana2katakana
import jp.deadend.noname.skk.isVowel

// 漢字変換のためのひらがな入力中(▽モード)
object SKKKanjiState : SKKState {
    override val isTransient = true
    override val icon = 0

    override fun handleKanaKey(context: SKKEngine) {}

    override fun processKey(context: SKKEngine, pcode: Int) {
        val composing = context.mComposing
        val kanjiKey = context.mKanjiKey
        if (pcode == ' '.toInt()) {
            // 変換開始
            composing.setLength(0)
            context.conversionStart(kanjiKey)
        } else {
            // 未確定
            composing.append(pcode.toChar())
            val hchr = context.romaji2kana(composing.toString())
            if (hchr != null) {
                if (hchr[0] != '@') { // 機能でなく普通のかな漢字の場合
                    composing.setLength(0)
                    kanjiKey.append(hchr)
                    context.setComposingTextSKK(kanjiKey, 1)
                }
            } else {
                context.setComposingTextSKK(kanjiKey.toString() + composing.toString(), 1)
            }
            context.updateSuggestions(kanjiKey.toString())
        }
    }

    override fun afterBackspace(context: SKKEngine) {
        val kanjiKey = context.mKanjiKey.toString()
        val composing = context.mComposing.toString()

        context.setComposingTextSKK(kanjiKey + composing, 1)
        context.updateSuggestions(kanjiKey)
    }

    override fun handleCancel(context: SKKEngine): Boolean {
        context.changeState(SKKHiraganaState)
        return true
    }
}

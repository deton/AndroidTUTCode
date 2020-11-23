package jp.deadend.noname.skk.engine

import jp.deadend.noname.skk.createTrimmedBuilder
import jp.deadend.noname.skk.hirakana2katakana
import jp.deadend.noname.skk.isVowel

// 漢字変換のためのひらがな入力中(▽モード)
object SKKKanjiState : SKKState {
    override val isTransient = true
    override val icon = 0

    override fun handleKanaKey(context: SKKEngine) {}

    override fun handleEisuKey(context: SKKEngine) {
        context.changeState(SKKASCIIState)
    }

    override fun processKey(context: SKKEngine, pcode: Int) {
        val kanjiKey = context.mKanjiKey
        val composing = context.mComposing
        composing.append(pcode.toChar())
        val hchr = context.romaji2kana(composing.toString())
        when (hchr) {
            null -> { // ローマ字シーケンス外の文字
                if (pcode == ' '.toInt()) { // 変換開始
                    composing.setLength(0)
                    context.conversionStart(kanjiKey)
                } else {
                    context.setComposingTextSKK(kanjiKey.toString() + composing.toString(), 1)
                }
            }
            "@cont" -> { // ローマ字内の文字
                context.setComposingTextSKK(kanjiKey.toString() + composing.toString(), 1)
            }
            "@tglkata" -> { // カタカナひらがなモードトグル
                composing.setLength(0) // 現状は無視する
            }
            "@maze" -> { // 再帰的な前置型交ぜ書き変換は未対応
                composing.setLength(0) // 無視する
            }
            else -> { // ローマ字シーケンスに対応するかな/漢字があった→hchr
                composing.setLength(0)
                kanjiKey.append(hchr)
                context.setComposingTextSKK(kanjiKey, 1)
            }
        }
        context.updateSuggestions(kanjiKey.toString())
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

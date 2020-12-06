package io.github.deton.androidtutcode

object BushuConverter {
    fun convert(ca: Char, cb: Char): Char? {
        convertSub(ca, cb)?.let { return it }
        // 逆順で試す
        convertSub(cb, ca)?.let { return it }
        return null
    }
    fun convertSub(ca: Char, cb: Char): Char? {
        fun isNewChar(c: Char): Boolean {
            return c != ca && c != cb
        }
        fun compose(a: Char?, b: Char?): Char? {
            if (a == null || b == null) {
                return null
            }
            BushuConvData.mBushuList.firstOrNull {
                it[0] == a && it[1] == b
            }?.let {
                if (isNewChar(it[2])) {
                    return it[2]
                }
            }
            return null
        }
        fun decompose(c: Char?): Pair<Char?, Char?> {
            if (c == null) {
                return Pair(null, null)
            }
            BushuConvData.mBushuList.firstOrNull {
                it[2] == c
            }?.let {
                return Pair(it[0], it[1])
            }
            return Pair(null, null)
        }
        // そのまま合成できる?
        compose(ca, cb)?.let { return it }
        // 等価文字どうしで合成できる?
        val a = BushuConvData.mAltCharMap[ca] ?: ca
        val b = BushuConvData.mAltCharMap[cb] ?: cb
        compose(a, b)?.let { return it }
        // 等価文字を部首に分解
        val (a1, a2) = decompose(a)
        val (b1, b2) = decompose(b)
        // YAMANOBE algorithm
        fun composeL2R(x: Char?, y: Char?, z: Char?): Char? {
            compose(x, y)?.let {
                compose(it, z)?.let { return it }
            }
            return null
        }
        fun composeR2L(x: Char?, y: Char?, z: Char?): Char? {
            compose(y, z)?.let {
                compose(x, it)?.let { return it }
            }
            return null
        }
        composeL2R(a1, b, a2)?.let { return it }
        composeR2L(a1, a2, b)?.let { return it }
        composeL2R(a, b1, b2)?.let { return it }
        composeR2L(b1, a, b2)?.let { return it }
        // 引き算
        /**
         * x == y の場合、zを返す。
         * 前提: (y, z) = decompose(w)
         */
        fun subtract(x: Char?, y: Char?, z: Char?): Char? {
            if (x == null || y == null || z == null) {
                return null
            }
            if (x == y && isNewChar(z)) {
                return z
            }
            return null
        }
        subtract(b, a2, a1)?.let { return it }
        subtract(b, a1, a2)?.let { return it }
        // YAMANOBE algorithm
        val (a11, a12) = decompose(a1)
        val (a21, a22) = decompose(a2)
        if (a12 == b) {
            compose(a11, a2)?.let { return it }
        }
        if (a11 == b) {
            compose(a12, a2)?.let { return it }
        }
        if (a22 == b) {
            compose(a1, a21)?.let { return it }
        }
        if (a21 == b) {
            compose(a1, a22)?.let { return it }
        }
        // 一方が部品による足し算
        compose(a, b1)?.let { return it }
        compose(a, b2)?.let { return it }
        compose(a1, b)?.let { return it }
        compose(a2, b)?.let { return it }
        // YAMANOBE algorithm
        composeL2R(a1, b1, a2)?.let { return it }
        composeR2L(a1, a2, b1)?.let { return it }
        composeL2R(a1, b2, a2)?.let { return it }
        composeR2L(a1, a2, b2)?.let { return it }
        composeL2R(a1, b1, b2)?.let { return it }
        composeR2L(b1, a1, b2)?.let { return it }
        composeL2R(a2, b1, b2)?.let { return it }
        composeR2L(b1, a2, b2)?.let { return it }
        // 両方が部品による足し算
        compose(a1, b1)?.let { return it }
        compose(a1, b2)?.let { return it }
        compose(a2, b1)?.let { return it }
        compose(a2, b2)?.let { return it }
        // 部品による引き算
        subtract(b1, a2, a1)?.let { return it }
        subtract(b2, a2, a1)?.let { return it }
        subtract(b1, a1, a2)?.let { return it }
        subtract(b2, a1, a2)?.let { return it }
        // YAMANOBE algorithm
        if (a12 != null && (a12 == b1 || a12 == b2)) {
            compose(a11, a2)?.let { return it }
        }
        if (a11 != null && (a11 == b1 || a11 == b2)) {
            compose(a12, a2)?.let { return it }
        }
        if (a22 != null && (a22 == b1 || a22 == b2)) {
            compose(a1, a21)?.let { return it }
        }
        if (a21 != null && (a21 == b1 || a21 == b2)) {
            compose(a1, a22)?.let { return it }
        }
        return null
    }
}
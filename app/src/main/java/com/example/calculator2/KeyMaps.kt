/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.calculator2

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Button
import java.text.DecimalFormatSymbols
import java.util.*

/**
 * Collection of mapping functions between key ids, characters, internationalized
 * and non-internationalized characters, etc.
 *
 *
 * KeyMap instances are not meaningful; everything here is static.
 * All functions are either pure, or are assumed to be called only from a single UI thread.
 */
@Suppress("MemberVisibilityCanBePrivate")
@SuppressLint("StaticFieldLeak")
object KeyMaps {

    const val NOT_DIGIT = 10

    const val ELLIPSIS = "\u2026"

    const val MINUS_SIGN = '\u2212'

    // The following two are only used for recognizing additional
    // input characters from a physical keyboard.  They are not used
    // for output internationalization.
    private var mDecimalPt: Char = ' '

    private var mPiChar: Char = ' '

    /**
     * Character used as a placeholder for digits that are currently unknown in a result that
     * is being computed.  We initially generate blanks, and then use this as a replacement
     * during final translation.
     *
     *
     * Note: the character must correspond closely to the width of a digit,
     * otherwise the UI will visibly shift once the computation is finished.
     */
    private const val CHAR_DIGIT_UNKNOWN = '\u2007'

    /**
     * Map typed function name strings to corresponding button ids.
     * We (now redundantly?) include both localized and English names.
     */
    private var sKeyValForFun: HashMap<String, Int>? = null

    /**
     * Result string corresponding to a character in the calculator result.
     * The string values in the map are expected to be one character long.
     */
    private var sOutputForResultChar: HashMap<Char, String>? = null

    /**
     * Locale corresponding to preceding map and character constants.
     * We recompute the map if this is not the current locale.
     */
    private var sLocaleForMaps: Locale? = null

    /**
     * Activity to use for looking up buttons.
     */
    @SuppressLint("StaticFieldLeak")
    private var mActivity: Activity? = null

    /**
     * Map key id to corresponding (internationalized) display string.
     * Pure function.
     */
    fun toString(context: Context, id: Int): String {
        when (id) {
            R.id.const_pi -> return context.getString(R.string.const_pi)
            R.id.const_e -> return context.getString(R.string.const_e)
            R.id.op_sqrt -> return context.getString(R.string.op_sqrt)
            R.id.op_fact -> return context.getString(R.string.op_fact)
            R.id.op_pct -> return context.getString(R.string.op_pct)
            R.id.fun_sin -> return context.getString(R.string.fun_sin) + context.getString(R.string.lparen)
            R.id.fun_cos -> return context.getString(R.string.fun_cos) + context.getString(R.string.lparen)
            R.id.fun_tan -> return context.getString(R.string.fun_tan) + context.getString(R.string.lparen)
            R.id.fun_arcsin -> return context.getString(R.string.fun_arcsin) + context.getString(R.string.lparen)
            R.id.fun_arccos -> return context.getString(R.string.fun_arccos) + context.getString(R.string.lparen)
            R.id.fun_arctan -> return context.getString(R.string.fun_arctan) + context.getString(R.string.lparen)
            R.id.fun_ln -> return context.getString(R.string.fun_ln) + context.getString(R.string.lparen)
            R.id.fun_log -> return context.getString(R.string.fun_log) + context.getString(R.string.lparen)
            R.id.fun_exp ->
                // Button label doesn't work.
                return context.getString(R.string.exponential) + context.getString(R.string.lparen)
            R.id.lparen -> return context.getString(R.string.lparen)
            R.id.rparen -> return context.getString(R.string.rparen)
            R.id.op_pow -> return context.getString(R.string.op_pow)
            R.id.op_mul -> return context.getString(R.string.op_mul)
            R.id.op_div -> return context.getString(R.string.op_div)
            R.id.op_add -> return context.getString(R.string.op_add)
            R.id.op_sub -> return context.getString(R.string.op_sub)
            R.id.op_sqr ->
                // Button label doesn't work.
                return context.getString(R.string.squared)
            R.id.dec_point -> return context.getString(R.string.dec_point)
            R.id.digit_0 -> return context.getString(R.string.digit_0)
            R.id.digit_1 -> return context.getString(R.string.digit_1)
            R.id.digit_2 -> return context.getString(R.string.digit_2)
            R.id.digit_3 -> return context.getString(R.string.digit_3)
            R.id.digit_4 -> return context.getString(R.string.digit_4)
            R.id.digit_5 -> return context.getString(R.string.digit_5)
            R.id.digit_6 -> return context.getString(R.string.digit_6)
            R.id.digit_7 -> return context.getString(R.string.digit_7)
            R.id.digit_8 -> return context.getString(R.string.digit_8)
            R.id.digit_9 -> return context.getString(R.string.digit_9)
            else -> return ""
        }
    }

    /**
     * Map key id to a single byte, somewhat human readable, description.
     * Used to serialize expressions in the database.
     * The result is in the range 0x20-0x7f.
     */
    fun toByte(id: Int): Byte {
        val result: Char
        // We only use characters with single-byte UTF8 encodings in the range 0x20-0x7F.
        when (id) {
            R.id.const_pi -> result = 'p'
            R.id.const_e -> result = 'e'
            R.id.op_sqrt -> result = 'r'
            R.id.op_fact -> result = '!'
            R.id.op_pct -> result = '%'
            R.id.fun_sin -> result = 's'
            R.id.fun_cos -> result = 'c'
            R.id.fun_tan -> result = 't'
            R.id.fun_arcsin -> result = 'S'
            R.id.fun_arccos -> result = 'C'
            R.id.fun_arctan -> result = 'T'
            R.id.fun_ln -> result = 'l'
            R.id.fun_log -> result = 'L'
            R.id.fun_exp -> result = 'E'
            R.id.lparen -> result = '('
            R.id.rparen -> result = ')'
            R.id.op_pow -> result = '^'
            R.id.op_mul -> result = '*'
            R.id.op_div -> result = '/'
            R.id.op_add -> result = '+'
            R.id.op_sub -> result = '-'
            R.id.op_sqr -> result = '2'
            else -> throw AssertionError("Unexpected key id")
        }
        return result.toByte()
    }

    /**
     * Map single byte encoding generated by key id generated by toByte back to
     * key id.
     */
    fun fromByte(b: Byte): Int {
        when (b.toChar()) {
            'p' -> return R.id.const_pi
            'e' -> return R.id.const_e
            'r' -> return R.id.op_sqrt
            '!' -> return R.id.op_fact
            '%' -> return R.id.op_pct
            's' -> return R.id.fun_sin
            'c' -> return R.id.fun_cos
            't' -> return R.id.fun_tan
            'S' -> return R.id.fun_arcsin
            'C' -> return R.id.fun_arccos
            'T' -> return R.id.fun_arctan
            'l' -> return R.id.fun_ln
            'L' -> return R.id.fun_log
            'E' -> return R.id.fun_exp
            '(' -> return R.id.lparen
            ')' -> return R.id.rparen
            '^' -> return R.id.op_pow
            '*' -> return R.id.op_mul
            '/' -> return R.id.op_div
            '+' -> return R.id.op_add
            '-' -> return R.id.op_sub
            '2' -> return R.id.op_sqr
            else -> throw AssertionError("Unexpected single byte operator encoding")
        }
    }

    /**
     * Map key id to corresponding (internationalized) descriptive string that can be used
     * to correctly read back a formula.
     * Only used for operators and individual characters; not used inside constants.
     * Returns null when we don't need a descriptive string.
     * Pure function.
     */
    fun toDescriptiveString(context: Context, id: Int): String? {
        when (id) {
            R.id.op_fact -> return context.getString(R.string.desc_op_fact)
            R.id.fun_sin -> return (context.getString(R.string.desc_fun_sin)
                    + " " + context.getString(R.string.desc_lparen))
            R.id.fun_cos -> return (context.getString(R.string.desc_fun_cos)
                    + " " + context.getString(R.string.desc_lparen))
            R.id.fun_tan -> return (context.getString(R.string.desc_fun_tan)
                    + " " + context.getString(R.string.desc_lparen))
            R.id.fun_arcsin -> return (context.getString(R.string.desc_fun_arcsin)
                    + " " + context.getString(R.string.desc_lparen))
            R.id.fun_arccos -> return (context.getString(R.string.desc_fun_arccos)
                    + " " + context.getString(R.string.desc_lparen))
            R.id.fun_arctan -> return (context.getString(R.string.desc_fun_arctan)
                    + " " + context.getString(R.string.desc_lparen))
            R.id.fun_ln -> return (context.getString(R.string.desc_fun_ln)
                    + " " + context.getString(R.string.desc_lparen))
            R.id.fun_log -> return (context.getString(R.string.desc_fun_log)
                    + " " + context.getString(R.string.desc_lparen))
            R.id.fun_exp -> return (context.getString(R.string.desc_fun_exp)
                    + " " + context.getString(R.string.desc_lparen))
            R.id.lparen -> return context.getString(R.string.desc_lparen)
            R.id.rparen -> return context.getString(R.string.desc_rparen)
            R.id.op_pow -> return context.getString(R.string.desc_op_pow)
            R.id.dec_point -> return context.getString(R.string.desc_dec_point)
            else -> return null
        }
    }

    /**
     * Does a button id correspond to a binary operator?
     * Pure function.
     */
    fun isBinary(id: Int): Boolean {
        return when (id) {
            R.id.op_pow, R.id.op_mul, R.id.op_div, R.id.op_add, R.id.op_sub -> true
            else -> false
        }
    }

    /**
     * Does a button id correspond to a trig function?
     * Pure function.
     */
    fun isTrigFunc(id: Int): Boolean {
        return when (id) {
            R.id.fun_sin, R.id.fun_cos, R.id.fun_tan, R.id.fun_arcsin, R.id.fun_arccos, R.id.fun_arctan -> true
            else -> false
        }
    }

    /**
     * Does a button id correspond to a function that introduces an implicit lparen?
     * Pure function.
     */
    fun isFunc(id: Int): Boolean {
        if (isTrigFunc(id)) {
            return true
        }
        return when (id) {
            R.id.fun_ln, R.id.fun_log, R.id.fun_exp -> true
            else -> false
        }
    }

    /**
     * Does a button id correspond to a prefix operator?
     * Pure function.
     */
    fun isPrefix(id: Int): Boolean {
        return when (id) {
            R.id.op_sqrt, R.id.op_sub -> true
            else -> false
        }
    }

    /**
     * Does a button id correspond to a suffix operator?
     */
    fun isSuffix(id: Int): Boolean {
        return when (id) {
            R.id.op_fact, R.id.op_pct, R.id.op_sqr -> true
            else -> false
        }
    }

    /**
     * Map key id to digit or NOT_DIGIT
     * Pure function.
     */
    fun digVal(id: Int): Int {
        return when (id) {
            R.id.digit_0 -> 0
            R.id.digit_1 -> 1
            R.id.digit_2 -> 2
            R.id.digit_3 -> 3
            R.id.digit_4 -> 4
            R.id.digit_5 -> 5
            R.id.digit_6 -> 6
            R.id.digit_7 -> 7
            R.id.digit_8 -> 8
            R.id.digit_9 -> 9
            else -> NOT_DIGIT
        }
    }

    /**
     * Map digit to corresponding key.  Inverse of above.
     * Pure function.
     */
    fun keyForDigVal(v: Int): Int {
        return when (v) {
            0 -> R.id.digit_0
            1 -> R.id.digit_1
            2 -> R.id.digit_2
            3 -> R.id.digit_3
            4 -> R.id.digit_4
            5 -> R.id.digit_5
            6 -> R.id.digit_6
            7 -> R.id.digit_7
            8 -> R.id.digit_8
            9 -> R.id.digit_9
            else -> View.NO_ID
        }
    }

    /**
     * Set activity used for looking up button labels.
     * Call only from UI thread.
     */
    fun setActivity(a: Activity) {
        mActivity = a
    }

    /**
     * Return the button id corresponding to the supplied character or return NO_ID.
     * Called only by UI thread.
     */
    fun keyForChar(c: Char): Int {
        validateMaps()
        if (Character.isDigit(c)) {
            val i = Character.digit(c, 10)
            return keyForDigVal(i)
        }
        when (c) {
            '.', ',' -> return R.id.dec_point
            '-', MINUS_SIGN -> return R.id.op_sub
            '+' -> return R.id.op_add
            '*', '\u00D7' // MULTIPLICATION SIGN
            -> return R.id.op_mul
            '/', '\u00F7' // DIVISION SIGN
            -> return R.id.op_div
            // We no longer localize function names, so they can't start with an 'e' or 'p'.
            'e', 'E' -> return R.id.const_e
            'p', 'P' -> return R.id.const_pi
            '^' -> return R.id.op_pow
            '!' -> return R.id.op_fact
            '%' -> return R.id.op_pct
            '(' -> return R.id.lparen
            ')' -> return R.id.rparen
            else -> {
                if (c == mDecimalPt) return R.id.dec_point
                return if (c == mPiChar) R.id.const_pi else View.NO_ID
                // pi is not translated, but it might be type-able on a Greek keyboard,
                // or pasted in, so we check ...
            }
        }
    }

    /**
     * Add information corresponding to the given button id to sKeyValForFun, to be used
     * when mapping keyboard input to button ids.
     */
    internal fun addButtonToFunMap(button_id: Int) {
        val button = mActivity!!.findViewById<Button>(button_id)
        sKeyValForFun!![button.text.toString()] = button_id
    }

    /**
     * Add information corresponding to the given button to sOutputForResultChar, to be used
     * when translating numbers on output.
     */
    internal fun addButtonToOutputMap(c: Char, button_id: Int) {
        val button = mActivity!!.findViewById<Button>(button_id)
        sOutputForResultChar!![c] = button.text.toString()
    }

    /**
     * Ensure that the preceding map and character constants correspond to the current locale.
     * Called only by UI thread.
     */
    internal fun validateMaps() {
        val locale = Locale.getDefault()
        if (locale != sLocaleForMaps) {
            Log.v("Calculator", "Setting locale to: " + locale.toLanguageTag())
            sKeyValForFun = HashMap()
            sKeyValForFun!!["sin"] = R.id.fun_sin
            sKeyValForFun!!["cos"] = R.id.fun_cos
            sKeyValForFun!!["tan"] = R.id.fun_tan
            sKeyValForFun!!["arcsin"] = R.id.fun_arcsin
            sKeyValForFun!!["arccos"] = R.id.fun_arccos
            sKeyValForFun!!["arctan"] = R.id.fun_arctan
            sKeyValForFun!!["asin"] = R.id.fun_arcsin
            sKeyValForFun!!["acos"] = R.id.fun_arccos
            sKeyValForFun!!["atan"] = R.id.fun_arctan
            sKeyValForFun!!["ln"] = R.id.fun_ln
            sKeyValForFun!!["log"] = R.id.fun_log
            sKeyValForFun!!["sqrt"] = R.id.op_sqrt // special treatment
            addButtonToFunMap(R.id.fun_sin)
            addButtonToFunMap(R.id.fun_cos)
            addButtonToFunMap(R.id.fun_tan)
            addButtonToFunMap(R.id.fun_arcsin)
            addButtonToFunMap(R.id.fun_arccos)
            addButtonToFunMap(R.id.fun_arctan)
            addButtonToFunMap(R.id.fun_ln)
            addButtonToFunMap(R.id.fun_log)

            // Set locale-dependent character "constants"
            mDecimalPt = DecimalFormatSymbols.getInstance().decimalSeparator
            // We recognize this in keyboard input, even if we use
            // a different character.
            val res = mActivity!!.resources
            mPiChar = 0.toChar()
            val piString = res.getString(R.string.const_pi)
            if (piString.length == 1) {
                mPiChar = piString[0]
            }

            sOutputForResultChar = HashMap()
            sOutputForResultChar!!['e'] = "E"
            sOutputForResultChar!!['E'] = "E"
            sOutputForResultChar!![' '] = CHAR_DIGIT_UNKNOWN.toString()
            sOutputForResultChar!![ELLIPSIS[0]] = ELLIPSIS
            // Translate numbers for fraction display, but not the separating slash, which appears
            // to be universal.  We also do not translate the ln, sqrt, pi
            sOutputForResultChar!!['/'] = "/"
            sOutputForResultChar!!['('] = "("
            sOutputForResultChar!![')'] = ")"
            sOutputForResultChar!!['l'] = "l"
            sOutputForResultChar!!['n'] = "n"
            sOutputForResultChar!![','] = DecimalFormatSymbols.getInstance().groupingSeparator.toString()
            sOutputForResultChar!!['\u221A'] = "\u221A" // SQUARE ROOT
            sOutputForResultChar!!['\u03C0'] = "\u03C0" // GREEK SMALL LETTER PI
            addButtonToOutputMap('-', R.id.op_sub)
            addButtonToOutputMap('.', R.id.dec_point)
            for (i in 0..9) {
                addButtonToOutputMap(('0'.toInt() + i).toChar(), keyForDigVal(i))
            }

            sLocaleForMaps = locale

        }
    }

    /**
     * Return function button id for the substring of s starting at nextPos and ending with
     * the next "(".  Return NO_ID if there is none.
     * We currently check for both (possibly localized) button labels, and standard
     * English names.  (They should currently be the same, and hence this is currently redundant.)
     * Callable only from UI thread.
     */
    fun funForString(s: String, pos: Int): Int {
        validateMaps()
        val parenPos = s.indexOf('(', pos)
        if (parenPos != -1) {
            val funString = s.substring(pos, parenPos)
            return sKeyValForFun!![funString] ?: return View.NO_ID
        }
        return View.NO_ID
    }

    /**
     * Return the localization of the string s representing a numeric answer.
     * Callable only from UI thread.
     * A trailing e is treated as the mathematical constant, not an exponent.
     */
    fun translateResult(s: String): String {
        val result = StringBuilder()
        val len = s.length
        validateMaps()
        for (i in 0 until len) {
            val c = s[i]
            if (i < len - 1 || c != 'e') {
                val translation = sOutputForResultChar!![c]
                if (translation == null) {
                    // Should not get here.  Report if we do.
                    Log.v("Calculator", "Bad character:$c")
                    result.append(c)
                } else {
                    result.append(translation)
                }
            }
        }
        return result.toString()
    }

}

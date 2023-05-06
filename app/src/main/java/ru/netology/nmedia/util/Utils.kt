package ru.netology.nmedia.util

object Utils {
    fun convert(num: Int): String {
        val form: String
        val n: Int
        when(num) {
            in 0 .. 999 -> return num.toString()
            in 1000 .. 9999 -> {
                n = num%1000
                form = (if((n < 100)||(n>900)) "%.0f" else "%.1f")
                return String.format(form, num.toDouble()/1000) + "K"
            }
            in 10000 .. 999999 -> return String.format("%.0f", num.toDouble()/1000) + "K"
            else -> {
                n = num%1000000
                form = (if((n < 100000)||(n>900000)) "%.0f" else "%.1f")
                return String.format(form, num.toDouble()/1000000) + "M"
            }
        }
    }
}
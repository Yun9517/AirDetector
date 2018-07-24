package com.microjet.airqi2


import java.util.Calendar

/**
 * @author airsaid
 *
 * 日期工具类.
 */
class DateUtils private constructor() {

    init {
        // 工具类, 禁止实例化
        throw AssertionError()
    }

    companion object {

        /**
         * 通过指定的年份和月份获取当月有多少天.
         *
         * @param year  年.
         * @param month 月.
         * @return 天数.
         */
        fun getMonthDays(year: Int, month: Int): Int {
            when (month) {
                1, 3, 5, 7, 8, 10, 12 -> return 31
                4, 6, 9, 11 -> return 30
                2 -> return if (year % 4 == 0 && year % 100 != 0 || year % 400 == 0) {
                    29
                } else {
                    28
                }
                else -> return -1
            }
        }

        /**
         * 获取指定年月的 1 号位于周几.
         * @param year  年.
         * @param month 月.
         * @return      周.
         */
        fun getFirstDayWeek(year: Int, month: Int): Int {
            val calendar = Calendar.getInstance()
            calendar.set(year, month - 1, 0)
            return calendar.get(Calendar.DAY_OF_WEEK)
        }
    }

}

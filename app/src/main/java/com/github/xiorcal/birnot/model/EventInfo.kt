package com.github.xiorcal.birnot.model


data class EventInfo(
    val contactName: String,
    val eventDateDay: String,
    val eventDateMonth: String,
    val eventDateYear: String,
    val eventType: EventType,
    val eventLabel: String?,
) {
    companion object {
        fun createFromAndroidInfos(
            displayName: String,
            eventType: String,
            eventLabel: String?,
            rawDate: String
        ): EventInfo {
            val type = if (eventType == "0") EventType.CUSTOM else EventType.BIRTHDAY
            val (day, month, year) = parseDate(rawDate)
            return EventInfo(displayName, day, month, year, type, eventLabel)
        }

        private fun parseDate(rawDate: String): Triple<String, String, String> {
            // year may not be present
            val year = if (rawDate.startsWith("-")) {
                "-"
            } else {
                rawDate.substring(0, 4)
            }
            // day and month are always present
            val monthDay = rawDate.takeLast(5)

            val month = monthDay.take(2)
            val day = monthDay.takeLast(2)

            return Triple(day, month, year)
        }
    }

}

enum class EventType() {
    BIRTHDAY,CUSTOM
}

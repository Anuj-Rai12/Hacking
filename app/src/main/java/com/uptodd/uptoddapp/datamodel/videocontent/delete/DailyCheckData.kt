package com.uptodd.uptoddapp.datamodel.videocontent.delete

data class DailyCheckData(
    val title: String,
    val url: String = "uSTCoECm3TA"
) {
    companion object {
        val list = listOf(
            DailyCheckData(
                "Video is of session content 1 segment"
            ), DailyCheckData(
                "Video is of session content 2 segment"
            ), DailyCheckData(
                "Video is of session content 3 segment"
            ), DailyCheckData(
                "Video is of session content 4 segment"
            ), DailyCheckData(
                "Video is of session content 5 segment"
            ), DailyCheckData(
                "Video is of session content 6 segment"
            ), DailyCheckData(
                "Video is of session content 7 segment"
            ), DailyCheckData(
                "Video is of session content 8 segment"
            ), DailyCheckData(
                "Video is of session content 9 segment"
            ), DailyCheckData(
                "Video is of session content 10 segment"
            ), DailyCheckData(
                "Video is of session content 11 segment"
            ), DailyCheckData(
                "Video is of session content 12 segment"
            )
        )
    }
}
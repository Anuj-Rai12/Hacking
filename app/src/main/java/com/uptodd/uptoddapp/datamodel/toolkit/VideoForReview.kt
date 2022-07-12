package com.uptodd.uptoddapp.datamodel.toolkit

data class VideoForReview(
    val title: String,
    val link: String
) {
    companion object {
        fun getVideContent(): List<VideoForReview> {
            val list = mutableListOf<VideoForReview>()
            list.add(
                VideoForReview(
                    "Testing One",
                    "https://uptodd.com/playYoutubeVideos/uxSh8svEoZQ/mqdefault.jpg"
                )
            )
            list.add(
                VideoForReview(
                    "Testing Tow",
                    "https://uptodd.com/playYoutubeVideos/uxSh8svEoZQ/mqdefault.jpg"
                )
            )
            list.add(
                VideoForReview(
                    "Testing Three",
                    "https://uptodd.com/playYoutubeVideos/uxSh8svEoZQ/mqdefault.jpg/mqdefault.jpg"
                )
            )
            list.add(
                VideoForReview(
                    "Testing Four",
                    "https://uptodd.com/playYoutubeVideos/uxSh8svEoZQ/mqdefault.jpg/mqdefault.jpg"
                )
            )
            list.add(
                VideoForReview(
                    "Testing Five",
                    "https://uptodd.com/playYoutubeVideos/uxSh8svEoZQ/mqdefault.jpg"
                )
            )
            list.add(
                VideoForReview(
                    "Testing Six",
                    "https://uptodd.com/playYoutubeVideos/uxSh8svEoZQ/mqdefault.jpg"
                )
            )
            return list
        }
    }
}
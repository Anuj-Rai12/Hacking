package com.uptodd.uptoddapp.datamodel.videocontent


data class VideoContent(
    val title: String,
    val desc: String,
    val module: ArrayList<ModuleList>,
    val link: String = "https://www.google.com"
) {
    companion object {

        fun getVideoContent(): ArrayList<VideoContent> {
            val list = ArrayList<VideoContent>()
            list.add(VideoContent(title = "Testing one","latest do ite!!", moduleList()))
            list.add(VideoContent(title = "Testing two","latest do ite!!", moduleList()))
            list.add(VideoContent(title = "Testing three","latest do ite!!", moduleList()))
            list.add(VideoContent(title = "Testing four","latest do ite!!", moduleList()))
            list.add(VideoContent(title = "Testing five","latest do ite!!", moduleList()))
            list.add(VideoContent(title = "Testing Six","latest do ite!!", moduleList()))
            list.add(VideoContent(title = "Testing Seven","latest do ite!!", moduleList()))
            return list
        }

       private fun moduleList(): ArrayList<ModuleList> {
            val list=ArrayList<ModuleList>()
            list.add(ModuleList("Video one","testing one of all time 1"))
            list.add(ModuleList("Video two","testing one of all time 2"))
            list.add(ModuleList("Video three","testing one of all time 3"))
            list.add(ModuleList("Video four","testing one of all time 4"))
            list.add(ModuleList("Video five","testing one of all time 5"))
            list.add(ModuleList("Video six","testing one of all time 6"))
            return list
        }

    }

}

data class ModuleList(
    val title: String,
    val desc: String
)
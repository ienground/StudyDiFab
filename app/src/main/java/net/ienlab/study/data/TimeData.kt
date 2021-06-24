package net.ienlab.study.data

class TimeData {
    var id: Int
    var type: Int
    var dateTime: Long
    var studyTime: Long
    var viewType: Int

    constructor() {
        id = -1
        type = -1
        dateTime = 0L
        studyTime = 0L
        viewType = 0
    }

    constructor(id: Int, type: Int, dateTime: Long, studyTime: Long, viewType: Int) {
        this.id = id
        this.type = type
        this.dateTime = dateTime
        this.studyTime = studyTime
        this.viewType = viewType
    }

    companion object {
        const val TYPE_SNOOZE = 0
        const val TYPE_STUDY_TIME = 1

        const val VIEWTYPE_NOTI = 0
        const val VIEWTYPE_DATE = 1
    }
}
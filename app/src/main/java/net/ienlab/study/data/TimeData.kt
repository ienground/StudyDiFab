package net.ienlab.study.data

class TimeData {
    var id: Int
    var type: Int
    var dateTime: Long
    var studyTime: Long

    constructor() {
        id = -1
        type = -1
        dateTime = 0L
        studyTime = 0L
    }

    constructor(id: Int, type: Int, dateTime: Long, studyTime: Long) {
        this.id = id
        this.type = type
        this.dateTime = dateTime
        this.studyTime = studyTime
    }

    companion object {
        const val TYPE_SNOOZE = 0
        const val TYPE_STUDY_TIME = 1
    }
}
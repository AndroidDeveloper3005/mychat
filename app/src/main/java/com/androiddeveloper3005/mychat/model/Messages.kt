package com.androiddeveloper3005.mychat.model

class Messages {
    var message:String? = null
    var type:String? = null
    var time:Long = 0
    var isSeen:Boolean = false
    var from:String? = null
    constructor(from:String) {
        this.from = from
    }
    constructor(message:String, type:String, time:Long, seen:Boolean) {
        this.message = message
        this.type = type
        this.time = time
        this.isSeen = seen
    }
    constructor() {
    }
}
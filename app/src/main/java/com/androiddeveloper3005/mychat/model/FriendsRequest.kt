package com.androiddeveloper3005.mychat.model

class FriendsRequest {
    var request_type:String? = null
    constructor() {}
    constructor(request_type:String) {
        this.request_type = request_type
    }
}
package com.uptodd.uptoddapp.datamodel.freeparentinglogin

class LoginSingletonResponse {

    private var request: FreeParentingLoginRequest? = null
    private var response: FreeParentingResponse? = null
    private var userId: Long? = null

    companion object {

        private var instance: LoginSingletonResponse? = null

        fun getInstance(): LoginSingletonResponse {
            if (instance == null) {
                instance = LoginSingletonResponse()
            }
            return instance!!
        }
    }

    fun setLoginResponse(response: FreeParentingResponse) {
        this.response = response
    }

    fun setLoginRequest(request: FreeParentingLoginRequest) {
        this.request = request
    }


    fun setLoginId(id: Long) {
        this.userId = id
    }

    fun getUserId() = this.userId

    fun getLoginResponse() = this.response

    fun getProgress() = (this.response?.data?.progress!! as Double).toInt()

    fun getLoginRequest() = this.request
}
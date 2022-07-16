package com.uptodd.uptoddapp.datamodel.freeparentinglogin

class LoginSingletonResponse {

    private var request: FreeParentingLoginRequest? = null
    private var response: FreeParentingResponse? = null

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

    fun getLoginResponse() = this.response

    fun getLoginRequest() = this.request
}
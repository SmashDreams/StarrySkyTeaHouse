package com.bird.StarrySkyTeaHouse.session

interface SessionGateway {
    fun isLoggedIn(): Boolean
    fun getCurrentUsername(): String?
    fun getLastLoginUsername(): String?
    fun isRememberPasswordEnabled(): Boolean
    fun getRememberedPassword(): String?
    fun login(username: String, password: String, rememberPassword: Boolean): Boolean
    fun logout()
}

interface RegisterSessionGateway {
    fun register(username: String, password: String, rememberPassword: Boolean): Boolean
}

class SessionStoreGateway(private val mSessionStore: SessionStore) : SessionGateway, RegisterSessionGateway {
    override fun isLoggedIn(): Boolean = mSessionStore.isLoggedIn()
    override fun getCurrentUsername(): String? = mSessionStore.getCurrentUsername()
    override fun getLastLoginUsername(): String? = mSessionStore.getLastLoginUsername()
    override fun isRememberPasswordEnabled(): Boolean = mSessionStore.isRememberPasswordEnabled()
    override fun getRememberedPassword(): String? = mSessionStore.getRememberedPassword()
    override fun login(username: String, password: String, rememberPassword: Boolean): Boolean {
        return mSessionStore.login(username, password, rememberPassword)
    }
    override fun logout() = mSessionStore.logout()
    override fun register(username: String, password: String, rememberPassword: Boolean): Boolean {
        return mSessionStore.register(username, password, rememberPassword)
    }
}

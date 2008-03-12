package org.grails.auth

class User {
    String email
    String login
	String password

    static hasMany = [roles:Role]

    static constraints = {
        email(email:true, unique:true)
        login(blank:false, size:5..15)
		password(blank:false)
	}

    static mapping = {
        cache true
    }
}
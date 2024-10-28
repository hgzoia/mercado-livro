package com.mercadolivro.controller.request

import com.mercadolivro.validation.EmailAvailable
import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty

data class PostCustomerRequest (

    @field:NotEmpty(message = "Name must not be empty.")
    var name: String,

    @field:Email(message = "Email needs to be valid.")
    @EmailAvailable
    var email: String,

    @field:NotEmpty(message = "Password must not be empty")
    var password: String
)
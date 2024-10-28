package com.mercadolivro.builder

import com.mercadolivro.controller.request.PostCustomerRequest
import com.mercadolivro.controller.request.PutCustomerRequest
import com.mercadolivro.enums.BookStatus
import com.mercadolivro.enums.CustomerStatus
import com.mercadolivro.enums.Role
import com.mercadolivro.model.BookModel
import com.mercadolivro.model.CustomerModel
import com.mercadolivro.model.PurchaseModel
import java.math.BigDecimal
import java.util.*
import kotlin.random.Random

fun buildCustomer(
        id: Int? = null,
        name: String = "customer name",
        email: String = "${UUID.randomUUID()}@email.com",
        password: String = "password",
    ) = CustomerModel(
        id = id,
        name = name,
        email = email,
        password = password,
        status = CustomerStatus.ATIVO,
        roles = setOf(Role.CUSTOMER)
    )

    fun buildBook(
        id: Int? = null,
        name: String = "book name",
        price: BigDecimal = BigDecimal(10.0),
    ) = BookModel(
            id = id,
            name = name,
            price = price,
            customer = buildCustomer(),
            status = BookStatus.ATIVO
        )

    fun buildPurchase(
        id: Int? = null,
        customer: CustomerModel = buildCustomer(),
        books: MutableList<BookModel> = mutableListOf(buildBook()),
        nfe: String? = UUID.randomUUID().toString(),
        price: BigDecimal = BigDecimal(10)
    ) = PurchaseModel(
        id = id,
        customer = customer,
        books = books,
        nfe = nfe,
        price = price
    )

    fun buildPostCustomerRequest(
        name: String = "Teste",
        email: String = "${Random.nextInt()}@teste.com",
        password: String = "1234"
    ) = PostCustomerRequest(
        name = name,
        email = email,
        password = password
    )

    fun buildPutCustomerRequest(
        name: String = "Teste",
        email: String = "${Random.nextInt()}@updatedemail.com",
    ) = PutCustomerRequest(
        name = name,
        email = email
    )
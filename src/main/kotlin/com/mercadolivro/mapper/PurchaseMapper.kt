package com.mercadolivro.mapper

import com.mercadolivro.controller.request.PostPurchaseRequest
import com.mercadolivro.enums.BookStatus
import com.mercadolivro.enums.Errors
import com.mercadolivro.exception.BadRequestException
import com.mercadolivro.model.PurchaseModel
import com.mercadolivro.service.BookService
import com.mercadolivro.service.CustomerService
import org.springframework.stereotype.Component

@Component
class PurchaseMapper(
    private val customerService: CustomerService,
    private val bookService: BookService
) {

    fun toModel(request: PostPurchaseRequest): PurchaseModel{
        val customer = customerService.findById(request.customerId)
        val books = bookService.findAllByIds(request.bookIds)

        for(book in books){
            if(book.status == BookStatus.VENDIDO || book.status == BookStatus.DELETADO)
                throw BadRequestException(Errors.ML102.message.format(book.status), Errors.ML102.code)
        }

        return PurchaseModel(
            customer = customer,
            books = books.toMutableList(),
            price = books.sumOf { it.price }
        )

    }

}
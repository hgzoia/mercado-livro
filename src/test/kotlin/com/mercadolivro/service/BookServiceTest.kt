package com.mercadolivro.service

import com.mercadolivro.builder.buildBook
import com.mercadolivro.enums.BookStatus
import com.mercadolivro.exception.NotFoundException
import com.mercadolivro.model.BookModel
import com.mercadolivro.repository.BookRepository
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import java.util.*

@ExtendWith(MockKExtension::class)
class BookServiceTest(){

    @MockK
    private lateinit var bookRepository: BookRepository

    @InjectMockKs
    @SpyK
    private lateinit var bookService: BookService

    @Test
    fun `should create book`(){
        val fakeBook = buildBook()

        every { bookRepository.save(any()) } returns fakeBook

        bookService.create(fakeBook)

        verify(exactly = 1) { bookRepository.save(any()) }

    }

    @Test
    fun `should get all books`(){
        val books = mockk<Page<BookModel>>()
        val pageable = PageRequest.of(1, 2)

        every { bookRepository.findAll(pageable) } returns books

        val response = bookService.findAll(pageable)

        verify(exactly = 1) { bookRepository.findAll(pageable) }
        assertEquals(books, response)
    }

    @Test
    fun `should find actives`(){
        val books = mockk<Page<BookModel>>()
        val pageable = PageRequest.of(1, 2)

        every { bookRepository.findByStatus(any(), any()) } returns books

        val response = bookService.findActives(pageable)

        verify(exactly = 1) { bookRepository.findByStatus(BookStatus.ATIVO, pageable) }
        assertEquals(books, response)
    }

    @Test
    fun `should find book by id`(){
        val id = 1
        val fakeBook = buildBook(id = id)

        every { bookRepository.findById(id) } returns Optional.of(fakeBook)

        val response = bookService.findById(id)

        verify(exactly = 1) { bookRepository.findById(id) }
        assertEquals(fakeBook, response)
    }

    @Test
    fun `should throw exception when find book by id`(){
        val id = 1

        every { bookRepository.findById(id) } returns Optional.empty()

        val error = assertThrows<NotFoundException> { bookService.findById(id) }

        verify(exactly = 1) { bookRepository.findById(id) }
        assertEquals("Book [${id}] not exists.", error.message)
        assertEquals("ML-101", error.errorCode)
    }

    @Test
    fun `should delete a book`(){
        val id = 1
        val book = buildBook()

        every { bookService.findById(id) } returns book
        every { bookService.update(book) } just runs

        bookService.delete(id)

        verify(exactly = 1) { bookService.findById(id) }
        verify(exactly = 1) { bookService.update(book) }
    }
}
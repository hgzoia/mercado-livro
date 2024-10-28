package com.mercadolivro.service

import com.mercadolivro.builder.buildCustomer
import com.mercadolivro.enums.CustomerStatus
import com.mercadolivro.enums.Errors
import com.mercadolivro.exception.NotFoundException
import com.mercadolivro.repository.CustomerRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.*

@ExtendWith(MockKExtension::class)
class CustomerServiceTest(
){

    @MockK
    private lateinit var customerRepository: CustomerRepository

    @MockK
    private lateinit var bookService: BookService

    @MockK
    private lateinit var bCrypt: BCryptPasswordEncoder

    @InjectMockKs
    @SpyK
    private lateinit var customerService: CustomerService

    @Test
    fun `should return all customers`(){
        val fakeCustomers = listOf(buildCustomer(), buildCustomer())

        every { customerRepository.findAll() } returns fakeCustomers

        val response = customerService.getAll(null)

        assertEquals(fakeCustomers, response)
        verify(exactly = 1) { customerRepository.findAll() }
        verify(exactly = 0) { customerRepository.findByNameContaining(any()) }
    }

    @Test
    fun `should return all customers containing name`(){
        val name = "customer"
        val fakeCustomers = listOf(buildCustomer(), buildCustomer())

        every { customerRepository.findByNameContaining(name) } returns fakeCustomers

        val response = customerService.getAll(name)

        assertEquals(fakeCustomers, response)
        verify(exactly = 1) { customerRepository.findByNameContaining(any()) }
    }

    @Test
    fun `should create customer`(){

        val fakeCustomer = buildCustomer()
        val password = "password"

        every { bCrypt.encode(any()) } returns password
        every { customerRepository.save(any()) } returns fakeCustomer

        customerService.create(fakeCustomer)

        verify(exactly = 1) { bCrypt.encode(any()) }
        verify(exactly = 1) { customerRepository.save(any()) }

    }

    @Test
    fun `should find customer by id`(){

        val id = Random().nextInt()
        val fakeCustomer = buildCustomer(id = id)

        every { customerRepository.findById(id) } returns Optional.of(fakeCustomer)

        val response = customerService.findById(id)

        assertEquals(fakeCustomer, response)
        verify(exactly = 1) { customerRepository.findById(id) }
    }

    @Test
    fun `should throw exception when customer not found`(){

        val id = Random().nextInt()

        every { customerRepository.findById(id) } returns Optional.empty()

        val error = assertThrows<NotFoundException> { customerService.findById(id) }

        assertEquals("Customer [${id}] not exists.", error.message)
        assertEquals("ML-201", error.errorCode)
        verify(exactly = 1) { customerRepository.findById(id) }
    }


    @Test
    fun `should update customer`(){
        val id = Random().nextInt()
        val fakeCustomer = buildCustomer(id = id)

        every { customerRepository.existsById(id) } returns true
        every { customerRepository.save(any()) } returns fakeCustomer

        customerService.update(fakeCustomer)

        verify(exactly = 1) { customerRepository.existsById(any()) }
        verify(exactly = 1) { customerRepository.save(any()) }

    }

    @Test
    fun `should throw exception when updating customer`(){

        val id = Random().nextInt()
        val fakeCustomer = buildCustomer(id = id)

        every { customerRepository.existsById(id) } returns false

        val error = assertThrows<NotFoundException> { customerService.update(fakeCustomer) }

        assertEquals("Customer [${id}] not exists.", error.message)
        assertEquals("ML-201", error.errorCode)
        verify(exactly = 1) { customerRepository.existsById(any()) }

    }

    @Test
    fun `should delete customer`(){
        val id = Random().nextInt()
        val fakeCustomer = buildCustomer(id = id)
        val expectedCustomer = fakeCustomer.copy(status = CustomerStatus.INATIVO)


        every { customerService.findById(id) } returns fakeCustomer
        every { customerRepository.save(expectedCustomer) } returns expectedCustomer
        every { bookService.deleteByCustomer(fakeCustomer) } just runs

        customerService.delete(id)

        verify(exactly = 1) { bookService.deleteByCustomer(any()) }
        verify(exactly = 1) { customerRepository.save(expectedCustomer) }

    }

    @Test
    fun `should throw exception when delete customer`(){
        val id = Random().nextInt()

        every { customerService.findById(id) } throws NotFoundException(Errors.ML201.message.format(id), Errors.ML201.code)

        val error = assertThrows<NotFoundException> { customerService.delete(id) }

        assertEquals("Customer [${id}] not exists.", error.message)
        assertEquals("ML-201", error.errorCode)

        verify(exactly = 1) { customerService.findById(id) }
        verify(exactly = 0) { bookService.deleteByCustomer(any()) }
        verify(exactly = 0) { customerRepository.save(any()) }

    }

    @Test
    fun `should find email`(){
        val email = "${UUID.randomUUID()}@email.com"

        every { customerRepository.existsByEmail(email) } returns false

        val response = customerService.emailAvailable(email)

        assertTrue(response)
        verify(exactly = 1) { customerRepository.existsByEmail(email) }
    }

    @Test
    fun `should not find email`(){
        val email = "${UUID.randomUUID()}@email.com"

        every { customerRepository.existsByEmail(email) } returns true

        val response = customerService.emailAvailable(email)

        assertFalse(response)
        verify(exactly = 1) { customerRepository.existsByEmail(email) }
    }
}
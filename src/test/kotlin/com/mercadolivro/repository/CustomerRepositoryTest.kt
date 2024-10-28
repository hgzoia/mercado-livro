package com.mercadolivro.repository

import com.mercadolivro.builder.buildCustomer
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(MockKExtension::class)
class CustomerRepositoryTest {

    @Autowired
    private lateinit var customerRepository: CustomerRepository

    @BeforeEach
    fun setup() = customerRepository.deleteAll()

    @Test
    fun `should return name containing`(){

        val c1 = customerRepository.save(buildCustomer(name = "Teste"))
        val c2 = customerRepository.save(buildCustomer(name = "Teste1"))
        val c3 = customerRepository.save(buildCustomer(name = "Teste2"))

        val response = customerRepository.findByNameContaining("T")

        assertEquals(listOf(c1, c2, c3), response)

    }

    @Nested
    inner class `exists by email`{
        @Test
        fun `should return true when email exists`(){
            val email = "email@teste.com"
            customerRepository.save(buildCustomer(email = email))

            val exists = customerRepository.existsByEmail(email)

            assertTrue(exists)
        }

        @Test
        fun `should return false when email do not exists`(){
            val email = "donotexist@teste.com"

            val exists = customerRepository.existsByEmail(email)

            assertFalse(exists)
        }
    }

    @Nested
    inner class `find by email`{
        @Test
        fun `should return customer when email exists`(){
            val email = "email@teste.com"
            val customer = customerRepository.save(buildCustomer(email = email))

            val response = customerRepository.findByEmail(email)

            assertNotNull(response)
            assertEquals(customer, response)
        }

        @Test
        fun `should return null when email do not exists`(){
            val email = "donotexist@teste.com"

            val response = customerRepository.findByEmail(email)

            assertNull(response)
        }
    }
}
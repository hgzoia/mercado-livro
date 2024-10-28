package com.mercadolivro.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.mercadolivro.builder.buildCustomer
import com.mercadolivro.builder.buildPostCustomerRequest
import com.mercadolivro.builder.buildPutCustomerRequest
import com.mercadolivro.enums.CustomerStatus
import com.mercadolivro.repository.CustomerRepository
import com.mercadolivro.security.UserCustomDetails
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WithMockUser
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration
@ActiveProfiles("test")
class CustomerControllerTest{

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var customerRepository: CustomerRepository

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setup() = customerRepository.deleteAll()

    @AfterEach
    fun tearDown() = customerRepository.deleteAll()

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `should return all customers` (){
        val c1 = customerRepository.save(buildCustomer())
        val c2 = customerRepository.save(buildCustomer())

        mockMvc.perform(get("/customers"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value(c1.id))
            .andExpect(jsonPath("$[0].name").value(c1.name))
            .andExpect(jsonPath("$[0].email").value(c1.email))
            .andExpect(jsonPath("$[0].status").value(c1.status.name))
            .andExpect(jsonPath("$[1].id").value(c2.id))
            .andExpect(jsonPath("$[1].name").value(c2.name))
            .andExpect(jsonPath("$[1].email").value(c2.email))
            .andExpect(jsonPath("$[1].status").value(c2.status.name))
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `should return all customers by name` (){
        val c1 = customerRepository.save(buildCustomer(name = "Teste"))
        customerRepository.save(buildCustomer(name = "RandomName"))

        mockMvc.perform(get("/customers?name=Te"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(c1.id))
            .andExpect(jsonPath("$[0].name").value(c1.name))
            .andExpect(jsonPath("$[0].email").value(c1.email))
            .andExpect(jsonPath("$[0].status").value(c1.status.name))
    }

    @Test
    fun `should create customer` (){
        val request = buildPostCustomerRequest()

        mockMvc.perform(post("/customers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated)

        val customers = customerRepository.findAll().toList()

        assertEquals(1, customers.size)
        assertEquals(request.name, customers[0].name)
        assertEquals(request.email, customers[0].email)
    }

    @Test
    fun `should throw exception when create customer has invalid information` (){
        val request = buildPostCustomerRequest(name = "")

        mockMvc.perform(post("/customers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnprocessableEntity)

            .andExpect(jsonPath("$.httpCode").value(422))
            .andExpect(jsonPath("$.message").value("Invalid Request."))
            .andExpect(jsonPath("$.internalCode").value("ML-001"))
    }

    @Test
    fun `should get user by id when user has the same id`(){
        val customer = customerRepository.save(buildCustomer())

        mockMvc.perform(get("/customers/${customer.id}").with(user(UserCustomDetails(customer))))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(customer.id))
            .andExpect(jsonPath("$.name").value(customer.name))
            .andExpect(jsonPath("$.email").value(customer.email))
            .andExpect(jsonPath("$.status").value(customer.status.name))
    }

    @Test
    fun `should return forbidden when user has different id`(){
        val customer = customerRepository.save(buildCustomer())

        mockMvc.perform(get("/customers/0").with(user(UserCustomDetails(customer))))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.httpCode").value(403))
            .andExpect(jsonPath("$.message").value("Access denied."))
            .andExpect(jsonPath("$.internalCode").value("ML-010"))
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `should get user by id when user is admin`(){
        val customer = customerRepository.save(buildCustomer())

        mockMvc.perform(get("/customers/${customer.id}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(customer.id))
            .andExpect(jsonPath("$.name").value(customer.name))
            .andExpect(jsonPath("$.email").value(customer.email))
            .andExpect(jsonPath("$.status").value(customer.status.name))
    }

    @Test
    fun `should update customer`(){
        val customer = customerRepository.save(buildCustomer())
        val request = buildPutCustomerRequest()

        mockMvc.perform(put("/customers/${customer.id}").with(user(UserCustomDetails(customer)))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNoContent)

        val customers = customerRepository.findAll().toList()

        assertEquals(1, customers.size)
        assertEquals(request.name, customers[0].name)
        assertEquals(request.email, customers[0].email)
    }

    @Test
    fun `should throw exception when update customer has invalid information` (){
        val request = buildPutCustomerRequest(name = "")

        mockMvc.perform(put("/customers/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnprocessableEntity)
            .andExpect(jsonPath("$.httpCode").value(422))
            .andExpect(jsonPath("$.message").value("Invalid Request."))
            .andExpect(jsonPath("$.internalCode").value("ML-001"))
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `should throw exception when update customer do not existing` (){
        val request = buildPutCustomerRequest()


        mockMvc.perform(put("/customers/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.httpCode").value(404))
            .andExpect(jsonPath("$.message").value("Customer [1] not exists."))
            .andExpect(jsonPath("$.internalCode").value("ML-201"))
    }

    @Test
    fun `should delete customer`(){
        val customer = customerRepository.save(buildCustomer())

        mockMvc.perform(delete("/customers/${customer.id}").with(user(UserCustomDetails(customer))))
            .andExpect(status().isNoContent)

        val response = customerRepository.findById(customer.id!!)
        assertEquals(CustomerStatus.INATIVO, response.get().status )
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `should return not found when customer do not exists`(){

        mockMvc.perform(delete("/customers/1"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.httpCode").value(404))
            .andExpect(jsonPath("$.message").value("Customer [1] not exists."))
            .andExpect(jsonPath("$.internalCode").value("ML-201"))
    }
}
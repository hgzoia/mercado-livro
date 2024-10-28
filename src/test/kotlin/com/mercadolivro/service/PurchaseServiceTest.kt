package com.mercadolivro.service

import com.mercadolivro.builder.buildPurchase
import com.mercadolivro.events.PurchaseEvent
import com.mercadolivro.repository.PurchaseRepository
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.context.ApplicationEventPublisher

@ExtendWith(MockKExtension::class)
class PurchaseServiceTest{

    @MockK
    private lateinit var purchaseRepository: PurchaseRepository

    @MockK
    private lateinit var applicationEventPublisher: ApplicationEventPublisher

    @InjectMockKs
    private lateinit var purchaseService: PurchaseService

    private val purchaseEventSlot = slot<PurchaseEvent>()

    @Test
    fun `should create purchase and publish event`(){

        val fakePurchase = buildPurchase()

        every { purchaseRepository.save(fakePurchase) } returns fakePurchase
        every { applicationEventPublisher.publishEvent(any()) } just runs

        purchaseService.create(fakePurchase)

        verify(exactly = 1) { purchaseRepository.save(fakePurchase) }
        verify(exactly = 1) { applicationEventPublisher.publishEvent(capture(purchaseEventSlot)) }
        assertEquals(fakePurchase, purchaseEventSlot.captured.purchaseModel)
    }

    @Test
    fun `should update purchase`(){

        val fakePurchase = buildPurchase()

        every { purchaseRepository.save(fakePurchase) } returns fakePurchase

        purchaseService.update(fakePurchase)

        verify(exactly = 1) { purchaseRepository.save(fakePurchase) }

    }

}
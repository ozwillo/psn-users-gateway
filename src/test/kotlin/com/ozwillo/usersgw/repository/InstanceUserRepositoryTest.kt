package com.ozwillo.usersgw.repository

import com.ozwillo.usersgw.model.local.InstanceUser
import com.ozwillo.usersgw.repository.local.InstanceUserRepository
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import java.util.*

@RunWith(SpringRunner::class)
@SpringBootTest
class InstanceUserRepositoryTest {

    @Autowired
    lateinit var instanceUserRepository: InstanceUserRepository

    @Test
    fun deleteUser() {
        val instanceId = UUID.randomUUID().toString()
        val userOzwilloId = UUID.randomUUID().toString()
        instanceUserRepository.save(InstanceUser(instanceId, userOzwilloId))
        Assert.assertEquals(1, instanceUserRepository.findByInstance(instanceId).size)

        instanceUserRepository.remove(InstanceUser(instanceId, userOzwilloId))
        Assert.assertEquals(0, instanceUserRepository.findByInstance(instanceId).size)
    }
}
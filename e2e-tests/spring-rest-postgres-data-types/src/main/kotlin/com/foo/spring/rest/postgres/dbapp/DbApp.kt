package com.foo.spring.rest.postgres.dbapp

import com.foo.spring.rest.postgres.SwaggerConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import springfox.documentation.swagger2.annotations.EnableSwagger2
import javax.persistence.EntityManager

/**
 * Created by jgaleotti on 18-Apr-22.
 */
@EnableSwagger2
@SpringBootApplication(exclude = [SecurityAutoConfiguration::class])
@RequestMapping(path = ["/api/postgres"])
open class DbApp : SwaggerConfiguration() {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(DbApp::class.java, *args)
        }
    }

    @Autowired
    private lateinit var em: EntityManager


    @GetMapping(path = ["/integerTypes"])
    open fun getIntegerTypes(): ResponseEntity<Any> {

        val query = em.createNativeQuery("select 1 from IntegerTypes where integerColumn>0")
        val res = query.resultList

        val status: Int
        if (res.isNotEmpty()) {
            status = 200
        } else {
            status = 400
        }

        return ResponseEntity.status(status).build()
    }

    @GetMapping(path = ["/arbitraryPrecisionNumbers"])
    open fun getArbitraryPrecisionNumbers(): ResponseEntity<Any> {

        val query = em.createNativeQuery("select 1 from ArbitraryPrecisionNumbers where numericColumn>0")
        val res = query.resultList

        val status: Int
        if (res.isNotEmpty()) {
            status = 200
        } else {
            status = 400
        }

        return ResponseEntity.status(status).build()
    }

    @GetMapping(path = ["/floatingPointTypes"])
    open fun getFloatingPointTypes(): ResponseEntity<Any> {

        val query = em.createNativeQuery("select 1 from FloatingPointTypes where realColumn>0")
        val res = query.resultList

        val status: Int
        if (res.isNotEmpty()) {
            status = 200
        } else {
            status = 400
        }

        return ResponseEntity.status(status).build()
    }

    //@GetMapping(path = ["/serialTypes"])
    open fun getSerialTypes(): ResponseEntity<Any> {

        val query = em.createNativeQuery("select 1 from SerialTypes where serialColumn>0")
        val res = query.resultList

        val status: Int
        if (res.isNotEmpty()) {
            status = 200
        } else {
            status = 400
        }

        return ResponseEntity.status(status).build()
    }

    @GetMapping(path = ["/monetaryTypes"])
    open fun getMonetaryTypes(): ResponseEntity<Any> {

        val query = em.createNativeQuery("select 1 from MonetaryTypes where moneyColumn>'0'")
        val res = query.resultList

        val status: Int
        if (res.isNotEmpty()) {
            status = 200
        } else {
            status = 400
        }

        return ResponseEntity.status(status).build()
    }


}


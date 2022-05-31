package org.evomaster.e2etests.spring.rest.datatypes

import com.foo.spring.rest.mysql.datatypes.MySqlDataTypesController
import org.evomaster.core.problem.rest.HttpVerb
import org.evomaster.e2etests.utils.RestTestBase
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class MySqlDataTypesEMTest : RestTestBase() {

    companion object {
        @BeforeAll
        @JvmStatic
        fun initClass() {
            initClass(MySqlDataTypesController())
        }
    }

    @Test
    fun testRunEM() {

        val budget = 200
        runTestHandlingFlakyAndCompilation(
                "DataTypesEM",
                "org.bar.mysql.MySqlDataTypesEM",
                budget
        ) { args ->

            val solution = initAndRun(args)

            assertTrue(solution.individuals.size >= 1)

            assertHasAtLeastOne(solution, HttpVerb.GET, 400,"/api/mysql/integerdatatypes",null)
            assertHasAtLeastOne(solution, HttpVerb.GET, 200,"/api/mysql/integerdatatypes",null)

            assertHasAtLeastOne(solution, HttpVerb.GET, 400,"/api/mysql/floatingpointtypes",null)
            assertHasAtLeastOne(solution, HttpVerb.GET, 200,"/api/mysql/floatingpointtypes",null)

            assertHasAtLeastOne(solution, HttpVerb.GET, 400,"/api/mysql/bitdatatype",null)
            assertHasAtLeastOne(solution, HttpVerb.GET, 200,"/api/mysql/bitdatatype",null)

            assertHasAtLeastOne(solution, HttpVerb.GET, 400,"/api/mysql/booleandatatypes",null)
            assertHasAtLeastOne(solution, HttpVerb.GET, 200,"/api/mysql/booleandatatypes",null)

            assertHasAtLeastOne(solution, HttpVerb.GET, 400,"/api/mysql/serialdatatype",null)
            assertHasAtLeastOne(solution, HttpVerb.GET, 200,"/api/mysql/serialdatatype",null)

            assertHasAtLeastOne(solution, HttpVerb.GET, 400,"/api/mysql/dateandtimetypes",null)
            assertHasAtLeastOne(solution, HttpVerb.GET, 200,"/api/mysql/dateandtimetypes",null)

            assertHasAtLeastOne(solution, HttpVerb.GET, 400,"/api/mysql/stringdatatypes",null)
            assertHasAtLeastOne(solution, HttpVerb.GET, 200,"/api/mysql/stringdatatypes",null)

//            assertHasAtLeastOne(solution, HttpVerb.GET, 400,"/api/mysql/fixedpointtypes",null)
//            assertHasAtLeastOne(solution, HttpVerb.GET, 200,"/api/mysql/fixedpointtypes",null)

        }
    }

}
package org.evomaster.core.problem.rest.service

import com.google.inject.Inject
import io.swagger.v3.oas.models.OpenAPI
import org.evomaster.client.java.controller.api.dto.SutInfoDto
import org.evomaster.core.EMConfig
import org.evomaster.core.database.DbAction
import org.evomaster.core.database.SqlInsertBuilder
import org.evomaster.core.output.OutputFormat
import org.evomaster.core.problem.httpws.service.HttpWsSampler
import org.evomaster.core.problem.rest.OpenApiAccess
import org.evomaster.core.problem.rest.RestActionBuilderV3
import org.evomaster.core.problem.rest.RestIndividual
import org.evomaster.core.problem.rest.auth.AuthenticationHeader
import org.evomaster.core.problem.rest.auth.AuthenticationInfo
import org.evomaster.core.problem.rest.auth.CookieLogin
import org.evomaster.core.problem.rest.auth.NoAuth
import org.evomaster.core.remote.SutProblemException
import org.evomaster.core.remote.service.RemoteController
import org.evomaster.core.search.service.Sampler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.annotation.PostConstruct


abstract class AbstractRestSampler : HttpWsSampler<RestIndividual>() {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(AbstractRestSampler::class.java)
    }

    @Inject(optional = true)
    protected lateinit var rc: RemoteController

    @Inject
    protected lateinit var configuration: EMConfig



    protected val adHocInitialIndividuals: MutableList<RestIndividual> = mutableListOf()

    protected var sqlInsertBuilder: SqlInsertBuilder? = null

    var existingSqlData : List<DbAction> = listOf()
        protected set

    //private val modelCluster: MutableMap<String, ObjectGene> = mutableMapOf()

    //private val usedObjects: UsedObjects = UsedObjects()

    protected lateinit var swagger: OpenAPI

    @PostConstruct
    open fun initialize() {

        log.debug("Initializing {}", AbstractRestSampler::class.simpleName)

        if(configuration.blackBox && !configuration.bbExperiments){
            initForBlackBox()
            return
        }

        rc.checkConnection()

        val started = rc.startSUT()
        if (!started) {
            throw SutProblemException("Failed to start the system under test")
        }

        val infoDto = rc.getSutInfo()
                ?: throw SutProblemException("Failed to retrieve the info about the system under test")

        val swaggerURL = infoDto.restProblem?.swaggerJsonUrl
                ?: throw IllegalStateException("Missing information about the Swagger URL")

        swagger = OpenApiAccess.getOpenAPI(swaggerURL)
        if (swagger.paths == null) {
            throw SutProblemException("There is no endpoint definition in the retrieved Swagger file")
        }

        actionCluster.clear()
        val skip = getEndpointsToSkip(swagger, infoDto)
        RestActionBuilderV3.addActionsFromSwagger(swagger, actionCluster, skip)

        setupAuthentication(infoDto)
        initSqlInfo(infoDto)

        initAdHocInitialIndividuals()

        postInits()

        updateConfigForTestOutput(infoDto)

        log.debug("Done initializing {}", AbstractRestSampler::class.simpleName)
    }

    abstract fun initSqlInfo(infoDto: SutInfoDto)

    abstract fun initAdHocInitialIndividuals()

    open fun postInits(){
        //do nothing
    }

    override fun resetSpecialInit() {
        initAdHocInitialIndividuals()
    }

    protected fun getEndpointsToSkip(swagger: OpenAPI, infoDto: SutInfoDto)
            : List<String>{

        /*
            If we are debugging, and focusing on a single endpoint, we skip
            everything but it.
            Otherwise, we just look at what configured in the SUT EM Driver.
         */

        if(configuration.endpointFocus != null){

            val all = swagger.paths.map{it.key}

            if(all.none { it == configuration.endpointFocus }){
                throw IllegalArgumentException(
                        "Invalid endpointFocus: ${configuration.endpointFocus}. " +
                                "\nAvailable:\n${all.joinToString("\n")}")
            }

            return all.filter { it != configuration.endpointFocus }
        }

        return  infoDto.restProblem?.endpointsToSkip ?: listOf()
    }

    private fun initForBlackBox() {

        swagger = OpenApiAccess.getOpenAPI(configuration.bbSwaggerUrl)
        if (swagger.paths == null) {
            throw SutProblemException("There is no endpoint definition in the retrieved Swagger file")
        }

        actionCluster.clear()
        RestActionBuilderV3.addActionsFromSwagger(swagger, actionCluster, listOf())

        //modelCluster.clear()
        // RestActionBuilder.getModelsFromSwagger(swagger, modelCluster)

        initAdHocInitialIndividuals()

        log.debug("Done initializing {}", RestSampler::class.simpleName)
    }






    fun getOpenAPI(): OpenAPI{
        return swagger
    }

    override fun hasSpecialInit(): Boolean {
        return !adHocInitialIndividuals.isEmpty() && config.probOfSmartSampling > 0
    }
}
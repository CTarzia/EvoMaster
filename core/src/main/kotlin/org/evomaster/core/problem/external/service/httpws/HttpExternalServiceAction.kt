package org.evomaster.core.problem.external.service.httpws

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.common.Metadata.metadata
import org.evomaster.core.problem.external.service.ApiExternalServiceAction
import org.evomaster.core.problem.external.service.httpws.param.HttpWsResponseParam
import org.evomaster.core.search.StructuralElement
import org.evomaster.core.search.gene.Gene
import java.util.UUID

/**
 * Action to execute the external service related need
 * to handle the external service calls.
 *
 * Typically, handle WireMock responses
 */
class HttpExternalServiceAction(

    /**
     * Received request to the respective WireMock instance
     *
     * TODO: Need to expand the properties further in future
     *  depending on the need
     */
    val request: HttpExternalServiceRequest,

    /**
     * currently, we support response with json format
     * then use ObjectGene now,
     * might extend it later
     */
    response: HttpWsResponseParam = HttpWsResponseParam(),

    /**
     * WireMock server which received the request
     */
    val externalService: ExternalService,
    active : Boolean = false,
    used : Boolean = false,
    private val id: Long,
    localId : String
) : ApiExternalServiceAction(response, active, used, localId) {

    companion object {
        private fun buildResponse(template: String): HttpWsResponseParam {
            // TODO: refactor later
            return HttpWsResponseParam()
        }
    }

    constructor(request: HttpExternalServiceRequest, template: String, externalService: ExternalService, id: Long, localId: String = NONE_ACTION_COMPONENT_ID) :
            this(request, buildResponse(template), externalService, id = id, localId = localId)


    /**
     * Holds the ID for the WireMock stub
     */
    var studIb: UUID = UUID(0,0)
        private set

    /**
     * UUID generated by WireMock is used under ExternalServiceRequest
     * is used as ID for action.
     *
     * TODO: After the ID refactor, this needs to be changed.
     */
    override fun getName(): String {
        return request.id.toString()
    }

    override fun seeTopGenes(): List<out Gene> {
        return response.genes
    }

    override fun shouldCountForFitnessEvaluations(): Boolean {
        return false
    }

    /**
     * Each external service will have a WireMock instance representing that
     * so when the ExternalServiceAction is copied, same instance will be passed
     * into the copy too. Otherwise, we have to manage multiple instances for the
     * same external service.
     */
    override fun copyContent(): StructuralElement {
        return HttpExternalServiceAction(
            request,
            response.copy() as HttpWsResponseParam,
            externalService,
            active,
            used,
            id,
            localId = getLocalId()
        )
    }

    /**
     * Experimental implementation of WireMock stub generation
     *
     * Method should randomize the response code
     *
     * TODO: This has to moved separetly to have extensive features
     *  in future.
     */
    fun buildResponse() {
        if (externalService.getWireMockServer().findStubMappingsByMetadata(matchingJsonPath("$.url", containing(request.url)))
                .isEmpty()
        ) {
            val id = externalService.getWireMockServer().stubFor(
                get(urlMatching(request.url))
                    .atPriority(1)
                    .willReturn(
                        aResponse()
                            .withStatus(viewStatus())
                            .withBody(viewResponse())
                    )
                    .withMetadata(
                        metadata()
                            .attr("url", request.url)
                    )
            )
            studIb = id.id
        }
    }

    /**
     * Remove the existing stub from WireMock. Will be used before building new response.
     */
    fun removeStub() {
        if (studIb != UUID(0,0)) {
            val existing = externalService.getWireMockServer().getStubMapping(studIb)
            if (existing.isPresent) {
                externalService.getWireMockServer().removeStubMapping(existing.item)
            }
        }
    }

    private fun viewStatus(): Int {
        return (response as HttpWsResponseParam).status.getValueAsRawString().toInt()
    }

    private fun viewResponse(): String {
        // TODO: Need to extend further to handle the response body based on the
        //  unmarshalled object inside SUT using the ParsedDto information.
        return (response as HttpWsResponseParam).response.getValueAsRawString()
    }

}
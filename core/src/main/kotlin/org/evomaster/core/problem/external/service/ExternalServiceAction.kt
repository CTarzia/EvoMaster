package org.evomaster.core.problem.external.service

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.common.Metadata.metadata
import org.evomaster.core.problem.external.service.param.ResponseParam
import org.evomaster.core.search.Action
import org.evomaster.core.search.StructuralElement
import org.evomaster.core.search.gene.Gene

/**
 * Action to execute the external service related need
 * to handle the external service calls.
 *
 * Typically, handle WireMock responses
 */
class ExternalServiceAction(

    /**
     * Received request to the respective WireMock instance
     *
     * TODO: Need to expand the properties further in future
     *  depending on the need
     */
    val request: ExternalServiceRequest,

    /**
     * currently, we support response with json format
     * then use ObjectGene now,
     * might extend it later
     */
    val response: ResponseParam = ResponseParam(),

    /**
     * WireMock server which received the request
     */
    val externalService: ExternalService,
    private val id: Long,
) : Action(listOf(response)) {

    companion object {
        private fun buildResponse(template: String): ResponseParam {
            // TODO: refactor later
            return ResponseParam()
        }
    }

    constructor(request: ExternalServiceRequest, template: String, externalService: ExternalService, id: Long) :
            this(request, buildResponse(template), externalService, id)

    init {
        // TODO: This is not the correct way to do this, but for now
        //  to test concept, this is triggered here.
        this.buildResponse()
    }

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
        return ExternalServiceAction(
            request,
            response.copy() as ResponseParam,
            externalService,
            id
        )
    }

    /**
     * Experimental implementation of WireMock stub generation
     *
     * Note: urlMatching should be a Regex, otherwise mapping will return
     * null when using getUrl()
     *
     * When there is two external service with same URL path, one will be ignored
     * because the path is used as the meta data for validation. To avoid absolute
     * URL will be used.
     *
     * TODO: This has to moved separetly to have extensive features
     *  in future.
     */
    fun buildResponse() {
        if (externalService.getWireMockServer().findStubMappingsByMetadata(matchingJsonPath("$.url", containing(request.getSignature())))
                .isEmpty()
        ) {
            if (request.method.lowercase() == "get") {
                externalService.getWireMockServer().stubFor(
                    get(urlEqualTo(request.url))
                        .atPriority(1)
                        .willReturn(
                            aResponse()
                                .withStatus(viewStatus())
                                .withBody(viewResponse())
                        )
                        .withMetadata(
                            metadata()
                                .attr("url", request.getSignature())
                        )
                )
            } else if (request.method.lowercase() == "post") {
                externalService.getWireMockServer().stubFor(
                    post(urlEqualTo(request.url))
                        .atPriority(1)
                        .willReturn(
                            aResponse()
                                .withStatus(viewStatus())
                                .withBody(viewResponse())
                        )
                        .withMetadata(
                            metadata()
                                .attr("url", request.getSignature())
                        )
                )
            }
        }
    }

    private fun viewStatus(): Int {
        return response.status.getValueAsRawString().toInt()
    }

    private fun viewResponse(): String {
        // TODO: This needs to be refactored
        return "{}"
    }

}
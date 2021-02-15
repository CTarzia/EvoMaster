package org.evomaster.core.problem.httpws.service

import org.evomaster.client.java.controller.api.dto.SutInfoDto
import org.evomaster.core.output.OutputFormat
import org.evomaster.core.problem.rest.RestCallAction
import org.evomaster.core.problem.rest.auth.AuthenticationHeader
import org.evomaster.core.problem.rest.auth.AuthenticationInfo
import org.evomaster.core.problem.rest.auth.CookieLogin
import org.evomaster.core.problem.rest.auth.NoAuth
import org.evomaster.core.problem.rest.service.AbstractRestSampler
import org.evomaster.core.remote.SutProblemException
import org.evomaster.core.search.Action
import org.evomaster.core.search.Individual
import org.evomaster.core.search.service.Sampler
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Common code shared among sampler that rely on a RemoteController, and
 * that use HTTP, ie typically Web Services like REST and GraphQL
 */
abstract class HttpWsSampler<T> : Sampler<T>() where T : Individual{

    companion object {
        private val log: Logger = LoggerFactory.getLogger(HttpWsSampler::class.java)
    }


    protected val authentications: MutableList<AuthenticationInfo> = mutableListOf()

    /**
     * When genes are created, those are not necessarily initialized.
     * The reason is that some genes might depend on other genes (eg., foreign keys in SQL).
     * So, once all genes are created, we force their initialization, which will also randomize their values.
     */
    fun randomizeActionGenes(action: Action, probabilistic: Boolean = false) {
        action.seeGenes().forEach { it.randomize(randomness, false) }
    }

    /**
     * Given the current schema definition, create a random action among the available ones.
     * All the genes in such action will have their values initialized at random, but still within
     * their given constraints (if any, e.g., a day number being between 1 and 12).
     *
     * @param noAuthP the probability of having an HTTP call without any authentication header.
     */
    fun sampleRandomAction(noAuthP: Double): HttpWsAction {
        val action = randomness.choose(actionCluster).copy() as HttpWsAction
        randomizeActionGenes(action)

        if (action is RestCallAction) {
            action.auth = getRandomAuth(noAuthP)
        }

        return action
    }

    fun getRandomAuth(noAuthP: Double): AuthenticationInfo {
        if (authentications.isEmpty() || randomness.nextBoolean(noAuthP)) {
            return NoAuth()
        } else {
            //if there is auth, should have high probability of using one,
            //as without auth we would do little.
            return randomness.choose(authentications)
        }
    }

    protected fun setupAuthentication(infoDto: SutInfoDto) {

        val info = infoDto.infoForAuthentication ?: return

        info.forEach { i ->
            if (i.name == null || i.name.isBlank()) {
                log.warn("Missing name in authentication info")
                return@forEach
            }

            val headers: MutableList<AuthenticationHeader> = mutableListOf()

            i.headers.forEach loop@{ h ->
                val name = h.name?.trim()
                val value = h.value?.trim()
                if (name == null || value == null) {
                    log.warn("Invalid header in ${i.name}")
                    return@loop
                }

                headers.add(AuthenticationHeader(name, value))
            }

            val cookieLogin = if(i.cookieLogin != null){
                CookieLogin.fromDto(i.cookieLogin)
            } else {
                null
            }

            val auth = AuthenticationInfo(i.name.trim(), headers, cookieLogin)

            authentications.add(auth)
        }
    }

    protected fun updateConfigForTestOutput(infoDto: SutInfoDto) {
        if (config.outputFormat == OutputFormat.DEFAULT) {
            try {
                val format = OutputFormat.valueOf(infoDto.defaultOutputFormat?.toString()!!)
                config.outputFormat = format
            } catch (e: Exception) {
                throw SutProblemException("Failed to use test output format: " + infoDto.defaultOutputFormat)
            }
        }
    }
}
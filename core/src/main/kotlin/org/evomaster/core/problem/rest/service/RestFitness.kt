package org.evomaster.core.problem.rest.service

import com.google.inject.Inject
import org.evomaster.client.java.controller.api.dto.ActionDto
import org.evomaster.client.java.instrumentation.shared.ObjectiveNaming
import org.evomaster.core.database.DbActionTransformer
import org.evomaster.core.logging.LoggingUtil
import org.evomaster.core.problem.rest.RestAction
import org.evomaster.core.problem.rest.RestCallAction
import org.evomaster.core.problem.rest.RestCallResult
import org.evomaster.core.problem.rest.RestIndividual
import org.evomaster.core.remote.service.RemoteController
import org.evomaster.core.search.ActionResult
import org.evomaster.core.search.EvaluatedIndividual
import org.evomaster.core.search.FitnessValue
import org.evomaster.core.search.gene.StringGene
import org.evomaster.core.search.gene.regex.RegexGene
import org.evomaster.core.taint.TaintAnalysis
import org.slf4j.Logger
import org.slf4j.LoggerFactory

open class RestFitness : AbstractRestFitness<RestIndividual>() {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(RestFitness::class.java)
    }

    @Inject(optional = true)
    private lateinit var rc: RemoteController

    @Inject
    private lateinit var sampler: RestSampler

    override fun doCalculateCoverage(individual: RestIndividual, targets: Set<Int>): EvaluatedIndividual<RestIndividual>? {

        rc.resetSUT()

        val cookies = getCookies(individual)

        doInitializingActions(individual)

        //individual.enforceCoherence()

        val fv = FitnessValue(individual.size().toDouble())

        val actionResults: MutableList<ActionResult> = mutableListOf()

        //used for things like chaining "location" paths
        val chainState = mutableMapOf<String, String>()

        //run the test, one action at a time
        for (i in 0 until individual.seeActions().size) {

            val a = individual.seeActions()[i]

            registerNewAction(a, i)

            var ok = false

            if (a is RestCallAction) {
                ok = handleRestCall(a, actionResults, chainState, cookies)
            } else {
                throw IllegalStateException("Cannot handle: ${a.javaClass}")
            }

            if (!ok) {
                break
            }
        }

        if(actionResults.any { it is RestCallResult && it.getTcpProblem() }){
            /*
                If there are socket issues, we avoid trying to compute any coverage.
                The caller might restart the SUT and try again.
                Hopefully, this should be just a glitch...
                TODO if we see this happening often, we need to find a proper solution.
                For example, we could re-run the test, and see if this one always fails,
                while others in the archive do pass.
                It could be handled specially in the archive.
             */
            return null
        }


        val ids = targetsToEvaluate(targets, individual)

        val dto = rc.getTestResults(ids)
        if (dto == null) {
            log.warn("Cannot retrieve coverage")
            return null
        }

        dto.targets.forEach { t ->

            if (t.descriptiveId != null) {

                val noMethodReplacement = !config.useMethodReplacement && t.descriptiveId.startsWith(ObjectiveNaming.METHOD_REPLACEMENT)
                val noNonIntegerReplacement = !config.useNonIntegerReplacement && t.descriptiveId.startsWith(ObjectiveNaming.NUMERIC_COMPARISON)

                if (noMethodReplacement || noNonIntegerReplacement) {
                    return@forEach
                }

                idMapper.addMapping(t.id, t.descriptiveId)
            }

            fv.updateTarget(t.id, t.value, t.actionIndex)
        }

        handleExtra(dto, fv)

        handleResponseTargets(fv, individual.seeActions(), actionResults, dto.additionalInfoList)

        if (config.expandRestIndividuals) {
            expandIndividual(individual, dto.additionalInfoList, actionResults)
        }

        if (config.baseTaintAnalysisProbability > 0) {
            assert(actionResults.size == dto.additionalInfoList.size)
            //TODO add taint analysis for resource-based solution
            TaintAnalysis.doTaintAnalysis(individual, dto.additionalInfoList, randomness)
        }

        return EvaluatedIndividual(fv, individual.copy() as RestIndividual, actionResults, trackOperator = individual.trackOperator, index = time.evaluatedIndividuals, config = config)
    }

    private fun registerNewAction(action: RestAction, index: Int){

        rc.registerNewAction(ActionDto().apply {
            this.index = index
            //for now, we only include specialized regex
            this.inputVariables = action.seeGenes()
                    .flatMap { it.flatView() }
                    .filterIsInstance<StringGene>()
                    .filter { it.getSpecializationGene() != null && it.getSpecializationGene() is RegexGene}
                    .map { it.getSpecializationGene()!!.getValueAsRawString()}
        })
    }



    override fun doInitializingActions(ind: RestIndividual) {

        if (ind.seeInitializingActions().none { !it.representExistingData }) {
            /*
                We are going to do an initialization of database only if there
                is data to add.
                Note that current data structure also keeps info on already
                existing data (which of course should not be re-inserted...)
             */
            return
        }

        val dto = DbActionTransformer.transform(ind.seeInitializingActions())

        val ok = rc.executeDatabaseCommand(dto)
        if (!ok) {
            //this can happen if we do not handle all constraints
            LoggingUtil.uniqueWarn(log, "Failed in executing database command")
        }
    }

    override fun hasParameterChild(a: RestCallAction): Boolean {
        return sampler.seeAvailableActions()
                .filterIsInstance<RestCallAction>()
                .map { it.path }
                .any { it.isDirectChildOf(a.path) && it.isLastElementAParameter() }
    }
}
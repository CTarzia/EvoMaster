package org.evomaster.core.search

import org.evomaster.core.EMConfig
import org.evomaster.core.search.gene.*
import org.evomaster.core.search.impact.impactinfocollection.*
import org.evomaster.core.search.service.mutator.MutatedGeneSpecification
import org.evomaster.core.search.tracer.Traceable
import org.evomaster.core.search.tracer.TraceableElementCopyFilter
import org.evomaster.core.search.tracer.TrackOperator
import org.evomaster.core.Lazy
import org.evomaster.core.search.action.Action
import org.evomaster.core.sql.SqlAction
import org.evomaster.core.sql.SqlActionResult
import org.evomaster.core.logging.LoggingUtil
import org.evomaster.core.mongo.MongoDbAction
import org.evomaster.core.problem.externalservice.ApiExternalServiceAction
import org.evomaster.core.problem.rest.RestCallAction
import org.evomaster.core.problem.rest.RestCallResult
import org.evomaster.core.problem.rest.RestIndividual
import org.evomaster.core.problem.rest.resource.ResourceImpactOfIndividual
import org.evomaster.core.search.Individual.GeneFilter
import org.evomaster.core.search.action.ActionFilter.*
import org.evomaster.core.search.action.ActionResult
import org.evomaster.core.search.service.mutator.EvaluatedMutation
import org.evomaster.core.search.tracer.TrackingHistory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

/**
 * EvaluatedIndividual allows to tracking its evolution.
 * Note that tracking EvaluatedIndividual can be enabled by set EMConfig.enableTrackEvaluatedIndividual true.
 */
class EvaluatedIndividual<T>(
    val fitness: FitnessValue,
    val individual: T,
    /**
     * Note: as the test execution could had been
     * prematurely stopped, there might be less
     * results than actions
     */
    val results: List<out ActionResult>,

    // for tracking its history
    override var trackOperator: TrackOperator? = null,
    override var index: Int = Traceable.DEFAULT_INDEX,

    // for impact
    val impactInfo: ImpactsOfIndividual? = null

) : Traceable where T : Individual {

    override var evaluatedResult: EvaluatedMutation? = null

    override var tracking: TrackingHistory<out Traceable>? = null

    companion object {
        const val ONLY_TRACKING_INDIVIDUAL_OF_EVALUATED = "ONLY_TRACKING_INDIVIDUAL_OF_EVALUATED"
        const val WITH_TRACK_WITH_CLONE_IMPACT = "WITH_TRACK_WITH_CLONE_IMPACT"
        const val WITH_TRACK_WITH_COPY_IMPACT = "WITH_TRACK_WITH_COPY_IMPACT"
        const val ONLY_WITH_COPY_IMPACT = "ONLY_WITH_COPY_IMPACT"
        const val ONLY_WITH_CLONE_IMPACT = "ONLY_WITH_CLONE_IMPACT"

        private val log: Logger = LoggerFactory.getLogger(EvaluatedIndividual::class.java)
    }

    /**
     * [hasImprovement] represents if [this] helps to improve Archive, e.g., reach new target.
     */
    var hasImprovement = false

    val clusterAssignments: MutableSet<String> = mutableSetOf()

    /**
     * How long it took to evaluate this individual.
     *
     * Note: could had it better to setup this value in the constructor, but it would had
     * led to quite a lot of refactoring. Furthermore, execution time is not fully deterministic,
     * and could make sense to re-update it when re-evaluating
     */
    var executionTimeMs: Long
        get() = fitness.executionTimeMs
        set(value) {
            fitness.executionTimeMs = value
        }

    init {
        if (individual.seeActions(ALL).size < results.size) {
            throw IllegalArgumentException("Less actions (${individual.seeActions(ALL).size}) than results (${results.size})")
        }
        if (!individual.isInitialized()) {
            throw IllegalArgumentException("Individual is not initialized")
        }
    }

    constructor(
        fitness: FitnessValue,
        individual: T,
        results: List<out ActionResult>,
        trackOperator: TrackOperator? = null,
        index: Int = -1,
        config: EMConfig
    ) :
            this(
                fitness, individual, results,
                trackOperator = trackOperator,
                index = index,
                impactInfo = if ((config.isEnabledImpactCollection())) {
                    if (individual is RestIndividual && config.isEnabledResourceDependency())
                        ResourceImpactOfIndividual(individual, config.abstractInitializationGeneToMutate, fitness)
                    else
                        ImpactsOfIndividual(individual, config.abstractInitializationGeneToMutate, fitness)
                } else
                    null
            )

    fun copy(): EvaluatedIndividual<T> {

        val ei = EvaluatedIndividual(
            fitness.copy(),
            individual.copy() as T,
            results.map(ActionResult::copy),
            trackOperator,
            index
        )
        ei.executionTimeMs = executionTimeMs
        ei.hasImprovement = hasImprovement
        ei.clusterAssignments.addAll(clusterAssignments)

        return ei
    }

    /**
     * @return action results based on the specified [actions].
     *      Note that if [actions] is null, then we employ individual.seeActions() as default
     */
    fun seeResults(actions: List<Action>? = null): List<ActionResult> {
        if (results.isEmpty()) {
            return listOf()
        }
        val list = actions ?: individual.seeActions(NO_EXTERNAL_SERVICE)
        val all = individual.seeActions(NO_EXTERNAL_SERVICE)
        val last = results.indexOfFirst { it.stopping }
        return list.mapNotNull {
            val index = all.indexOf(it)
            if (last == -1 || index <= last)
                results[index]
            else null
        }
    }

    /**
     * Note: if a test execution was prematurely stopped,
     * the number of evaluated actions would be lower than
     * the total number of actions
     */
    fun evaluatedMainActions(): List<EvaluatedAction> {

        val list: MutableList<EvaluatedAction> = mutableListOf()

        /*
            TODO unclear whether here we just need main actions, are all (eg for DB)
         */

        val actions = individual.seeMainExecutableActions()
        val actionResults = seeResults(actions)

        (0 until actionResults.size).forEach { i ->
            list.add(EvaluatedAction(actions[i], actionResults[i]))
        }

        return list
    }

    /**
     * @return grouped evaluated actions based on its resource structure
     *      first are db actions and their results
     *      second are rest actions and their results
     */
    fun evaluatedResourceActions(): List<Pair<List<EvaluatedDbAction>, List<EvaluatedAction>>> {
        if (individual !is RestIndividual)
            throw IllegalStateException("the method do not support the individual with the type: ${individual::class.java.simpleName}");

        val list = mutableListOf<Pair<List<EvaluatedDbAction>, List<EvaluatedAction>>>();

        individual.getResourceCalls().forEach { c ->
            val dbActions = c.seeActions(ONLY_SQL)
            val dbResults = seeResults(dbActions)

            // TODO: with the current ActionFilter seeActions returns everything except DbActions
            //  so filtering only the RestCallAction/s for now. It's a temporary fix.
            val restActions = c.seeActions(NO_SQL).filterIsInstance<RestCallAction>()
            val restResult = seeResults(restActions)

            // get evaluated action based on the list of action results
            list.add(
                dbResults.mapIndexed { index, actionResult ->
                    EvaluatedDbAction(
                        (dbActions[index] as? SqlAction)
                            ?: throw IllegalStateException("mismatched action type, expected is DbAction but it is ${dbActions[index]::class.java.simpleName}"),
                        (actionResult as? SqlActionResult)
                            ?: throw IllegalStateException("mismatched action result type, expected is DbActionResult but it is ${actionResult::class.java.simpleName}")
                    )
                } to restResult.mapIndexed { index, actionResult ->
                    EvaluatedAction(
                        (restActions[index] as? RestCallAction)
                            ?: throw IllegalStateException("mismatched action type, expected is RestCallAction but it is ${restActions[index]::class.java.simpleName}"),
                        (actionResult as? RestCallResult)
                            ?: throw IllegalStateException("mismatched action result type, expected is RestCallResult but it is ${actionResult::class.java.simpleName}")
                    )
                }
            )
        }
        return list
    }

    override fun copy(copyFilter: TraceableElementCopyFilter): EvaluatedIndividual<T> {
        if (copyFilter.name == ONLY_TRACKING_INDIVIDUAL_OF_EVALUATED) {
            return EvaluatedIndividual(
                fitness.copy(),
                individual.copy(TraceableElementCopyFilter.WITH_TRACK) as T,
                results.map(ActionResult::copy),
                trackOperator
            )
        }

        when (copyFilter) {
            TraceableElementCopyFilter.NONE -> return copy()
            TraceableElementCopyFilter.WITH_ONLY_EVALUATED_RESULT -> {
                return copy().also {
                    it.wrapWithEvaluatedResults(evaluatedResult)
                }
            }
            TraceableElementCopyFilter.DEEP_TRACK -> throw IllegalArgumentException("there is no need to track individual when evaluated individual is tracked")
            TraceableElementCopyFilter.WITH_TRACK -> {
                // the copy includes tracking info, but it is no need to include tracking info for the element in the tracking.
                return copy().also { it.wrapWithTracking(evaluatedResult, trackingHistory = tracking) }
            }
            else -> {
                return EvaluatedIndividual(
                    fitness.copy(),
                    individual.copy() as T,
                    results.map(ActionResult::copy),
                    trackOperator,
                    index,
                    when (copyFilter.name) {
                        ONLY_WITH_COPY_IMPACT, WITH_TRACK_WITH_COPY_IMPACT -> {
                            impactInfo?.copy()
                        }
                        ONLY_WITH_CLONE_IMPACT, WITH_TRACK_WITH_CLONE_IMPACT -> {
                            impactInfo?.clone()
                        }
                        else -> throw IllegalStateException("${copyFilter.name} is not available")
                    }
                ).also {
                    if (copyFilter.name == WITH_TRACK_WITH_COPY_IMPACT
                        || copyFilter.name == WITH_TRACK_WITH_CLONE_IMPACT
                    ) {
                        it.wrapWithTracking(evaluatedResult, trackingHistory = tracking)
                    }
                }
            }
        }

    }

    fun nextForIndividual(next: Traceable, evaluatedResult: EvaluatedMutation): EvaluatedIndividual<T>? {
        (next as? EvaluatedIndividual<T>) ?: throw IllegalArgumentException("mismatched tracking element")

        val nextIndividual = individual.next(next.individual, TraceableElementCopyFilter.WITH_TRACK, evaluatedResult)!!

        return EvaluatedIndividual(
            next.fitness.copy(),
            (nextIndividual as T).copy(TraceableElementCopyFilter.WITH_TRACK) as T,
            next.results.map(ActionResult::copy),
            next.trackOperator,
            next.index,
            impactInfo?.clone()
        )
    }

    override fun next(
        next: Traceable,
        copyFilter: TraceableElementCopyFilter,
        evaluatedResult: EvaluatedMutation
    ): EvaluatedIndividual<T>? {

        tracking ?: throw IllegalStateException("cannot create next due to unavailable tracking info")
        (next as? EvaluatedIndividual<T>) ?: throw IllegalArgumentException("mismatched tracking element")

        val nextInTracking = next.copy(copyFilter)
        nextInTracking.wrapWithEvaluatedResults(evaluatedResult)
        pushLatest(nextInTracking)

        val new = EvaluatedIndividual(
            next.fitness.copy(),
            next.individual.copy() as T,
            next.results.map(ActionResult::copy),
            next.trackOperator,
            next.index,
            impactInfo = impactInfo?.clone()
        )

        // tracking is shared with all mutated individual originated from same sampled ind
        new.wrapWithTracking(evaluatedResult, tracking)

        return new
    }


    /**
     * compare current with latest
     * [inTrack] indicates how to find the latest two elements to compare.
     * For instance, if the latest modification does not improve the fitness, it will be saved in [undoTracking].
     * in this case, the latest is the last of [undoTracking], not [this]
     */
    fun updateImpactOfGenes(
        previous: EvaluatedIndividual<T>,
        mutated: EvaluatedIndividual<T>,
        mutatedGenes: MutatedGeneSpecification,
        targetsInfo: Map<Int, EvaluatedMutation>
    ) {

        Lazy.assert {
            mutatedGenes.mutatedIndividual != null && tracking != null
        }

        if (previous.getSizeOfImpact(false) != mutated.getSizeOfImpact(false)) {
            LoggingUtil.uniqueWarn(log, "impacts should be same before updating")
        }

        compareWithLatest(next = mutated, previous = previous, targetsInfo = targetsInfo, mutatedGenes = mutatedGenes)
    }

    private fun verifyImpacts() {
        impactInfo?.verifyFixedActionGeneImpacts(individual)
    }

    private fun compareWithLatest(
        next: EvaluatedIndividual<T>,
        previous: EvaluatedIndividual<T>,
        targetsInfo: Map<Int, EvaluatedMutation>,
        mutatedGenes: MutatedGeneSpecification
    ) {

        val noImpactTargets = targetsInfo.filterValues { !it.isImpactful() }.keys
        val impactTargets = targetsInfo.filterValues { it.isImpactful() }.keys
        val improvedTargets = targetsInfo.filterValues { it.isImproved() }.keys

        val didStructureMutation = mutatedGenes.didStructureMutation()
        if (didStructureMutation) { // structure mutated
            updateImpactsAfterStructureMutation(
                next,
                previous.individual,
                mutatedGenes,
                noImpactTargets,
                impactTargets,
                improvedTargets
            )
            verifyImpacts()
            return
        }

        impactInfo!!.syncBasedOnIndividual(individual)

        if (mutatedGenes.addedInitializationGenes.isNotEmpty()) {
            //TODO there is no any impact with added initialization, we may record this case.
            return
        }

        //we only sync impact info according to latest individual when next is this
        syncImpact(previous.individual, mutatedGenes.mutatedIndividual!!)

        updateImpactsAfterStandardMutation(
            previous = previous.individual,
            mutatedGenes = mutatedGenes,
            noImpactTargets = noImpactTargets,
            impactTargets = impactTargets,
            improvedTargets = improvedTargets
        )
    }

    private fun updateImpactsAfterStructureMutation(
        next: EvaluatedIndividual<T>,
        previous: Individual,
        mutatedGenes: MutatedGeneSpecification,
        noImpactTargets: Set<Int>,
        impactTargets: Set<Int>,
        improvedTargets: Set<Int>
    ) {
        Lazy.assert { impactInfo != null }
        val sizeChanged =
            (mutatedGenes.mutatedIndividual!!.seeActions(NO_INIT).size != previous.seeActions(NO_INIT).size)

        //we update genes impact regarding structure only if structure mutated individual is 'next'
        if (this.index == next.index) {

            //remove a number of resource with sql
            if (mutatedGenes.removedSqlActions.isNotEmpty()) {
                impactInfo!!.removeInitializationImpacts(
                    mutatedGenes.removedSqlActions,
                    individual.seeInitializingActions().count { it is SqlAction && it.representExistingData })
            }

            if (mutatedGenes.addedSqlActions.isNotEmpty()) {
                impactInfo!!.appendInitializationImpacts(mutatedGenes.addedSqlActions)
            }

            /*
                dynamic action should not be involved in the structure mutator
                might later add some checks in order to ensture it
             */


            //handle removed
            if (mutatedGenes.getRemoved(true).isNotEmpty()) { //delete an action
                val fixedIndexed = mutatedGenes.getRemoved(true).filter { it.actionPosition != null && it.actionPosition > 0 }.map { it.actionPosition!! }.toSet()
                impactInfo!!.deleteFixedActionGeneImpacts(fixedIndexed)

                val dynamicLocalIds = mutatedGenes.getRemoved(true)
                    .filter { it.actionPosition == null || it.actionPosition < 0 }
                    .map { it.localId ?:throw IllegalStateException("the mutated info is lack of position and local id") }.toSet()

                impactInfo.deleteDynamicActionGeneImpacts(
                    dynamicLocalIds
                )
            }

            //handle added
            val fixedInMutatedInd = mutatedGenes.mutatedIndividual!!.seeFixedMainActions()

            if (mutatedGenes.getAdded(true).isNotEmpty()) { //add new action
                val addedGenes = mutatedGenes.getAdded(true)
                //handle added actions with genes
                val groupGeneByActionIndex = addedGenes.filter { it.gene != null }.groupBy { g ->
                    fixedInMutatedInd.find { a -> a.seeTopGenes().contains(g.gene) }
                        .run { fixedInMutatedInd.indexOf(this) }
                }

                //handle added actions without genes
                val emptyActions =
                    addedGenes.filter { it.gene == null }.mapNotNull { it.actionPosition }.toSet().sorted()

                addedGenes.mapNotNull { it.actionPosition }.toSet().sorted().forEach { actionIndex ->
                    val action = individual.seeFixedMainActions()[actionIndex]
                    if (emptyActions.contains(actionIndex)) {
                        impactInfo!!.addOrUpdateMainActionGeneImpacts(
                            localId = action.getLocalId(),
                            fixedIndexedAction = true,
                            actionName = action.getName(),
                            actionIndex = actionIndex,
                            newAction = true,
                            impacts = mutableMapOf()
                        )
                    } else {
                        val mgenes = groupGeneByActionIndex.getValue(actionIndex)
                        val index = mgenes.mapNotNull { it.actionPosition }.toSet()
                        if (index.size != 1 || index.first() != actionIndex)
                            throw IllegalArgumentException("mismatched impact info: genes should be mutated at $index action, but actually the index is $actionIndex")
                        impactInfo!!.addOrUpdateMainActionGeneImpacts(
                            localId = action.getLocalId(),
                            fixedIndexedAction = true,
                            actionIndex = actionIndex,
                            actionName = action.getName(),
                            impacts = mgenes.map { g ->
                                g.gene ?: throw IllegalStateException("Added gene is not recorded")
                                val id = ImpactUtils.generateGeneId(mutatedGenes.mutatedIndividual!!, g.gene)
                                id to ImpactUtils.createGeneImpact(g.gene, id)
                            }.toMap().toMutableMap(),
                            newAction = true
                        )
                    }
                }
            }

            //handle swap
            if (mutatedGenes.getSwap().isNotEmpty()) {
                if (mutatedGenes.getSwap().size > 1)
                    throw IllegalStateException("the swap mutator is applied more than one times, i.e., ${mutatedGenes.getSwap().size}")

                val swap = mutatedGenes.getSwap().first()
                val from = swap.from ?: throw IllegalStateException("the resourcePosition is missing")
                val to = swap.to ?: throw IllegalStateException("the swapToResourcePosition is missing")
                impactInfo!!.swapFixedActionGeneImpact(from, to)
            }

            /*
                actions might be changed due to dependency handling or db repairing
                e.g., ind A is (table_a, table_b, resource_b)
                if added resource_b at the beginning, mutated ind A (table_a, resource_a, table_a, table_b, resource_b)
                in this case, we might remove second table_a, thus the mutated ind A becomes
                (table_a, resource_a, table_b, resource_b), and the table_b refers to the table_a in the front of resource_a
             */
            var fix = impactInfo!!.findFirstMismatchedIndexForFixedMainActions(individual.seeFixedMainActions())
            while (fix.first != -1) {
                if (fix.second!!) {
                    impactInfo.deleteFixedActionGeneImpacts(setOf(fix.first))
                } else {
                    val action = individual.seeFixedMainActions()[fix.first]
                    impactInfo.addOrUpdateMainActionGeneImpacts(
                        localId = action.getLocalId(),
                        fixedIndexedAction = true,
                        actionName = action.getName(),
                        actionIndex = fix.first,
                        newAction = true,
                        impacts = mutableMapOf()
                    )
                }
                val nextFix = impactInfo.findFirstMismatchedIndexForFixedMainActions(individual.seeFixedMainActions())
                if (nextFix.first < fix.first) {
                    if (nextFix.first != -1)
                        log.warn(
                            "the fix at {} with remove/add ({}) does not work, and the next fix is at {}",
                            fix.first,
                            fix.second,
                            nextFix.first
                        )
                    break
                }
                fix = nextFix
            }
        }
        impactInfo!!.impactsOfStructure.countImpact(
            next,
            sizeChanged,
            noImpactTargets = noImpactTargets,
            impactTargets = impactTargets,
            improvedTargets = improvedTargets
        )

        if (impactInfo is ResourceImpactOfIndividual) {
            // count impact of changing size of resource
            impactInfo.countResourceSizeImpact(
                previous as RestIndividual,
                current = next.individual as RestIndividual,
                noImpactTargets = noImpactTargets,
                impactTargets = impactTargets,
                improvedTargets = improvedTargets
            )
        }
    }


    private fun updateImpactsAfterStandardMutation(
        previous: Individual,
        mutatedGenes: MutatedGeneSpecification,
        noImpactTargets: Set<Int>,
        impactTargets: Set<Int>,
        improvedTargets: Set<Int>
    ) {

        if (mutatedGenes.didAddInitializationGenes()) {
            Lazy.assert {
                impactInfo!!.getSQLExistingData() == individual.seeInitializingActions()
                    .count { it is SqlAction && it.representExistingData }
            }
        }

        var updated = 0
        // update rest action genes and/or sql genes
        mutatedGenes.mutatedActionOrInit().forEach { isInit ->
            val mutatedGenesWithContext = ImpactUtils.extractMutatedGeneWithContext(
                mutatedGenes, mutatedGenes.mutatedIndividual!!, previousIndividual = previous, isInit = isInit
            )

            updated += mutatedGenesWithContext.size

            mutatedGenesWithContext.forEach { gc ->
                val impact = impactInfo!!.getGene(
                    localId = gc.actionLocalId,
                    fixedIndexedAction = !gc.isDynamicAction,
                    actionName = gc.action,
                    actionIndex = gc.position,
                    geneId = ImpactUtils.generateGeneId(mutatedGenes.mutatedIndividual!!, gc.current),
                    fromInitialization = isInit
                ) ?: throw IllegalArgumentException("mismatched impact info")

                impact.countImpactWithMutatedGeneWithContext(
                    gc,
                    noImpactTargets = noImpactTargets,
                    impactTargets = impactTargets,
                    improvedTargets = improvedTargets,
                    onlyManipulation = false
                )
            }
        }

        Lazy.assert { updated == mutatedGenes.numOfMutatedGeneInfo() }
    }

    /**
     * sync impact info based on [mutated]
     */
    private fun syncImpact(previous: Individual, mutated: Individual) {
        // db action
        mutated.seeInitializingActions().forEachIndexed { index, action ->
            action.seeTopGenes().filter { it.isMutable() }.forEach { sg ->
                val rootGeneId = ImpactUtils.generateGeneId(mutated, sg)
                val p = previous.seeInitializingActions().getOrNull(index)?.seeTopGenes()?.find {
                    rootGeneId == ImpactUtils.generateGeneId(previous, it)
                }
                if (p != null){
                    impactInfo!!.getGene(
                        localId = action.getLocalId(),
                        fixedIndexedAction = false,
                        actionName = action.getName(),
                        actionIndex = index,
                        fromInitialization = true,
                        geneId = rootGeneId
                    )?.syncImpact(p, sg)
                }
            }
        }

        // fixed action
        mutated.seeFixedMainActions().forEachIndexed { index, action ->
            action.seeTopGenes().filter { it.isMutable() }.forEach { sg ->
                val rootGeneId = ImpactUtils.generateGeneId(mutated, sg)

                val p = previous.seeFixedMainActions()[index].seeTopGenes().find {
                    rootGeneId == ImpactUtils.generateGeneId(previous, it)
                }
                val impact = impactInfo!!.getGene(
                    localId = action.getLocalId(),
                    fixedIndexedAction = true,
                    actionName = action.getName(),
                    actionIndex = index,
                    fromInitialization = false,
                    geneId = rootGeneId
                )
                    ?: throw IllegalArgumentException("fail to identify impact info for the gene $rootGeneId at $index of actions")
                impact.syncImpact(p, sg)
            }
        }

        // dynamic action
        mutated.seeDynamicMainActions().forEach { daction ->
            val pAction = previous.findActionByLocalId(daction.getLocalId())
            if (pAction != null) {
                daction.seeTopGenes().filter { it.isMutable() }.forEach { sg ->
                    val rootGeneId = ImpactUtils.generateGeneId(mutated, sg)

                    val p = pAction.seeTopGenes().find {
                        rootGeneId == ImpactUtils.generateGeneId(previous, it)
                    }
                    val impact = impactInfo!!.getGene(
                        localId = daction.getLocalId(),
                        fixedIndexedAction = false,
                        actionName = daction.getName(),
                        actionIndex = null,
                        fromInitialization = false,
                        geneId = rootGeneId
                    ) ?: throw IllegalArgumentException("fail to identify impact info for the gene $rootGeneId with localid ${daction.getLocalId()}")
                    impact.syncImpact(p, sg)
                }
            }
        }
    }

    //**************** for impact *******************//


    fun anyImpactfulGene(): Boolean {
        impactInfo ?: return false
        return impactInfo.anyImpactfulInfo()
    }

    /**
     *  gene impact can be added only if the gene is root gene
     *  this is to handle unclassified, eg, a gene might be empty gson or a gene constrained with some class
     */
    fun addGeneImpact(individual: Individual, gene: Gene): GeneImpact? {
        val actions = individual.seeActions(NO_INIT)

        val action = actions.find {
            it.seeTopGenes().contains(gene)
        }
        if (action == null && !individual.seeGenes().contains(gene)) return null

        val isFixed = individual.seeFixedMainActions().contains(action)
        val index = if (isFixed) individual.seeFixedMainActions().indexOf(action) else -1

        val geneId = ImpactUtils.generateGeneId(individual, gene)
        val impact = ImpactUtils.createGeneImpact(gene, geneId)

        impactInfo?.addOrUpdateMainActionGeneImpacts(
            localId = action!!.getLocalId(),
            fixedIndexedAction = isFixed,
            actionName = action.getName(),
            actionIndex = index,
            newAction = false,
            impacts = mutableMapOf(geneId to ImpactUtils.createGeneImpact(gene, geneId))
        )
        return impact
    }

    fun getImpact(individual: Individual, gene: Gene): GeneImpact? {
        impactInfo ?: return null

        val id = ImpactUtils.generateGeneId(individual, gene)
        var action = individual.seeActions(NO_INIT).find { it.seeTopGenes().contains(gene) }

        val index = individual.seeFixedMainActions().indexOf(action)

        if (action != null) {
            return impactInfo.getGene(
                localId = action.getName(),
                fixedIndexedAction = index > 0,
                actionName = action.getName(),
                actionIndex = index,
                geneId = id,
                fromInitialization = false
            )
        }

        initializingActionClasses().forEach { initializingActionClass ->
            action = individual.seeInitializingActions().filter { initializingActionClass.isInstance(it)}
                .find { it.seeTopGenes().contains(gene) }

            if (action != null) {
                action as Action
                return impactInfo.getGene(
                    localId = null,
                    fixedIndexedAction = true,
                    actionName = action!!.getName(),
                    actionIndex = individual.seeInitializingActions().indexOf(action),
                    geneId = id,
                    fromInitialization = true
                )
            }
        }

        return impactInfo.getGene(
            localId = null,
            fixedIndexedAction = false,
            actionName = null,
            actionIndex = null,
            geneId = id,
            fromInitialization = individual.seeGenes(GeneFilter.ONLY_SQL).contains(gene)
        )
    }

    fun updateImpactGeneDueToAddedExternalService(
        mutatedGenes: MutatedGeneSpecification,
        addedExActions: List<ApiExternalServiceAction>
    ){
        impactInfo ?: throw IllegalStateException("there is no any impact initialized when adding impacts for external service actions")
        addedExActions.forEach { e->
            impactInfo.addOrUpdateMainActionGeneImpacts(
                localId = e.getLocalId(),
                fixedIndexedAction = false,
                actionName = e.getName(),
                actionIndex = -1,
                newAction = true,
                impacts = mutableMapOf()
            )
        }
        mutatedGenes.addedExternalServiceActions.addAll(addedExActions)

    }

    //TODO check this when integrating with SQL resource handling
    fun updateImpactGeneDueToAddedInitializationGenes(
        mutatedGenes: MutatedGeneSpecification,
        old: List<Action>,
        addedInsertions: List<List<Action>>?
    ) {
        impactInfo ?: throw IllegalStateException("there is no any impact initialized")

        val allExistingData = individual.seeInitializingActions().filter { it is SqlAction && it.representExistingData }
        val diff = individual.seeInitializingActions()
            .filter { !old.contains(it) && ((it is SqlAction && !it.representExistingData) || it is MongoDbAction) }

        if (allExistingData.isNotEmpty())
            impactInfo.updateExistingSQLData(allExistingData.size)

        if (diff.isEmpty()) {
            return
        }
        mutatedGenes.addedInitializationGenes.addAll(diff.flatMap { it.seeTopGenes() })

        // update impact due to newly added initialization actions
        val modified = if (addedInsertions!!.flatten().size == diff.size)
            addedInsertions
        else if (addedInsertions.flatten().size > diff.size) {
            addedInsertions.mapNotNull {
                val m = it.filter { a -> diff.contains(a) }
                if (m.isEmpty()) null else m
            }
        } else {
            log.warn("unexpected handling on Initialization Action after repair")
            return
        }

        if (modified.flatten().size != diff.size) {
            log.warn("unexpected handling on Initialization Action")
            return
        }

        if (old.isEmpty()) {
            initAddedInitializationGenes(modified, allExistingData.size)
        } else {
            impactInfo.updateInitializationImpactsAtEnd(modified, allExistingData.size)
        }

        mutatedGenes.addedInitializationGroup.addAll(modified)

        Lazy.assert {
            individual.seeInitializingActions()
                .filter { (it is SqlAction && !it.representExistingData) || it is MongoDbAction }.size == impactInfo.getSizeOfActionImpacts(true)
        }
    }


    fun initAddedInitializationGenes(group: List<List<Action>>, existingSize: Int) {
        impactInfo!!.initInitializationImpacts(group, existingSize)
    }

    fun anyImpactInfo(): Boolean {
        impactInfo ?: return false
        return impactInfo.anyImpactInfo()
    }

    fun flattenAllGeneImpact(): List<GeneImpact> {
        impactInfo ?: return listOf()
        return impactInfo.flattenAllGeneImpact()
    }

    fun getSizeOfImpact(fromInitialization: Boolean): Int {
        impactInfo ?: return -1
        return impactInfo.getSizeOfActionImpacts(fromInitialization)
    }

    fun getImpactOfFixedAction(actionIndex: Int, fromInitialization: Boolean): MutableMap<String, GeneImpact>? {
        impactInfo ?: return null
        return impactInfo.findImpactsByAction(
            localId = null,
            fixedIndexedAction = true,
            actionIndex = actionIndex,
            actionName = if (fromInitialization)
                individual.seeInitializingActions()[actionIndex].getName()
            else individual.seeFixedMainActions()[actionIndex].getName(),
            fromInitialization = fromInitialization
        )
    }

    fun getInitializationGeneImpact(): List<MutableMap<String, GeneImpact>> {
        impactInfo ?: throw IllegalArgumentException("impact info is null")
        return impactInfo.getInitializationGeneImpact()
    }

    fun getActionGeneImpact(isFixed: Boolean = true): List<MutableMap<String, GeneImpact>> {
        impactInfo ?: throw IllegalArgumentException("impact info is null")
        return impactInfo.getMainActionGeneImpact(isFixed)
    }

    fun getGeneImpact(geneId: String): List<GeneImpact> {
        impactInfo ?: return listOf()
        return impactInfo.getGeneImpact(geneId)
    }

    fun exportImpactAsListString(targets: Set<Int>? = null): MutableList<String> {
        impactInfo ?: throw IllegalArgumentException("do not enable collecting impacts info")

        val content = mutableListOf<String>()

        impactInfo.exportImpactInfo(true, true, content, targets)
        impactInfo.exportImpactInfo(false, true, content, targets)
        impactInfo.exportImpactInfo(false, false, content, targets)
        return content
    }

    fun assignToCluster(cluster: String): EvaluatedIndividual<T> {
        clusterAssignments.add(cluster)
        return this
    }

    fun getClusters(): MutableSet<String> {
        return clusterAssignments
    }

    fun belongsToCluster(cluster: String): Boolean {
        return clusterAssignments.contains(cluster)
    }

    fun isValid(): Boolean {
        val index = results.indexOfFirst { it.stopping }
        val all = individual.seeActions(ALL)
        if (results.size > all.size) return false
        val invalid = (0 until (if (index == -1) index + 1 else results.size)).any {
            !results[it].matchedType(all[it])
        }
        return !invalid
    }
    private fun initializingActionClasses(): List<KClass<*>> {
        return listOf(MongoDbAction::class, SqlAction::class)
    }

    fun getRestCallResults(): List<RestCallResult> {
        return results.filterIsInstance<RestCallResult>()
    }
}
package org.evomaster.core.search

import org.evomaster.core.search.gene.Gene
import org.evomaster.core.search.service.Randomness
import org.evomaster.core.search.service.mutator.EvaluatedMutation
import org.evomaster.core.search.tracer.TraceableElement
import org.evomaster.core.search.tracer.TraceableElementCopyFilter
import org.evomaster.core.search.tracer.TrackOperator

/**
 * An individual for the search.
 * Also called "chromosome" in evolutionary algorithms.
 * In our context, most of the time an Individual will represent
 * a single test case, composed by 1 or more "actions" (eg, calls
 * to a RESTful API, SQL operations on a database or WireMock setup)
 *
 */
abstract class Individual(trackOperator: TrackOperator? = null, index : Int = DEFAULT_INDEX) : TraceableElement(trackOperator, index){

    /**
     * Make a deep copy of this individual
     */
    abstract fun copy(): Individual

    /**
     * Return a view of all the Genes in this chromosome/individual
     */
    abstract fun seeGenes(filter: GeneFilter = GeneFilter.ALL): List<out Gene>

    /**
     * An estimation of the "size" of this individual.
     * Longer/bigger individuals are usually considered worse,
     * unless they cover more coverage targets
     */
    abstract fun size(): Int

    /**
     * Return a view of all the "actions" defined in this individual.
     * Note: each action could be composed by 0 or more genes
     *
     * @return actions based on [filter] in this individual.
     *      By default ([ActionFilter.NO_INIT]), return actions excluding [seeInitializingActions]
     */
    abstract fun seeActions(filter: ActionFilter = ActionFilter.NO_INIT): List<out Action>

    /**
     * Return a view of all initializing actions done before the main
     * ones. Example: these could set up database before doing HTTP
     * calls toward the SUT.
     * A test does not require to have initializing actions.
     */
    open fun seeInitializingActions(): List<Action> = listOf()


    /**
     * Determine if the structure (ie the actions) of this individual
     * can be mutated (eg, add/remove actions).
     * Note: even if this is false, it would still be possible to
     * mutate the genes in those actions
     */
    open fun canMutateStructure() = false


    /**
     * Returns true if the initialization actions
     * are correct (i.e. all constraints are satisfied)
     */
    abstract fun verifyInitializationActions(): Boolean

    /**
     * Attempts to repair the initialization actions.
     * Initialization actions must pass the verifyInitializationAction()
     * test after this method is invoked.
     */
    abstract fun repairInitializationActions(randomness: Randomness)

    override fun copy(options: TraceableElementCopyFilter): TraceableElement {
        val copy = copy()
        when(options){
            TraceableElementCopyFilter.NONE -> return copy
            TraceableElementCopyFilter.WITH_TRACK->{
                copy.wrapWithTracking(evaluatedResult, tracking?.copy())
                return copy
            }
            TraceableElementCopyFilter.WITH_ONLY_EVALUATED_RESULT ->{
                copy.wrapWithEvaluatedResults(evaluatedResult)
                return copy
            }
            else -> throw IllegalArgumentException("NOT support $options")
        }
    }

    override fun next(next: TraceableElement, copyFilter: TraceableElementCopyFilter, evaluatedResult: EvaluatedMutation): TraceableElement? {
        tracking?: throw IllegalStateException("cannot create next due to unavailable tracking info")

        val nextInTracking = (next.copy(copyFilter) as Individual).also { this.wrapWithEvaluatedResults(evaluatedResult) }
        pushLatest(nextInTracking)

        val new = (next as Individual).copy()
//        new.wrapWithTracking(
//                evaluatedResult = evaluatedResult,
//                trackingHistory = tracking?.copy(copyFilter)
//        )

        new.wrapWithTracking(
                evaluatedResult = evaluatedResult,
                trackingHistory = tracking
        )

        return new
    }
    /**
     * @return whether this individual has same actions with [other]
     */
    open fun sameActions(other: Individual, excludeInitialization : Boolean = false) : Boolean{
        if (!excludeInitialization || seeInitializingActions().size != other.seeInitializingActions().size)
            return false
        if (seeActions().size != other.seeActions().size)
            return false
        if (!excludeInitialization || (seeInitializingActions().indices).any { seeInitializingActions()[it].getName() != other.seeInitializingActions()[it].getName() })
            return false
        if ((seeActions().indices).any { seeActions()[it].getName() != other.seeActions()[it].getName() })
            return false
        return true
    }

}


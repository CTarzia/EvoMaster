package org.evomaster.core.mongo

import org.evomaster.core.problem.rest.RestActionBuilderV3.createObjectGenesForDTOs
import org.evomaster.core.search.Action
import org.evomaster.core.search.gene.Gene
import org.evomaster.core.search.gene.ObjectGene
import org.evomaster.core.search.gene.mongo.ObjectIdGene
import java.util.*

class MongoDbAction(
    /**
     * The database to insert document into
     */
    val database: String,
    /**
     * The collection to insert document into
     */
    val collection: String,
    /**
     * The type of the new document. Should map the type of the documents of the collection
     */
    val documentsType: String,
    computedGenes: List<Gene>? = null
) : Action(listOf()) {

    private val genes: List<Gene> = (computedGenes ?: computeGenes()).also { addChildren(it) }

    private fun computeGenes(): List<Gene> {
        val documentsTypeName = documentsType.substringBefore(":").drop(1).dropLast(1)
        val gene = createObjectGenesForDTOs(
                documentsTypeName, documentsType, true
            )
        gene as ObjectGene
        return Collections.singletonList(ObjectGene(gene.name, gene.fixedFields + (Collections.singletonList(ObjectIdGene("_id")))))
    }

    override fun getName(): String {
        return "MONGO_Insert_${database}_${collection}_${documentsType}"
    }

    override fun seeTopGenes(): List<out Gene> {
        return genes
    }

    override fun shouldCountForFitnessEvaluations(): Boolean {
        return false
    }

    override fun copyContent(): Action {
        return MongoDbAction(database, collection, documentsType, genes.map(Gene::copy))
    }
}
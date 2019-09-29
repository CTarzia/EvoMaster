package org.evomaster.core.search.gene

import org.evomaster.core.output.OutputFormat
import org.evomaster.core.search.EvaluatedIndividual
import org.evomaster.core.search.impact.GeneImpact
import org.evomaster.core.search.impact.ImpactMutationSelection
import org.evomaster.core.search.impact.value.collection.MapGeneImpact
import org.evomaster.core.search.service.AdaptiveParameterControl
import org.evomaster.core.search.service.Randomness
import org.evomaster.core.search.service.mutator.geneMutation.ArchiveMutator
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class ArrayGene<T>(
        name: String,
        val template: T,
        val maxSize: Int = 5,
        var elements: MutableList<T> = mutableListOf()
) : Gene(name)
        where T : Gene {

    init {
        if (elements.size > maxSize) {
            throw IllegalArgumentException(
                    "More elements (${elements.size}) than allowed ($maxSize)")
        }
    }

    companion object{
        val log : Logger = LoggerFactory.getLogger(ArrayGene::class.java)
    }

    override fun copy(): Gene {
        return ArrayGene<T>(name,
                template.copy() as T,
                maxSize,
                elements.map { e -> e.copy() as T }.toMutableList()
        )
    }

    override fun copyValueFrom(other: Gene) {
        if (other !is ArrayGene<*>) {
            throw IllegalArgumentException("Invalid gene type ${other.javaClass}")
        }
        this.elements = other.elements.map { e -> e.copy() as T }.toMutableList()
    }

    override fun containsSameValueAs(other: Gene): Boolean {
        if (other !is ArrayGene<*>) {
            throw IllegalArgumentException("Invalid gene type ${other.javaClass}")
        }
        return this.elements.zip(other.elements) { thisElem, otherElem ->
            thisElem.containsSameValueAs(otherElem)
        }.all { it == true }
    }


    override fun isMutable(): Boolean {
        //it wouldn't make much sense to have 0, but let's just be safe here
        return maxSize > 0
    }

    override fun randomize(randomness: Randomness, forceNewValue: Boolean, allGenes: List<Gene>) {

        //maybe not so important here to complicate code to enable forceNewValue

        elements.clear()
        val n = randomness.nextInt(maxSize)
        (0 until n).forEach {
            val gene = template.copy() as T
            gene.randomize(randomness, false)
            elements.add(gene)
        }
    }

    override fun standardMutation(randomness: Randomness, apc: AdaptiveParameterControl, allGenes: List<Gene>) {

        if(elements.isEmpty() || (elements.size < maxSize && randomness.nextBoolean(0.1))){
            val gene = template.copy() as T
            gene.randomize(randomness, false)
            elements.add(gene)
        } else if(elements.size > 0 && randomness.nextBoolean(0.1)){
            elements.removeAt(randomness.nextInt(elements.size))
        } else {
            val gene = randomness.choose(elements)
            gene.standardMutation(randomness, apc, allGenes)
        }
    }

    override fun archiveMutation(
            randomness: Randomness,
            allGenes: List<Gene>,
            apc: AdaptiveParameterControl,
            selection: ImpactMutationSelection,
            impact: GeneImpact?,
            geneReference: String,
            archiveMutator: ArchiveMutator,
            evi: EvaluatedIndividual<*>
    ) {
        var add = elements.isEmpty()
        var delete = elements.size == maxSize

        val modifySize = if (impact != null && impact is MapGeneImpact && archiveMutator.enableArchiveSelection() && impact.sizeImpact.niCounter < 2){
            randomness.nextBoolean(0.3)
        }else {
            randomness.nextBoolean(0.1)
        }
        if (modifySize){
            val p = randomness.nextBoolean()
            add = add || p
            delete = delete || !p
        }

        if (add && add == delete)
            log.warn("add and delete an element cannot happen in a mutation")

        when{
            add ->{
                val gene = template.copy() as T
                gene.randomize(randomness, false)
                elements.add(gene)
            }
            delete ->{
                elements.removeAt(randomness.nextInt(elements.size))
            }
            else -> {
                val gene = randomness.choose(elements)
                gene.archiveMutation(randomness, allGenes, apc, selection, null, geneReference, archiveMutator, evi)
            }
        }
    }

    override fun getValueAsPrintableString(previousGenes: List<Gene>, mode: String?, targetFormat: OutputFormat?): String {
        return "[" +
                elements.map { g -> g.getValueAsPrintableString(previousGenes, mode, targetFormat) }.joinToString(", ") +
                "]"
    }


    override fun flatView(excludePredicate: (Gene) -> Boolean): List<Gene>{
        return if (excludePredicate(this)) listOf(this) else
            listOf(this).plus(elements.flatMap { g -> g.flatView(excludePredicate) })
    }


}
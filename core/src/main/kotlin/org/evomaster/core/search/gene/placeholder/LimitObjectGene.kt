package org.evomaster.core.search.gene.placeholder

import org.evomaster.core.output.OutputFormat
import org.evomaster.core.search.gene.Gene
import org.evomaster.core.search.gene.ObjectGene
import org.evomaster.core.search.gene.utils.GeneUtils
import org.evomaster.core.search.service.AdaptiveParameterControl
import org.evomaster.core.search.service.Randomness
import org.evomaster.core.search.service.mutator.MutationWeightControl
import org.evomaster.core.search.service.mutator.genemutation.AdditionalGeneMutationInfo
import org.evomaster.core.search.service.mutator.genemutation.SubsetGeneSelectionStrategy

/**
 * This gene is mainly created for GraphQL.
 * It is used as a placeholder when a certain limit is reached
 */

class LimitObjectGene(name: String) : ObjectGene(name, listOf()) {

    override fun isMutable() = false

    override fun copyContent(): Gene = LimitObjectGene(name)

    override fun isLocallyValid() : Boolean{
        return true
    }

    override fun randomize(randomness: Randomness, tryToForceNewValue: Boolean) {
        //nothing to do
    }



    override fun getValueAsPrintableString(previousGenes: List<Gene>, mode: GeneUtils.EscapeMode?, targetFormat: OutputFormat?, extraCheck: Boolean): String {
        throw IllegalStateException("LimitObjectGene has no value")
    }


    override fun isPrintable(): Boolean {
        return false
    }
}
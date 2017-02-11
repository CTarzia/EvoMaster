package org.evomaster.core.search.gene

import org.evomaster.core.search.service.Randomness
import java.util.*


class Base64StringGene(
        name: String,
        val data: StringGene = StringGene("data")
) : Gene(name) {

    override fun copy(): Gene = Base64StringGene(name, data.copy() as StringGene)

    override fun randomize(randomness: Randomness, forceNewValue: Boolean) {
        data.randomize(randomness, forceNewValue)
    }

    override fun getValueAsString(): String {
        return Base64.getEncoder().encodeToString(data.value.toByteArray())
    }
}
package org.evomaster.core.problem.util

import org.evomaster.core.Lazy
import org.evomaster.core.database.DbAction
import org.evomaster.core.database.DbActionUtils
import org.evomaster.core.logging.LoggingUtil
import org.evomaster.core.problem.rest.RestCallAction
import org.evomaster.core.problem.rest.RestPath
import org.evomaster.core.problem.rest.param.*
import org.evomaster.core.problem.rest.resource.RestResourceNode
import org.evomaster.core.problem.util.inference.model.ParamGeneBindMap
import org.evomaster.core.search.gene.*
import org.evomaster.core.search.gene.sql.SqlAutoIncrementGene
import org.evomaster.core.search.gene.sql.SqlForeignKeyGene
import org.evomaster.core.search.gene.sql.SqlPrimaryKeyGene
import org.slf4j.LoggerFactory

object BindingBuilder {

    private val log = LoggerFactory.getLogger(BindingBuilder::class.java)


    /**
     * @param target bind [target] based on other params, i.e., [params]
     * @param targetPath is the path of [target]
     * @param sourcePath
     * @param params
     */
    fun buildBindBetweenParams(target : Param, targetPath: RestPath, sourcePath: RestPath, params: List<Param>, inner : Boolean = false) : List<Pair<Gene, Gene>>{
        return when(target){
            is BodyParam -> buildBindBodyParam(target, targetPath, sourcePath, params, inner)
            is PathParam -> buildBindPathParm(target, targetPath, sourcePath, params, inner)
            is QueryParam -> buildBindQueryParm(target, targetPath, sourcePath, params, inner)
            is FormParam -> buildBindFormParam(target, params)?.run { listOf(this) }?: listOf()
            is HeaderParam -> buildBindHeaderParam(target, params)?.run { listOf(this) }?: listOf()
            else -> {
                LoggingUtil.uniqueWarn(log, "do not support gene binding for ${target::class.java.simpleName}")
                emptyList()
            }
        }
    }

    private fun buildBindHeaderParam(p : HeaderParam, params: List<Param>): Pair<Gene, Gene>?{
        return params.find { it is HeaderParam && p.name == it.name}?.run {
            Pair(p.gene, this.gene)
        }
    }

    private fun buildBindFormParam(p : FormParam, params: List<Param>): Pair<Gene, Gene>?{
        return params.find { it is FormParam && p.name == it.name}?.run {
            Pair(p.gene, this.gene)
        }
    }

    private fun buildBindQueryParm(p : QueryParam, targetPath: RestPath, sourcePath: RestPath, params: List<Param>, inner : Boolean): List<Pair<Gene, Gene>>{
        if(params.isNotEmpty() && ParamUtil.numOfBodyParam(params) == params.size){
            return buildBindBodyAndOther(params.first { pa -> pa is BodyParam } as BodyParam,
                sourcePath,
                p,
                targetPath,
                false,
                inner)
        }else{
            val sg = params.filter { pa -> !(pa is BodyParam) }.find { pa -> pa.name == p.name }
            if(sg != null){
                return listOf(Pair(p.gene, sg.gene))
            }
        }
        return emptyList()
    }

    private fun buildBindBodyParam(bp : BodyParam, targetPath: RestPath, sourcePath: RestPath, params: List<Param>, inner : Boolean) : List<Pair<Gene, Gene>>{
        if(ParamUtil.numOfBodyParam(params) != params.size ){
            return params.filter { p -> p !is BodyParam }
                .flatMap {ip->
                    buildBindBodyAndOther(bp, targetPath, ip, sourcePath, true, inner)
                }
        }else if(params.isNotEmpty()){
            val valueGene = ParamUtil.getValueGene(bp.gene)
            val pValueGene = ParamUtil.getValueGene(params[0].gene)
            if(valueGene !is ObjectGene){
                return listOf()
            }
            if (pValueGene !is ObjectGene){
                val field = valueGene.fields.find {
                    it::class.java.simpleName == pValueGene::class.java.simpleName && (it.name.equals(pValueGene.name, ignoreCase = true) || StringSimilarityComparator.isSimilar(
                        ParamUtil.modifyFieldName(valueGene, it), pValueGene.name))
                }?: return listOf()
                return listOf(Pair(field, pValueGene))
            }

            return buildBindObjectGeneWithObjectGene(valueGene, pValueGene)
        }
        return emptyList()
    }

    private fun buildBindObjectGeneWithObjectGene(b : ObjectGene, g : ObjectGene) : List<Pair<Gene, Gene>>{
        val map = mutableListOf<Pair<Gene, Gene>>()
        b.fields.forEach { f->
            val bound = f !is OptionalGene || f.isActive || (Math.random() < 0.5)
            if (bound){
                val mf = ParamUtil.getValueGene(f)
                val mName = ParamUtil.modifyFieldName(b, mf)
                val found = g.fields.find {ot->
                    val mot = ParamUtil.getValueGene(ot)
                    val pMName = ParamUtil.modifyFieldName(g, mot)
                    mf::class.java.simpleName == mot::class.java.simpleName && (pMName.equals(mName, ignoreCase = true) || StringSimilarityComparator.isSimilar(mName,pMName) )
                }
                if(found != null){
                    if (found is ObjectGene)
                        map.addAll(buildBindObjectGeneWithObjectGene(mf as ObjectGene, found))
                    else{
                        // FIXME, binding point
                        val vg = ParamUtil.getValueGene(found)
                        map.add(Pair(mf, vg))
                    }
                }
            }
        }
        return map
    }

    private fun buildBindPathParm(p : PathParam, targetPath: RestPath, sourcePath: RestPath, params: List<Param>, inner : Boolean): List<Pair<Gene, Gene>>{
        val k = params.find { pa -> pa is PathParam && pa.name == p.name }
        if(k != null){
            val mp = ParamUtil.getValueGene(p.gene)
            val mk = ParamUtil.getValueGene(k.gene)
            if (mp::class.java.simpleName == mk::class.java.simpleName){
                return listOf(Pair(mp, mk))
            } else{
                return listOf(buildBindingGene(mp, mk, true))
            }
        }
        else{
            if(ParamUtil.numOfBodyParam(params) == params.size && params.isNotEmpty()){
                return buildBindBodyAndOther(params.first { pa -> pa is BodyParam } as BodyParam,
                    sourcePath,
                    p,
                    targetPath,
                    false,
                    inner)
            }
        }
        return emptyList()
    }

    private fun buildBindBodyAndOther(body : BodyParam, bodyPath:RestPath, other : Param, otherPath : RestPath, b2g: Boolean, inner : Boolean): List<Pair<Gene, Gene>>{
        val otherGene = ParamUtil.getValueGene(other.gene)
        if (!ParamUtil.isGeneralName(otherGene.name)){
            val f = ParamUtil.getValueGene(body.gene).run {
                if (this is ObjectGene){
                    fields.find { f->
                        ParamUtil.findField(f.name, refType, otherGene.name)
                    }
                }else
                    null
            }
            if (f != null && f::class.java.simpleName == otherGene::class.java.simpleName){
                return listOf(buildBindingGene(f, otherGene, b2g))
            }
        }

        val pathMap = ParamUtil.geneNameMaps(listOf(other), otherPath.getNonParameterTokens().reversed())
        val bodyMap = ParamUtil.geneNameMaps(listOf(body), bodyPath.getNonParameterTokens().reversed())

        return pathMap.mapNotNull { (pathkey, pathGene) ->
            if(bodyMap.containsKey(pathkey)){
                buildBindingGene(bodyMap.getValue(pathkey), pathGene, b2g)
            }else{
                val matched = bodyMap.keys.filter { s -> ParamUtil.scoreOfMatch(pathkey, s, inner) == 0 }
                if(matched.isNotEmpty()){
                    val first = matched.first()
                    buildBindingGene(bodyMap.getValue(first), pathGene, b2g)
                }else{
                    null
                }
            }
        }
    }

    /**
     * bind values between [restAction] and [dbActions]
     * @param restAction is the action to be bounded with [dbActions]
     * @param restNode is the resource node for the [restAction]
     * @param dbActions are the dbactions generated for the [call]
     * @param bindingMap presents how to map the [restAction] and [dbActions] at Gene-level
     * @param forceBindParamBasedOnDB specifies whether to bind params based on [dbActions] or reversed
     * @param dbRemovedDueToRepair indicates whether the dbactions are removed due to repair.
     */
    fun buildBindRestActionBasedOnDbActions(restAction: RestCallAction,
                                            restNode: RestResourceNode,
                                            paramGeneBindMap: List<ParamGeneBindMap>,
                                            dbActions: MutableList<DbAction>,
                                            forceBindParamBasedOnDB: Boolean = false,
                                            dbRemovedDueToRepair : Boolean) : List<Pair<Gene, Gene>>{

        val map = mutableListOf<Pair<Gene, Gene>>()

        Lazy.assert {
            paramGeneBindMap.isNotEmpty()
        }

        paramGeneBindMap.forEach { pToGene ->
            val dbAction = DbActionUtils.findDbActionsByTableName(dbActions, pToGene.tableName).firstOrNull()
            //there might due to a repair for dbactions
            if (dbAction == null && !dbRemovedDueToRepair)
                log.warn("cannot find ${pToGene.tableName} in db actions ${
                    dbActions.joinToString(";") { it.table.name }
                }")
            if(dbAction != null){
                // columngene might be null if the column is nullable
                val columngene = findGeneBasedNameAndType(dbAction.seeGenes(), pToGene.column, type = null).firstOrNull()
                if (columngene != null){
                    val param = restAction.parameters.find { p -> restNode.getParamId(restAction.parameters, p)
                        .equals(pToGene.paramId, ignoreCase = true) }
                    if(param!= null){
                        if (pToGene.isElementOfParam) {
                            if (param is BodyParam && param.gene is ObjectGene) {
                                param.gene.fields.find { f -> f.name == pToGene.targetToBind }?.let { paramGene ->
                                    map.add(buildBindingParamsWithDbAction(columngene, paramGene, forceBindParamBasedOnDB || dbAction.representExistingData))
                                }
                            }
                        } else {
                            map.add(buildBindingParamsWithDbAction(columngene, param.gene, forceBindParamBasedOnDB || dbAction.representExistingData))
                        }
                    }
                }
            }
        }

        return map
    }

    private fun findGeneBasedNameAndType(genes : List<Gene>, name: String?, type: String?) : List<Gene>{
        if (name == null && type == null)
            throw IllegalArgumentException("cannot find the gene with 'null' name and 'null' type")
        return genes.filter { g->
            (name?.equals(g.name, ignoreCase = true)?:true) && (type?.equals(g::class.java.simpleName)?:true)
        }
    }


    /**
     * derive a binding map between [dbgene] and [paramGene]
     */
    fun buildBindingParamsWithDbAction(dbgene: Gene, paramGene: Gene, existingData: Boolean, enableFlexibleBind : Boolean = true): Pair<Gene, Gene>{
        return if(dbgene is SqlPrimaryKeyGene || dbgene is SqlForeignKeyGene || dbgene is SqlAutoIncrementGene){
            /*
                if gene of dbaction is PK, FK or AutoIncrementGene,
                    bind gene of Param according to the gene from dbaction
             */
             buildBindingGene(b = ParamUtil.getValueGene(dbgene), g = ParamUtil.getValueGene(paramGene), b2g = false)
        }else{
            val db2Action = !existingData && (!enableFlexibleBind ||
                    checkBindSequence(ParamUtil.getValueGene(dbgene), ParamUtil.getValueGene(paramGene)) ?:true)
            buildBindingGene(
                b = ParamUtil.getValueGene(dbgene),
                g = ParamUtil.getValueGene(paramGene),
                b2g = db2Action
            )
        }
    }

    private fun buildBindingGene(b : Gene, g : Gene, b2g :Boolean): Pair<Gene, Gene>{
        return if(b::class.java.simpleName == g::class.java.simpleName){
            if (b2g) Pair(b,g)
            else Pair(g,b)
        }else if(b2g && (g is SqlPrimaryKeyGene || g is ImmutableDataHolderGene || g is SqlForeignKeyGene || g is SqlAutoIncrementGene)){
            Pair(b,g)
        }else if(!b2g && (b is SqlPrimaryKeyGene || b is ImmutableDataHolderGene || b is SqlForeignKeyGene || b is SqlAutoIncrementGene))
            Pair(g,b)
        else{
            if(b2g) Pair(b,g)
            else Pair(g,b)
        }
    }

    /**
     * [geneA] copy values from [geneB]
     * @return null cannot find its priority
     *         true keep current sequence
     *         false change current sequence
     */
    private fun checkBindSequence(geneA: Gene, geneB: Gene) : Boolean?{
        val pA = getGeneTypePriority(geneA)
        val pB = getGeneTypePriority(geneB)

        if(pA == -1 || pB == -1) return null

        if(pA >= pB) return true

        return false
    }

    private val GENETYPE_BINDING_PRIORITY = mapOf<Int, Set<String>>(
        (0 to setOf(SqlPrimaryKeyGene::class.java.simpleName, SqlAutoIncrementGene::class.java.simpleName, SqlForeignKeyGene::class.java.simpleName, ImmutableDataHolderGene::class.java.simpleName)),
        (1 to setOf(DateTimeGene::class.java.simpleName, DateGene::class.java.simpleName, TimeGene::class.java.simpleName)),
        (2 to setOf(Boolean::class.java.simpleName)),
        (3 to setOf(IntegerGene::class.java.simpleName)),
        (4 to setOf(LongGene::class.java.simpleName)),
        (5 to setOf(FloatGene::class.java.simpleName)),
        (6 to setOf(DoubleGene::class.java.simpleName)),
        (7 to setOf(ArrayGene::class.java.simpleName, ObjectGene::class.java.simpleName, EnumGene::class.java.simpleName, CycleObjectGene::class.java.simpleName, MapGene::class.java.simpleName)),
        (8 to setOf(StringGene::class.java.simpleName, Base64StringGene::class.java.simpleName))
    )

    private fun getGeneTypePriority(gene: Gene) : Int{
        val typeName = gene::class.java.simpleName
        GENETYPE_BINDING_PRIORITY.filter { it.value.contains(typeName) }.let {
            return if(it.isEmpty()) -1 else it.keys.first()
        }
    }
}
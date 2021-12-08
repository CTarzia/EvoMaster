package org.evomaster.core.problem.rpc

import com.google.common.annotations.VisibleForTesting
import org.evomaster.client.java.controller.api.dto.problem.rpc.RPCExceptionInfoDto
import org.evomaster.client.java.controller.api.dto.problem.rpc.exception.RPCExceptionType
import org.evomaster.core.search.Action
import org.evomaster.core.search.ActionResult

/**
 * define RPC call result with various situations,
 *  eg, success, exception, potential bug, fail (some problems when invoking the call, eg, timeout, network)
 */
class RPCCallResult : ActionResult {

    companion object {
        const val LAST_STATEMENT_WHEN_P_BUG = "LAST_STATEMENT_WHEN_P_BUG"
        const val INVOCATION_CODE = "INVOCATION_CODE"
        const val CUSTOM_EXP_BODY = "CUSTOM_EXP_BODY"
        const val EXCEPTION_CODE = "EXCEPTION_CODE"
    }

    constructor(stopping: Boolean = false) : super(stopping)

    @VisibleForTesting
    internal constructor(other: ActionResult) : super(other)

    override fun copy(): ActionResult {
        return RPCCallResult(this)
    }

    fun setFailedCall(){
        addResultValue(INVOCATION_CODE, RPCCallResultCategory.FAILED.name)
    }

    fun failedCall(): Boolean{
        return getInvocationCode() == RPCCallResultCategory.FAILED.name
    }

    fun setSuccess(){
        addResultValue(INVOCATION_CODE, RPCCallResultCategory.SUCCESS.name)
    }

    fun getInvocationCode(): String?{
        return getResultValue(INVOCATION_CODE)
    }

    fun getExceptionCode() = getResultValue(EXCEPTION_CODE)

    fun setLastStatementForPotentialBug(info: String){
        addResultValue(LAST_STATEMENT_WHEN_P_BUG, info)
    }

    fun getLastStatementForPotentialBug() = getResultValue(LAST_STATEMENT_WHEN_P_BUG)

    fun setRPCException(dto: RPCExceptionInfoDto) {

        if (dto.type != null){
            val code = when(dto.type){
                RPCExceptionType.APP_INTERNAL_ERROR -> RPCCallResultCategory.P_BUG
                RPCExceptionType.CUSTOMIZED_EXCEPTION-> RPCCallResultCategory.CUSTOM_EXCEPTION
                else -> RPCCallResultCategory.EXCEPTION
            }

            addResultValue(EXCEPTION_CODE, dto.type.name)
            addResultValue(INVOCATION_CODE, code.name)

        }
    }

    fun setCustomizedExceptionBody(json: String){
        addResultValue(CUSTOM_EXP_BODY, json)
    }

    fun getCustomizedExceptionBody() = getResultValue(CUSTOM_EXP_BODY)

    override fun matchedType(action: Action): Boolean {
        return action is RPCCallAction
    }

    fun hasPotentialBug() : Boolean = getInvocationCode() == RPCCallResultCategory.P_BUG.name
}
package org.evomaster.core.problem.graphql

object GqlConst {

    /**
     * This tag is for the GQL union type. Needed in getValueAsPrintableString to print out things like:
     *   fieldXName{
     *        ... on UnionObject1 {
     *           field
     *        }
     *        ... on UnionObjectN {
     *          field
     *        }
     *   }
     */
    const val UNION_TAG = "#UNION#"
    /**
     * Those tags are for the GQL interface type. Needed in getValueAsPrintableString to print out things like:
     *     fieldXName{
     *        field1
     *        fieldN
     *        ... on InterfaceObject1 {
     *           field
     *        }
     *        ... on InterfaceObjectN {
     *          field
     *        }
     *     }
     */
    const val INTERFACE_BASE_TAG = "#BASE#"
    const val INTERFACE_TAG = "#INTERFACE#"
    const val SCALAR_TAG = "scalar"
    const val OBJECT_TAG = "object"
    const val UNION_STRING_TAG = "union"
    const val INTERFACE_STRING_TAG = "interface"
    const val ENUM_TAG = "enum"
    const val LIST_TAG = "list"
    const val INPUT_OBJECT_TAG = "input_object"
    /**/
    const val QUERY = "query"
    const val QUERY_TYPE = "querytype"
    const val ROOT = "root"
    const val MUTATION = "mutation"


}
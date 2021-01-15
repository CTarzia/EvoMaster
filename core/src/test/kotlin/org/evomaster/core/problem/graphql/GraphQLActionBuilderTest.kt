package org.evomaster.core.problem.graphql

import org.evomaster.core.problem.graphql.param.GQInputParam
import org.evomaster.core.problem.graphql.param.GQReturnParam
import org.junit.Test
import org.junit.jupiter.api.Assertions.*
import org.evomaster.core.search.Action
import org.evomaster.core.search.gene.*
import org.junit.jupiter.api.Disabled


class GraphQLActionBuilderTest {


    @Test
    fun testPetClinic() {

        val actionCluster = mutableMapOf<String, Action>()
        val json = PetClinicCheckMain::class.java.getResource("/graphql/QueryTypeGlobalPetsClinic.json").readText()

        GraphQLActionBuilder.addActionsFromSchema(json, actionCluster)

        assertEquals(15, actionCluster.size)

        val pettypes = actionCluster.get("pettypes") as GraphQLAction
        assertEquals(1, pettypes.parameters.size)
        assertTrue(pettypes.parameters[0] is GQReturnParam)
        assertTrue(pettypes.parameters[0].gene is ArrayGene<*>)
        val objPetType = (pettypes.parameters[0].gene as ArrayGene<*>).template as ObjectGene
        assertEquals(2, objPetType.fields.size)
        assertTrue(objPetType.fields.any { it is IntegerGene && it.name == "id" })
        assertTrue(objPetType.fields.any { it is StringGene && it.name == "name" })
        val gQlInput= GQReturnParam(pettypes.parameters[0].name,pettypes.parameters[0].gene)
        val gQlInputcopy=gQlInput.copy()
        assertEquals(gQlInput.name,gQlInputcopy.name)
        assertEquals(gQlInput.gene.name,gQlInputcopy.gene.name)
        /**/
        val vets = actionCluster.get("vets") as GraphQLAction
        assertEquals(1, vets.parameters.size)
        assertTrue(vets.parameters[0] is GQReturnParam)
        assertTrue(vets.parameters[0].gene is ArrayGene<*>)
        val objVets = (vets.parameters[0].gene as ArrayGene<*>).template as ObjectGene
        assertEquals(4, objVets.fields.size)
        assertTrue(objVets.fields.any { it is IntegerGene && it.name == "id" })
        assertTrue(objVets.fields.any { it is StringGene && it.name == "firstName" })
        assertTrue(objVets.fields.any { it is StringGene && it.name == "lastName" })
        assertTrue(objVets.fields.any { it is ArrayGene<*> && it.name == "Specialty" })
        val objSpecialty = (objVets.fields.first { it.name == "Specialty" } as ArrayGene<*>).template as ObjectGene
        assertEquals(2, objSpecialty.fields.size)
        assertTrue(objSpecialty.fields.any { it is IntegerGene && it.name == "id" })
        assertTrue(objSpecialty.fields.any { it is StringGene && it.name == "name" })
        /**/
        val owners = actionCluster.get("owners") as GraphQLAction
        assertEquals(3, owners.parameters.size)
        assertTrue(owners.parameters[0] is GQInputParam)
        assertTrue(owners.parameters[0].name == "OwnerFilter")
        assertTrue((owners.parameters[0].gene as OptionalGene).gene is ObjectGene)
        val objOwnerFilter = (owners.parameters[0].gene as OptionalGene).gene as ObjectGene
        assertTrue(objOwnerFilter.fields.any { it is OptionalGene && it.name == "firstName" })
        assertTrue(objOwnerFilter.fields.any { it is OptionalGene && it.name == "lastName" })
        assertTrue(objOwnerFilter.fields.any { it is OptionalGene && it.name == "address" })
        assertTrue(objOwnerFilter.fields.any { it is OptionalGene && it.name == "city" })
        assertTrue(objOwnerFilter.fields.any { it is OptionalGene && it.name == "telephone" })
        assertTrue(owners.parameters[1] is GQInputParam)
        assertTrue(owners.parameters[1].name == "OwnerOrder")
        assertTrue(owners.parameters[2] is GQReturnParam)
        assertTrue(owners.parameters[2].gene is ArrayGene<*>)
        /**/
        val owner = (owners.parameters[2].gene as ArrayGene<*>).template as ObjectGene
        assertEquals(7, owner.fields.size)
        assertTrue(owner.fields.any { it is IntegerGene && it.name == "id" })
        assertTrue(owner.fields.any { it is StringGene && it.name == "firstName" })
        assertTrue(owner.fields.any { it is StringGene && it.name == "lastName" })
        assertTrue(owner.fields.any { it is StringGene && it.name == "address" })
        assertTrue(owner.fields.any { it is StringGene && it.name == "city" })
        assertTrue(owner.fields.any { it is StringGene && it.name == "telephone" })
        assertTrue(owner.fields.any { it is ArrayGene<*> && it.name == "Pet" })
        val objPet = (owner.fields.first { it.name == "Pet" } as ArrayGene<*>).template as ObjectGene
        assertEquals(6, objPet.fields.size)
        assertTrue(objPet.fields.any { it is IntegerGene && it.name == "id" })
        assertTrue(objPet.fields.any { it is StringGene && it.name == "name" })
        assertTrue(objPet.fields.any { it is DateGene && it.name == "birthDate" })
        assertTrue(objPet.fields.any { it is ObjectGene && it.name == "PetType" })
        assertTrue(objPet.fields.any { it is CycleObjectGene && it.name == "Owner" })
        assertTrue(objPet.fields.any { it is ObjectGene && it.name == "VisitConnection" })
        assertTrue(objPet.fields[5] is ObjectGene)
        val objVisitConnection = objPet.fields[5] as ObjectGene
        assertEquals(2, objVisitConnection.fields.size)
        assertTrue(objVisitConnection.fields[0] is IntegerGene)
        assertTrue(objVisitConnection.fields.any{ it is IntegerGene && it.name == "totalCount"})
        assertTrue(objVisitConnection.fields.any{ it is ArrayGene<*> && it.name == "Visit"})

        /**/
        val pet = actionCluster.get("pet") as GraphQLAction
        assertEquals(2, pet.parameters.size)
        assertTrue(pet.parameters[0] is GQInputParam)
        assertTrue(pet.parameters[0].gene is IntegerGene)
        assertTrue(pet.parameters[1] is GQReturnParam)
        assertTrue(pet.parameters[1].gene is ObjectGene)
        val objPet2 = (pet.parameters[1].gene as ObjectGene)
        assertEquals(6, objPet2.fields.size)
        assertTrue(objPet2.fields.any { it is ObjectGene && it.name == "VisitConnection" })
        /**/
        val specialties = actionCluster.get("specialties") as GraphQLAction
        assertEquals(1, specialties.parameters.size)
        assertTrue(specialties.parameters[0] is GQReturnParam)
        assertTrue(specialties.parameters[0].gene is ArrayGene<*>)

        //TODO other assertions on the actions
    }
}
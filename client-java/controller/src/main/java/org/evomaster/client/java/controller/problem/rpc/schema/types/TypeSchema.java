package org.evomaster.client.java.controller.problem.rpc.schema.types;

import org.evomaster.client.java.controller.api.dto.problem.rpc.TypeDto;

/**
 * type schema
 */
public class TypeSchema {

    /**
     * simple name of the type
     */
    private final String type;
    /**
     * full name of the type, ie, including full package path
     */
    private final String fullTypeName;

    /**
     * original class
     */
    private final Class<?> clazz;

    public TypeSchema(String type, String fullTypeName, Class<?> clazz){
        this.type = type;
        this.fullTypeName = fullTypeName;
        this.clazz = clazz;
    }


    public String getType() {
        return type;
    }

    public String getFullTypeName() {
        return fullTypeName;
    }

    public TypeSchema copy(){
        return new TypeSchema(type, fullTypeName, clazz);
    }

    public TypeDto getDto(){
        TypeDto dto = new TypeDto();
        dto.fullTypeName = fullTypeName;
        return dto;
    }

    public boolean sameType(TypeDto dto){
        return fullTypeName.equals(dto.fullTypeName);
    }

    public Class<?> getClazz() {
        return clazz;
    }
}

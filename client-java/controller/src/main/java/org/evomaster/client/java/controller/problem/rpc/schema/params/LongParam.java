package org.evomaster.client.java.controller.problem.rpc.schema.params;

import org.evomaster.client.java.controller.api.dto.problem.rpc.ParamDto;
import org.evomaster.client.java.controller.api.dto.problem.rpc.RPCSupportedDataType;
import org.evomaster.client.java.controller.problem.rpc.schema.types.PrimitiveOrWrapperType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * long param
 */
public class LongParam extends PrimitiveOrWrapperParam<Long> {
    public LongParam(String name, String type, String fullTypeName, Class<?> clazz) {
        super(name, type, fullTypeName, clazz);
    }

    public LongParam(String name, PrimitiveOrWrapperType type) {
        super(name, type);
    }

    @Override
    String getValueWithJava() {
        if (getValue() == null)
            return null;
        return ""+getValue()+"L";
    }

    @Override
    public ParamDto getDto() {
        ParamDto dto = super.getDto();
        if (getType().isWrapper)
            dto.type.type = RPCSupportedDataType.LONG;
        else
            dto.type.type = RPCSupportedDataType.P_LONG;
        if (getValue() != null)
            dto.jsonValue = getValue().toString();
        return dto;
    }

    @Override
    public LongParam copyStructure() {
        return new LongParam(getName(), getType());
    }


    @Override
    public void setValueBasedOnDto(ParamDto dto) {
        try {
            if (dto.jsonValue != null)
                setValue(Long.parseLong(dto.jsonValue));
        }catch (NumberFormatException e){
            throw new RuntimeException("ERROR: fail to convert "+dto.jsonValue+" as long value");
        }
    }

    @Override
    protected void setValueBasedOnValidInstance(Object instance) {
        setValue((Long) instance);
    }

    @Override
    public boolean isValidInstance(Object instance) {
        return instance instanceof Long;
    }

}

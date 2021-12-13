package org.evomaster.client.java.controller.problem.rpc.schema.params;

import org.evomaster.client.java.controller.api.dto.problem.rpc.ParamDto;
import org.evomaster.client.java.controller.api.dto.problem.rpc.RPCSupportedDataType;
import org.evomaster.client.java.controller.problem.rpc.CodeJavaGenerator;
import org.evomaster.client.java.controller.problem.rpc.schema.types.MapType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * thrift
 *  HashMap (see https://thrift.apache.org/docs/types#containers)
 */
public class MapParam extends NamedTypedValue<MapType, List<PairParam>>{

    public MapParam(String name, MapType type) {
        super(name, type);
    }

    @Override
    public Object newInstance() throws ClassNotFoundException {
        if (getValue() == null) return null;
        return getValue().stream().map(i-> {
            try {
                return new AbstractMap.SimpleEntry<>(i.getValue().getKey().newInstance(), i.getValue().getValue().newInstance());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(String.format("MapParam: could not create new instance for key and value (%s,%s)",
                        i.getValue().getKey().toString(), i.getValue().getValue().getType()));
            }
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public ParamDto getDto() {
        ParamDto dto = super.getDto();
        dto.type.type = RPCSupportedDataType.MAP;
        if (getValue()!=null){
            dto.innerContent = getValue().stream().map(s->s.getDto()).collect(Collectors.toList());
        }
        return dto;
    }

    @Override
    public MapParam copyStructure() {
        return new MapParam(getName(), getType());
    }

    @Override
    public void setValueBasedOnDto(ParamDto dto) {
        if (dto.innerContent!= null && !dto.innerContent.isEmpty()){
            PairParam t = getType().getTemplate();
            List<PairParam> values = dto.innerContent.stream().map(s-> {
                PairParam c = t.copyStructure();
                c.setValueBasedOnDto(s);
                return c;
            }).collect(Collectors.toList());
            setValue(values);
        }

    }

    @Override
    protected void setValueBasedOnValidInstance(Object instance) {
        if (instance == null) return;
        PairParam t = getType().getTemplate();
        List<PairParam> values = new ArrayList<>();
        for (Object e : ((Map) instance).entrySet()){
            PairParam copy = t.copyStructure();
            copy.setValueBasedOnInstance(e);
            values.add(copy);
        }
        setValue(values);
    }

    @Override
    public List<String> newInstanceWithJava(boolean isDeclaration, boolean doesIncludeName, String variableName, int indent) {
        String fullName = getType().getTypeNameForInstance();
        List<String> codes = new ArrayList<>();
        String var = CodeJavaGenerator.oneLineInstance(isDeclaration, doesIncludeName, fullName, variableName, null);
        CodeJavaGenerator.addCode(codes, var, indent);
        if (getValue() == null) return codes;
        CodeJavaGenerator.addCode(codes, "{", indent);
        // new array
        CodeJavaGenerator.addCode(codes,
                CodeJavaGenerator.setInstance(
                        variableName,
                        CodeJavaGenerator.newMap()), indent+1);
        int index = 0;
        for (PairParam e: getValue()){
            String eKeyVarName = variableName+"_key_"+index;
            if (e.getValue().getKey() == null)
                throw new RuntimeException("key should not been null");
            codes.addAll(e.getValue().getKey().newInstanceWithJava(true, true, eKeyVarName, indent+1));
            String eValueVarName = variableName+"_value_"+index;
            if (e.getValue().getValue() == null)
                throw new RuntimeException("value should not been null");
            codes.addAll(e.getValue().getValue().newInstanceWithJava(true, true, eValueVarName, indent+1));
            CodeJavaGenerator.addCode(codes, variableName+".put("+eKeyVarName+","+eValueVarName+");", indent+1);
            index++;
        }

        CodeJavaGenerator.addCode(codes, "}", indent);
        return codes;
    }
}

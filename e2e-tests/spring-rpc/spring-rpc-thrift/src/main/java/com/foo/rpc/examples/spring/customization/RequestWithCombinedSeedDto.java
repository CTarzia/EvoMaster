/**
 * Autogenerated by Thrift Compiler (0.15.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package com.foo.rpc.examples.spring.customization;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})
@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.15.0)", date = "2022-01-07")
public class RequestWithCombinedSeedDto implements org.apache.thrift.TBase<RequestWithCombinedSeedDto, RequestWithCombinedSeedDto._Fields>, java.io.Serializable, Cloneable, Comparable<RequestWithCombinedSeedDto> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("RequestWithCombinedSeedDto");

  private static final org.apache.thrift.protocol.TField REQUEST_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("requestId", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField REQUEST_CODE_FIELD_DESC = new org.apache.thrift.protocol.TField("requestCode", org.apache.thrift.protocol.TType.STRING, (short)2);
  private static final org.apache.thrift.protocol.TField VALUE_FIELD_DESC = new org.apache.thrift.protocol.TField("value", org.apache.thrift.protocol.TType.DOUBLE, (short)3);

  private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new RequestWithCombinedSeedDtoStandardSchemeFactory();
  private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new RequestWithCombinedSeedDtoTupleSchemeFactory();

  public @org.apache.thrift.annotation.Nullable java.lang.String requestId; // required
  public @org.apache.thrift.annotation.Nullable java.lang.String requestCode; // required
  public double value; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    REQUEST_ID((short)1, "requestId"),
    REQUEST_CODE((short)2, "requestCode"),
    VALUE((short)3, "value");

    private static final java.util.Map<java.lang.String, _Fields> byName = new java.util.HashMap<java.lang.String, _Fields>();

    static {
      for (_Fields field : java.util.EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    @org.apache.thrift.annotation.Nullable
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // REQUEST_ID
          return REQUEST_ID;
        case 2: // REQUEST_CODE
          return REQUEST_CODE;
        case 3: // VALUE
          return VALUE;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new java.lang.IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    @org.apache.thrift.annotation.Nullable
    public static _Fields findByName(java.lang.String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final java.lang.String _fieldName;

    _Fields(short thriftId, java.lang.String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public java.lang.String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private static final int __VALUE_ISSET_ID = 0;
  private byte __isset_bitfield = 0;
  public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.REQUEST_ID, new org.apache.thrift.meta_data.FieldMetaData("requestId", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.REQUEST_CODE, new org.apache.thrift.meta_data.FieldMetaData("requestCode", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.VALUE, new org.apache.thrift.meta_data.FieldMetaData("value", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.DOUBLE)));
    metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(RequestWithCombinedSeedDto.class, metaDataMap);
  }

  public RequestWithCombinedSeedDto() {
  }

  public RequestWithCombinedSeedDto(
    java.lang.String requestId,
    java.lang.String requestCode,
    double value)
  {
    this();
    this.requestId = requestId;
    this.requestCode = requestCode;
    this.value = value;
    setValueIsSet(true);
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public RequestWithCombinedSeedDto(RequestWithCombinedSeedDto other) {
    __isset_bitfield = other.__isset_bitfield;
    if (other.isSetRequestId()) {
      this.requestId = other.requestId;
    }
    if (other.isSetRequestCode()) {
      this.requestCode = other.requestCode;
    }
    this.value = other.value;
  }

  public RequestWithCombinedSeedDto deepCopy() {
    return new RequestWithCombinedSeedDto(this);
  }

  @Override
  public void clear() {
    this.requestId = null;
    this.requestCode = null;
    setValueIsSet(false);
    this.value = 0.0;
  }

  @org.apache.thrift.annotation.Nullable
  public java.lang.String getRequestId() {
    return this.requestId;
  }

  public RequestWithCombinedSeedDto setRequestId(@org.apache.thrift.annotation.Nullable java.lang.String requestId) {
    this.requestId = requestId;
    return this;
  }

  public void unsetRequestId() {
    this.requestId = null;
  }

  /** Returns true if field requestId is set (has been assigned a value) and false otherwise */
  public boolean isSetRequestId() {
    return this.requestId != null;
  }

  public void setRequestIdIsSet(boolean value) {
    if (!value) {
      this.requestId = null;
    }
  }

  @org.apache.thrift.annotation.Nullable
  public java.lang.String getRequestCode() {
    return this.requestCode;
  }

  public RequestWithCombinedSeedDto setRequestCode(@org.apache.thrift.annotation.Nullable java.lang.String requestCode) {
    this.requestCode = requestCode;
    return this;
  }

  public void unsetRequestCode() {
    this.requestCode = null;
  }

  /** Returns true if field requestCode is set (has been assigned a value) and false otherwise */
  public boolean isSetRequestCode() {
    return this.requestCode != null;
  }

  public void setRequestCodeIsSet(boolean value) {
    if (!value) {
      this.requestCode = null;
    }
  }

  public double getValue() {
    return this.value;
  }

  public RequestWithCombinedSeedDto setValue(double value) {
    this.value = value;
    setValueIsSet(true);
    return this;
  }

  public void unsetValue() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __VALUE_ISSET_ID);
  }

  /** Returns true if field value is set (has been assigned a value) and false otherwise */
  public boolean isSetValue() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __VALUE_ISSET_ID);
  }

  public void setValueIsSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __VALUE_ISSET_ID, value);
  }

  public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
    switch (field) {
    case REQUEST_ID:
      if (value == null) {
        unsetRequestId();
      } else {
        setRequestId((java.lang.String)value);
      }
      break;

    case REQUEST_CODE:
      if (value == null) {
        unsetRequestCode();
      } else {
        setRequestCode((java.lang.String)value);
      }
      break;

    case VALUE:
      if (value == null) {
        unsetValue();
      } else {
        setValue((java.lang.Double)value);
      }
      break;

    }
  }

  @org.apache.thrift.annotation.Nullable
  public java.lang.Object getFieldValue(_Fields field) {
    switch (field) {
    case REQUEST_ID:
      return getRequestId();

    case REQUEST_CODE:
      return getRequestCode();

    case VALUE:
      return getValue();

    }
    throw new java.lang.IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new java.lang.IllegalArgumentException();
    }

    switch (field) {
    case REQUEST_ID:
      return isSetRequestId();
    case REQUEST_CODE:
      return isSetRequestCode();
    case VALUE:
      return isSetValue();
    }
    throw new java.lang.IllegalStateException();
  }

  @Override
  public boolean equals(java.lang.Object that) {
    if (that instanceof RequestWithCombinedSeedDto)
      return this.equals((RequestWithCombinedSeedDto)that);
    return false;
  }

  public boolean equals(RequestWithCombinedSeedDto that) {
    if (that == null)
      return false;
    if (this == that)
      return true;

    boolean this_present_requestId = true && this.isSetRequestId();
    boolean that_present_requestId = true && that.isSetRequestId();
    if (this_present_requestId || that_present_requestId) {
      if (!(this_present_requestId && that_present_requestId))
        return false;
      if (!this.requestId.equals(that.requestId))
        return false;
    }

    boolean this_present_requestCode = true && this.isSetRequestCode();
    boolean that_present_requestCode = true && that.isSetRequestCode();
    if (this_present_requestCode || that_present_requestCode) {
      if (!(this_present_requestCode && that_present_requestCode))
        return false;
      if (!this.requestCode.equals(that.requestCode))
        return false;
    }

    boolean this_present_value = true;
    boolean that_present_value = true;
    if (this_present_value || that_present_value) {
      if (!(this_present_value && that_present_value))
        return false;
      if (this.value != that.value)
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int hashCode = 1;

    hashCode = hashCode * 8191 + ((isSetRequestId()) ? 131071 : 524287);
    if (isSetRequestId())
      hashCode = hashCode * 8191 + requestId.hashCode();

    hashCode = hashCode * 8191 + ((isSetRequestCode()) ? 131071 : 524287);
    if (isSetRequestCode())
      hashCode = hashCode * 8191 + requestCode.hashCode();

    hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(value);

    return hashCode;
  }

  @Override
  public int compareTo(RequestWithCombinedSeedDto other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = java.lang.Boolean.compare(isSetRequestId(), other.isSetRequestId());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetRequestId()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.requestId, other.requestId);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.compare(isSetRequestCode(), other.isSetRequestCode());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetRequestCode()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.requestCode, other.requestCode);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.compare(isSetValue(), other.isSetValue());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetValue()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.value, other.value);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  @org.apache.thrift.annotation.Nullable
  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    scheme(iprot).read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    scheme(oprot).write(oprot, this);
  }

  @Override
  public java.lang.String toString() {
    java.lang.StringBuilder sb = new java.lang.StringBuilder("RequestWithCombinedSeedDto(");
    boolean first = true;

    sb.append("requestId:");
    if (this.requestId == null) {
      sb.append("null");
    } else {
      sb.append(this.requestId);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("requestCode:");
    if (this.requestCode == null) {
      sb.append("null");
    } else {
      sb.append(this.requestCode);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("value:");
    sb.append(this.value);
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    if (requestId == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'requestId' was not present! Struct: " + toString());
    }
    if (requestCode == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'requestCode' was not present! Struct: " + toString());
    }
    // alas, we cannot check 'value' because it's a primitive and you chose the non-beans generator.
    // check for sub-struct validity
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, java.lang.ClassNotFoundException {
    try {
      // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
      __isset_bitfield = 0;
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class RequestWithCombinedSeedDtoStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public RequestWithCombinedSeedDtoStandardScheme getScheme() {
      return new RequestWithCombinedSeedDtoStandardScheme();
    }
  }

  private static class RequestWithCombinedSeedDtoStandardScheme extends org.apache.thrift.scheme.StandardScheme<RequestWithCombinedSeedDto> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, RequestWithCombinedSeedDto struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // REQUEST_ID
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.requestId = iprot.readString();
              struct.setRequestIdIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // REQUEST_CODE
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.requestCode = iprot.readString();
              struct.setRequestCodeIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // VALUE
            if (schemeField.type == org.apache.thrift.protocol.TType.DOUBLE) {
              struct.value = iprot.readDouble();
              struct.setValueIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      if (!struct.isSetValue()) {
        throw new org.apache.thrift.protocol.TProtocolException("Required field 'value' was not found in serialized data! Struct: " + toString());
      }
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, RequestWithCombinedSeedDto struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.requestId != null) {
        oprot.writeFieldBegin(REQUEST_ID_FIELD_DESC);
        oprot.writeString(struct.requestId);
        oprot.writeFieldEnd();
      }
      if (struct.requestCode != null) {
        oprot.writeFieldBegin(REQUEST_CODE_FIELD_DESC);
        oprot.writeString(struct.requestCode);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldBegin(VALUE_FIELD_DESC);
      oprot.writeDouble(struct.value);
      oprot.writeFieldEnd();
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class RequestWithCombinedSeedDtoTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public RequestWithCombinedSeedDtoTupleScheme getScheme() {
      return new RequestWithCombinedSeedDtoTupleScheme();
    }
  }

  private static class RequestWithCombinedSeedDtoTupleScheme extends org.apache.thrift.scheme.TupleScheme<RequestWithCombinedSeedDto> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, RequestWithCombinedSeedDto struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      oprot.writeString(struct.requestId);
      oprot.writeString(struct.requestCode);
      oprot.writeDouble(struct.value);
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, RequestWithCombinedSeedDto struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      struct.requestId = iprot.readString();
      struct.setRequestIdIsSet(true);
      struct.requestCode = iprot.readString();
      struct.setRequestCodeIsSet(true);
      struct.value = iprot.readDouble();
      struct.setValueIsSet(true);
    }
  }

  private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
    return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
  }
}

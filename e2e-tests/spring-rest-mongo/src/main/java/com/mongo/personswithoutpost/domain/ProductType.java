package com.mongo.personswithoutpost.domain;

public enum ProductType {
	COMMON("Comum"),
    BLOOD("Sangue");
 
    private String descricao;
 
    ProductType(String descricao) {
        this.descricao = descricao;
    }
 
    public String getDescricao() {
        return descricao;
    }
}

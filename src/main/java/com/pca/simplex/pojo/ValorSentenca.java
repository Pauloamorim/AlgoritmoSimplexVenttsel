package com.pca.simplex.pojo;

import java.math.BigDecimal;

/**
 * Classe que representa o valor e o nome dentro de cada restrição
 * @author pauloamorim
 *
 */
public class ValorSentenca {

	private BigDecimal valor;
	private String nome;
	
	public ValorSentenca(BigDecimal valor, String nome) {
		super();
		this.valor = valor;
		this.nome = nome;
	}
	public ValorSentenca() {
		super();
	}

	public BigDecimal getValor() {
		return valor;
	}
	public void setValor(BigDecimal valor) {
		this.valor = valor;
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	
	
}

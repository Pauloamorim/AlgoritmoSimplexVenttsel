package com.pca.simplex.pojo;

import java.math.BigDecimal;
import java.util.List;

import com.pca.simplex.pojo.estatico.SinalOperacao;


/**
 * Classe que identifica os dados de uma restrição da função objetiva
 * @author pauloamorim
 *
 */
public class DadoRestricao {

	private List<ValorSentenca> listaValoresRestricoes;
	private SinalOperacao sinalOperacao;
	private BigDecimal resultado;
	
	
	public List<ValorSentenca> getListaValoresRestricoes() {
		return listaValoresRestricoes;
	}
	public void setListaValoresRestricoes(List<ValorSentenca> listaValoresRestricoes) {
		this.listaValoresRestricoes = listaValoresRestricoes;
	}
	public BigDecimal getResultado() {
		return resultado;
	}
	public void setResultado(BigDecimal resultado) {
		this.resultado = resultado;
	}
	public SinalOperacao getSinalOperacao() {
		return sinalOperacao;
	}
	public void setSinalOperacao(SinalOperacao sinalOperacao) {
		this.sinalOperacao = sinalOperacao;
	} 
	
	
}

package com.pca.simplex.pojo;

import java.util.List;

import com.pca.simplex.pojo.estatico.TipoFuncaoObjetivo;


/**
 * Classe para representar as informações da função objetiva
 * @author pauloamorim
 *
 */
public class DadoFuncaoObjetivo {

	private List<ValorSentenca> listaSentencasFuncaoObjetiva;
	private TipoFuncaoObjetivo tipoFuncaoObjetivo;
	private List<DadoRestricao> listaDadosRestricoes;
	
	
	public List<ValorSentenca> getListaSentencasFuncaoObjetiva() {
		return listaSentencasFuncaoObjetiva;
	}
	public void setListaSentencasFuncaoObjetiva(List<ValorSentenca> listaSentencasFuncaoObjetiva) {
		this.listaSentencasFuncaoObjetiva = listaSentencasFuncaoObjetiva;
	}
	public TipoFuncaoObjetivo getTipoFuncaoObjetivo() {
		return tipoFuncaoObjetivo;
	}
	public void setTipoFuncaoObjetivo(TipoFuncaoObjetivo tipoFuncaoObjetivo) {
		this.tipoFuncaoObjetivo = tipoFuncaoObjetivo;
	}
	public List<DadoRestricao> getListaDadosRestricoes() {
		return listaDadosRestricoes;
	}
	public void setListaDadosRestricoes(List<DadoRestricao> listaDadosRestricoes) {
		this.listaDadosRestricoes = listaDadosRestricoes;
	}
	
	
	
	
}

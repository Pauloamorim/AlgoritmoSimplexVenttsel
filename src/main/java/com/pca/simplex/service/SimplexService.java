package com.pca.simplex.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;

import com.pca.simplex.pojo.DadoFuncaoObjetivo;
import com.pca.simplex.pojo.DadoRestricao;
import com.pca.simplex.pojo.ValorSentenca;
import com.pca.simplex.pojo.estatico.SinalOperacao;
import com.pca.simplex.pojo.estatico.TipoFuncaoObjetivo;

@Service
public final class SimplexService {

	private  final String POSITIVO = "positivo";
	private  final String NEGATIVO = "negativo";
	private  final BigDecimal UM_NEGATIVO = new BigDecimal("-1");
	private  BigDecimal[][] tabelaSubCelulaSuperior;
	private  BigDecimal[][] tabelaSubCelulaInferior;

	public  void  calcularSimplex(DadoFuncaoObjetivo dadoFuncaoObjetiva) throws RuntimeException{
		System.out.println("-----------------------INICIO CALCULO SIMPLEX---------------");
		//monta a tabela do método simplex	
		tabelaSubCelulaSuperior = new BigDecimal[dadoFuncaoObjetiva.getListaDadosRestricoes().size()+1][dadoFuncaoObjetiva.getListaSentencasFuncaoObjetiva().size()+1];
		tabelaSubCelulaInferior = new BigDecimal[dadoFuncaoObjetiva.getListaDadosRestricoes().size()+1][dadoFuncaoObjetiva.getListaSentencasFuncaoObjetiva().size()+1];
		
		alterarTipoFuncaoObjetiva(dadoFuncaoObjetiva);
		adicionaVariaveisAuxiliares(dadoFuncaoObjetiva);
		preencheFuncaoObjetivaTabelaSimplex(dadoFuncaoObjetiva);
		preencheRestricoesTabelaSimplex(dadoFuncaoObjetiva);
		
		procurarVariavelBasicaMembroLivreNegativo();
		
	}

	/**
	 * Se a função for de maximização, multiplica seus elementos por -1 e troca o enum para MIN
	 * @param dadoFuncaoObjetiva
	 */
	private  void alterarTipoFuncaoObjetiva(DadoFuncaoObjetivo dadoFuncaoObjetiva) {
		//se for problema de maximização, inverte para um problema de minimização
		if(TipoFuncaoObjetivo.MAX.equals(dadoFuncaoObjetiva.getTipoFuncaoObjetivo())){
			//multiplica valores da FO por -1
			for ( ValorSentenca valorSentenca : dadoFuncaoObjetiva.getListaSentencasFuncaoObjetiva()) {
				valorSentenca.setValor(valorSentenca.getValor().multiply(UM_NEGATIVO));
			}
			//altera a FO para um problema do tipo MINIMIZAÇÃO
			dadoFuncaoObjetiva.setTipoFuncaoObjetivo(TipoFuncaoObjetivo.MIN);
		}
	}
	/**
	 * Adiciona variaveis auxiliares em todas as restrições.
	 * Se for >= a variavel auxiliar será negativa
	 * Se for <= a variavel auxiliar será positiva
	 * @param dadoFuncaoObjetiva
	 */
	private  void adicionaVariaveisAuxiliares(DadoFuncaoObjetivo dadoFuncaoObjetiva) {
		//adiciona as variáveis auxiliares
		Integer proximoNumeroVariavel = obterProximoNumeroVariavel(dadoFuncaoObjetiva.getListaSentencasFuncaoObjetiva());
		for (DadoRestricao dadoRestricao : dadoFuncaoObjetiva.getListaDadosRestricoes()) {
			
			BigDecimal valor;
			if(SinalOperacao.MAIOR_IGUAL.equals(dadoRestricao.getSinalOperacao()))
				valor = UM_NEGATIVO;
			else
				valor = BigDecimal.ONE;
			
			//adiciona a variavel auxiliar com o valor 1 e o nome x + o próximo valor
			dadoRestricao.getListaValoresRestricoes().add(new ValorSentenca(valor,"x".concat(proximoNumeroVariavel.toString())));
			//a operação passa a ser uma igualdade
			dadoRestricao.setSinalOperacao(SinalOperacao.IGUAL);
			
			proximoNumeroVariavel++;
		}
	}
	/**
	 * Obtem o próximo nome para a variável auxiliar, exemplo:
	 * 		80x1 + 60x2 -> O método retornara 3 que será a próxima variável de decisão
	 * 
	 * @param listaSentencasFuncaoObjetiva
	 * @return {@link Integer}
	 */
	private  Integer obterProximoNumeroVariavel(List<ValorSentenca> listaSentencasFuncaoObjetiva) {
		return Integer.valueOf(listaSentencasFuncaoObjetiva.get(listaSentencasFuncaoObjetiva.size()-1).getNome().substring(1)) + 1;
	}
	/**
	 * Preenche a linha da função objetiva na tabela Simplex
	 * @param dadoFuncaoObjetiva
	 * @param tabelaSubCelulaSuperior
	 */
	private  void preencheFuncaoObjetivaTabelaSimplex(DadoFuncaoObjetivo dadoFuncaoObjetiva) {
		//preenchendo a tabela, parte função objetiva
		int controlaLista = 0;
		for (int i = 0; i <=  dadoFuncaoObjetiva.getListaSentencasFuncaoObjetiva().size(); i++) {
			if(i==0)
				tabelaSubCelulaSuperior[0][i] = BigDecimal.ZERO;
			else{
				tabelaSubCelulaSuperior[0][i] = dadoFuncaoObjetiva.getListaSentencasFuncaoObjetiva().get(controlaLista).getValor().multiply(UM_NEGATIVO);
				controlaLista++;
			}
		}
	}
	/**
	 * preenche as linhas e colunas das restrições na tabela SIMPLEX
	 * @param dadoFuncaoObjetiva
	 * @param tabelaSubCelulaSuperior
	 */
	private  void preencheRestricoesTabelaSimplex(DadoFuncaoObjetivo dadoFuncaoObjetiva) {
		int linha = 1;
		int controlaListaRestricao = 0;
		BigDecimal valorVariavelAuxiliar = BigDecimal.ZERO;
		
		for (DadoRestricao dadoRestricao : dadoFuncaoObjetiva.getListaDadosRestricoes()) {
			controlaListaRestricao = 0;
			//TODO POSSÍVEL PONTO DE ERRO, INFERIMOS QUE SE A VARIAVEL AUXILIAR FOR NEGATIVA, TODOS OS VALORES TAMBÉM SERÃO NEGATIVOS
			//E SE POSITIVO, TODOS ELEMENTO SERÃO POSITIVOS
			valorVariavelAuxiliar = dadoRestricao.getListaValoresRestricoes().get(dadoRestricao.getListaValoresRestricoes().size()-1).getValor();
			for (int j = 0; j <  dadoRestricao.getListaValoresRestricoes().size(); j++) {
				if(j == 0)
					tabelaSubCelulaSuperior[linha][j] = dadoRestricao.getResultado().multiply(valorVariavelAuxiliar).setScale(3,RoundingMode.HALF_EVEN);
				else{
					tabelaSubCelulaSuperior[linha][j] = dadoRestricao.getListaValoresRestricoes().get(controlaListaRestricao).getValor().multiply(valorVariavelAuxiliar).setScale(3,RoundingMode.HALF_EVEN);
					controlaListaRestricao++;
				}
			}
			linha++;
		}
	}
	@SuppressWarnings("unused")
	private  void printarRestricoes(DadoFuncaoObjetivo dadoFuncaoObjetiva) {
		for (DadoRestricao dadoRestricao : dadoFuncaoObjetiva.getListaDadosRestricoes()) {
			for (ValorSentenca valorSentenca : dadoRestricao.getListaValoresRestricoes()) {
				System.out.print(" ".concat((valorSentenca.getValor().compareTo(BigDecimal.ZERO) == 1 ? "+" : "")
						.concat(valorSentenca.getValor().toString()).concat(valorSentenca.getNome())));
			}
			System.out.print(" ".concat(dadoRestricao.getSinalOperacao().toString()).concat(" ").concat(dadoRestricao.getResultado().toString()));
			System.out.println();
		}
	}
	/**
	 * 1ª Fase - Passo 1
	 * Procura uma variável básica com membro livre negativo<br>
	 * 	- se encontrar: chama a operação 2 do algoritmo
	 * 	- se não encontrar: chama a segunda etapa da solução do problema
	 * @param tabelaSubCelulaSuperior
	 * @throws RuntimeException 
	 */
	private  void procurarVariavelBasicaMembroLivreNegativo() throws RuntimeException {
		//1ª Fase, passo 1
		//procura uma variável básica com membro livro negativo;
		for (int i = 1; i < tabelaSubCelulaSuperior.length; i++) {
			if(tabelaSubCelulaSuperior[i][0].compareTo(BigDecimal.ZERO) == -1){
				// passo 1.1
				System.out.println("Variável básica negativa -> "+tabelaSubCelulaSuperior[i][0].toString());
				System.out.println("Indice da linha da variável básica negativa -> "+i);
				procurarElementoNegativoNaLinha(i);
			}else{
				executaSegundaFaseAlgoritmoSimplex();
			}
		}
	}

	/**
	 * 1ª Fase - passo 2<br>
	 * Procura um elmento negativo na linha em que foi encontrado o membro livre negativo
	 * 	- se encontrar: A coluna onde o elemento foi encontrado é considerada permissível
	 * 	- se não encontrar: A Solução permissível não existe
	 * 
	 * @param indiceLinhaPossuiMembroLivreNegativo
	 * @param tabelaSubCelulaSuperior
	 * @throws RuntimeException 
	 */
	private  void procurarElementoNegativoNaLinha(int indiceLinhaPossuiMembroLivreNegativo) throws RuntimeException {
		BigDecimal elementoPermitido = null;
		Integer indiceColunaPermitida = null;
		for (int i = 1; i < tabelaSubCelulaSuperior[indiceLinhaPossuiMembroLivreNegativo].length; i++) {
		
			//verifica se é negativo o valor
			if(tabelaSubCelulaSuperior[indiceLinhaPossuiMembroLivreNegativo][i].compareTo(BigDecimal.ZERO) == -1){
				elementoPermitido = tabelaSubCelulaSuperior[indiceLinhaPossuiMembroLivreNegativo][i];
				indiceColunaPermitida = i;
				break;
			}
		}
		if(elementoPermitido == null && indiceColunaPermitida == null){
			throw new RuntimeException("Solução Permissível Não Existe");
		}else{
			System.out.println("Elemento negativo na linha -> "+elementoPermitido.toString());
			System.out.println("Indice da coluna do elemento permitido -> "+indiceColunaPermitida);
			procurarLinhaPermitida(indiceColunaPermitida);
		}
	}

	/**
	 * 1ª Fase - passo 3
	 * 	Busca a linha permitida, a partir do menor resultado da divisão do membro livre pelos elementos da coluna permitida<br>
	 * 	-** Só é quociente valido se o numerador e o denomidador possuirem o mesmo sinal e o denominador for maior que zero
	 * @param indiceColunaPermitida
	 * @throws RuntimeException 
	 */
	private  void procurarLinhaPermitida(Integer indiceColunaPermitida) throws RuntimeException {
		Integer indiceLinhaPermitida = null;
		BigDecimal resultadoDivisao = new BigDecimal(Integer.MAX_VALUE); //Integer.MAX_VALUE para garantir que o primeiro valor seja sempre atribuido
		for (int i = 1; i < tabelaSubCelulaSuperior.length; i++) {
			BigDecimal membroLivre = tabelaSubCelulaSuperior[i][0];
			BigDecimal elemento = tabelaSubCelulaSuperior[i][indiceColunaPermitida];
			
			//Só é quociente valido se o numerador e o denomidador possuirem o mesmo sinal e o denominador for diferente de zero
			if(verificarSinalElemento(elemento).equals(verificarSinalElemento(membroLivre)) && elemento.compareTo(BigDecimal.ZERO) != 0){
				BigDecimal resultadoAux = membroLivre.divide(elemento).setScale(3,RoundingMode.HALF_EVEN);
				//se o resultado da divisão atual for menor que o que está gravado na variável resultadoDivisao, atribui novamente
				if(resultadoAux.compareTo(resultadoDivisao) == -1){
					resultadoDivisao = resultadoAux;
					indiceLinhaPermitida = i;
				}
			}
			
		}
		System.out.println("Indice da linha permitida ->"+indiceLinhaPermitida);
		//executa o passo 4 que é a chamada do algoritmo de troca
		executarAlgoritmoTroca(indiceLinhaPermitida,indiceColunaPermitida);
	}
	private  String verificarSinalElemento(BigDecimal elementoPermitido) {
		return elementoPermitido.compareTo(BigDecimal.ZERO) == -1 ? NEGATIVO : POSITIVO;
	}

	//----------------------------------------------------------------------------------------------------------
	//---------------------------------INÍCIO ALGORITMO DE TROCA------------------------------------------------
	//----------------------------------------------------------------------------------------------------------
	
	private  void executarAlgoritmoTroca(Integer indiceLinhaPermitida, Integer indiceColunaPermitida) throws RuntimeException {
		//PASSO 1 - calcula o inverso do elemento permitido
		BigDecimal elementoPermitidoInvertido = calculaInversoElementoPermitido(indiceLinhaPermitida,indiceColunaPermitida);
		System.out.println("Elemento permitido invertido -> "+elementoPermitidoInvertido.toString());
		
		
		boolean encontrouVariavelBasicaMembroLivreNegativo = false; 
		
		//preenche o elemento da tabelaSubCelulainferior com o elemento invertido
		tabelaSubCelulaInferior[indiceLinhaPermitida][indiceColunaPermitida] = elementoPermitidoInvertido;
		
		multiplicarLinhaPermitidaPorElementoPermitidoInvertido(elementoPermitidoInvertido,indiceLinhaPermitida,indiceColunaPermitida);
		multiplicarColunaPermitidaPorElementoPermitidoInvertido(elementoPermitidoInvertido,indiceLinhaPermitida,indiceColunaPermitida);
		
		preencherTodasCelulasTabelaSubCelulaInferiorVazias(indiceLinhaPermitida,indiceColunaPermitida);
		
		inverterLinhaPermitidaComColunaPermitida(indiceLinhaPermitida,indiceColunaPermitida);
		
		imprimirTabela();
		
		for (int i = 1; i < tabelaSubCelulaSuperior.length; i++) {
			if(tabelaSubCelulaSuperior[i][0].compareTo(BigDecimal.ZERO) == -1){
				encontrouVariavelBasicaMembroLivreNegativo = true;
				procurarVariavelBasicaMembroLivreNegativo();
			}
		}
		
		if(!encontrouVariavelBasicaMembroLivreNegativo){
			executaSegundaFaseAlgoritmoSimplex();
		}
		
	}

	private  BigDecimal calculaInversoElementoPermitido(Integer indiceLinhaPermitida, Integer indiceColunaPermitida) {
		return BigDecimal.ONE.divide(tabelaSubCelulaSuperior[indiceLinhaPermitida][indiceColunaPermitida]);
	}

	/**
	 * Algoritmo de Troca - Passo 2
	 * 	Multiplica toda a linha permitida pelo inverso do elemento permitido
	 * @param elementoPermitidoInvertido
	 * @param indiceLinhaPermitida
	 * @param indiceColunaPermitida 
	 */
	private  void multiplicarLinhaPermitidaPorElementoPermitidoInvertido(BigDecimal elementoPermitidoInvertido,
			Integer indiceLinhaPermitida, Integer indiceColunaPermitida) {
		for (int i = 0; i < tabelaSubCelulaSuperior[indiceLinhaPermitida].length; i++) {
			//verificação para não alterar o valor do elemento permitido que já foi calculado
			if(i != indiceColunaPermitida)
				tabelaSubCelulaInferior[indiceLinhaPermitida][i] = tabelaSubCelulaSuperior[indiceLinhaPermitida][i].multiply(elementoPermitidoInvertido).setScale(3,RoundingMode.HALF_EVEN);
		}
	}
	/**
	 * Algoritmo de Troca - Passo 3
	 * 	Multiplica toda a coluna permitida pelo - ( elemento permitido inverso)
	 * @param elementoPermitidoInvertido
	 * @param indiceLinhaPermitida
	 * @param indiceColunaPermitida 
	 */
	private  void multiplicarColunaPermitidaPorElementoPermitidoInvertido(BigDecimal elementoPermitidoInvertido,
			Integer indiceLinhaPermitida, Integer indiceColunaPermitida) {
		for (int i = 0; i < tabelaSubCelulaSuperior.length; i++) {
			
			//verificação para não alterar o valor do elemento permitido que já foi calculado
			if(i != indiceLinhaPermitida)
				tabelaSubCelulaInferior[i][indiceColunaPermitida] = tabelaSubCelulaSuperior[i][indiceColunaPermitida].multiply(elementoPermitidoInvertido.multiply(UM_NEGATIVO)).setScale(3,RoundingMode.HALF_EVEN);
		}
		
	}
	/**
	 * Preenche todas as células da tabela inferior multiplicando da seguinte forma:
	 * 	Elemento marcado da respectiva linha da tabela inferior X elemento marcado da respectiva coluna da tabela superior
	 * 
	 * ** Elemento marcado entende-se por célula da coluna/linha permitida que já possui valor
	 * @param indiceLinhaPermitida
	 * @param indiceColunaPermitida
	 */
	private  void preencherTodasCelulasTabelaSubCelulaInferiorVazias(Integer indiceLinhaPermitida,
			Integer indiceColunaPermitida) {
		for (int i = 0; i < tabelaSubCelulaInferior.length; i++) {
			
			//busca o elemento que já possui valor na coluna permitida, na tabela tabelaSubCelulaInferior
			BigDecimal elementoMarcadoLinha = tabelaSubCelulaInferior[i][indiceColunaPermitida];
			for (int j = 0; j < tabelaSubCelulaInferior[i].length; j++) {
				
				//busca o elemento que já possui valor na linha permitida, na tabela tabelaSubCelulaSuperior
				BigDecimal elementoMarcadoColuna = tabelaSubCelulaSuperior[indiceLinhaPermitida][j];
				if(tabelaSubCelulaInferior[i][j] == null){
					tabelaSubCelulaInferior[i][j] = elementoMarcadoLinha.multiply(elementoMarcadoColuna).setScale(3,RoundingMode.HALF_EVEN);
				}
			}
		}
	}
	
	private  void inverterLinhaPermitidaComColunaPermitida(Integer indiceLinhaPermitida,
			Integer indiceColunaPermitida) {
		
		//TODO QUANDO ADICIONAR O NOME DA RESTRIÇÃO NAS MATRIZES(X1,X2), MUDAR a linha de posição com a coluna
		
		for (int i = 0; i < tabelaSubCelulaSuperior.length; i++) {
			for (int j = 0; j < tabelaSubCelulaSuperior[i].length; j++) {
				
				//se pertencer a linha ou a coluna permitida, somente transporta o valor para a tabela superior
				if(i == indiceLinhaPermitida || j == indiceColunaPermitida){
					tabelaSubCelulaSuperior[i][j] = tabelaSubCelulaInferior[i][j];
				}else{
					//soma o valor da tabela inferior com a tabela superior e atribui a nova tabela superior
					tabelaSubCelulaSuperior[i][j] = tabelaSubCelulaInferior[i][j].add(tabelaSubCelulaSuperior[i][j]);
				}
				tabelaSubCelulaInferior[i][j] = null;
			}
		}
	}
	
	
	
	//------------------------------------------------------------------
	//-----------------------2ª Fase Algoritmo Simplex
	//------------------------------------------------------------------
	
	/**
	 * Executa a segunda Fase do algoritmo Simplex
	 * @throws RuntimeException 
	 * 
	 */
	private  void executaSegundaFaseAlgoritmoSimplex() throws RuntimeException {
		System.out.println("\n\n -------------INICIOU SEGUNDA FASE ALGORITMO SIMPLEX---------------");
		procurarElementoPositivoLinhaFuncaoObjetivo();
		
	}
	
	/**
	 * Passo 1 - 2ª Fase algoritmo Simplex
	 * Procura por um elemento positivo na linha da função objetiva
	 * 	- se existe, passa para a 2ª fase da 2ª etapa do método simplex
	 *  - se não existe, ENCONTROU A SOLUÇÃO ÓTIMA
	 * @throws RuntimeException 
	 */
	private  void procurarElementoPositivoLinhaFuncaoObjetivo() throws RuntimeException {
		//não considera o membro livre
		for (int i = 1; i < tabelaSubCelulaSuperior[0].length; i++) {
			if(tabelaSubCelulaSuperior[0][i].compareTo(BigDecimal.ZERO) == 1 ){
				procuraElementoPositivoColunaPermitida(i);
			}
		}
		System.out.println("\n\n\nChegou na solução ótima");
		imprimirTabela();
		throw new RuntimeException("Solução encontrada");//TODO PASSAR O NOME DAS VARIAVEIS DA FUNÇÃO OBJETIVO
	}

	/**
	 * Passo 2 - 2ª Fase algoritmo Simplex
	 * Na coluna permitida do elemento positivo encontrado na linha da função objetivo, procura um elemento positivo
	 * 	- caso encontre, irá chamar o método para buscar a linha permitida
	 * 	-caso não encontre, a solução ótima é ilimitada
	 * @param indiceColuna
	 * @throws RuntimeException 
	 */
	private  void procuraElementoPositivoColunaPermitida(int indiceColuna) throws RuntimeException {
		 for (int i = 1; i < tabelaSubCelulaSuperior.length; i++) {
			if(tabelaSubCelulaSuperior[i][indiceColuna].compareTo(BigDecimal.ZERO) == 1){
				procurarLinhaPermitida(indiceColuna);
			}
		}
		 throw new RuntimeException("Solução Ótima Ilimitada");
	}

	private  void imprimirTabela() {
		System.out.println();
		System.out.println("------------------TABELA COM VALORES ------------------");
		for (int i = 0; i < tabelaSubCelulaSuperior.length; i++) {
			for (int j = 0; j < tabelaSubCelulaSuperior[i].length; j++) {
				System.out.print(tabelaSubCelulaSuperior[i][j]+"/"+tabelaSubCelulaInferior[i][j]+"                     ");
			}
			System.out.println();
		}
	}
}


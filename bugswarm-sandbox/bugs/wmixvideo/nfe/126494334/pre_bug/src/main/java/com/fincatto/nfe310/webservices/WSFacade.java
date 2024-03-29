package com.fincatto.nfe310.webservices;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import org.apache.commons.httpclient.protocol.Protocol;

import com.fincatto.nfe310.NFeConfig;
import com.fincatto.nfe310.classes.NFUnidadeFederativa;
import com.fincatto.nfe310.classes.cadastro.NFRetornoConsultaCadastro;
import com.fincatto.nfe310.classes.evento.NFEnviaEventoRetorno;
import com.fincatto.nfe310.classes.evento.inutilizacao.NFRetornoEventoInutilizacao;
import com.fincatto.nfe310.classes.lote.consulta.NFLoteConsultaRetorno;
import com.fincatto.nfe310.classes.lote.envio.NFLoteEnvio;
import com.fincatto.nfe310.classes.lote.envio.NFLoteEnvioRetorno;
import com.fincatto.nfe310.classes.lote.envio.NFLoteIndicadorProcessamento;
import com.fincatto.nfe310.classes.nota.consulta.NFNotaConsultaRetorno;
import com.fincatto.nfe310.classes.statusservico.consulta.NFStatusServicoConsultaRetorno;
import com.fincatto.nfe310.validadores.xsd.XMLValidador;

public class WSFacade {

	private final WSLoteEnvio wsLoteEnvio;
	private final WSLoteConsulta wsLoteConsulta;
	private final WSStatusConsulta wsStatusConsulta;
	private final WSNotaConsulta wsNotaConsulta;
	private final WSCartaCorrecao wsCartaCorrecao;
	private final WSCancelamento wsCancelamento;
	private final WSConsultaCadastro wsConsultaCadastro;
	private final WSInutilizacao wsInutilizacao;

	public WSFacade(final NFeConfig config) throws IOException, KeyManagementException, UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
		//registra o protocolo e o socket factory correspondente
		Protocol.registerProtocol("https", new Protocol("https", new NFSocketFactory(config), 443));

		//inicia os servicos disponiveis da nfe
		this.wsLoteEnvio = new WSLoteEnvio(config);
		this.wsLoteConsulta = new WSLoteConsulta(config);
		this.wsStatusConsulta = new WSStatusConsulta(config);
		this.wsNotaConsulta = new WSNotaConsulta(config);
		this.wsCartaCorrecao = new WSCartaCorrecao(config);
		this.wsCancelamento = new WSCancelamento(config);
		this.wsConsultaCadastro = new WSConsultaCadastro(config);
		this.wsInutilizacao = new WSInutilizacao(config);
	}

	public NFLoteEnvioRetorno enviaLote(final NFLoteEnvio lote) throws Exception {
		XMLValidador.validaLote(lote.toString());
		if (lote.getIndicadorProcessamento().equals(NFLoteIndicadorProcessamento.PROCESSAMENTO_SINCRONO)) {
			throw new IllegalStateException("Nao existe ainda a forma de envio sincrona, faca o envio de forma assincrona");
		}
		return this.wsLoteEnvio.enviaLote(lote);
	}

	public NFLoteConsultaRetorno consultaLote(final String numeroRecibo) throws Exception {
		return this.wsLoteConsulta.consultaLote(numeroRecibo);
	}

	public NFStatusServicoConsultaRetorno consultaStatus(final NFUnidadeFederativa uf) throws Exception {
		return this.wsStatusConsulta.consultaStatus(uf);
	}

	public NFNotaConsultaRetorno consultaNota(final String chaveDeAcesso) throws Exception {
		return this.wsNotaConsulta.consultaNota(chaveDeAcesso);
	}

	public NFEnviaEventoRetorno corrigeNota(final String chaveDeAcesso, final String textoCorrecao, final int numeroSequencialEvento) throws Exception {
		return this.wsCartaCorrecao.corrigeNota(chaveDeAcesso, textoCorrecao, numeroSequencialEvento);
	}

	public NFEnviaEventoRetorno cancelaNota(final String chaveAcesso, final String numeroProtocolo, final String motivo) throws Exception {
		return this.wsCancelamento.cancelaNota(chaveAcesso, numeroProtocolo, motivo);
	}

	public NFRetornoEventoInutilizacao inutilizaNota(final int anoInutilizacaoNumeracao, final String cnpjEmitente, final String serie, final String numeroInicial, final String numeroFinal, final String justificativa) throws Exception {
		return this.wsInutilizacao.inutilizaNota(anoInutilizacaoNumeracao, cnpjEmitente, serie, numeroInicial, numeroFinal, justificativa);
	}

	public NFRetornoConsultaCadastro consultaCadastro(final String cnpj, final NFUnidadeFederativa uf) throws Exception {
		return this.wsConsultaCadastro.consultaCadastro(cnpj, uf);
	}
}
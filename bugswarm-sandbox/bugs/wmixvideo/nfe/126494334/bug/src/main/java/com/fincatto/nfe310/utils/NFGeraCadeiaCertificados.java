package com.fincatto.nfe310.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import com.fincatto.nfe310.classes.NFAmbiente;
import com.fincatto.nfe310.classes.NFAutorizador31;

public class NFGeraCadeiaCertificados {

	private static final int TIMEOUT_WS = 30;
	private static final File JSSECACERTS = new File("/tmp/cacerts");

	public static void main(final String[] args) {
		NFGeraCadeiaCertificados.geraCadeiaCertificados(NFAmbiente.HOMOLOGACAO, "/tmp");
	}

	private static void geraCadeiaCertificados(final NFAmbiente ambiente, final String diretorio) {
		try {
			final KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
			for (final NFAutorizador31 aut : NFAutorizador31.values()) {
				//Para NFe...
				String urlNfe = aut.getNfeConsultaProtocolo(ambiente);
				urlNfe = NFGeraCadeiaCertificados.getDomainName(urlNfe);
				NFGeraCadeiaCertificados.get(urlNfe, 443, ks);

				//Para NFCe...
				String urlNfce = aut.getNfceStatusServico(ambiente);
				if (urlNfce != null) {
					urlNfce = NFGeraCadeiaCertificados.getDomainName(urlNfce);
					if (urlNfce != null) {
						NFGeraCadeiaCertificados.get(urlNfce, 443, ks);
					}
				}
			}

			try (OutputStream out = new FileOutputStream(NFGeraCadeiaCertificados.JSSECACERTS)) {
				ks.store(out, "".toCharArray());
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private static String getDomainName(final String url) throws URISyntaxException {
		final URI uri = new URI(url);
		final String domain = uri.getHost();
		return domain.startsWith("www.") ? domain.substring(4) : domain;
	}

	public static void get(final String host, final int port, final KeyStore ks) throws Exception {
		final SSLContext context = SSLContext.getInstance("TLSv1");
		final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(ks);
		final X509TrustManager defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
		final SavingTrustManager tm = new SavingTrustManager(defaultTrustManager);
		context.init(null, new TrustManager[] { tm }, null);
		final SSLSocketFactory factory = context.getSocketFactory();

		NFGeraCadeiaCertificados.info("| Opening connection to " + host + ":" + port + "...");
		final SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
		socket.setSoTimeout(NFGeraCadeiaCertificados.TIMEOUT_WS * 1000);
		try {
			NFGeraCadeiaCertificados.info("| Starting SSL handshake...");
			socket.startHandshake();
			socket.close();
			NFGeraCadeiaCertificados.info("| No errors, certificate is already trusted");
		} catch (final SSLHandshakeException e) {
		} catch (final SSLException e) {
			NFGeraCadeiaCertificados.error("| " + e.toString());
		}

		final X509Certificate[] chain = tm.chain;
		if (chain == null) {
			NFGeraCadeiaCertificados.info("| Could not obtain server certificate chain");
		}

		NFGeraCadeiaCertificados.info("| Server sent " + chain.length + " certificate(s):");
		final MessageDigest sha1 = MessageDigest.getInstance("SHA1");
		final MessageDigest md5 = MessageDigest.getInstance("MD5");
		for (int i = 0; i < chain.length; i++) {
			final X509Certificate cert = chain[i];
			sha1.update(cert.getEncoded());
			md5.update(cert.getEncoded());

			final String alias = host + "-" + (i);
			ks.setCertificateEntry(alias, cert);
			NFGeraCadeiaCertificados.info("| Added certificate to keystore '" + NFGeraCadeiaCertificados.JSSECACERTS + "' using alias '" + alias + "'");
		}
	}

	private static class SavingTrustManager implements X509TrustManager {
		private final X509TrustManager tm;
		private X509Certificate[] chain;

		SavingTrustManager(final X509TrustManager tm) {
			this.tm = tm;
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void checkClientTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void checkServerTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
			this.chain = chain;
			this.tm.checkServerTrusted(chain, authType);
		}
	}

	private static void info(final String log) {
		System.out.println("INFO: " + log);
	}

	private static void error(final String log) {
		System.out.println("ERROR: " + log);
	}

}

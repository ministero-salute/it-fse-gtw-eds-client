/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright (C) 2023 Ministero della Salute
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package it.finanze.sanita.fse2.ms.edsclient.config;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import it.finanze.sanita.fse2.ms.edsclient.client.RestTemplateResponseErrorHandler;

@Configuration
public class RestTemplateConfig {

    /**
     * Unico RestTemplate dell'applicazione: SSL context che accetta qualunque certificato server
     * (i servizi interni espongono certificati firmati da una CA non presente nel trust store
     * della JVM, che altrimenti fa fallire le chiamate con "PKIX path building failed")
     * ed error handler custom per la traduzione degli errori HTTP.
     */
    @Bean
    @Primary
    @Qualifier("restTemplate")
    public RestTemplate restTemplate() throws Exception {

        SSLContext sslContext = createSslCustomContext();

        SimpleClientHttpRequestFactory requestFactory =
                new HostnameVerifyingSslContextRequestFactory(sslContext);

        RestTemplate restTemplate = new RestTemplate(requestFactory);
        restTemplate.setErrorHandler(new RestTemplateResponseErrorHandler());

        return restTemplate;
    }

    public SSLContext createSslCustomContext()
            throws NoSuchAlgorithmException,
            KeyManagementException{

        SSLContext sslContext = SSLContext.getInstance("TLS");

        sslContext.init(
                null,                           // nessun certificato client inviato al server
                trustAllCerts,                      // accetta tutti i certificati server
                new java.security.SecureRandom()
        );

        return sslContext;
    }

    private static final TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType)
                        throws CertificateException {
                    // non usato per validare il server remoto
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType)
                        throws CertificateException {
                    // accetta qualsiasi certificato server
                }
            }
    };

    private static class HostnameVerifyingSslContextRequestFactory
            extends SimpleClientHttpRequestFactory {

        private final SSLSocketFactory sslSocketFactory;
        private final HostnameVerifier hostnameVerifier;

        private HostnameVerifyingSslContextRequestFactory(SSLContext sslContext) {
            this.sslSocketFactory = sslContext.getSocketFactory();
            this.hostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
        }

        @Override
        protected void prepareConnection(HttpURLConnection connection, String httpMethod)
                throws IOException {

            if (connection instanceof HttpsURLConnection) {
                HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;
                httpsConnection.setSSLSocketFactory(sslSocketFactory);
                httpsConnection.setHostnameVerifier(hostnameVerifier);
            }

            super.prepareConnection(connection, httpMethod);
        }
    }

}

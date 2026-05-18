package it.finanze.sanita.fse2.ms.edsclient.config;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

@Configuration
public class RestTemplateConfig {

    @Bean
    @Qualifier("restTemplate")
    public RestTemplate restTemplate() throws Exception {

        SSLContext sslContext = createSslCustomContext();

        SimpleClientHttpRequestFactory requestFactory =
                new HostnameVerifyingSslContextRequestFactory(sslContext);

        return new RestTemplate(requestFactory);
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
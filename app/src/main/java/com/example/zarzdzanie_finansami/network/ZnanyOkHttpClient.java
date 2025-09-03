package com.example.zarzdzanie_finansami.network;
import android.content.Context;

import com.example.zarzdzanie_finansami.R;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class ZnanyOkHttpClient {

    public static OkHttpClient createTrustedClient(Context context) {
        try {
            // 1. Wczytaj certyfikat z zasobów res/raw
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream caInput = context.getResources().openRawResource(R.raw.server); // Upewnij się, że plik to 'server.cer'
            Certificate ca;
            try {
                ca = cf.generateCertificate(caInput);
            } finally {
                caInput.close();
            }

            // 2. Stwórz KeyStore zawierający nasz zaufany certyfikat
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // 3. Stwórz TrustManager, który ufa certyfikatom w naszym KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            // 4. Stwórz SSLContext, który używa naszego TrustManagera
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
            // interceptor do logowania zapytań i odpowiedzi
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // 5. Zbuduj klienta OkHttp z niestandardowym SSLSocketFactory
            return new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) tmf.getTrustManagers()[0])
                    .build();

        } catch (Exception e) {
            // W praktyce obsłuż wyjątki w bardziej elegancki sposób
            throw new RuntimeException(e);
        }
    }
}

/*
 * Sposób użycia:
 *
 * OkHttpClient trustedOkHttpClient = TrustedOkHttpClient.createTrustedClient(this); // 'this' to kontekst np. Activity
 *
 * Retrofit retrofit = new Retrofit.Builder()
 * .baseUrl("https://localhost:8443/")
 * .client(trustedOkHttpClient)
 * .addConverterFactory(GsonConverterFactory.create())
 * .build();
 */

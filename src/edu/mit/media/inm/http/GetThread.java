package edu.mit.media.inm.http;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import edu.mit.media.inm.MainActivity;
import edu.mit.media.inm.handlers.PreferenceHandler;

public abstract class GetThread extends AsyncTask<Void, Void, Boolean> {
	public static String TAG = "RequestThread";

	private SSLContext context;
	protected final int id;
	protected String uri;
	protected static String charset = "UTF-8";

	protected final MainActivity ctx;

	protected final int TIMEOUT = 1000;

	protected final PreferenceHandler ph;

	public GetThread(int id, MainActivity ctx) {
		this.id = id;
		this.ctx = ctx;
		this.ph = new PreferenceHandler(ctx);

		InputStream caInput;
		try {
			caInput = new BufferedInputStream(ctx.getAssets()
					.open("server.crt"));

			// Load CAs from an InputStream
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			Certificate ca = cf.generateCertificate(caInput);

			// Create a KeyStore containing our trusted CAs
			String keyStoreType = KeyStore.getDefaultType();
			KeyStore keyStore = KeyStore.getInstance(keyStoreType);
			keyStore.load(null, null);
			keyStore.setCertificateEntry("ca", ca);

			// Create a TrustManager that trusts the CAs in our KeyStore
			String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
			TrustManagerFactory tmf = TrustManagerFactory
					.getInstance(tmfAlgorithm);
			tmf.init(keyStore);

			// Create an SSLContext that uses our TrustManager
			context = SSLContext.getInstance("TLS");
			context.init(null, tmf.getTrustManagers(), null);

			caInput.close();

		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public class NullHostNameVerifier implements HostnameVerifier {
		@Override
		public boolean verify(String hostname, SSLSession arg1) {
			Log.i("RestUtilImpl", "Approving certificate for " + hostname);
			return true;
		}
	}

	/**
	 * Executes the GetMethod and prints some status information.
	 */
	protected Boolean doInBackground(Void... arg0) {
		try {
			URL url = new URL(this.uri);
			Log.d(TAG, this.id + " - Getting from server.");
			HttpsURLConnection
					.setDefaultHostnameVerifier(new NullHostNameVerifier());
			HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

			conn.setSSLSocketFactory(context.getSocketFactory());

			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "text/html");
			conn.setRequestProperty("Accept-Charset", charset);
			conn.setRequestProperty(
					"Authorization",
					"Basic "
							+ Base64.encodeToString((ph.username() + ":" + ph
									.password()).getBytes(), Base64.NO_WRAP));

			conn.setConnectTimeout(TIMEOUT);

			InputStream in = new BufferedInputStream(conn.getInputStream());

			BufferedReader r = new BufferedReader(new InputStreamReader(in));
			StringBuilder total = new StringBuilder();
			String line;
			while ((line = r.readLine()) != null) {
				total.append(line);
			}

			conn.disconnect();
			handleResults(total.toString());
			return true;
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	protected abstract void handleResults(String result);

	protected abstract void onPostExecute(Boolean result);
}
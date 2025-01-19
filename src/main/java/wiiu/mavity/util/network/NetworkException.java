package wiiu.mavity.util.network;

import java.net.URL;

public class NetworkException extends RuntimeException {

	public static String errorRequest(String url) {
		return "An error occurred during a request to url '" + url + "'";
	}

	public NetworkException(String message) {
		super(message);
	}

	public NetworkException(URL url) {
		this(errorRequest(url.toString()));
	}

	public NetworkException(URL url, Throwable cause) {
		this(errorRequest(url.toString()), cause);
	}

	public NetworkException(String message, Throwable cause) {
		super(message, cause);
	}
}
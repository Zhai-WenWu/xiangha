package aplug.web.tools;

import android.os.Handler;
import android.os.Looper;

public class JsBase {
	protected String TAG = "";
	protected Handler handler;
	public JsBase(){
		handler = new Handler(Looper.getMainLooper());
	}
}

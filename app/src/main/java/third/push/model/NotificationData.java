package third.push.model;

import com.xiangha.R;

/**
 * PackageName : third.push.model
 * Created by MrTrying on 2016/8/2 10:37.
 * E_mail : ztanzeyu@gmail.com
 */
public class NotificationData {

	public int type = 0;
	public String url = "";
	public String value = "";

	public int notificationId = 0;
	public int iconResId = R.drawable.ic_launcher;
	public String ticktext = "";
	public String title = "";
	public String content = "";
	public String channel = "";
	public String umengMessage = "";
	public Class<?> startAvtiviyWhenClick = null;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public int getNotificationId() {
		return notificationId;
	}

	public void setNotificationId(int notificationId) {
		this.notificationId = notificationId;
	}

	public int getIconResId() {
		return iconResId;
	}

	public void setIconResId(int iconResId) {
		this.iconResId = iconResId;
	}

	public String getTicktext() {
		return ticktext;
	}

	public void setTicktext(String ticktext) {
		this.ticktext = ticktext;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Class<?> getStartAvtiviyWhenClick() {
		return startAvtiviyWhenClick;
	}

	public void setStartAvtiviyWhenClick(Class<?> startAvtiviyWhenClick) {
		this.startAvtiviyWhenClick = startAvtiviyWhenClick;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getUmengMessage() {
		return umengMessage;
	}

	public void setUmengMessage(String umengMessage) {
		this.umengMessage = umengMessage;
	}
}

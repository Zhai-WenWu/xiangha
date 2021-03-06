package amodule.article.view;

import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.xiangha.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import amodule.article.view.richtext.RichText;

/**
 * PackageName : amodule.article.view
 * Created by MrTrying on 2017/5/19 09:42.
 * E_mail : ztanzeyu@gmail.com
 */
public class EditTextView extends BaseView {

    boolean isDialogShow = false;
    private RichText mRichText;
    boolean needRefreshCenter = false;

    private OnFocusChangeCallback mOnFocusChangeCallback;

    //CENTER_HORIZONTAL
    private boolean isCenterHorizontal = false;

    /** emoji拦截 */
    private InputFilter emojiFilter = new InputFilter() {
        Pattern emoji = Pattern.compile(
                "[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
                Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);

        @Override
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            Matcher emojiMatcher = emoji.matcher(source);
            if (emojiMatcher.find()) {
                return dest.toString().substring(dstart, dend);
            }
            return null;
        }
    };

    public EditTextView(Context context) {
        this(context, null);
    }

    public EditTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EditTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.a_article_view_edit, this);
        init();
    }

    @Override
    public void init() {
        mRichText = (RichText) findViewById(R.id.rich_text);
        mRichText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (mOnFocusChangeCallback != null) {
                    mOnFocusChangeCallback.onFocusChange(EditTextView.this, hasFocus);
                }
            }
        });
        mRichText.setFilters(new InputFilter[]{emojiFilter});
        mRichText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                needRefreshCenter = after == 0
                            && start  >= 0
                            && start + 1< s.toString().length()
                            && '\n' == s.charAt(start);
                if(needRefreshCenter)
                    mRichText.centerFormat(mRichText.previousLineContainCenter());
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (onAfterTextChanged != null) {
                    onAfterTextChanged.afterTextChanged(s);
                }
            }
        });
        mRichText.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                switch (keyCode){
                    case KeyEvent.KEYCODE_DEL:
                        if(mRichText.getSelectionStart() == 0){
                            mRichText.centerFormat(false);
                        }
                        break;
                }
                return false;
            }
        });
        mRichText.setOnSelectTypeCallback(new RichText.OnSelectContainsType() {
            @Override
            public void onSelectBold(boolean isSelected) {
                if (onSelectBoldCallback != null) onSelectBoldCallback.onSelectBold(isSelected);
            }

            @Override
            public void onSelectUnderline(boolean isSelected) {
                if (onSelectUnderlineCallback != null)
                    onSelectUnderlineCallback.onSelectUnderline(isSelected);
            }

            @Override
            public void onSelecrCenter(boolean isSelected) {
                if (onSelectCenterCallback != null)
                    onSelectCenterCallback.onSelectCenter(isSelected);
            }

            @Override
            public void onSelectLink(String url, String desc, final int startIndex,final int endIndex) {
                if (isDialogShow) return;
                isDialogShow = true;
                final InputUrlDialog dialog = new InputUrlDialog(getContext());
                dialog.setDescDefault(desc);
                dialog.setUrl(url);
                dialog.setOnDismissListener(dialog1 -> isDialogShow = false);
                dialog.setOnReturnResultCallback(
                        new InputUrlDialog.OnReturnResultCallback() {
                            @Override
                            public void onSure(String url, String desc) {
                                mRichText.link(url, startIndex, endIndex);
                                dialog.dismiss();
                            }

                            @Override
                            public void onCannel() {
                                dialog.dismiss();
                            }
                        });
                dialog.show();
            }
        });
    }

    @Override
    public JSONObject getOutputData() {
        JSONObject jsonObject = new JSONObject();
        try {
            //正则处理html标签
            SpannableStringBuilder ssbuilder = new SpannableStringBuilder();
            ssbuilder.append(delHTMLTag(mRichText.getText()));
            final int selecttionEnd = mRichText.getSelectionEnd();
            mRichText.setText(ssbuilder);
            mRichText.setSelection(selecttionEnd < ssbuilder.length() ? selecttionEnd : ssbuilder.length());
            //拼接正式数据
            StringBuilder builder = new StringBuilder();
            builder.append("<p align=\"").append(isCenterHorizontal ? "center" : "left").append("\">")
                    .append(TextUtils.isEmpty(mRichText.getText()) ? "" : mRichText.toHtml())
                    .append("<br></p>");
            jsonObject.put("html", builder.toString());
            jsonObject.put("type", TEXT);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public void setTextFrormHtml(String html) {
        if (mRichText != null)
            mRichText.fromHtml(html);
    }

    public void appendText(Editable text) {
        if (mRichText != null) {
            SpannableStringBuilder builder = new SpannableStringBuilder();
            builder.append(mRichText.getText()).append(text);
            mRichText.setText(builder);
        }
    }

    public void setText(CharSequence text) {
        if (mRichText != null)
            mRichText.setText(text);
    }

    public void setSelection(int index) {
        if (mRichText != null)
            mRichText.setSelection(index);
    }

    public String getTextHtml() {
        return mRichText.toHtml();
    }

    public Editable getText() {
        if (mRichText != null)
            return mRichText.getEditableText();
        return null;
    }

    /**
     * @param isInput 是否弹起键盘
     */
    public void setEditTextFocus(boolean isInput) {
        mRichText.setFocusable(true);
        mRichText.setFocusableInTouchMode(true);
        mRichText.requestFocus();
        // 手动弹出键盘
        if (isInput) {
            InputMethodManager inputManager = (InputMethodManager) mRichText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.showSoftInput(mRichText, 0);
        }
    }

    public void setupTextLink(String url, String desc, int start, int end) {
        if (!TextUtils.isEmpty(desc)) {
            mRichText.getText().replace(start, end, desc);
            end = start + desc.length();
        }
        // When KnifeText lose focus, use this method
        mRichText.link(url, start, end);
    }

    public void addLinkToData(String url, String desc) {
        mRichText.addLinkMapToArray(url, desc);
    }

    public void setupTextBold() {
        if (mRichText.containLink()) {
            Toast.makeText(getContext(), "不能包含链接", Toast.LENGTH_SHORT).show();
            return;
        }
        mRichText.bold(!mRichText.contains(RichText.FORMAT_BOLD));
    }

    public void setupUnderline() {
        if (mRichText.containLink()) {
            Toast.makeText(getContext(), "不能包含链接", Toast.LENGTH_SHORT).show();
            return;
        }
        mRichText.underline(!mRichText.contains(RichText.FORMAT_UNDERLINED));
    }

    public void setCenterHorizontal(boolean isCenterHorizontal) {
        this.isCenterHorizontal = isCenterHorizontal;
        mRichText.setGravity(isCenterHorizontal ?
                Gravity.CENTER_HORIZONTAL : Gravity.TOP | Gravity.START);
    }

    public void setupTextCenter() {
        mRichText.centerFormat(!mRichText.contains(RichText.FORMAT_CENTER));
//        mRichText.bullet(!mRichText.contains(RichText.FORMAT_BULLET));
    }

    public int getSelectionStart() {
        return mRichText.getSelectionStart();
    }

    public int getSelectionEnd() {
        return mRichText.getSelectionEnd();
    }

    public OnFocusChangeCallback getOnFocusChangeCallback() {
        return mOnFocusChangeCallback;
    }

    public void setOnFocusChangeCallback(OnFocusChangeCallback mOnFocusChangeCallback) {
        this.mOnFocusChangeCallback = mOnFocusChangeCallback;
    }

    public String getSelectionText() {
        int start = getSelectionStart();
        int end = getSelectionEnd();
        return mRichText.getText()
                .subSequence(start, end)
                .toString();
    }

    public CharSequence getSelectionEndContent() {
        Editable editable = mRichText.getText();
        CharSequence text = editable.subSequence(getSelectionEnd(), editable.length());
        setText(editable.subSequence(0, getSelectionEnd()));
        return text;
    }

    public List<Map<String, String>> getLinkMapArray() {
        return mRichText.getLinkMapArray();
    }

    public void putLinkMapArray(List<Map<String, String>> urls){
        mRichText.putLinkMapArray(urls);
    }

    public interface OnFocusChangeCallback {
        public void onFocusChange(EditTextView v, boolean hasFocus);
    }

    private static final String regEx_script = "<script[^>]*?>[\\s\\S]*?<\\/script>"; // 定义script的正则表达式
    private static final String regEx_style = "<style[^>]*?>[\\s\\S]*?<\\/style>"; // 定义style的正则表达式
    private static final String regEx_html = "<[^>]+>"; // 定义HTML标签的正则表达式
    private static final String regEx_space = "\\s*|\t|\r|\n";//定义空格回车换行符

    /**
     * @param htmlStr
     *
     * @return 删除Html标签
     */
    private Editable delHTMLTag(Editable htmlStr) {
        // 过滤script标签
        htmlStr = delTag(htmlStr, regEx_script);
        // 过滤style标签
        htmlStr = delTag(htmlStr, regEx_style);
        // 过滤html标签
        htmlStr = delTag(htmlStr, regEx_html);
        // 过滤空格回车标签
//        htmlStr = delTag(htmlStr, regEx_space);
        // 返回文本字符串
        return htmlStr;
    }

    private Editable delTag(Editable htmlStr, String regEx) {
        Pattern pattern = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(htmlStr);
        // 过滤script标签
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            if (start == end
                    || start < 0 || start > htmlStr.length()
                    || end < 0 || end > htmlStr.length()) {
                break;
            }
            htmlStr = htmlStr.replace(start, end, "");
            matcher = pattern.matcher(htmlStr);
        }
        return htmlStr;
    }

    public RichText getRichText() {
        return mRichText;
    }

    private OnAfterTextChanged onAfterTextChanged;

    public void setOnAfterTextChanged(OnAfterTextChanged onAfterTextChanged) {
        this.onAfterTextChanged = onAfterTextChanged;
    }

    public OnSelectBoldCallback onSelectBoldCallback;
    public OnSelectUnderlineCallback onSelectUnderlineCallback;
    public OnSelectCenterCallback onSelectCenterCallback;

    public void setOnSelectBoldCallback(OnSelectBoldCallback onSelectBoldCallback) {
        this.onSelectBoldCallback = onSelectBoldCallback;
    }

    public void setOnSelectUnderlineCallback(OnSelectUnderlineCallback onSelectUnderlineCallback) {
        this.onSelectUnderlineCallback = onSelectUnderlineCallback;
    }

    public void setOnSelectCenterCallback(OnSelectCenterCallback onSelectCenterCallback) {
        this.onSelectCenterCallback = onSelectCenterCallback;
    }

    public interface OnAfterTextChanged {
        public void afterTextChanged(Editable s);
    }

    public interface OnSelectBoldCallback {
        public void onSelectBold(boolean isSelected);
    }

    public interface OnSelectUnderlineCallback {
        public void onSelectUnderline(boolean isSelected);
    }

    public interface OnSelectCenterCallback{
        public void onSelectCenter(boolean callback);
    }

}

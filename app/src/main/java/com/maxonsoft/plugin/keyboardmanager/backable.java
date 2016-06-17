package com.maxonsoft.plugin.keyboardmanager;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

/**
 * Created by sungwon on 2016-05-25.
 */

public class backable extends EditText { //꼴랑 뒤로가기 가능하게 만드려고
    public backable(Context context) { super(context); }
    public backable(Context context, AttributeSet attrs) { super(context, attrs); }
    public backable(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

    @Override public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        //이걸 이용해 숨겨논 백키 입력을 가로챌수 있음. 꼴랑 이거 하려고...
        //MainActivity.m.chats.setText("p]"+keyCode +"\r\n"+MainActivity.m.chats.getText().toString());
        if(keyCode == KeyEvent.KEYCODE_BACK && onBackPress != null) onBackPress.run();
        return super.onKeyPreIme(keyCode,event);
    }
    private Runnable onBackPress = null;
    public void setOnBackPressListener(Runnable r){onBackPress = r;}
}
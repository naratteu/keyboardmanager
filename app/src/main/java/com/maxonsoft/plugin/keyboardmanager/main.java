package com.maxonsoft.plugin.keyboardmanager;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.unity3d.player.UnityPlayer;

public class main{
    public static main getKeyboard() { return new main(); }
    main(){ init(); }

    private class unitySendAddress{
        String to,tm;
        void init(String targetObject,String targetMethod){
                    to = targetObject;  tm = targetMethod;
        }
        void send(String string) {
            if (to != null && tm != null)
                UnityPlayer.UnitySendMessage(to, tm, string);
        }
    }
    private unitySendAddress FinishedText = new unitySendAddress();//null냅두다가 크래시만들지 말고..
    public void setWhereToSendFinishedText(String targetObject,String targetMethod){ FinishedText.init(targetObject,targetMethod); }

          private byte                        typeNum=0;
    final private int Default               = typeNum++;//	현재 입력을 위한 기본 키보드를 나타냅니다.
    final private int ASCIICapable          = typeNum++;//	표준 아스키 문자를 보여주는 키보드를 나타냅니다.
    final private int NumbersAndPunctuation = typeNum++;//	숫자와 구두점을 가진 키보드를 나타냅니다.
    final private int URL                   = typeNum++;//	URL 입력을 위해 최적화된 키보드를 나타내며, \".\", \"/\", 와 \".com\"
    final private int NumberPad             = typeNum++;//	PIN 입력을 위해 만들어진 숫자 키패드를 나타내며, 숫자 0부터 9까지 뚜렷하게 나타납니다.//0~9이외엔 불가능
    final private int PhonePad              = typeNum++;//	전화번호 입력을 위한 키패드를 나타내며, 숫자 0부터 9까지 그리고 \"*\" 과 \"#\" 문자가 나타납니다.
    final private int NamePhonePad          = typeNum++;//	사람의 이름 또는 전화번호의 입력을 위해 만들어진 키패드를 나타냅니다.
    final private int EmailAddress          = typeNum++;//	이메일 주소를 입력하는 데 최적화된 키보드를 나타내며, \"@\",
    //유니티의 키보드타입값과 일치한다. //하지만 유니티쪽 타입값을 int로 형변환 시켜줘야함
    // 예를 들어 ->(int)TouchScreenKeyboardType.Default
    final private int[] keyboardType = new int[typeNum];{
        keyboardType[Default]               = InputType.TYPE_CLASS_TEXT;//뭐 완성이지
        keyboardType[ASCIICapable]          = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES;//해석은 안되는데 일단 똑같이 작동 초기, 점찍스페이스 후 대문자
        keyboardType[NumbersAndPunctuation] = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED;//완성! 마이너스 소숫점
        keyboardType[URL]                   = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI;//완성!
        keyboardType[NumberPad]             = InputType.TYPE_CLASS_NUMBER;//완성! 0~9이외엔 이미 불가능처리인거같지만, 셀프필터를 넣어야 할까?
        keyboardType[PhonePad]              = InputType.TYPE_CLASS_PHONE;//완성! 유니티에서도 설명안한 키가 입력됨. 그리고 설명안한 키를 막음. 동일한듯.
        keyboardType[NamePhonePad]          = InputType.TYPE_CLASS_TEXT;//todo 디폴트랑 모가다른건지 모르게씀
        keyboardType[EmailAddress]          = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;//완성!
    }
    final private int[] secureByType = new int[typeNum];{
        secureByType[Default]               = InputType.TYPE_TEXT_VARIATION_PASSWORD;//완벽
        secureByType[ASCIICapable]          = InputType.TYPE_TEXT_VARIATION_PASSWORD;//완벽
        secureByType[NumbersAndPunctuation] = InputType.TYPE_NUMBER_VARIATION_PASSWORD;//완벽
        secureByType[URL]                   = InputType.TYPE_TEXT_VARIATION_PASSWORD;//...
        secureByType[NumberPad]             = InputType.TYPE_NUMBER_VARIATION_PASSWORD;//완벽
        secureByType[PhonePad]              = InputType.TYPE_TEXT_VARIATION_PASSWORD;//...
        secureByType[NamePhonePad]          = InputType.TYPE_TEXT_VARIATION_PASSWORD;
        secureByType[EmailAddress]          = InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD;
    }


    private String rawText;
    private int rawType;
    private boolean rawSecure;
    private String rawHint;
    public void resetRaw(){
        rawText = "";
        rawType = Default;
        rawSecure = false;
        rawHint = "";

        wasCanceled = false;
    }{resetRaw();}

    public void setRawText(String text){ rawText = text; }
    public void setRawType(int type){ rawType = type; }
    public void setRawSecure(boolean secure){ rawSecure = secure; }
    public void setRawHint(String hint){ rawHint = hint; }

    public void openRaw(){ UCA.runOnUiThread(new Runnable() { @Override public void run() { keyboardDialog.show(); }}); }
    /**유니티상에서 어떻게 작성해야할지는
     * https://developer.android.com/reference/android/text/InputType.html
     * 참조
     */ //(참고)setRaw들은 open직전에 적용되는거라 키보드 뜨워진 중간엔 변경안됨~


    //(참고)유니티키보드오픈함수//public static PluginKeyboard Open(string text, TouchScreenKeyboardType keyboardType,/*안씀// bool autocorrection, bool multiline*/, bool secure,/*안씀// bool alert*/, string textPlaceholder)
    public void open(String text, int type, boolean secure, String hint){
        resetRaw();

        setRawText(text);
        setRawSecure(secure);
        setRawType(typeToRawType(type,rawSecure));//요기서 일단secure모드와 합친다.
        setRawHint(hint);

        openRaw();
    }//키보드를 열어요(유니티랑 닮게)

    public int typeToRawType(int type, boolean secure){
        type = ((type%typeNum)+typeNum)%typeNum;//보정
        return keyboardType[type] | (secure?secureByType[type]:0);
    }//유니티의 inputtype옵션을 안드로이드용으로 변경

    public void close(){ keyboardDialog.dismiss(); }

    private Dialog keyboardDialog;
    private backable textBox; //send와back은 일회용이라 따로 선언하지 않는다
    static Activity UCA;//unityCurrentActivity;//나름 최적화용?
    private void init(){
        UCA = UnityPlayer.currentActivity;
        final Handler ucaH = new Handler(UCA.getMainLooper());
                ucaH.post(new Runnable() { @Override public void run() {
            keyboardDialog = new Dialog(UCA,getId("android:Theme.Translucent.NoTitleBar.Fullscreen","style")){
                @Override protected void onCreate(Bundle savedInstanceState){
                    super.onCreate(savedInstanceState);
                    init();
                }
                private final void init(){
                    setContentView(getId("textinput","layout"));

                    textBox = (backable)findViewById(getId("textbox","id"));

                //    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                  //  getWindow().getAttributes().width = WindowManager.LayoutParams.MATCH_PARENT;
                    //투명풀스크린이면서 adjustresize가 먹히는 다이얼로그를 만드는 우회코드였지만 파편화때문에 이제 그런거 필요없어...


                    findViewById(getId("send","id")).setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) {
                        sendtext();
                    }});//보내기버튼을 누르면 보내짐//람다 직접 써보려 했는데 지원안한다고라?
                    textBox.setOnKeyListener(new View.OnKeyListener() { @Override public boolean onKey(View v, int keyCode, KeyEvent event) {
                        if(event.getAction() == KeyEvent. ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER)
                            sendtext();
                        return false;
                    }});
                    textBox.setOnEditorActionListener(new TextView.OnEditorActionListener() { @Override public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        sendtext();
                        //확인누르면 지맘대로 내려가는놈 어째//노액션플래그로 해결?
                        return false;
                    }});//보내기버튼을 누르거나 엔터,확인을 누르면 채팅을 전송한다.
                    //엔터버튼과 에디터액션버튼은 동일한것이라 둘중 하나만 써도 되지만 혹시 모르니깐 두갠데 가능하면 하나 지워버리고싶다.

                    textBox.setOnBackPressListener(new Runnable() { @Override public void run() {
                        //백키를 눌럿을경우는 조금 특별하다.
                        //다른건 모두 정상입력종료처리지만 얘만 [취소]종료처리기 때문에
                        cancel();
                        //근데 배경화면 터치도 취소처리로 하기로 해서 이제 안특별ㅎㅎ
                        //setText(rawText);//유니티에서 하기 초기화는 입력된 내용을 초기화 시켜야 한다.
                    }});
                    findViewById(getId("back","id")).setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) {
                        cancel();
                    }});//백키를 누르거나 뒷배경을 누르면 키보드와 키보드 박스가 사라진다
                    findViewById(getId("cancel","id")).setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) {
                        cancel();
                    }});//캔슬버튼 추가
                    //keyboardoff(textBox);//이거 안해도 키보드가 알아서 잘 사라짐

  //                  textBox.addTextChangedListener(new TextWatcher() {
    //                    /*안쓰는데 지우면 욕함*/@Override public void beforeTextChanged(CharSequence s,int start,int count,int after) {}
      //                  /*안쓰는데 지우면 욕함*/@Override public void onTextChanged(CharSequence s,int start,int before,int count) {}
        //                @Override public void afterTextChanged(Editable s) {
          //                  WrittenText.send(s.toString());
            //            }
              //          // b->t->a 순서로 작동함
                //    });//텍스트가 변경될때마다 유니티로 쏴준다.

//                    back.setOnSizeChangedListener(new Runnable() { @Override public void run() {
  //                      keyboardBottom.send("");//뭔 정보를 주는건 아니고 알아서 가져가라고 알려만 주는거임
    //                }});//키보드가 올라오거나 내려와서 레이아웃크기가 반응하면 세로해상도 대 edittext의 화면Y를 백분율로 유니티에 보내준다.

                    setOnShowListener(new OnShowListener() { @Override public void onShow(DialogInterface dialog) {
                        keyboardon(rawText,rawType,rawSecure,rawHint); //내가 생기자 마자 키보드도 생기고, 사라질때도 같이사라지고
                    }});
                }

                final InputMethodManager imm = (InputMethodManager)UCA.getSystemService(UCA.INPUT_METHOD_SERVICE);
                private void keyboardon (String text, int type, boolean secure, String hint){//아마 v는 키보드 포커스를 어디에 잡느냐는 질문이겠지
                    /*textBox.*/setText(text);
                    textBox.setInputType(type);
                    if(secure) textBox.setTransformationMethod(PasswordTransformationMethod.getInstance());//비번 적용안될경우의 보험/phone쪽이 그래
                    textBox.setHint(hint);

                    textBox.requestFocus();//포커스를 주지않아도 안올라가드라... toggle은 다자동이여쓰면서...

                    //키보드 올려
                    imm.showSoftInput(textBox, 0);

                    //안올라갔으면 이따 또올려
                    ucaH.postDelayed(new Runnable() { @Override public void run() {
                        imm.showSoftInput(textBox, 0);
                    }},100);


                    //앱을 켜자마자는 키보드가 안켜져서 쓰레드로 기다렸다가 키보드를 올리는 헛짓을 해봤는데 생각해보니 켜자마자 키보드를 올릴일은 디버그외에는 없다.
                    //라고 생각했는데 다이얼로그를 켜자마자도 키보드가 안올라간다...
                }
                //private void keyboardoff(View v){ imm.hideSoftInputFromWindow(v.getWindowToken(), 0); }//내릴땐 포커스가 왜필요한거지 //그냥 null 넣어도 작동하던듯

                private void sendtext(){
                    String t = textBox.getText().toString();
                    if(!t.isEmpty()){
                        FinishedText.send(t.replace("\r","").replace("\n",""));//개행을 모두 제거후 전송
                        //textBox.setText("");//clear함수 따로 없나?
                    }
                }
            };
        }});//다이얼로그보이기는runOnUiThread로는 안되서 더 쌘 핸들러를 이용해야함.
    }

    private int  getId  (String name,String type){
        return UCA.getResources().getIdentifier(name,type,UCA.getPackageName());
    }//findViewById() 까지 추상화 하려고 했는데 얘는 context내부함수라 다른 뷰에서 같이쓸수가 없다.

    public double getScreenYper(){
        if(textBox == null) return -1;
        int height = UnityPlayer.currentActivity.getApplicationContext().getResources().getDisplayMetrics().heightPixels;
        int[] tbglosh = new int[2]; //textBox.getLocationOnScreen height! ㅋㅋㅋ!
        textBox.getLocationOnScreen(tbglosh);
        return (double)tbglosh[1]/height;
        /**
         * 키보드 다이얼로그가 한번도 켜지지 않았다면 textbox도 생성되지않았을테니 -1
         * 다이얼로그가 켜져있으면 textbox의 위치 제대로 나옴
         * 다이얼로그가 dismiss되면 없는건 아니라 그런지 0으로 나옴
         * */

        //todo ngui처럼 스크린 가로세로중 작은걸 찾아서 -1~1 의 기준으로 삼으면 세로모드에서도 쓸수 있을거같다.
    }

    public boolean active(){
        return  //keyboardDialog != null && //혹시 모르니깐 ㅎㅎ//어차피 사라질일이 없는 객체들이니깐?
                keyboardDialog.isShowing();
    }
    public String getText()    { return textBox.getText().toString(); }
    public void   setText(final String text){
        if(UCA != null) UCA.runOnUiThread(new Runnable() { @Override public void run() {
            textBox.setText(text);
            textBox.setSelection(textBox.length());
        }});//텍스트가 GUI에 표시되서...
    }
    private boolean wasCanceled = false;
    public  boolean wasCanceled() {
        return wasCanceled;
    }
    public void cancel() {
        wasCanceled = true;//취소란걸 명시하고
        close();
    }
}
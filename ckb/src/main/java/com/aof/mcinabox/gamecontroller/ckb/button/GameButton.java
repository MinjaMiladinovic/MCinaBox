package com.aof.mcinabox.gamecontroller.ckb.button;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.aof.mcinabox.definitions.id.AppEvent;
import com.aof.mcinabox.gamecontroller.ckb.achieve.CkbManager;
import com.aof.mcinabox.gamecontroller.ckb.achieve.GameButtonDialog;
import com.aof.mcinabox.gamecontroller.ckb.support.CallCustomizeKeyboard;
import com.aof.mcinabox.gamecontroller.ckb.support.CkbThemeMarker;
import com.aof.mcinabox.gamecontroller.ckb.support.CkbThemeRecorder;
import com.aof.mcinabox.gamecontroller.controller.Controller;
import com.aof.mcinabox.gamecontroller.event.BaseKeyEvent;
import com.aof.utils.ColorUtils;
import com.aof.utils.DisplayUtils;

import java.util.HashMap;

@SuppressLint("ViewConstructor")
public class GameButton extends androidx.appcompat.widget.AppCompatButton implements View.OnTouchListener , AppEvent {

    private CallCustomizeKeyboard mCall;
    private Controller mController;
    private Context mContext;
    private CkbManager mManager;
    private Activity mActivity;

    private int buttonMode;
    private int screenWidth;
    private int screenHeight;

    public final static int MODE_MOVEABLE_EDITABLE = 1;
    public final static int MODE_PREVIEW = 2;
    public final static int MODE_GAME = 3;

    public final static int MAX_KEYMAP_SIZE = 4;
    public final static int MAX_KEY_SIZE_DP = 250;
    public final static int MIN_KEY_SIZE_DP = 20;
    public final static int MIN_TEXT_SIZE_SP = 2;
    public final static int MAX_TEXT_SIZE_SP = 20;
    public final static int MIN_ALPHA_SIZE_PT = 0;
    public final static int MAX_ALPHA_SIZE_PT = 100;
    public final static int MIN_CORNOR_SIZE_PT = 0;
    public final static int MAX_CORNOR_SIZE_PT = 100;
    public final static int MIN_MOVE_DISTANCE = 10;

    public final static int DEFAULT_DESIGN_INDEX = CkbThemeMarker.DESIGN_SIGNLE_FILL;
    public final static int DEFAULT_BUTTON_MODE = MODE_MOVEABLE_EDITABLE;
    public final static int DEFAULT_KEY_SIZE_DP = 50;
    public final static int DEFAULT_CORNOR_SIZE_PT = 20;
    public final static int DEFAULT_ALPHA_SIZE_PT = 30;
    public final static int DEFAULT_TEXT_SIZE_SP = 5;
    public final static String DEFAULT_BACK_COLOR_HEX = "#000000";
    public final static String DEFAULT_TEXT_COLOR_HEX = "#FFFFFF";

    private final static String TAG = "GameButton";
    private final static int KEY_TYPE = KEYBOARD_BUTTON;
    private final static int POINTER_TYPE = MOUSE_POINTER;
    private final static int MOUSE_TYPE = MOUSE_BUTTON;

    public final static int SHOW_ALL = 0;
    public final static int SHOW_IN_GAME = 1;
    public final static int SHOW_OUT_GAME = 2;

    private String[] keyMaps;  //最多映射N个按键
    private int[] keyTypes; //映射的按键的类型
    private CkbThemeRecorder mRecorder; //主题记录器

    private boolean isKeep;  //自动保持
    private boolean isHide; //隐藏
    private float[] keyPos;  // leftPx , topPx
    private float[] keySize;  // widthDp, heightDp
    private int alphaSize; //透明度百分比
    private String keyName; //按键名
    private int textSize; //字体大小
    private boolean viewerFollow; //视角跟随
    private int inputMode = MARK_INPUT_MODE_ALONE; //输入模式 |捕获|独立|
    private int show;
    private boolean isFirstedAdded = false; //被首次创建



    public GameButton( @NonNull Context context, @NonNull CallCustomizeKeyboard call , @NonNull CkbManager manager){
        this(context,call,null,manager);
    }
    public GameButton(Context context, @NonNull CallCustomizeKeyboard call , @Nullable Controller controller , @NonNull CkbManager manager){
        super(context);
        this.mContext = context;
        this.mCall = call;
        this.mController = controller;
        this.mManager = manager;
        if(mContext instanceof Activity){
            this.mActivity = (Activity) mContext;

        }else{
            mActivity = null;
        }
        //初始化
        init();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init(){
        //添加params
        this.setLayoutParams(new ViewGroup.LayoutParams(0,0));
        //屏幕长宽
        screenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
        screenHeight = mContext.getResources().getDisplayMetrics().heightPixels;
        //设定监听
        this.setOnTouchListener(this);
        //初始化状态列表
        this.stateMap = new HashMap<>();
        //初始化默认属性
        initAttribute();
        //更新UI
        updateUI();
    }

    private void initAttribute(){
        if(mController != null){
            this.inputMode = mController.getInputMode();
        }
        mRecorder = new CkbThemeRecorder();
        this.setKeyName("");
        this.setButtonMode(DEFAULT_BUTTON_MODE);
        this.setTextSize(DEFAULT_TEXT_SIZE_SP);

        String[] strs = new String[MAX_KEYMAP_SIZE];
        for(int i =0 ; i < MAX_KEYMAP_SIZE ; i++){
            strs[i] = "";
        }
        this.setKeyMaps(strs);
        this.setKeyTypes(new int[]{KEY_TYPE,KEY_TYPE,KEY_TYPE,KEY_TYPE});
        this.setShow(SHOW_ALL);
        this.setKeep(false);
        this.setViewerFollow(false);
        this.setBackColor(DEFAULT_BACK_COLOR_HEX);
        this.setTextColor(DEFAULT_TEXT_COLOR_HEX);
        this.setKeyPos(0,0);
        this.setKeySize(DEFAULT_KEY_SIZE_DP,DEFAULT_KEY_SIZE_DP);
        this.setCornerRadius(DEFAULT_CORNOR_SIZE_PT);
        this.setAlphaSize(DEFAULT_ALPHA_SIZE_PT);
        this.setDesignIndex(DEFAULT_DESIGN_INDEX);

    }

    public boolean setKeyMaps(String[] map){
        if(map.length == MAX_KEYMAP_SIZE){
            String[] tmp = new String[MAX_KEYMAP_SIZE];
            System.arraycopy(map, 0, tmp, 0, MAX_KEYMAP_SIZE);
            this.keyMaps = tmp;
            return true;
        }else{
            return false;
        }
    }

    public boolean setKeyTypes(int[] types){
        if(types.length == MAX_KEYMAP_SIZE){
            int[] tmp = new int[MAX_KEYMAP_SIZE];
            System.arraycopy(types,0,tmp,0,MAX_KEYMAP_SIZE);
            this.keyTypes = tmp;
            return true;
        }else{
            return false;
        }
    }

    public String[] setKeyMaps(String keyName, int index){
        if(index > MAX_KEYMAP_SIZE || index < 0){
            return null;
        }else{
            keyMaps[index] = keyName;
            return keyMaps;
        }
    }

    public GameButton setButtonMode(int mode){
        if(mode == MODE_GAME || mode == MODE_MOVEABLE_EDITABLE || mode == MODE_PREVIEW) {
            this.buttonMode = mode;
            updateUI();
        }
        return this;
    }

    public void setKeep(boolean isKeep){
        this.isKeep = isKeep;
    }

    public void setHide(boolean isHide){
        this.isHide = isHide;
        //UI刷新
        updateUI();
    }

    public boolean setBackColor(String colorHex){
        int color = ColorUtils.hex2Int(colorHex);
        if( color != 0){
            this.mRecorder.setColors(0,color);
            updateUI();
            return true;
        }
        return false;
    }

    public boolean setTextColor(String colorHex){
        int color = ColorUtils.hex2Int(colorHex);
        if( color != 0 ){
            this.setTextColor(color);
            this.mRecorder.setTextColor(color);
            return true;
        }
        return false;
    }

    public float[] setKeyPos(float x,float y){

        int viewWidth = this.getLayoutParams().width;
        int viewHeight = this.getLayoutParams().height;

        if((int)x > screenWidth - viewWidth){
            x = screenWidth - viewWidth;
        }
        if((int)x < 0){
            x = 0;
        }
        if((int)y > screenHeight - viewHeight){
            y = screenHeight - viewHeight;
        }
        if((int)y < 0){
            y = 0;
        }
        this.setX(x);
        this.setY(y);

        this.keyPos = new float[]{x,y};

        return this.keyPos; //x ,y
    }

    //按键大小用Dp表示
    public boolean setKeySize(float widthDp, float heightDp){
        int widthPx = DisplayUtils.getPxFromDp(mContext,widthDp);
        int heightPx = DisplayUtils.getPxFromDp(mContext,heightDp);
        if( (widthDp <= MAX_KEY_SIZE_DP && heightDp <= MAX_KEY_SIZE_DP) && (widthDp >= MIN_KEY_SIZE_DP && heightDp >= MIN_KEY_SIZE_DP) ){
            ViewGroup.LayoutParams params = this.getLayoutParams();
            params.width = widthPx;
            params.height = heightPx;
            this.requestLayout();
            this.keySize = new float[]{widthDp,heightDp};
            return true;
        }else{
            return false;
        }
    }

    public boolean setCornerRadius(int radius){
        if(radius < MIN_CORNOR_SIZE_PT  || radius > MAX_CORNOR_SIZE_PT){
            return false;
        }else{
            this.mRecorder.setCornerRadiusPt(radius);
            updateUI();
            return true;
        }
    }

    public boolean setAlphaSize(int alphaPt){
        if( alphaPt < MIN_ALPHA_SIZE_PT || alphaPt > MAX_ALPHA_SIZE_PT ) {
            return false;
        }else{
            this.setAlpha(alphaPt * 0.01f);
            this.alphaSize = alphaPt;
            return true;
        }
    }

    public boolean setKeyName(String str){
        if(str != null){
            this.setText(str);
            this.keyName = new String(str);
            return true;
        }else{
            return false;
        }
    }

    public boolean setTextSize(int spValue){
        if( spValue >= MIN_TEXT_SIZE_SP && spValue <= MAX_TEXT_SIZE_SP ){
            this.setTextSize((float) DisplayUtils.getPxFromSp(mContext,spValue));
            this.textSize = spValue;
            return true;
        }else{
            return false;
        }
    }

    public GameButton setShow(int s){
        this.show = s;
        updateUI();
        return this;
    }

    public GameButton setDesignIndex(int index){
        this.mRecorder.setDesignIndex(index);
        updateUI();
        return this;
    }

    public String[] getColorHexs(){
        int[] c = mRecorder.getColors();
        String[] tmp = new String[CkbThemeRecorder.COLOR_INDEX_LENGTH];
        int a;
        for(a = 0; a < c.length ; a++){
            tmp[a] = ColorUtils.int2Hex(c[a]);
        }
        return tmp;
    }

    public void setInputMode(int mode){
        this.inputMode = mode;
        //执行UI刷新
        updateUI();
    }

    public void setViewerFollow(boolean follow){
        this.viewerFollow = follow;
    }

    public GameButton getNewButtonLikeThis(){
        GameButton g = new GameButton(mContext,mCall,mController,mManager);
        g.setButtonMode(this.buttonMode);
        g.setKeyName(this.keyName);
        g.setKeySize(this.keySize[0],this.keySize[1]);
        g.setKeyPos(this.keyPos[0],this.keyPos[1]);
        g.setKeyMaps(this.keyMaps);
        g.setKeyTypes(this.keyTypes);
        g.setBackColor(ColorUtils.int2Hex(mRecorder.getColor(0)));
        g.setTextColor(ColorUtils.int2Hex(mRecorder.getTextColor()));
        g.setAlphaSize(this.alphaSize);
        g.setCornerRadius(this.mRecorder.getCornerRadiusPt());
        g.setTextSize(this.textSize);
        g.setKeep(this.isKeep);
        g.setHide(this.isHide);
        g.setShow(this.show);
        g.setViewerFollow(this.viewerFollow);
        g.setInputMode(this.inputMode);
        g.setDesignIndex(this.mRecorder.getDesignIndex());
        return g;
    }

    private HashMap<String,Boolean> stateMap;
    private void sendKey(String keyName, boolean pressed, int type){

        //该算法可以保证CustomizeKeyboard不会造成clientinput的setkey()方法堵塞;
        if(pressed){

            if( stateMap.containsKey(keyName) && stateMap.get(keyName) ){
                return;
            }
            if( ! stateMap.containsKey(keyName) ){
                stateMap.put(keyName,pressed);
            }
            if( stateMap.containsKey(keyName) && !stateMap.get(keyName) ){
                stateMap.remove(keyName);
                stateMap.put(keyName,pressed);
            }
            mController.sendKey( new BaseKeyEvent(TAG,keyName,pressed,type,null) );

        }else{

            if( stateMap.containsKey(keyName) && stateMap.get(keyName) ){
                stateMap.remove(keyName);
                stateMap.put(keyName,pressed);
                mController.sendKey( new BaseKeyEvent(TAG,keyName,pressed,type,null) );
            }

        }

    }

    public GameButton setFirstAdded(){
        this.isFirstedAdded = true;
        return this;
    }

    public GameButton unsetFirstAdded(){
        this.isFirstedAdded = false;
        return this;
    }

    private boolean isBeingPressed = false;
    private int initialX = 0;
    private int initialY = 0;
    private int baseX = 0;
    private int baseY = 0;
    private void inputPointerEvent(MotionEvent e){
        switch (e.getAction()){
            case MotionEvent.ACTION_DOWN:
                initialX = (int)e.getX();
                initialY = (int)e.getY();
                baseX = mController.getPointer()[0];
                baseY = mController.getPointer()[1];
                break;
            case MotionEvent.ACTION_MOVE:
                int incrementX = (int) (e.getX() - initialX);
                int incrementY = (int) (e.getY() - initialY);
                int resultX = baseX + incrementX;
                int resultY = baseY + incrementY;
                mController.sendKey(new BaseKeyEvent(TAG,null,false,POINTER_TYPE,new int[]{resultX,resultY}));
                break;
            case MotionEvent.ACTION_UP:
                break;

        }
    }

    private void inputKeyEvent(MotionEvent e){
        switch (e.getAction()){
            case MotionEvent.ACTION_DOWN:
                if(isKeep){
                    if(!isBeingPressed){
                        for(int a = 0; a < MAX_KEYMAP_SIZE ; a++){
                            sendKey(keyMaps[a],true,keyTypes[a]);
                        }
                    }
                }else{
                    for(int a = 0; a < MAX_KEYMAP_SIZE ; a++){
                        sendKey(keyMaps[a],true,keyTypes[a]);
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:

                if(isKeep){
                    if(isBeingPressed){
                        for(int a = 0; a < MAX_KEYMAP_SIZE ; a++){
                            sendKey(keyMaps[a],false,keyTypes[a]);
                        }
                        isBeingPressed = false;
                    }else{
                        isBeingPressed = true;
                    }
                }else{
                    for(int a = 0; a < MAX_KEYMAP_SIZE ; a++){
                        sendKey(keyMaps[a],false,keyTypes[a]);
                    }
                }
                break;
            default:
                break;
        }
    }

    private boolean hasDragged = false;
    private int touchPosX;
    private int touchPosY;
    private void editView(MotionEvent e){
        switch(e.getAction()){
            case MotionEvent.ACTION_DOWN:
                touchPosX = (int)e.getRawX();
                touchPosY = (int)e.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                if(hasDragged){
                    int tmpTouchPosX = (int)e.getRawX();
                    int tmpTouchPosY = (int)e.getRawY();
                    int lastPosX = (int)getKeyPos()[0];
                    int lastPosY = (int)getKeyPos()[1];
                    int dx = tmpTouchPosX - touchPosX;
                    int dy = tmpTouchPosY - touchPosY;
                    int viewWidth = getLayoutParams().width;
                    int viewHeight = getLayoutParams().height;
                    int posLeft = dx + lastPosX;
                    int posTop = dy + lastPosY;
                    int posRight = posLeft + viewWidth;
                    int posBottom = posTop + viewHeight;
                    int resultX = posLeft;
                    int resultY = posTop;

                    //判断边界
                    if(posLeft < 0){
                        resultX = 0;
                    }
                    if(posTop < 0){
                        resultY = 0;
                    }
                    if(posRight > screenWidth){
                        resultX = screenWidth - viewWidth;
                    }
                    if(posBottom > screenHeight){
                        resultY = screenHeight - viewHeight;
                    }
                    touchPosX = tmpTouchPosX;
                    touchPosY = tmpTouchPosY;
                    setKeyPos(resultX,resultY);
                }else{
                    if(Math.abs((int)e.getRawX() - touchPosX) >= MIN_MOVE_DISTANCE && Math.abs((int)e.getRawY() - touchPosY) >= MIN_MOVE_DISTANCE){
                        hasDragged = true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if(!hasDragged){
                    new GameButtonDialog(mContext,this,mManager).show();
                }
                hasDragged = false;
                break;
            default:
                break;
        }
    }

    public void addSelfToParent(){
        mManager.addGameButton(this);
    }

    public void removeSelfFromParent(){
        mManager.removeGameButton(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(v == this){
            switch(this.buttonMode){
                case MODE_GAME:
                    switch(this.inputMode){
                        case MARK_INPUT_MODE_ALONE:
                            break;
                        case MARK_INPUT_MODE_CATCH:
                            if(viewerFollow){
                                inputPointerEvent(event);
                            }
                            break;
                    }
                    inputKeyEvent(event);
                    break;
                case MODE_PREVIEW:
                case MODE_MOVEABLE_EDITABLE:
                    editView(event);
                    break;
            }
            return true;
        }

        return false;
    }

    public void updateUI(){

        //判断按键的显示/隐藏
        switch(this.buttonMode){
            case MODE_GAME:
                if(!isHide){
                    switch (inputMode){
                        case MARK_INPUT_MODE_CATCH:
                            if(show == SHOW_ALL || show == SHOW_IN_GAME){
                                this.setVisibility(VISIBLE);
                            }else{
                                this.setVisibility(GONE);
                            }
                            break;
                        case MARK_INPUT_MODE_ALONE:
                            if(show == SHOW_ALL || show == SHOW_OUT_GAME){
                                this.setVisibility(VISIBLE);
                            }else{
                                this.setVisibility(GONE);
                            }
                            break;
                    }
                }else{
                    this.setVisibility(GONE);
                }
                break;
            case MODE_PREVIEW:
            case MODE_MOVEABLE_EDITABLE:
                if(this.getVisibility() == GONE){
                    this.setVisibility(VISIBLE);
                }
                break;
        }
        //刷新背景
        this.setBackground(CkbThemeMarker.getDesign(mRecorder));
    }

    //getter

    public int getButtonMode() {
        return buttonMode;
    }

    public String[] getKeyMaps() {
        return keyMaps;
    }

    public boolean isKeep() {
        return isKeep;
    }

    public String getBackColorHex() {
        return ColorUtils.int2Hex(mRecorder.getColor(0));
    }

    public String getTextColorHex() {
        return ColorUtils.int2Hex(mRecorder.getTextColor());
    }

    public float[] getKeyPos() {
        return keyPos;
    }

    public float[] getKeySize() {
        return keySize;
    }

    public int getTextProgress(){
        return textSize;
    }

    public int getCornerRadius() {
        return mRecorder.getCornerRadiusPt();
    }

    public int getAlphaSize() {
        return alphaSize;
    }

    public String getKeyName(){
        return keyName;
    }

    public boolean isHide(){
        return isHide;
    }

    public boolean isFirstAdded(){
        return isFirstedAdded;
    }

    public boolean isViewerFollow(){
        return viewerFollow;
    }

    public int getDesignIndex(){
        return mRecorder.getDesignIndex();
    }

    public CkbThemeRecorder getThemeRecorder(){
        return this.mRecorder;
    }

    public int[] getKeyTypes(){
        return this.keyTypes;
    }

    public int getShow(){
        return this.show;
    }

}
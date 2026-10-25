package com.vype.browser;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.webkit.*;
import android.widget.*;
import org.json.*;
import java.util.*;

public class MainActivity extends Activity {
    final ArrayList<Tab> tabs = new ArrayList<>();
    final ArrayList<JSONObject> bookmarks = new ArrayList<>(), history = new ArrayList<>();
    LinearLayout root, toolbar, tabsBar, content;
    EditText address;
    WebView web;
    int active = 0;
    android.content.SharedPreferences prefs;
    static class Tab { String url, title; Tab(String u,String t){url=u;title=t;} }
    int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
    @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);prefs=getSharedPreferences("browser",0);loadData();build();switchTab(Math.min(active,tabs.size()-1));}
    void loadData(){
        try { JSONArray a=new JSONArray(prefs.getString("tabs","[]"));for(int i=0;i<a.length();i++){JSONObject o=a.getJSONObject(i);tabs.add(new Tab(o.optString("url","https://www.google.com"),o.optString("title","Вкладка")));}}catch(Exception ignored){}
        if(tabs.isEmpty())tabs.add(new Tab("https://www.google.com","Главная"));
        active=prefs.getInt("active",0);
        readArray("bookmarks",bookmarks);readArray("history",history);
    }
    void readArray(String key,ArrayList<JSONObject> list){try{JSONArray a=new JSONArray(prefs.getString(key,"[]"));for(int i=0;i<a.length();i++)list.add(a.getJSONObject(i));}catch(Exception ignored){}}
    void save(){try{JSONArray a=new JSONArray();for(Tab t:tabs){JSONObject o=new JSONObject();o.put("url",t.url);o.put("title",t.title);a.put(o);}prefs.edit().putString("tabs",a.toString()).putInt("active",active).putString("bookmarks",new JSONArray(bookmarks).toString()).putString("history",new JSONArray(history).toString()).apply();}catch(Exception ignored){}}
    TextView button(String text,Runnable action){TextView v=new TextView(this);v.setText(text);v.setTextColor(Color.WHITE);v.setTextSize(17);v.setGravity(Gravity.CENTER);v.setPadding(dp(11),dp(12),dp(11),dp(12));v.setFocusable(true);v.setBackgroundResource(android.R.drawable.list_selector_background);v.setOnClickListener(w->action.run());return v;}
    void build(){root=new LinearLayout(this);root.setOrientation(1);root.setBackgroundColor(Color.rgb(22,26,33));setContentView(root);
        tabsBar=new LinearLayout(this);tabsBar.setOrientation(0);root.addView(tabsBar,new LinearLayout.LayoutParams(-1,dp(48)));
        toolbar=new LinearLayout(this);toolbar.setGravity(Gravity.CENTER_VERTICAL);root.addView(toolbar,new LinearLayout.LayoutParams(-1,dp(56)));
        toolbar.addView(button("◀",()->{if(web!=null&&web.canGoBack())web.goBack();}));
        toolbar.addView(button("▶",()->{if(web!=null&&web.canGoForward())web.goForward();}));
        toolbar.addView(button("⟳",()->{if(web!=null)web.reload();}));
        address=new EditText(this);address.setSingleLine(true);address.setTextColor(Color.WHITE);address.setHintTextColor(Color.LTGRAY);address.setHint("Адрес или поиск");address.setHint("Адрес или поиск");address.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_URI);address.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_GO);
        toolbar.addView(address,new LinearLayout.LayoutParams(0,dp(52),1));address.setOnEditorActionListener((v,id,event)->{navigate(address.getText().toString());((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(address.getWindowToken(),0);address.clearFocus();return true;});
        toolbar.addView(button("★",this::addBookmark));toolbar.addView(button("☰",this::showMenu));
        content=new LinearLayout(this);root.addView(content,new LinearLayout.LayoutParams(-1,0,1));refreshTabs();
    }
    void refreshTabs(){tabsBar.removeAllViews();HorizontalScrollView scroll=new HorizontalScrollView(this);scroll.setHorizontalScrollBarEnabled(false);tabsBar.addView(scroll,new LinearLayout.LayoutParams(0,-1,1));LinearLayout inner=new LinearLayout(this);scroll.addView(inner);
        for(int i=0;i<tabs.size();i++){final int index=i;String name=tabs.get(i).title; if(name.length()>15)name=name.substring(0,15)+"…";TextView b=button((i==active?"● ":"")+name,()->switchTab(index));inner.addView(b);}
        tabsBar.addView(button("＋",()->{tabs.add(new Tab("https://www.google.com","Новая вкладка"));switchTab(tabs.size()-1);}));
        tabsBar.addView(button("×",this::closeTab));
    }
    void switchTab(int index){if(index<0||index>=tabs.size())return;if(web!=null){web.stopLoading();web.destroy();web=null;}active=index;content.removeAllViews();web=new WebView(this);web.setFocusable(true);web.setFocusableInTouchMode(true);content.addView(web,new LinearLayout.LayoutParams(-1,-1));
        WebSettings s=web.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setDatabaseEnabled(true);s.setJavaScriptCanOpenWindowsAutomatically(false);s.setSupportMultipleWindows(false);s.setLoadWithOverviewMode(true);s.setUseWideViewPort(true);s.setMediaPlaybackRequiresUserGesture(false);s.setCacheMode(WebSettings.LOAD_DEFAULT);s.setBuiltInZoomControls(true);s.setDisplayZoomControls(false);
        CookieManager.getInstance().setAcceptCookie(true);
        web.setWebViewClient(new WebViewClient(){@Override public boolean shouldOverrideUrlLoading(WebView view,WebResourceRequest request){return !request.getUrl().getScheme().equals("http")&&!request.getUrl().getScheme().equals("https");}
            @Override public void onPageFinished(WebView view,String url){if(view!=web)return;tabs.get(active).url=url;address.setText(url);record(url,view.getTitle());save();}});
        web.setWebChromeClient(new WebChromeClient(){@Override public void onReceivedTitle(WebView view,String title){if(view!=web)return;tabs.get(active).title=title==null?"Вкладка":title;refreshTabs();save();}
            @Override public void onPermissionRequest(PermissionRequest request){request.deny();}});
        address.setText(tabs.get(active).url);web.loadUrl(tabs.get(active).url);refreshTabs();save();
    }
    void navigate(String text){text=text.trim();if(text.isEmpty())return;String url;if(text.matches("(?i)^https?://.*"))url=text;else if(text.contains(".")&&!text.contains(" "))url="https://"+text;else url="https://www.google.com/search?q="+android.net.Uri.encode(text);web.loadUrl(url);}
    void record(String url,String title){if(url==null||url.isEmpty())return;try{JSONObject o=new JSONObject();o.put("url",url);o.put("title",title==null?url:title);o.put("time",System.currentTimeMillis());history.add(0,o);while(history.size()>300)history.remove(history.size()-1);}catch(Exception ignored){}}
    void addBookmark(){if(web==null)return;EditText name=new EditText(this);name.setSingleLine(true);name.setText(web.getTitle()==null?web.getUrl():web.getTitle());new AlertDialog.Builder(this).setTitle("Название закладки").setView(name).setNegativeButton("Отмена",null).setPositiveButton("Сохранить",(d,w)->{try{JSONObject o=new JSONObject();o.put("title",name.getText().toString());o.put("url",web.getUrl());bookmarks.add(o);save();Toast.makeText(this,"Закладка сохранена",Toast.LENGTH_SHORT).show();}catch(Exception ignored){}}).show();}
    void showMenu(){String[] options={"Закладки","История","Новая вкладка","Закрыть вкладку","Очистить историю","Очистить данные сайтов"};new AlertDialog.Builder(this).setTitle("Меню браузера").setItems(options,(d,which)->{switch(which){case 0:showEntries(bookmarks,"Закладки");break;case 1:showEntries(history,"История");break;case 2:tabs.add(new Tab("https://www.google.com","Новая вкладка"));switchTab(tabs.size()-1);break;case 3:closeTab();break;case 4:new AlertDialog.Builder(this).setMessage("Удалить всю историю?").setNegativeButton("Отмена",null).setPositiveButton("Удалить",(x,y)->{history.clear();save();}).show();break;case 5:new AlertDialog.Builder(this).setMessage("Удалить cookies и кэш сайтов?").setNegativeButton("Отмена",null).setPositiveButton("Удалить",(x,y)->{CookieManager.getInstance().removeAllCookies(null);CookieManager.getInstance().flush();if(web!=null){web.clearCache(true);web.clearFormData();}WebStorage.getInstance().deleteAllData();Toast.makeText(this,"Данные сайтов очищены",Toast.LENGTH_SHORT).show();}).show();break;}}).show();}
    void showEntries(ArrayList<JSONObject> entries,String title){String[] names=new String[entries.size()];for(int i=0;i<entries.size();i++)names[i]=entries.get(i).optString("title",entries.get(i).optString("url"));new AlertDialog.Builder(this).setTitle(title).setItems(names,(d,i)->{String url=entries.get(i).optString("url");if(web!=null)web.loadUrl(url);}).setNeutralButton("Удалить / изменить",(d,w)->manageEntries(entries,title)).setNegativeButton("Закрыть",null).show();}
    void manageEntries(ArrayList<JSONObject> entries,String title){String[] names=new String[entries.size()];for(int i=0;i<entries.size();i++)names[i]=entries.get(i).optString("title");new AlertDialog.Builder(this).setTitle("Выберите запись").setItems(names,(d,i)->{String[] actions=entries==bookmarks?new String[]{"Переименовать","Удалить"}:new String[]{"Удалить"};new AlertDialog.Builder(this).setItems(actions,(dialog,action)->{if(entries==bookmarks&&action==0){EditText input=new EditText(this);input.setText(entries.get(i).optString("title"));new AlertDialog.Builder(this).setTitle("Название").setView(input).setPositiveButton("Сохранить",(x,y)->{try{entries.get(i).put("title",input.getText().toString());save();}catch(Exception ignored){}}).setNegativeButton("Отмена",null).show();}else{entries.remove(i);save();}}).show();}).setNegativeButton("Назад",null).show();}
    void closeTab(){if(tabs.size()==1){tabs.set(0,new Tab("https://www.google.com","Главная"));switchTab(0);return;}tabs.remove(active);switchTab(Math.min(active,tabs.size()-1));}
    @Override public void onBackPressed(){if(web!=null&&web.canGoBack())web.goBack();else new AlertDialog.Builder(this).setMessage("Выйти из браузера?").setPositiveButton("Выйти",(d,w)->finish()).setNegativeButton("Отмена",null).show();}
    @Override protected void onPause(){super.onPause();save();if(web!=null)web.onPause();}
    @Override protected void onResume(){super.onResume();if(web!=null)web.onResume();}
    @Override protected void onDestroy(){if(web!=null){web.destroy();web=null;}super.onDestroy();}
}

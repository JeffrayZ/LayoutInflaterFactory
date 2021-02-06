package com.test.layoutinflaterfactory;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.LayoutInflaterCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

public class MainActivity extends AppCompatActivity {
    String TAG = "MainActivity";
    int id = 0;
    Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final Typeface typeface = Typeface.createFromAsset(getAssets(), "hyzhengyuan_65w.ttf");
        LayoutInflaterCompat.setFactory2(LayoutInflater.from(this), new LayoutInflater.Factory2() {
            @Nullable
            @Override
            public View onCreateView(@Nullable View parent, @NonNull String name,
                                     @NonNull Context context, @NonNull AttributeSet attrs) {

                /*AppCompatDelegate delegate = getDelegate();
                View view = delegate.createView(parent, name, context, attrs);
                if(view != null && view instanceof TextView){
                    ((TextView) view).setTypeface(typeface);
                }
                return view;*/

                // =====================================================
                /*Log.e(TAG, "有parent name= " + name);
                int n = attrs.getAttributeCount();
                for (int i = 0; i < n; i++) {
                    Log.e(TAG,
                            "有parent " + attrs.getAttributeName(i) + "," + attrs
                            .getAttributeValue(i));
                }
                if (name.equals("TextView")) {
                    Button button = new Button(context, attrs);
                    return button;
                }
                return null;*/


                // =============================================================================================================

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String apkPath = getCacheDir().getAbsolutePath() + "/skinapp-debug.apk";
                        // 通过反射获取未安装apk的AssetManager
                        AssetManager assetManager = null;
                        try {
                            assetManager = AssetManager.class.newInstance();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InstantiationException e) {
                            e.printStackTrace();
                        }
                        // 通过反射增加资源路径
                        Method method = null;
                        try {
                            method = assetManager.getClass().getMethod("addAssetPath", String.class);
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        }
                        try {
                            method.invoke(assetManager, apkPath);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                        File dexDir = getDir("dex", Context.MODE_PRIVATE);
                        if (!dexDir.exists()) {
                            dexDir.mkdir();
                        }
                        // 获取未安装apk的Resources
                        resources = new Resources(assetManager, getResources().getDisplayMetrics(),
                                getResources().getConfiguration());
                        // 获取未安装apk的ClassLoader
                        ClassLoader classLoader = new DexClassLoader(apkPath, dexDir.getAbsolutePath(),
                                null, getClassLoader());
                        // 反射获取class
                        Class aClass = null;
                        try {
                            aClass = classLoader.loadClass("com.test.skinapp.R$color");
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                        try {
                            id = (int) aClass.getField("bg_main_top").get(null);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (NoSuchFieldException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                View view = getDelegate().createView(parent, name, context, attrs);
                return view;
            }

            @Nullable
            @Override
            public View onCreateView(@NonNull String name, @NonNull Context context,
                                     @NonNull AttributeSet attrs) {
                Log.e(TAG, "name= " + name);
                int n = attrs.getAttributeCount();
                for (int i = 0; i < n; i++) {
                    Log.e(TAG, attrs.getAttributeName(i) + "," + attrs.getAttributeValue(i));
                }
                if (name.equals("TextView")) {
                    Button button = new Button(context, attrs);
                    return button;
                }
                return null;
            }
        });
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_load).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadPlugin(MainActivity.this, "skinapp-debug.apk");
            }
        });

        findViewById(R.id.btn_change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.tv_skin).setBackgroundColor(resources.getColor(id));
            }
        });
    }

    // 用来加载插件
    private String loadPlugin(Context context, String fileName) {
        try {
            File cacheDir = context.getCacheDir();
            if (!cacheDir.exists()) {
                cacheDir.mkdir();
            }
            File outFile = new File(cacheDir, fileName);
            if (!outFile.exists()) {
                boolean res = outFile.createNewFile();
                if (res) {
                    InputStream is = context.getAssets().open(fileName);
                    FileOutputStream os = new FileOutputStream(outFile);
                    byte[] buffer = new byte[is.available()];
                    int byteCount;
                    while ((byteCount = is.read(buffer)) != -1) {
                        os.write(buffer, 0, byteCount);
                    }
                    os.flush();
                    is.close();
                    os.close();
                    Toast.makeText(context, "下载成功", Toast.LENGTH_SHORT).show();
                    return outFile.getAbsolutePath();
                }
            } else {
                Toast.makeText(context, "文件已存在", Toast.LENGTH_SHORT).show();
                return outFile.getAbsolutePath();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
package org.zhoutz.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import android.app.Activity;
import android.os.Environment;
import cn.qemu.launcher.R;

public class ACode{
	public static AlertDialog alertdialog;
	private TextView tx;
	private Activity d;

	public ACode(Activity activity){
		this.d=activity;
	}

	public View loadlayout(int res){
		return View.inflate(this.d, res, null);
	}

	public void clp(CharSequence car){
		android.content.ClipboardManager cm=(android.content.ClipboardManager)this.d.getSystemService(Context.CLIPBOARD_SERVICE);
		android.content.ClipData cd=android.content.ClipData.newPlainText("Label",car);
		cm.setPrimaryClip(cd);
	}
	
	public void tw(Object obj){
		Toast.makeText(this.d, String.valueOf(obj),1).show();
	}

	public void tw(Object obj,int i){
		Toast.makeText(this.d, String.valueOf(obj),i).show();
	}

	public void tw(Object obj,int time,int color){
		View vi = loadlayout(R.layout.toast);
		tx = vi.findViewById(R.id.toast);
		tx.setText(String.valueOf(obj));
		tx.setBackgroundColor(color);
		Toast n=new Toast(this.d);
		n.setDuration(time);
		n.setView(vi);
		n.show();
	}

	public static void sw(View v,Object obj){
		Snackbar.make(v,String.valueOf(obj),Snackbar.LENGTH_SHORT).show();
	}
	
	public static void sw(View v,Object obj,CharSequence act,OnClickListener onc){
		Snackbar.make(v,String.valueOf(obj),Snackbar.LENGTH_SHORT).setAction(act,onc).show();
	}
/*
	public void endkeyboard(){
		View peekDecorView=this.d.getWindow().peekDecorView();
		if(peekDecorView!=null)((InputMethodManager)this.d.getSystemService("input_method")).hideSoftInputFromWindow(peekDecorView.getWindowToken(),0);
	}
*
	public void ends(){
		Intent intent = new Intent("android.intent.action.MAIN");
		intent.addCategory("android.intent.category.HOME");
		intent.addFlags(270532608);
		this.d.startActivity(intent);
	}*

	public void endutw() {
		if (alertdialog != null) {
			alertdialog.dismiss();
			alertdialog = null;
		}
	}*/

	public void hws(String str){
		this.d.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(str)));
	}

	public void fo(String str){
		String path="file://"+str;
		hws(path);
	}

	public static boolean fi(String lj){
		return new File(lj).isDirectory();
	}

	public static boolean fe(String lj){
		return new File(lj).isFile();
	}

	public static long fs(String lj){
		return new File(lj).length();
	}

	public static boolean fw(String path,String intext){
		try{
			File file=new File(path);file.getParentFile().mkdir();
			FileWriter wre=new FileWriter(file);
			wre.write(intext);
			wre.close();
		}catch(IOException e){
			return false;
		}
		return true;
	}

	public static String fr(String path){
		try{
			FileInputStream fin=new FileInputStream(path);
			byte[] bit=new byte[fin.available()];
			fin.read(bit);
			return new String(bit);
		}catch (IOException e){
			return null;
		}
	}

	public static String[] fl(String str){
        return new File(str).list();
	}

	public static Object[] fl(String str, boolean z) {
		String[] n=new File(str).list();
		ArrayList<String> ar=new ArrayList<String>();
		if(!str.endsWith("/"))str=str+"/";
		if(z){
			for(String a:n)
			if(fi(str+a))ar.add(a);
		}else{
			for(String a:n)
			if(fe(str+a))ar.add(a);
		}
		Object[] or=ar.toArray();
		return or;
    }

	public static boolean fd(String str) {
		return ActivityUtil.rmDir(new File(str));
	}

	public static String sj(String str,String str2,String str3){
		int indexOf;
		int length;
		if(str2!=null){
			indexOf=str.indexOf(str2);
			if(indexOf==-1)return null;
			length=indexOf+str2.length();
		}else length=0;
		if(str3==null)
			indexOf=str.length();
        else{
			indexOf=str.indexOf(str3,length);
			if(indexOf==-1)return null;
		}
		return str.substring(length,indexOf);
	}

	private AlertDialog.Builder utwf(CharSequence title,CharSequence[] button,DialogInterface.OnClickListener[] run){
		Builder bu=new Builder(this.d).setTitle(title);
		if(button.length==run.length){
			if(button.length>=1)bu.setPositiveButton(button[0],run[0]);
			if(button.length>=2)bu.setNegativeButton(button[1],run[1]);
			if(button.length==3)bu.setNeutralButton(button[2],run[2]);
		}
		return bu;
	}
	
	public View utw(Integer icon,CharSequence title,View view,CharSequence[] button, boolean isl, DialogInterface.OnClickListener[] event) {
        Builder negativeButton =utwf(title,button,event);
        if(icon!=null)negativeButton.setIcon(icon);
        if(view!=null)negativeButton.setView(view);
        alertdialog=negativeButton.create();
        if(isl){
            alertdialog.setCanceledOnTouchOutside(false);
            alertdialog.setCancelable(false);
		}alertdialog.show();
        return view;
    }

    public void utw(Integer icon,CharSequence title,CharSequence msg,CharSequence[] button,boolean isl,DialogInterface.OnClickListener[] event) {
        Builder neutralButton=utwf(title,button,event);
		if(icon!=null)neutralButton.setIcon(icon);
        if(msg!=null)neutralButton.setMessage(msg);
        alertdialog=neutralButton.create();
        if(isl){
            alertdialog.setCanceledOnTouchOutside(false);
            alertdialog.setCancelable(false);
		}
		alertdialog.show();
    }

    public void utw(Integer icon,CharSequence title,CharSequence msg,boolean isl) {
		Builder tit=new Builder(this.d).setTitle(title);
        if(icon!=null)tit.setIcon(icon);
        if(msg!=null)tit.setMessage(msg);
		alertdialog=tit.create();
        if (isl) {
            alertdialog.setCanceledOnTouchOutside(false);
            alertdialog.setCancelable(false);
        }
		alertdialog.show();
    }
	
	public void utw(Integer icon,CharSequence title,View view,boolean isl) {
		Builder tit=new Builder(this.d).setTitle(title);
        if(icon!=null)tit.setIcon(icon);
        if(view!=null)tit.setView(view);
		alertdialog=tit.create();
        if(isl){
            alertdialog.setCanceledOnTouchOutside(false);
            alertdialog.setCancelable(false);
		}
		alertdialog.show();
    }

	public void uycl(boolean z) {
        if(z){
            this.d.getWindow().clearFlags(2048);
            this.d.getWindow().setFlags(1024,1024);
        }
        this.d.getWindow().addFlags(2048);
    }

	public static boolean ha(String str,boolean z) {
        return z?new File(str).mkdirs():new File(new File(str).getParent()).mkdirs();
    }
	
	public static String shell(String[] cmd) throws Exception{
		return shell(cmd, false);
	}

	public static String shell(String[] cmd,boolean isError) throws Exception{
		String result = "";
		Process mProcess=Runtime.getRuntime().exec(cmd);
		mProcess.waitFor();
		if (isError) {
			InputStream isr=mProcess.getErrorStream();
			byte[] bt=new byte[isr.available()];
			isr.read(bt);
			isr.close();
			result=new String(bt)+"\n";
		}
		InputStream ir=mProcess.getInputStream();
		byte[] bt=new byte[ir.available()];
		ir.read(bt);
		ir.close();
		result+=new String(bt);
		return result;
	}

	public static String shell(String cmd) throws IOException{
		Process mProcess=Runtime.getRuntime().exec(cmd);
		InputStream is=mProcess.getInputStream();
		byte[] bs=new byte[is.available()];
		is.read(bs);
		is.close();
		return new String(bs);
	}
}

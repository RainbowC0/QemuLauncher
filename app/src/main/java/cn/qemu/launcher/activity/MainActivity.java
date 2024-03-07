package cn.qemu.launcher.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.app.AlertDialog;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.view.menu.MenuBuilder;
import java.lang.reflect.Method;
import android.text.InputType;
import android.content.res.Resources;
import android.view.Gravity;
import android.support.v7.widget.Toolbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.design.widget.NavigationView;
import android.content.SharedPreferences;
import android.view.SubMenu;
import android.util.TypedValue;
import cn.qemu.launcher.R;
import org.zhoutz.utils.*;
import android.support.graphics.drawable.VectorDrawableCompat;
import cn.qemu.launcher.fragment.*;
import android.view.View.*;
import java.util.*;
import android.support.v7.widget.ThemeUtils;
import android.support.v4.app.*;
import android.*;
import android.content.pm.*;
import android.os.*;
import cn.qemu.launcher.application.*;
import android.support.design.widget.*;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
	private String HOME,SH_PATH;
	public static String SDHOME = android.os.Environment.getExternalStorageDirectory().getPath(), SELECTEDPATH="_selectedpath";
	private String[] fragtag = new String[4];
	private static long last = System.currentTimeMillis()-2000L;
	private Fragment[] fraglist = new Fragment[4];
	private FrameLayout fam;
	DrawerLayout drawer;
	byte lastfrag = 0;
	public ACode in = new ACode(this);
	public int RED;
	public int GREEN;
	public int DEFAULT;

	@Override
	public void onCreate(Bundle savedInstanceState){
		SharedPreferences set = getSharedPreferences(MainApplication.PREF_SETTINGS, Context.MODE_PRIVATE);
		if (MainApplication.IS_DARK)
			setTheme(R.style.AppTheme_Dark);
		super.onCreate(savedInstanceState);
		SH_PATH = set.getString("sh_path",SDHOME+"/Qemu");
		RED = getResources().getColor(R.color.red);
		GREEN = getResources().getColor(R.color.green);
		DEFAULT = getResources().getColor(R.color.main);
		if (android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.KITKAT
			&& android.os.Build.VERSION.SDK_INT<android.os.Build.VERSION_CODES.LOLLIPOP) {
			int c = ThemeUtils.getThemeAttrColor(this, R.attr.colorPrimary);
			ActivityUtil.setStatusBarColor(this, c);
		}
		setContentView(R.layout.activity_main);
		ActivityCompat.requestPermissions(this, new String[] {
			Manifest.permission.READ_EXTERNAL_STORAGE,
			Manifest.permission.WRITE_EXTERNAL_STORAGE
		}, PackageManager.PERMISSION_GRANTED);
		StrictMode.VmPolicy.Builder bui = new StrictMode.VmPolicy.Builder();
		StrictMode.setVmPolicy(bui.build());
		HOME = getFilesDir().getAbsolutePath();
		Toolbar toolbar = getWindow().getDecorView().findViewById(R.id.action_bar);
		getSupportActionBar().setHomeButtonEnabled(true);
		drawer = findViewById(R.id.activitymainDrawerLayout1);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawer,toolbar,0,0);
		drawer.addDrawerListener(toggle);
		toggle.syncState();
		NavigationView nav = findViewById(R.id.activitymainNavigationView1);
		TextView tv = nav.getHeaderView(0).findViewById(R.id.drawerheaderTextView1);
		tv.setText(String.format(getString(R.string.app_ver), ActivityUtil.getVerName(this)));
		nav.setNavigationItemSelectedListener(this);
		fam = findViewById(R.id.activitymainFrameLayout1);
		fraglist[0] = new MainFragment();
		fragtag[0] = getString(R.string.app_name);
		fraglist[1] = new SettingsFragment();
		fragtag[1] = getString(R.string.settings);
		fraglist[2] = new HelpFragment();
		fragtag[2] = getString(R.string.help_documentations);
		fraglist[3] = new AboutFragment();
		fragtag[3] = getString(R.string.about);
		if (savedInstanceState == null)
			setFragment(lastfrag);
		File f = new File(HOME+"/bin");
		if (!f.exists())
			f.mkdirs();
	}

	@Override public boolean onNavigationItemSelected(MenuItem item){
		byte i = 0;
		switch (item.getItemId()) {
			case R.id.dr_about:
				i++;
			case R.id.dr_help:
				if (i==0&&in.fl(HOME+"/bin").length==0) {
					in.tw("请先安装Qemu!",1,RED);
					break;
				}
				i++;
			case R.id.dr_settings:
				i++;
			case R.id.dr_home:
				if (lastfrag!=i)
					setFragment(i);
		}
		if (drawer!=null)
			drawer.closeDrawers();
		return true;
	}

	private void setFragment(byte thisFragment) {
		FragmentTransaction tra = getSupportFragmentManager().beginTransaction()
		.hide(fraglist[lastfrag]);
		if (!fraglist[thisFragment].isAdded())
			tra.add(R.id.activitymainFrameLayout1, fraglist[thisFragment], fragtag[thisFragment]);
		tra.show(fraglist[thisFragment])
		.commit();
		if (thisFragment == 2)
			fam.setPadding(0,0,0,0);
		else {
			int inn = ActivityUtil.dpToPx(10);
			fam.setPadding(inn, 0, inn, 0);
			getSupportActionBar().setTitle(fraglist[thisFragment].getTag());
		}
		lastfrag=thisFragment;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		fraglist[lastfrag].onActivityResult(requestCode,resultCode,data);
	}

	public String getShPath() {
		return SH_PATH;
	}

	@Override
	protected boolean onPrepareOptionsPanel(View view, Menu menu) {
		if (menu != null && menu.getClass() == MenuBuilder.class)
			try {
				Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
				m.setAccessible(true);
				m.invoke(menu, true);
			} catch(Exception e) {
				e.printStackTrace();
			}
		return super.onPrepareOptionsPanel(view, menu);
    }

	public void selectDir(View view) {
		Intent it=new Intent(MainActivity.this, FileActivity.class)
		.putExtra(FileActivity.FDF, FileActivity.DIR);
		startActivityForResult(it, 1);
	}

	public void selectFile(View view) {
		Intent it=new Intent(MainActivity.this, FileActivity.class)
		.putExtra(FileActivity.FDF, FileActivity.FILE);
		startActivityForResult(it, 1);
	}

	public void selectFAD(View view) {
		Intent it=new Intent(MainActivity.this, FileActivity.class)
		.putExtra(FileActivity.FDF, FileActivity.FAD);
		startActivityForResult(it, 1);
	}

	public void addNew(View v) {
		final View c = View.inflate(this, R.layout.conf_text, null);
		new AlertDialog.Builder(this)
			.setTitle(R.string.addnew)
			.setView(c)
			.setNegativeButton(android.R.string.cancel, null)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface d, int p) {
					String cfn = ((EditText)c.findViewById(android.R.id.edit)).getText().toString();
					if (!cfn.endsWith(".sh")) {
						cfn += ".sh";
					}
					Intent it = new Intent(MainActivity.this, EditActivity.class)
						.putExtra("conf_name", cfn)
						.putExtra("new", true);
					startActivityForResult(it, 1);
				}
			})
			.create().show();
	}

/*
	public void save(View view){
		if(pz.getSelectedItem().toString().endsWith(".sh"))
			in.utw(null,"保存更改","确认保存更改后的内容?",tof,false,new DialogInterface.OnClickListener[]{
				new DialogInterface.OnClickListener(){
					@Override public void onClick(DialogInterface p,int p2){
						if(saveQ(SDHOME+"/Qemu/"+pz.getSelectedItem())==true)in.tw("保存成功!",1,GREEN);
						else in.tw("保存失败!",1,RED);
					}
				},diao});
		else if(pz.getSelectedItem().toString().endsWith(".conf"))
			in.utw(null,"保存更改","此文件是.conf配置文件，是否另存为.sh配置文件?",tof,false,new DialogInterface.OnClickListener[]{
				new DialogInterface.OnClickListener(){
					@Override public void onClick(DialogInterface dia,int pos){
						if(CtoQ()){
							in.tw("保存成功!",1,GREEN);
							ArrayList flt=fle();
							in.uls(pz,flt);
						}else in.tw("保存失败!",1,RED);
					}
				},diao});
		else in.tw("请选择配置文件!",1,RED);
	}

    public static String execCmd(String[] cmd){  
        String result="";
        Runtime mRuntime=Runtime.getRuntime();
        try{
            Process mProcess=mRuntime.exec(cmd);
            InputStreamReader isr=new InputStreamReader(mProcess.getInputStream());
            BufferedReader mReader=new BufferedReader(isr);
            String string;
            while((string=mReader.readLine())!=null)
                result=result+"\n"+string;
        }catch(IOException e){
            e.printStackTrace();
        }
        return result.substring(1);
    }

	public static String[] arst(ArrayList list){
		return (String[])list.toArray();
	}

	public void addNew(final View view){
		in.tw("yyyyy",1,DEFAULT);
		/*if(de==null){
			de=execCmd(VMR.shellSh(
			"ps|grep -c './qemu-system-'"));
		}
		if(!de.equals("0")&&de.length()==1){
			in.utw(null,"已经在运行","已经有至少一个虚拟机正在运行！为了保证虚拟机运行稳定，本软件暂不开放多虚拟机运行。是否强制运行?",new String[]{ok,cancel,"强制关闭虚拟机"},false,new DialogInterface.OnClickListener[]{
				new DialogInterface.OnClickListener(){
					@Override public void onClick(DialogInterface p,int p2){
						de="0";
						go(view);
					}
				},diao,
				new DialogInterface.OnClickListener(){
					@Override public void onClick(DialogInterface p,int p1){
						try{
							Runtime.getRuntime().exec(VMR.shellSh(
								"ps|grep \"./qemu-system-\"|grep -v grep|cut -c 10-15|xargs kill -9"));
							String a=in.shell(VMR.shellSh(
								"ps|grep -c ./qemu-system-"));
							if(a.equals("0")){
								in.tw("已关闭虚拟机",1,GREEN);
								de=null;
							}else
								in.tw("未关闭虚拟机",1,RED);
					}catch(IOException e){
						in.tw(oe,1,RED);}
					}
				}});
		}else{
			de=null;
			saveQ(SDHOME+"/Qemu/"+pz.getSelectedItem());
			sk=(Spinner)findViewById(R.id.vos);
			final String vos=sk.getSelectedItem().toString();
			String db=vos.equals("SDL")?"使用SDL需要先启动XServer XSDL\n":"";
			sk=(Spinner)findViewById(R.id.ws);
			String da=sk.getSelectedItem().toString();
			String d1=pz.getSelectedItem().toString();
			if(da.equals(nedtap)){
				db=db+execCmd(new String[]{"/system/bin/su","-c",
				"cd "+HOME+"/bin;./creatap;./tapup"})+"\n";
			}
			final String dc="cd "+HOME+"/bin;"+in.fr(SDHOME+"/Qemu/"+d1).trim();
			final String cv=db+dc+"\n";
			new Thread(){
				@Override public void run(){
					try{
			Runtime.getRuntime().exec(new String[]{"/system/bin/sh","-c",
					dc});
					}catch(IOException e){}
			in.ufnsui(new Runnable(){
				@Override public void run(){
					in.tw(cv+"2秒后检查运行状态",1,DEFAULT);}});
			String dd=execCmd(VMR.shellSh(
					"sleep 2;ps|grep -c './qemu-system-'"));
			if(!dd.equals("0"))
				in.ufnsui(new Runnable(){
					@Override public void run(){
				in.utw(null,"虚拟机已成功运行!","虚拟机已运行成功,是否启动"+(vos.equals("VNC")?"VNC Viewer?":"XServer XSDL?"),tof,false,new DialogInterface.OnClickListener[]{
					new DialogInterface.OnClickListener(){
						@Override public void onClick(DialogInterface p1,int p2){
							in.uapp(vos.equals("VNC")?"com.realvnc.viewer.android":"x.org.server");
						}
					},diao
				});}});
			else
				in.ufnsui(new Runnable(){
					@Override public void run(){
				in.utw(R.drawable.ic_warning_amber_24dp,"运行出错","运行结束或配置出错,虚拟机已停止运行",tru,false,new DialogInterface.OnClickListener[]{diao});
				}});
			}}.start();
		}
	}
/*
	private void readQ(String ps){
		f=in.fr(SDHOME+"/Qemu/"+ps);
		String ff;
		int i=0;
		int[] ah={R.id.pa,R.id.pb,R.id.pc,R.id.pd,R.id.fa,R.id.fb,R.id.cd,R.id.kl,R.id.ite};
		String[] af={"-hda ","-hdb ","-hdc ","-hdd ","-fda ","-fdb ","-cdrom ","-kernel ","-initrd "};
		for(i=0;i<ah.length;i++){
			sk=(Spinner)findViewById(ah[i]);
			ff=in.sj(f,af[i]," ");
			if(ff!=null){
				String[] f1={ff,none,open};
				in.uls(sk,f1);
			}else
				in.uls(sk,mm);
			sk.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
					public void onItemSelected(AdapterView<?> parent,View view,int position,long id){
						String st=parent.getItemAtPosition(parent.getSelectedItemPosition()).toString();
						if(st.equals(open)){
							Intent it=new Intent(MainActivity.this,FileActivity.class);
							it.putExtra("id",parent.getId());
							startActivityForResult(it,1);
						}
					}
					@Override public void onNothingSelected(AdapterView<?> parent){}
				});
		}
		ff=in.sj(f,"./qemu-system-"," ");
		af=vm.getArchitecture();
		sk=(Spinner)findViewById(R.id.cpj);
		for(i=0;i<af.length;i++)
			if(ff!=null&&ff.equals(af[i])){
				sk.setSelection(i);
				i=af.length;
			}else if(ff==null){
				sk.setSelection(0);
				i=af.length;
			}
		ff=in.sj(f," -machine "," ");
		af=vm.getMachine(sk.getSelectedItem().toString());
		sk=(Spinner)findViewById(R.id.mac);
		for(i=0;i<af.length;i++)
			if(ff!=null&&ff.equals(af[i])){
				sk.setSelection(i);
				i=af.length;
			}else if(ff==null){
				sk.setSelection(0);
				i=af.length;
			}
		ff=in.sj(f," -cpu "," ");
		af=vm.CPU_MODE;
		sk=(Spinner)findViewById(R.id.cpl);
		for(i=0;i<af.length;i++)
			if(ff!=null&&ff.equals(af[i])){
				sk.setSelection(i+1);
				i=af.length;
			}else if(ff==null){
				sk.setSelection(0);
				i=af.length;
			}
		ff=in.sj(f," -smp "," ");
		sk=(Spinner)findViewById(R.id.cph);
		sk.setSelection(ff==null?0:Integer.parseInt(ff));
		ff=in.sj(f," -m "," ");
		seek.setProgress(ff==null?0:Integer.parseInt(ff));
		ff=in.sj(f," -vga "," ");
		af=vm.VGA;
		sk=(Spinner)findViewById(R.id.xk);
		for(i=0;i<af.length;i++)
			if(ff!=null&&ff.equals(af[i])){
				sk.setSelection(i+1);
				i=af.length;
			}else if(ff==null){
				sk.setSelection(0);
				i=af.length;
			}
		ff=in.sj(f," -soundhw "," ");
		af=vm.SOUNDHW;
		sk=(Spinner)findViewById(R.id.sk);
		for(i=0;i<af.length;i++)
			if(ff!=null&&ff.equals(af[i])){
				sk.setSelection(i+1);
				i=af.length;
			}else if(ff==null){
				sk.setSelection(0);
				i=af.length;
			}
		ff=in.sj(f," -net "," -net nic");
		af=new String[]{"none","user","tap,vlan=0,ifname=apqnet,script=no,downscript=no"};
		sk=(Spinner)findViewById(R.id.ws);
		for(i=0;i<af.length;i++)
			if(ff!=null&&ff.equals(af[i])){
				sk.setSelection(i);
				i=af.length;
			}else if(ff==null){
				sk.setSelection(0);
				i=af.length;
			}
		if(ff!=null){
			ff=in.sj(f,",model="," ");
			af=vm.NETWORK_CARD;
			sk=(Spinner)findViewById(R.id.wk);
			for(i=0;i<af.length;i++)
      			if(ff!=null&&ff.equals(af[i])){
					sk.setSelection(i);
					i=af.length;
     			}else if(ff==null){
					sk.setSelection(0);
					i=af.length;
				}
		}
		ff=in.sj(f," -vnc :"," ");
		sk=(Spinner)findViewById(R.id.vos);
		if(ff!=null){
			sk.setSelection(0);
			et=(EditText)findViewById(R.id.vd);
			et.setText(ff);
		}else
			sk.setSelection(1);
		ff=in.sj(f," -boot "," ");
		af=new String[]{"c","a","d"};
		sk=(Spinner)findViewById(R.id.bt);
		for(i=0;i<af.length;i++)
			if(ff!=null&&ff.equals(af[i])){
				sk.setSelection(i+1);
				i=af.length;
			}else if(ff==null){
				sk.setSelection(0);
				i=af.length;
			}
		ff=in.sj(f," -append \"","\" ");
		et=(EditText)findViewById(R.id.ad);
		et.setText(ff==null?"":ff);
		ff=in.sj(f," -loadvm "," ");
		et=(EditText)findViewById(R.id.loadvm);
		et.setText(ff==null?"":ff);
		ff=in.sj(f," -daemonize "," &\n");
		et=(EditText)findViewById(R.id.els);
		et.setText(ff==null?"":ff);
	}

	private void readC(String ps){
		f=in.fr(SDHOME+"/Qemu/"+ps);
		String ff;
		int i=0;
		int[] ah={R.id.pa,R.id.pb,R.id.pc,R.id.pd,R.id.fa,R.id.fb,R.id.cd,R.id.kl,R.id.ite};
		String[] af={"\n-hda ","\n-hdb ","\n-hdc ","\n-hdd ","\n-fda ","\n-fdb ","\n-cdrom ","\n-kernel ","\n-initrd "};
		for(i=0;i<ah.length;i++){
			sk=(Spinner)findViewById(ah[i]);
			ff=in.sj(f,af[i],"\n");
			if(ff!=null){
				String[] f1={ff,none,open};
				in.uls(sk,f1);
			}else
				in.uls(sk,mm);
			sk.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
					@Override public void onItemSelected(AdapterView<?> parent,View view,int position,long id){
						String st=parent.getItemAtPosition(parent.getSelectedItemPosition()).toString();
						if(st.equals(open)){
							Intent it=new Intent(MainActivity.this,FileActivity.class);
							it.putExtra("id",parent.getId());
							startActivityForResult(it,1);
						}
					}
					@Override public void onNothingSelected(AdapterView<?> parent){}
				});
		}
		ff=in.sj(f,"\n./qemu-system-","\n");
		af=vm.getArchitecture();
		sk=(Spinner)findViewById(R.id.cpj);
		for(i=0;i<af.length;i++)
			if(ff!=null&&ff.equals(af[i])){
				sk.setSelection(i);
				i=af.length;
			}else if(ff==null){
				sk.setSelection(0);
				i=af.length;
			}
		ff=in.sj(f,"\n-cpu ","\n");
		af=vm.CPU_MODE;
		sk=(Spinner)findViewById(R.id.cpl);
		for(i=0;i<af.length;i++)
			if(ff!=null&&ff.equals(af[i])){
				sk.setSelection(i+1);
				i=af.length;
			}else if(ff==null){
				sk.setSelection(0);
				i=af.length;
			}
		ff=in.sj(f,"\n-smp ","\n");
		sk=(Spinner)findViewById(R.id.cph);
		sk.setSelection(ff==null?0:Integer.parseInt(ff));
		ff=in.sj(f,"\n-m ","\n");
		seek.setProgress(ff==null?0:Integer.parseInt(ff));
		ff=in.sj(f,"\n-vga ","\n");
		af=vm.VGA;
		sk=(Spinner)findViewById(R.id.xk);
		for(i=0;i<af.length;i++)
			if(ff!=null&&ff.equals(af[i])){
				sk.setSelection(i+1);
				i=af.length;
			}else if(ff==null){
				sk.setSelection(0);
				i=af.length;
			}
		ff=in.sj(f,"\n-soundhw ","\n");
		af=vm.SOUNDHW;
		sk=(Spinner)findViewById(R.id.sk);
		for(i=0;i<af.length;i++)
			if(ff!=null&&ff.equals(af[i])){
				sk.setSelection(i+1);
				i=af.length;
			}else if(ff==null){
				sk.setSelection(0);
				i=af.length;
			}
		ff=in.sj(f,"\n-net ","\n");
		af=new String[]{"none","user","tap,vlan=0,ifname=apqnet,script=no,downscript=no"};
		sk=(Spinner)findViewById(R.id.ws);
		for(i=0;i<af.length;i++)
			if(ff!=null&&ff.equals(af[i])){
				sk.setSelection(i);
				i=af.length;
			}else if(ff==null){
				sk.setSelection(0);
				i=af.length;
			}
		if(ff!=null){
			ff=in.sj(f,"\n-net nic,model=","\n");
			af=vm.NETWORK_CARD;
			sk=(Spinner)findViewById(R.id.wk);
			for(i=0;i<af.length;i++)
      			if(ff!=null&&ff.equals(af[i])){
					sk.setSelection(i);
					i=af.length;
     			}else if(ff==null){
					sk.setSelection(0);
					i=af.length;
				}
		}
		ff=in.sj(f,"\n-vnc :","\n");
		sk=(Spinner)findViewById(R.id.vos);
		if(ff!=null){
			sk.setSelection(0);
			et=(EditText)findViewById(R.id.vd);
			et.setText(ff);
		}else
			sk.setSelection(1);
		ff=in.sj(f,"\n-boot ","\n");
		af=new String[]{"c","a","d"};
		sk=(Spinner)findViewById(R.id.bt);
		for(i=0;i<af.length;i++)
			if(ff!=null&&ff.equals(af[i])){
				sk.setSelection(i+1);
				i=af.length;
			}else if(ff==null){
				sk.setSelection(0);
				i=af.length;
			}
		ff=in.sj(f,"\n-append \"","\"\n");
		et=(EditText)findViewById(R.id.ad);
		et.setText(ff==null?"":ff);
		ff=in.sj(f,"\n-loadvm ","\n");
		et=(EditText)findViewById(R.id.loadvm);
		et.setText(ff==null?"":ff);
		ff=in.sj(f,"\n-daemonize\n","#\n");
		et=(EditText)findViewById(R.id.els);
		et.setText(ff==null?"":ff);
	}

	private boolean saveQ(String a){
		String xx="";
		sk=(Spinner)findViewById(R.id.vos);
		CharSequence ii=((TextView)sk.getSelectedView()).getText();
		if(ii.equals("SDL"))
			xx=xx+"export DISPLAY=:0.0 PULSE_SERVER=tcp:127.0.0.1:4712;";
		sk=(Spinner)findViewById(R.id.cpj);
		ii=((TextView)sk.getSelectedView()).getText();
		xx=xx+"./qemu-system-"+ii;
		int i=0;
		int[] ah={R.id.pa,R.id.pb,R.id.pc,R.id.pd,R.id.fa,R.id.fb,R.id.cd,R.id.kl,R.id.ite};
		String[] af={" -hda "," -hdb "," -hdc "," -hdd "," -fda "," -fdb "," -cdrom "," -kernel "," -initrd "};
		for(i=0;i<af.length;i++){
			sk=(Spinner)findViewById(ah[i]);
			ii=sk.getItemAtPosition(sk.getSelectedItemPosition()).toString();
			if(!ii.equals(none)&&!ii.equals(open))
				xx=xx+af[i]+ii;
		}
		et=(EditText)findViewById(R.id.ad);
		ii=et.getText().toString();
		if(ii.length()>0)
			xx=xx+" -append \""+ii+"\"";
		sk=(Spinner)findViewById(R.id.bt);
		ii=((TextView)sk.getSelectedView()).getText();
		if(!ii.equals(adefault))
			if(ii.equals(VMR.BOOT[1]))
				xx=xx+" -boot a";
			else if(ii.equals(VMR.BOOT[2]))
				xx=xx+" -boot c";
			else if(ii.equals(VMR.BOOT[3]))
				xx=xx+" -boot d";
		i=seek.getProgress();
		xx=xx+" -m "+i;
		sk=(Spinner)findViewById(R.id.mac);
		ii=((TextView)sk.getSelectedView()).getText();
		xx=xx+" -machine "+ii;
		sk=(Spinner)findViewById(R.id.vos);
		ii=sk.getItemAtPosition(sk.getSelectedItemPosition()).toString();
		if(ii.equals("VNC")){
			et=(EditText)findViewById(R.id.vd);
			xx=xx+" -vnc :"+et.getText().toString();
		}else
			xx=xx+" -sdl";
		sk=(Spinner)findViewById(R.id.xk);
		ii=((TextView)sk.getSelectedView()).getText();
		if(!ii.equals(none))
			xx=xx+" -vga "+ii;
		sk=(Spinner)findViewById(R.id.sk);
		ii=sk.getItemAtPosition(sk.getSelectedItemPosition()).toString();
		if(!ii.equals(none))
			xx=xx+" -soundhw "+ii;
		sk=(Spinner)findViewById(R.id.ws);
		ii=sk.getItemAtPosition(sk.getSelectedItemPosition()).toString();
		Spinner wk=(Spinner)findViewById(R.id.wk);
		if(ii.equals("User"))
			xx=xx+" -net user -net nic,model="+wk.getSelectedItem();
		else if(ii.equals(nedtap))
			xx=xx+" -net tap,vlan=0,ifname=apqnet,script=no,downscript=no -net nic,vlan=0,model="+wk.getSelectedItem();
		et=(EditText)findViewById(R.id.loadvm);
		ii=et.getText().toString();
		if(ii.length()>0)
			xx=xx+" -loadvm "+ii;
		sk=(Spinner)findViewById(R.id.cpl);
		ii=sk.getItemAtPosition(sk.getSelectedItemPosition()).toString();
		if(!ii.equals(adefault))
			xx=xx+" -cpu "+ii;
		sk=(Spinner)findViewById(R.id.cph);
		ii=sk.getItemAtPosition(sk.getSelectedItemPosition()).toString();
		if(!ii.equals(adefault))
			xx=xx+" -smp "+ii;
		xx=xx+" -daemonize ";
		et=(EditText)findViewById(R.id.els);
		ii=et.getText().toString();
		if(ii.length()>0)
			xx=xx+ii+" &\n";
		else
			xx=xx+" &\n";
		ii=pz.getItemAtPosition(pz.getSelectedItemPosition()).toString();
		return in.fw(a,xx);
	}

	private boolean CtoQ(){
		String sh=pz.getSelectedItem().toString();
		String st=SDHOME+"/Qemu/"+in.sr(sh,".conf",".sh");
		return saveQ(st);
	}
*/

	public void showSnack(CharSequence str, CharSequence act, View.OnClickListener onc){
		Snackbar.make(lastfrag==0?findViewById(R.id.activitymainFloatingActionButton1):fraglist[lastfrag].getView(),
			str,Snackbar.LENGTH_SHORT)
			.setAction(act,onc)
			.show();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (drawer.isShown())
				drawer.closeDrawers();
			if (lastfrag == 2) {
				android.webkit.WebView wb = findViewById(R.id.wb);
				if (wb.canGoBack()) {
					wb.goBack();
					return false;
				}
			}
			long now = System.currentTimeMillis();
			if (now-last > 2000L) {
				showSnack(
					"再按一次退出",
					getText(android.R.string.ok),
					new View.OnClickListener() {
						public void onClick (View p) {
							finish();
							System.exit(0);
						}
					});
				last = now;
				return false;
			} else {
				finish();
				System.exit(0);
				return true;
			}
		}
		return super.onKeyDown(keyCode,event);
	}
}

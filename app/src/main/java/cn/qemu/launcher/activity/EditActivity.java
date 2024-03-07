package cn.qemu.launcher.activity;
import android.support.v7.app.AppCompatActivity;
import android.os.*;
import android.support.v4.util.*;
import cn.qemu.launcher.R;
import android.view.MenuItem;
import android.content.Intent;
import android.support.design.widget.*;
import android.support.v4.view.*;
import java.util.*;
import cn.qemu.launcher.fragment.*;
import cn.qemu.launcher.adapter.*;
import android.view.*;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import java.io.*;
import org.zhoutz.utils.*;
import android.content.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import android.util.TypedValue;
import android.util.Log;
import android.support.v4.graphics.drawable.*;
import android.content.res.*;
import android.support.v4.content.res.*;
import cn.qemu.launcher.application.*;
import android.graphics.*;

public class EditActivity extends AppCompatActivity {

	ViewPager pag;
	ACode ad;
	final Pattern arp = Pattern.compile("[^-].*");
	String confPath;
	boolean _new = false;
	final static int EDIT_DELETE = 0, EDIT_SAVE = 1;
	final static String[] KEYS = {"cpu", "machine", "smp", "hda", "hdb", "hdc", "hdd", "fda", "fdb", "cdrom", "vga", "boot", "kernel", "initrd", "append"};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (MainApplication.IS_DARK)
			setTheme(R.style.AppTheme_Dark);
		super.onCreate(savedInstanceState);
		ad = new ACode(this);
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
			TypedValue ty=new TypedValue();
			getTheme().resolveAttribute(R.attr.colorPrimaryDark,ty,true);
			org.zhoutz.utils.ActivityUtil.setStatusBarColor(this,ty.data);
		}
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		String confName = getIntent().getStringExtra("conf_name");
		confPath = String.format("%s/%s", getSharedPreferences(MainApplication.PREF_SETTINGS, MODE_PRIVATE).getString("sh_path","/sdcard/Qemu"), confName);
		_new = getIntent().getBooleanExtra("new", false);
		if (!_new) {
			if (confName.endsWith(".sh"))
				readSh(confPath);
			else if (confName.endsWith(".conf")) {
				readConf(confPath);
				confPath = confPath.substring(0, confPath.length()-4) + "sh";
			}
		}
		getSupportActionBar().setTitle(confName);
		getSupportFragmentManager().beginTransaction().add(android.R.id.content, new ConfFragment(), "tsh").commit();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		getSharedPreferences("configuration", MODE_PRIVATE).edit().clear().commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!_new) {
			MenuItem mi = menu.add(Menu.NONE, EDIT_DELETE, 0, R.string.delete)
			.setIcon(R.drawable.baseline_delete_24);
			mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			DrawableCompat.setTint(mi.getIcon(), Color.WHITE);
		}
		MenuItem mi = menu.add(Menu.NONE, EDIT_SAVE, 1, R.string.save)
		.setIcon(R.drawable.baseline_save_24);
		mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		DrawableCompat.setTint(mi.getIcon(), Color.WHITE);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
			case EDIT_DELETE:
				new File(confPath).delete();
				setResult(RESULT_FIRST_USER, getIntent().putExtra("conf_name", getSupportActionBar().getTitle()).putExtra("add", false));
				finish();
				break;
			case EDIT_SAVE:
				saveSh(confPath);
				setResult(RESULT_FIRST_USER, getIntent().putExtra("conf_name", getSupportActionBar().getTitle()).putExtra("add", true));
				finish();
				break;
			case android.R.id.home:
				setResult(RESULT_CANCELED, getIntent());
				finish();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void readConf(String path) {
		try{
			FileInputStream input = new FileInputStream(path);
			byte[] bt = new byte[input.available()];
			input.read(bt);
			input.close();
			String text = new String(bt).replaceAll("#[^\n]*","").replace(" -","\n-")+"\n";
			ad.tw(text, 1, getResources().getColor(R.color.main));
			SharedPreferences.Editor prop = getSharedPreferences("configuration",MODE_PRIVATE).edit();
			String vc = ad.sj(text,"qemu-system-","\n");
			prop.putString("qemu_system",vc==null?"i386":vc.trim());
			for (String arg:KEYS) {
				vc = ad.sj(text,new StringBuilder("-").append(arg).append(" ").toString(),"\n");
				if (vc!=null)
					prop.putString(arg,vc.trim());
			}
			prop.putInt("m",Integer.parseInt(ad.sj(text,"-m ","\n")));
			vc = ad.sj(text,"-soundhw ","\n");
			if (vc!=null) {
				Set<String> sa = new ArraySet<String>();
				for (String item:vc.split(","))
					sa.add(item.trim());
				if (!sa.isEmpty())
					prop.putStringSet("soundhw",sa);
			}
			Matcher mx = Pattern.compile("-net (user|tap)").matcher(text);
			if (mx.find()) {
				prop.putString("net_type",mx.group(1));
				int n=0;
				String al="";
				while ((n=text.indexOf("-net nic,",n))!=-1) {
					String as = text.substring(n+9,text.indexOf("\n",n+9));
					if (as==null)
						break;
					n+=as.length();
					al+=as+" ";
				}
				if (!al.isEmpty())
					prop.putString("net_card", al.trim());
			}
			if(text.indexOf("-vnc :")>-1)
				prop.putString("graphics","vnc").putString("vnc_port",ad.sj(text,"-vnc :","\n"));
			else
				prop.putString("graphics","sdl");
			prop.commit();
		} catch (FileNotFoundException fe) {
			ad.tw(fe.getMessage(),1,getResources().getColor(R.color.red));
		} catch (IOException ioe) {
			ad.tw(ioe.getMessage(),1,getResources().getColor(R.color.red));
		}
	}

	public void readSh(String path){
		try{
			Scanner st = new Scanner(new FileReader(path));
			String ck;
			String display = null;
			boolean usb = false;
			SharedPreferences prd = getSharedPreferences("configuration", MODE_PRIVATE);
			SharedPreferences.Editor prop = prd.edit();
			if (st.hasNext() && (ck = st.next()).startsWith("./qemu")) {
				prop.putString("qemu_system", ck.substring(14));
			}
			StringBuffer netcs = new StringBuffer(), exparms = new StringBuffer();
			while (st.hasNext()) {
				ck = st.next();
				if (ck.charAt(0) == '-') {
					switch (ck = ck.substring(1)) {
						case "M":
							ck = "machine";
						case "cpu":
						case "machine":
						case "smp":
						case "hda":
						case "hdb":
						case "hdc":
						case "hdd":
						case "fda":
						case "fdb":
						case "cdrom":
						case "vga":
						case "boot":
						case "kernel":
						case "initrd":
						case "append":
							if (st.hasNext(arp))
								prop.putString(ck, st.next());
							break;
						case "m":
							if (st.hasNextInt())
								prop.putInt(ck, st.nextInt());
							break;
						case "soundhw":
							if (st.hasNext(arp)) {
								ArraySet<String> shw = new ArraySet<String>();
								for (String it:st.next().split(","))
									shw.add(it);
								if (!shw.isEmpty())
									prop.putStringSet(ck, shw);
							}
							break;
						case "net":
							if (st.hasNext(arp)) {
								ck = st.next();
								if (ck.startsWith("nic"))
									netcs.append(ck.substring(4)).append(' ');
								else
									prop.putString("net_type", ck);
							}
							break;
						case "sdl":
							break;
						case "vnc":
							if (st.hasNext(arp)) {
								prop.putString("graphics", ck);
								display = st.next().substring(1);
							}
							break;
						case "spice":
							if (st.hasNext(arp)) {
								prop.putString("graphics", ck);
								display = st.next();
								int off = display.indexOf("port=")+5;
								int end = display.indexOf(",", off);
								display = display.substring(off, end==-1?display.length():end);
							}
							break;
						case "usb":
							usb = true;
							break;
						default:
							boolean b = st.hasNext(arp);
							String cv = null;
							if (b) {
								cv = st.next();
								if (usb && "device".equals(ck) && "usb-tablet".equals(cv)) {
									prop.putBoolean("usb_tablet", true);
									usb = false;
									break;
								}
							}
							exparms.append(" -").append(ck);
							if (b)
								exparms.append(' ').append(cv);
							break;
					}
				}
			}
			st.close();
			prop.putString("net_card", netcs.append(' ').toString());
			if (exparms.length() > 0)
				prop.putString("exparms", exparms.substring(1));
			if (display != null && !display.isEmpty())
				prop.putString("port", display);
			else
				prop.putString("graphics", "sdl");
			prop.commit();
		} catch(Exception e) {
			e.printStackTrace();
			ad.tw(e.getMessage(), 1, getResources().getColor(R.color.red));
		}
	}

	public void saveSh(String path) {
		try {
			FileWriter fw = new FileWriter(path);
			SharedPreferences prop = getSharedPreferences("configuration", MODE_PRIVATE);
			fw.write("./qemu-system-");
			fw.write(prop.getString("qemu_system", "i386"));
			String v = null;
			for (String k:KEYS) {
				v = prop.getString(k, "");
				if (!v.isEmpty()) {
					fw.write(" -");
					fw.write(k);
					fw.write(" ");
					fw.write(v);
				}
			}
			int i = prop.getInt("m", -1);
			if (i > -1) {
				fw.write(" -m ");
				fw.write(Integer.toString(i));
			}
			Set<String> s = prop.getStringSet("soundhw", new ArraySet<String>());
			if (!s.isEmpty()) {
				fw.write(" -soundhw ");
				fw.write(String.join(",", s.toArray(new CharSequence[s.size()])));
			}
			v = prop.getString("net_type", null);
			if (v != null) {
				fw.write(" -net ");
				fw.write(v);
			}
			if (!"none".equals(v)) {
				v = prop.getString("net_card", null);
				if (v != null)
					for (String k:v.split("\\s+")) {
						fw.write(" -net nic,");
						fw.write(k);
					}
			}
			v = prop.getString("graphics", "sdl");
			if ("vnc".equals(v) && (v=prop.getString("port", null)) != null) {
				fw.write(" -vnc :");
				fw.write(v);
			} else if ("spice".equals(v) && (v=prop.getString("port", null)) != null) {
				fw.write(" -spice disable-ticketing,port=");
				fw.write(v);
			}
			if (prop.getBoolean("usb_tablet", false))
				fw.write(" -usb -device usb-tablet");
			v = prop.getString("exparms", null);
			if (v != null) {
				fw.write(' ');
				fw.write(v);
			}
			fw.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}

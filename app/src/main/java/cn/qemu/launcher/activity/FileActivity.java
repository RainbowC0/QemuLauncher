package cn.qemu.launcher.activity;

import android.os.Bundle;
import android.content.Context;
import android.view.View;
import android.view.KeyEvent;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import java.text.DecimalFormat;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.ViewGroup;
import org.zhoutz.utils.ACode;
import cn.qemu.launcher.R;
import android.os.Environment;
import cn.qemu.launcher.adapter.*;
import android.util.TypedValue;
import android.support.v7.app.AlertDialog.Builder;
import android.content.DialogInterface;
import java.io.*;
import java.util.*;
import android.support.v7.widget.*;
import android.support.v7.app.*;
import android.widget.*;
import cn.qemu.launcher.application.*;
import org.zhoutz.utils.*;

public class FileActivity extends AppCompatActivity
	implements AdapterView.OnItemClickListener, Comparator<File>, MenuItem.OnMenuItemClickListener,
	View.OnClickListener, View.OnLongClickListener, DialogInterface.OnClickListener {
	private ListView ls;
	private ACode i = new ACode(this);
	private ListItemAdapter sp;
	private String path;
	private Context cot = this;
	private int fdf;
	private String format = ".+\\.(qcow|qed|vdi|img|iso|qcow2|vmdk)";
	public final static String FDF = "_fdf";
	public final static String PATH = "_path";
	public final static int FILE = 0, DIR = 1, FAD = 2;
	private long last = System.currentTimeMillis()-2000;
	private TextView transTx;

	@Override protected void onCreate(Bundle savedInstanceState) {
		if (MainApplication.IS_DARK)
			setTheme(R.style.AppTheme_Dark);
		super.onCreate(savedInstanceState);
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
			TypedValue ty = new TypedValue();
			getTheme().resolveAttribute(R.attr.colorPrimaryDark, ty, true);
			ActivityUtil.setStatusBarColor(this, ty.data);
		}
		setContentView(R.layout.file);

		Bundle bd = getIntent().getExtras();
		String ps = bd.getString(PATH);
		if (ps==null || ps.isEmpty())
			ps = Environment.getExternalStorageDirectory().getAbsolutePath();
		else if (ps.startsWith("fat:rw:"))
			ps = ps.substring(7);
		else
			ps = new File(ps).getParentFile().getAbsolutePath();
		ActionBar ab = getSupportActionBar();
		ab.setSubtitle(ps);
		ab.setDisplayHomeAsUpEnabled(true);

		ViewGroup par = getWindow().getDecorView().findViewById(R.id.action_bar);
		TextView pat = (TextView)par.getChildAt(1);
		pat.setEllipsize(android.text.TextUtils.TruncateAt.START);
		pat.setGravity(Gravity.CENTER_VERTICAL);
		pat.setSingleLine(true);
		pat.setTextColor(((TextView)par.getChildAt(0)).getTextColors());
		pat.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				View c = View.inflate(FileActivity.this, R.layout.conf_text, null);
				EditText et = c.findViewById(android.R.id.edit);
				transTx = et;
				et.setText(getSupportActionBar().getSubtitle());
				Builder bd = new Builder(FileActivity.this);
				bd.setTitle(R.string.jump_to_path);
				bd.setView(c);
				bd.setPositiveButton(android.R.string.ok, FileActivity.this);
				bd.setNegativeButton(android.R.string.cancel, null);
				bd.create().show();
			}
		});
		pat.setOnLongClickListener(this);

		fdf = bd.getInt(FDF);
		ls = findViewById(R.id.flist);
		sp = new ListItemAdapter(cot, getFlist());
		ls.setAdapter(sp);
		ls.setOnItemClickListener(this);
	}

	public void onClick(DialogInterface di, int p) {
		getSupportActionBar().setSubtitle(transTx.getText());
		sp.setList(getFlist());
	}

	public boolean onLongClick(View v) {
		CharSequence pac = ((TextView)v).getText();
		i.clp(pac);
		i.tw(getText(R.string.copied), 1, getResources().getColor(R.color.green));
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		if (fdf==DIR||fdf==FAD) {
			MenuItem mi = menu.add(R.string.select_dir);
			mi.setIcon(R.drawable.ic_open_in_new_black_24dp);
			mi.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM);
			mi.setOnMenuItemClickListener(this);
		}
		return true;
	}

	public boolean onMenuItemClick(MenuItem item) {
		path=getSupportActionBar().getSubtitle().toString();
		endThis(fdf==DIR?path:"fat:rw:"+path);
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> p, View v, int pos, long pps) {
		String st = ((Item)p.getAdapter().getItem(pos)).title;
		path = getSupportActionBar().getSubtitle().toString();
		if (pos==0 && "..".equals(st)) {
			path = new File(path).getParent();
			getSupportActionBar().setSubtitle(path);
			sp.setList(getFlist());
		} else {
			path = path.endsWith(File.separator)?path+st:path+File.separator+st;
			if (i.fi(path)) {
				getSupportActionBar().setSubtitle(path);
				sp.setList(getFlist());
			} else if(i.fe(path)) {
				if (path.matches(format))
					endThis(path);
				else {
					Builder bd = new Builder(FileActivity.this);
					bd.setIcon(R.drawable.ic_warning_amber_24dp);
					bd.setTitle(R.string.choose_file);
					bd.setMessage("这不是镜像文件，确定选择？");
					bd.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dig, int p) {
								endThis(path);
							}
						});
					bd.setNegativeButton(android.R.string.cancel, null);
					bd.create().show();
				}
			}
		}
	}

	private void endThis(String path) {
		Intent it = new Intent();
		it.putExtra(MainActivity.SELECTEDPATH, path);
		setResult(RESULT_OK, it);
		finish();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onClick(null);
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	private int dri(File fl) {
		return fl.isDirectory()?R.drawable.ic_folder_black_24dp:(fl.getName().matches(format)?R.drawable.ic_album_black_24dp:R.drawable.ic_insert_drive_file_black_24dp);
    }

    private String siz(File fl) {
        if (fl.isDirectory())
            return getResources().getString(R.string.dir);
		long s = fl.length();
		if(s<0x400L)
			return s+"B";
		DecimalFormat df = new DecimalFormat("#.00");
		if (s<0x100000L)
			return df.format(s/1024.)+"K";
		else if (s<0x40000000L)
			return df.format((s>>10)/1024.)+"M";
		else
			return df.format((s>>20)/1024.)+"G";
	}

    private ArrayList<Item> getFlist() {
        ArrayList<Item> arrayList = new ArrayList<Item>();
       	path = getSupportActionBar().getSubtitle().toString();
		if (!"/".equals(path)) {
			Item it = new Item();
			it.icon = R.drawable.ic_folder_black_24dp;
			it.title = "..";
			arrayList.add(it);
		}
		File fpa = new File(path);
		if (!fpa.canRead())
			return arrayList;
		File[] fl = fdf==DIR?fpa.listFiles(ActivityUtil.DIRFILTER):fpa.listFiles();
		Arrays.sort(fl, this);
        for (File i:fl) {
            Item it=new Item();
            it.icon=dri(i);
            it.title=i.getName();
            it.sub=siz(i);
            arrayList.add(it);
        }
        return arrayList;
    }

	public int compare(File a, File b) {
		boolean ad=a.isDirectory(), bd=b.isDirectory();
		return ad==bd?
			a.getName().compareToIgnoreCase(b.getName())
			:ad?-1:1;
	}

	public void onClick(View p) {
		Intent it=getIntent();
		setResult(RESULT_CANCELED,it);
		finish();
	}

	@Override
	public void onBackPressed() {
		long ne = System.currentTimeMillis();
		path = getSupportActionBar().getSubtitle().toString();
		if (ne-last>2000L && "/".equals(path)) {
			ACode.sw(ls, "再按一次返回", getText(android.R.string.ok), this);
			last=ne;
		} else if(!"/".equals(path)) {
			path = new File(path).getParent();
			getSupportActionBar().setSubtitle(path);
			sp.setList(getFlist());
		} else {
			onClick(null);
			super.onBackPressed();
		}
	}
}

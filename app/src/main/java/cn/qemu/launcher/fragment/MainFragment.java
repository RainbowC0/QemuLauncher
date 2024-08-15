package cn.qemu.launcher.fragment;

import android.support.v4.app.Fragment;
import android.view.*;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler;
import cn.qemu.launcher.R;
import org.zhoutz.utils.*;
import android.support.v7.app.*;
import cn.qemu.launcher.activity.*;
import java.io.File;
import android.widget.ListView;
import cn.qemu.launcher.adapter.*;
import java.util.*;
import java.text.*;
import android.support.graphics.drawable.*;
import android.widget.*;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.content.DialogInterface;
import cn.qemu.launcher.utils.*;
import java.io.*;
import android.util.*;
import android.content.Intent;
import android.support.v4.graphics.drawable.*;
import android.support.v4.content.res.*;
import android.content.res.*;
import android.app.Activity;
import android.content.*;
import cn.qemu.launcher.application.*;
import android.graphics.*;
import java.util.concurrent.*;
import android.view.View.*;
import android.support.v7.util.*;

public class MainFragment extends Fragment
implements FileFilter, DialogInterface.OnClickListener, View.OnClickListener,
AdapterView.OnItemSelectedListener, Runnable {
	public static final int LOG = 0, UNZIP_FAILED = 1, HALTED = 2, STARTED = 3;
	private View floatbutton, infov, transV;
	private ListView list;
	private TextView pth;
	private MainActivity ma;
	private AlertDialog alt=null;
	private int actionColor;
	private ACode in;
	private ArrayList<Item> array = new ArrayList<Item>();
	private ItemAdapter adp;
	private File obb;
	private Handler hd = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case LOG:
					pth.append((String)msg.obj);
					pth.append("\n");
					break;
				case UNZIP_FAILED:
					in.tw("解压失败\n错误信息: " + msg.obj, 1, ma.RED);
					break;
				case HALTED:
					Builder bd = new Builder(ma);
					bd.setIcon(R.drawable.ic_warning_amber_24dp);
					bd.setTitle(R.string.run_err);
					bd.setMessage("运行结束或配置出错，虚拟机已停止运行");
					bd.setNegativeButton(android.R.string.cancel,null);
					bd.setPositiveButton(R.string.view_log, (DialogInterface.OnClickListener)msg.obj);
					bd.create().show();
					break;
				case STARTED:
					ma.in.tw(ma.getText(R.string.running),Toast.LENGTH_SHORT,ma.getResources().getColor(R.color.green));
					((android.support.v7.widget.AppCompatImageView)msg.obj).setImageResource(R.drawable.ic_stop_black_24dp);
					break;
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		ma = (MainActivity)getActivity();
		in = ma.in;
		super.onCreate(savedInstanceState);
		TypedValue ty = new TypedValue();
		Resources.Theme t = ma.getTheme();
		t.resolveAttribute(R.attr.actionMenuTextColor, ty, true);
		actionColor = ResourcesCompat.getColor(ma.getResources(), ty.resourceId, t);
		setHasOptionsMenu(true);

		if (new File(ma.getFilesDir(), "bin").list().length == 0)
			installQemu();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.conf_list, container, false);
		floatbutton = v.findViewById(R.id.activitymainFloatingActionButton1);
		View en = v.findViewById(R.id.conflinear);
		((TextView)en.findViewById(R.id.list_empty_text)).setText(R.string.empty_text_main);
		((TextView)en.findViewById(R.id.list_empty_summary)).setText(R.string.empty_summary_main);
		list = v.findViewById(R.id.conflist);
		list.setEmptyView(en);
		adp = new ItemAdapter(this, array);
		list.setAdapter(adp);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		refresh();
	}

	public boolean accept(File f) {
		return f.isFile() && f.getName().matches(".+\\.(sh|conf)");
	}

	private void refresh() {
		File[] fl = new File(((MainActivity)getActivity()).getShPath()).listFiles(this);
		array.clear();
		if (fl != null)
			for (File fli:fl) {
				Item item = new Item();
				item.title = fli.getName();
				Date date = new Date(fli.lastModified());
				SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.edit_last));
				item.sub = sdf.format(date);
				array.add(item);
			}
		adp.notifyDataSetInvalidated();
	}

	public void sendMessage(Message msg) {
		hd.sendMessage(msg);
	}

	@Override public void onCreateOptionsMenu(Menu menu, MenuInflater me) {
		menu.clear();
		me.inflate(R.menu.main, menu);
		for (int i=0;i < menu.size();i++) {
			MenuItem itm = menu.getItem(i);
			DrawableCompat.setTint(itm.getIcon(), actionColor);
			if (itm.hasSubMenu()) {
				SubMenu sub = itm.getSubMenu();
				for (int ii=0;ii < sub.size();ii++)
					DrawableCompat.setTint(sub.getItem(ii).getIcon(), actionColor);
			}
		}
	}

	private void showProgressDialog(CharSequence chr) {
		if (alt == null)
			alt = new AlertDialog.Builder(ma)
				.setView(R.layout.progress_dialog)
				.create();
		alt.show();
		((TextView)alt.getWindow().getDecorView().findViewById(R.id.message)).setText(chr);
	}

	private void installQemu() {
 		in.utw(null, getText(R.string.install_title), getText(R.string.install), new CharSequence[]{getText(android.R.string.ok),getText(android.R.string.cancel)}, false, new DialogInterface.OnClickListener[]{
				   new DialogInterface.OnClickListener(){
					   @Override
					   public void onClick(DialogInterface p1, int p) {
						   File ob = new File(String.format("%s/%s.obb", ma.getObbDir().getPath(), ma.getPackageName()));
						   if (ob.isFile())
							   unzip(ob);
						   else in.utw(R.drawable.ic_warning_amber_24dp, "没有数据文件", "未发现数据文件：" + ob.getPath(), new CharSequence[]{getText(android.R.string.ok)}, false, new DialogInterface.OnClickListener[]{null});
					   }},null});
	}

	private OnClickListener onc = new View.OnClickListener(){
		public void onClick(View v) {
			ma.selectFile(v);
		}
	};

	public void viewLog() {
		final Builder adb = new Builder(ma);
		adb.setTitle(R.string.log);
		adb.setMessage("");
		adb.setPositiveButton(android.R.string.cancel, null);
		adb.setNegativeButton(android.R.string.copy, this);
		adb.setNeutralButton(R.string.clear_log, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface d, int p) {
					try {
						VMR.exec("logcat -c").waitFor();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		AlertDialog ad = adb.create();
		ad.show();
		infov = ad.getWindow().getDecorView().findViewById(R.id.parentPanel);
		pth = infov.findViewById(android.R.id.message);
		pth.setTextIsSelectable(true);
		pth.setTypeface(Typeface.MONOSPACE);
		pth.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.f);
		new Thread(){
			public void run() {
				SharedPreferences sp = ma.getSharedPreferences(MainApplication.PREF_SETTINGS, MainApplication.MODE_PRIVATE);
				try {
					Process p = VMR.exec("logcat " + sp.getString("logcat_params", ""));
					BufferedReader is = new BufferedReader(new InputStreamReader(p.getInputStream()));
					String l;
					while ((l = is.readLine()) != null) {
						Message msg = new Message();
						msg.what = LOG;
						msg.obj = l;
						hd.sendMessage(msg);
					}
					is.close();
					p.destroy();
				} catch (Exception e) {
					e.printStackTrace();
					Log.e("viewlog", e.getMessage());
				}
			}}.start();
	}

	private class Resize implements Runnable, DialogInterface.OnClickListener {
		Resize(Spinner s, TextView n, Spinner t) {
			sign = s;
			num = n;
			unit = t;
		}
		Spinner sign, unit;
		TextView num;
		@Override public void onClick(DialogInterface dig, int idx) {
			showProgressDialog(getText(R.string.resizing));
			new Thread(this).start();
		}
		@Override public void run() {
			String sg = sign.getSelectedItem().toString(),
				pt = pth.getText().toString(),
				nu = num.getText().toString(),
				un = unit.getSelectedItem().toString();
			String comd = String.format("$PREF ./qemu-img resize %s%s %s%s%s", "-".equals(sg) && pt.matches(".+\\.qcow2?") ?"--shrink ": "", pt, sg, nu, un);
			try {
				runCmd(comd);
				ACode.sw(floatbutton, getText(R.string.operation_completed));
			} catch (Exception e) {
				ACode.sw(floatbutton, getText(R.string.operation_error));
				e.printStackTrace();
			}
			alt.dismiss();
		}
	}

	private class Create implements Runnable, DialogInterface.OnClickListener {
		Create(View u) {
			ut = u;
		}
		View ut;
		public void onClick(DialogInterface dig, int ind) {
			showProgressDialog(getText(R.string.creating));
			new Thread(this).start();
		}
		public void run() {
			Spinner a = ut.findViewById(R.id.disk_format);
			String x0 = a.getSelectedItem().toString();
			String x1 = pth.getText().toString();
			EditText b = ut.findViewById(R.id.nam);
			String x2 = b.getText().toString();
			b = ut.findViewById(R.id.siz);
			String x3 = b.getText().toString();
			a = ut.findViewById(R.id.size_unit);
			String x4 = a.getSelectedItem().toString();
			String comd = String.format("$PREF ./qemu-img create -f %1$s %2$s/%3$s.%1$s %4$s%5$s", x0, x1, x2, x3, x4);
			try {
				runCmd(comd);
				ACode.sw(floatbutton, getText(R.string.operation_completed));
			} catch (Exception e) {
				ACode.sw(floatbutton, getText(R.string.operation_error));
				e.printStackTrace();
			}
			alt.dismiss();
		}
	}

	private class Convert implements Runnable, DialogInterface.OnClickListener {
		Convert(View r, Spinner f) {
			root = r;
			fmtv = f;
		}
		View root;
		Spinner fmtv;

		public void onClick(DialogInterface p1, int p2) {
			showProgressDialog(getText(R.string.converting));
			new Thread(this).start();
		}

		public void run() {
			EditText a = root.findViewById(R.id.imgpath);
			String ipth = a.getText().toString();
			a = root.findViewById(R.id.output_name);
			String nam = a.getText().toString();
			String fmt = fmtv.getSelectedItem().toString();
			a = root.findViewById(R.id.outputdir);
			String opth = a.getText().toString();
			CheckBox cprv = (CheckBox)transV;
			String cpr = cprv.isEnabled() && cprv.isChecked() ? "-c": "";
			String cmd = String.format("$PREF ./qemu-img convert %s -O %s %s %s/%s.%s", cpr, fmt, ipth, opth, nam, fmt);
			try {
				runCmd(cmd);
				ACode.sw(floatbutton, getText(R.string.operation_completed));
			} catch (Exception e) {
				ACode.sw(floatbutton, getText(R.string.operation_error));
				e.printStackTrace();
			}
			alt.dismiss();
		}
	}

	@Override public void onClick(DialogInterface dlg, int p) {
		CharSequence car = ((TextView)infov.findViewById(android.R.id.message)).getText();
		in.clp(car);
		in.tw(getText(R.string.copied), 1, ma.GREEN);
	}

	@Override public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
			case R.id.log:
				viewLog();
				break;
			case R.id.size:
				View sizev = View.inflate(ma, R.layout.imgsize, null);
				pth = sizev.findViewById(R.id.imgpath);
				pth.setText(MainActivity.SDHOME + "/Qemu/kolibri.img");
				sizev.findViewById(R.id.imgopen).setOnClickListener(onc);
				Builder bd = new Builder(ma);
				bd.setTitle(R.string.resize_img);
				bd.setView(sizev);
				EditText num = sizev.findViewById(R.id.imgsizeEditText2);
				Spinner sign = sizev.findViewById(R.id.adjust_sign);
				Spinner unit = sizev.findViewById(R.id.size_unit);
				bd.setPositiveButton(R.string.perform, new Resize(sign, num, unit));
				bd.setNegativeButton(android.R.string.cancel, null);
				bd.create().show();
				break;
			case R.id.info:
				bd = new Builder(ma);
				bd.setTitle(R.string.img_info);
				bd.setMessage(R.string.choose_file);
				bd.setPositiveButton(R.string.open, null);
				bd.setNegativeButton(android.R.string.cancel, null);
				bd.setNeutralButton(android.R.string.copy, this);
				AlertDialog adt = bd.create();
				adt.show();
				adt.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(onc);
				adt.getButton(AlertDialog.BUTTON_NEUTRAL).setVisibility(View.GONE);
				infov = adt.getWindow().getDecorView().findViewById(R.id.parentPanel);
				pth = infov.findViewById(android.R.id.message);
				break;
			case R.id.create:
				View ut = View.inflate(ma, R.layout.create_img, null);
				pth = ut.findViewById(R.id.imgpath);
				pth.setText(MainActivity.SDHOME + "/Qemu");
				ut.findViewById(R.id.imgopen).setOnClickListener(this);
				bd = new Builder(ma);
				bd.setTitle(R.string.newf);
				bd.setView(ut);
				bd.setPositiveButton(R.string.perform, new Create(ut));
				bd.setNegativeButton(android.R.string.cancel, null);
				bd.create().show();
				break;
			case R.id.convert:
				final View root = View.inflate(ma, R.layout.img_convert, null);
				root.findViewById(R.id.imgopen).setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							pth = root.findViewById(R.id.imgpath);
							ma.selectFile(v);
						}
					});
				root.findViewById(R.id.opendir).setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							pth = root.findViewById(R.id.outputdir);
							ma.selectDir(v);
						}
					});
				CheckBox cprv = root.findViewById(R.id.compress);
				transV = cprv;
				Spinner fmtv = root.findViewById(R.id.disk_format);
				fmtv.setOnItemSelectedListener(this);
				bd = new AlertDialog.Builder(ma);
				bd.setTitle(R.string.convert_format);
				bd.setView(root);
				bd.setPositiveButton(R.string.perform, new Convert(root, fmtv));
				bd.setNegativeButton(android.R.string.cancel, null);
				bd.create().show();
				break;
			case R.id.dres:
				in.hws("https://rainbowc0.gitee.io/qemu-res/");
				break;
			case R.id.install:
				installQemu();
				break;
			case R.id.remove:
				in.utw(null, getText(R.string.remove_title), "确认卸载QEMU？卸载后可以重新安装", new CharSequence[]{getText(android.R.string.ok),getText(android.R.string.cancel)}, false, new DialogInterface.OnClickListener[]{
						   new DialogInterface.OnClickListener(){
							   @Override
							   public void onClick(DialogInterface p1, int p) {
								   if (in.fd(ma.getFilesDir().getPath()))
									   in.tw(getText(R.string.operation_completed), 1, ma.GREEN);
								   else
									   in.tw(getText(R.string.operation_error), 1, ma.RED);
							   }
						   },null});
				break;
			case R.id.ctap:
				if (!new File(ma.getFilesDir().getPath() + "/bin/tapdown").isFile())
					in.utw(R.drawable.ic_warning_amber_24dp, "未发现tapdown脚本", "未发现tapdown脚本,请检查是否已安装配置文件", new String[]{getString(android.R.string.ok)}, false, new DialogInterface.OnClickListener[]{null});
				else
					try {
						String n = ACode.shell(new String[]{"/system/bin/su","-c",
												   "cd " + ma.getFilesDir().getPath() + "/bin;./tapdown"});
						in.tw(n, 1, ma.GREEN);
					} catch (Exception e) {
						e.printStackTrace();
						in.tw(getText(R.string.operation_error), 1, ma.RED);
					}
				break;
		}
		return true;
	}

	@Override
	public void onClick(View v) {
		ma.selectDir(v);
	}

	public void onItemSelected(AdapterView<?> p, View v, int idx, long num) {
		transV.setEnabled(idx < 2);
	}

	public void onNothingSelected(AdapterView<?> sp) {}

	public void run() {
		if (!ma.getFilesDir().exists())
			ma.getFilesDir().mkdir();
		try {
			ZipUtil.upZipFile(obb, getContext().getFilesDir().getAbsolutePath());
			Process p = Runtime.getRuntime().exec(VMR.shellSh(
													  "chmod -R 771 " + ma.getFilesDir().getPath()));
			p.waitFor();
			ma.runOnUiThread(new Unzipped());
		} catch (final Exception e) {
			Message msg = new Message();
			msg.what = UNZIP_FAILED;
			msg.obj = e.getMessage();
			hd.sendMessage(msg);
			e.printStackTrace();
		}
		alt.dismiss();
	}

	private class Unzipped implements Runnable, DialogInterface.OnClickListener {
		@Override
		public void run() {
			in.utw(null, "解压成功", "是否删除obb数据包？删除后不可重新安装", new CharSequence[]{getText(android.R.string.ok),getText(android.R.string.cancel)}, false, new DialogInterface.OnClickListener[]{
					   this,null
				   });
		}
		@Override
		public void onClick(DialogInterface p1, int p2) {
			obb.delete();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		//refresh();
		switch (resultCode) {
			case Activity.RESULT_OK:
				Bundle bun = data.getExtras();
				String path = bun.getString(MainActivity.SELECTEDPATH);
				if (pth.getId() == android.R.id.message)
					try {
						Process ps = VMR.exec(String.format("$PREF ./qemu-img info %s", path));
						ps.waitFor();
						InputStream ist = ps.getInputStream();
						byte[] bs = new byte[ist.available()];
						ist.read(bs);
						ist.close();
						path = new String(bs);
						infov.findViewById(android.R.id.button3).setVisibility(View.VISIBLE);
					} catch (Exception e) {
						e.printStackTrace();
					}
				pth.setText(path);
				break;
			case Activity.RESULT_FIRST_USER:
				String na = data.getStringExtra("conf_name");
				boolean b = data.getBooleanExtra("add", false);
				boolean _new = data.getBooleanExtra("new", false);
				if (na == null)
					break;
				if (_new && b) {
					Item item = new Item();
					item.title = na;
					Date date = new Date(new File(ma.getShPath() + "/" + na).lastModified());
					SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.edit_last));
					item.sub = sdf.format(date);
					array.add(item);
				} else if (!(_new || b))
					for (int i=0,l=array.size(); i < l; i++) {
						if (na.equals(array.get(i).title)) {
							array.remove(i);
							break;
						}
					}
				adp.notifyDataSetChanged();
				break;
		}
	}

	private void unzip(File ob) {
		showProgressDialog(getText(R.string.uncompressing));
		obb = ob;
		new Thread(this).start();
	}

	private static void runCmd(String cmd) throws Exception {
		Process p = VMR.exec(cmd);
		p.waitFor();
		if (p.exitValue() != 0) {
			InputStream error = p.getErrorStream();
			byte[] errorBytes = new byte[error.available()];
			error.read(errorBytes);
			error.close();
			throw new Exception(new String(errorBytes));
		}
	}
}

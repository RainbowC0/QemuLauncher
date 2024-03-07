package cn.qemu.launcher.fragment;
import android.support.v4.app.*;
import android.support.v7.view.ActionMode;
import android.os.*;
import cn.qemu.launcher.R;
import android.webkit.*;
import android.widget.*;
import android.support.v7.app.ActionBar;
import java.util.ArrayList;
import android.view.*;
import android.content.*;
import java.io.*;
import cn.qemu.launcher.application.*;
import android.text.*;

public class HelpFragment extends Fragment
	implements ActionMode.Callback, TextWatcher, FileFilter
{
	private cn.qemu.launcher.activity.MainActivity ma;
	private WebView wb;
	private ProgressBar prb;

	@Override
	public void onCreate(Bundle savedInstanceState){
		ma = (cn.qemu.launcher.activity.MainActivity)getActivity();
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View v=inflater.inflate(R.layout.help,container,false);
		wb = v.findViewById(R.id.wb);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		final ArrayAdapter<String> adp = new ArrayAdapter<String>(getActivity(),R.layout.action_title,getHelpList());
		adp.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
		ActionBar ab = ma.getSupportActionBar();
		ab.setDisplayShowTitleEnabled(false);
		ab.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		ab.setListNavigationCallbacks(adp, new ActionBar.OnNavigationListener() {
			public boolean onNavigationItemSelected(int i, long l) {
				wb.loadUrl(new StringBuilder("file://").append(getContext().getFilesDir().getAbsolutePath()).append("/share/doc/qemu/").append(adp.getItem(i)).toString());
				return true;
			}
		});
		if (MainApplication.IS_DARK)
			getActivity().findViewById(R.id.helpView1).setVisibility(View.VISIBLE);
		WebSettings st=wb.getSettings();
		st.setUseWideViewPort(true);
		st.setLoadWithOverviewMode(true);
		st.setJavaScriptEnabled(true);
		st.setSupportZoom(true);
		st.setDefaultZoom(WebSettings.ZoomDensity.FAR);
		st.setBuiltInZoomControls(true);
		st.setDisplayZoomControls(false);
		prb=new ProgressBar(getActivity(),null,android.R.attr.progressBarStyleHorizontal);
		wb.addView(prb,new WebView.LayoutParams(WebView.LayoutParams.FILL_PARENT,WebView.LayoutParams.WRAP_CONTENT,0,0));
		prb.setVisibility(View.GONE);
		wb.setWebChromeClient(new WebChromeClient(){
				@Override public boolean onJsAlert(WebView webView, String str, String str2, final JsResult jr){
					new android.support.v7.app.AlertDialog.Builder(getActivity())
						.setTitle("网页信息").setMessage(str2)
						.setPositiveButton(android.R.string.ok,null)
						.setOnDismissListener(new DialogInterface.OnDismissListener(){
							@Override
							public void onDismiss(DialogInterface dia){
								jr.confirm();
							}
						}).create().show();
					return true;
				}
				@Override public void onProgressChanged(WebView view,int progress){
					prb.setProgress(progress);
				}
			});
		wb.setWebViewClient(new WebViewClient(){
				@Override public void onPageFinished(WebView view, String url) {
					prb.setVisibility(View.GONE);
				}
				@Override public void onPageStarted(WebView view,String url,android.graphics.Bitmap bp){
					prb.setVisibility(View.VISIBLE);
				}
			});
	}

	@Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
		inflater.inflate(R.menu.help_tool,menu);
	}

	@Override public void onHiddenChanged(boolean hidden){
		ActionBar act=ma.getSupportActionBar();
		act.setDisplayShowTitleEnabled(hidden);
		act.setNavigationMode(hidden?ActionBar.NAVIGATION_MODE_STANDARD:ActionBar.NAVIGATION_MODE_LIST);
	}

	@Override public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
			case R.id.help_search:
				ma.startSupportActionMode(this);
				break;
		}
		return true;
	}

	@Override public void onDestroyActionMode(ActionMode actionMode){
		wb.clearMatches();
	}

	@Override public boolean onCreateActionMode(ActionMode p1, Menu p2){
		View lay=View.inflate(getActivity(),R.layout.help_action,null);
		EditText edt=lay.findViewById(R.id.helpactionEditText1);
		final TextView txv=lay.findViewById(R.id.helpactionTextView1);
		p1.setCustomView(lay);
		edt.requestFocus();
		edt.addTextChangedListener(this);
		wb.setFindListener(new WebView.FindListener(){
				@Override
				public void onFindResultReceived(int i, int i2, boolean isf) {
					txv.setText((i2>0?i+1:0)+"/"+i2);
				}
			});
		p1.getMenuInflater().inflate(R.menu.help_find,p2);
		return true;
	}

	@Override public boolean onPrepareActionMode(ActionMode p1, Menu p2){
		return false;
	}

	@Override public boolean onActionItemClicked(ActionMode acm, MenuItem itm){
		switch(itm.getItemId()){
			case R.id.help_last:
				wb.findNext(false);
				break;
			case R.id.help_next:
				wb.findNext(true);
				break;
		}
		return true;
	}

	@Override
	public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3)
	{}

	@Override
	public void onTextChanged(CharSequence charSequence, int i, int i2, int i3)
	{}

	@Override
	public void afterTextChanged(Editable editable){
		wb.findAllAsync(editable.toString());
	}

	public boolean accept(File f) {
		return f.isFile()&&f.getName().endsWith(".html");
	}

	private ArrayList<String> getHelpList(){
		File[] hl = new File(getActivity().getFilesDir().getPath()+"/share/doc/qemu").listFiles(this);
		ArrayList<String> list = new ArrayList<String>();
		if (hl==null)
			return list;
		for (File f:hl)
			list.add(f.getName());
		return list;
	}
}

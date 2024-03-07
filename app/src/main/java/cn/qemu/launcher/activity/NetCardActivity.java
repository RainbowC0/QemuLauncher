package cn.qemu.launcher.activity;
import android.support.v7.app.*;
import android.os.*;
import android.content.*;
import cn.qemu.launcher.*;
import org.zhoutz.utils.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import cn.qemu.launcher.adapter.*;
import java.util.*;
import android.support.design.widget.*;
import cn.qemu.launcher.application.*;

public class NetCardActivity extends AppCompatActivity{
	ListView list;
	SharedPreferences conf;
	String[] networkCards;
	ArrayList<String> ls = new ArrayList<String>();

	@Override protected void onCreate(Bundle savedInstanceState){
		SharedPreferences set=getSharedPreferences(MainApplication.PREF_SETTINGS, MODE_PRIVATE);
		if(set.getBoolean("is_dark",false))setTheme(R.style.AppTheme_Dark);
		super.onCreate(savedInstanceState);
		networkCards = getIntent().getExtras().getStringArray("network_cards");
		TypedValue ty=new TypedValue();getTheme().resolveAttribute(R.attr.actionMenuTextColor,ty,true);
		//DEFAULT=getResources().getColor(R.color.main);
		if(android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.KITKAT&&android.os.Build.VERSION.SDK_INT<android.os.Build.VERSION_CODES.LOLLIPOP){
			getTheme().resolveAttribute(R.attr.colorPrimaryDark,ty,true);
			ActivityUtil.setStatusBarColor(this,ty.data);
		}
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.conf_list);
		list = findViewById(R.id.conflist);
		View en=findViewById(R.id.conflinear);
		((TextView)en.findViewById(R.id.list_empty_text)).setText(R.string.empty_text_netcards);
		((TextView)en.findViewById(R.id.list_empty_summary)).setText(R.string.empty_summary_netcards);
		list.setEmptyView(en);
		conf = getSharedPreferences("configuration", MODE_PRIVATE);
		for (String s:conf.getString("net_card"," ").split("\\s+"))
			ls.add(s);
		final NetCardsAdapter aap=new NetCardsAdapter(this, ls);
		list.setAdapter(aap);
		list.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			@Override public void onItemClick(AdapterView<?> p1,View p2,int p3,long p4){
				showEditDialog(p3,aap.getItem(p3));
			}
		});
	}

	@Override public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
			case android.R.id.home:
				setResult(RESULT_CANCELED,getIntent());
				finish();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void addNew(View v){
		showEditDialog(-1,"");
	}

	public void showEditDialog(final int pos,String content){
		content = content+",";
		View v = View.inflate(this,R.layout.netcard_dialog,null);
		final Spinner sp = v.findViewById(R.id.net_type);
		sp.setAdapter(new ArrayAdapter<String>(this,R.layout.select_dialog_item_material, networkCards));
		if (pos > -1)
			sp.setSelection(((ArrayAdapter<String>)sp.getAdapter()).getPosition(ACode.sj(content,"model=",",")));
		final CheckBox ad = v.findViewById(R.id.net_advance);
		final View lin = v.findViewById(R.id.net_adlayout);
		ad.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){
			public void onCheckedChanged(CompoundButton ck,boolean bool){
				lin.setVisibility(bool?View.VISIBLE:View.GONE);
			}
		});
		final TextInputEditText ev = v.findViewById(R.id.net_vlan);
		final TextInputEditText en = v.findViewById(R.id.net_name);
		final TextInputEditText em = v.findViewById(R.id.net_mac);
		if(content.matches(".*(vlan|name|macaddr).*")){
			ad.setChecked(true);
			ev.setText(ACode.sj(content,"vlan=",","));
			en.setText(ACode.sj(content,"name=",","));
			em.setText(ACode.sj(content,"macaddr=",","));
		}
		AlertDialog.Builder bd=new AlertDialog.Builder(this)
		.setTitle("配置网卡")
		.setView(v)
		.setPositiveButton(android.R.string.ok,new DialogInterface.OnClickListener(){
			@Override public void onClick(DialogInterface p1,int p2){
				StringBuffer str = new StringBuffer("model=").append(sp.getSelectedItem().toString());
				if(ad.isChecked()){
					if(ev.length()>0)str.append(",vlan=").append(ev.getText());
					if(en.length()>0)str.append(",name=").append(en.getText());
					if(em.length()>0)str.append(",macaddr=").append(em.getText());
				}
				if(pos>-1)ls.set(pos,str.toString());
				else ls.add(str.toString());
				((NetCardsAdapter)list.getAdapter()).notifyDataSetChanged();
			}
		})
		.setNegativeButton(android.R.string.cancel,null);
		if(pos>-1){
			bd.setNeutralButton(R.string.delete,new DialogInterface.OnClickListener(){
				@Override public void onClick(DialogInterface p1,int p2){
					new AlertDialog.Builder(NetCardActivity.this)
					.setIcon(R.drawable.ic_warning_amber_24dp)
					.setTitle("警告")
					.setMessage("确定删除该网卡？")
					.setPositiveButton(android.R.string.ok,new DialogInterface.OnClickListener(){
						@Override public void onClick(DialogInterface p1,int p2){
							ls.remove(pos);((NetCardsAdapter)list.getAdapter()).notifyDataSetChanged();
						}
					})
					.setNegativeButton(android.R.string.cancel,null)
					.create().show();
				}
			});
		}
		bd.create().show();
	}

	@Override
	public void finish(){
		StringBuffer as = new StringBuffer();
		for(String a:ls)
			as.append(a).append(' ');
		conf.edit().putString("net_card", as.append(' ').toString()).commit();
		super.finish();
	}
}

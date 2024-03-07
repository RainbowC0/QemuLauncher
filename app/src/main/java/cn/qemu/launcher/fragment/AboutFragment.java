package cn.qemu.launcher.fragment;

import android.support.v4.app.Fragment;
import android.view.*;
import android.os.*;
import cn.qemu.launcher.R;
import android.widget.TextView;
import android.text.method.LinkMovementMethod;

public class AboutFragment extends Fragment{
	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		return inflater.inflate(R.layout.about,container,false);
	}

	@Override public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		((TextView)getActivity().findViewById(R.id.versionView))
		.setText(String.format(getString(R.string.app_ver),org.zhoutz.utils.ActivityUtil.getVerName(getActivity())));
		((TextView)getActivity().findViewById(R.id.about_text))
		.setMovementMethod(LinkMovementMethod.getInstance());
	}
}

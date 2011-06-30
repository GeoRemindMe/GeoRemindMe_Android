package org.georemindme.community.view;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import org.georemindme.community.R;

public class ListTabActivity extends TabActivity
{
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		TabHost host = getTabHost();
		
		TabSpec tabAllUndoneTask = host.newTabSpec(getString(R.string.pending));
		tabAllUndoneTask.setIndicator(getString(R.string.pending));
		Intent undone = new Intent(this, UndoneAlertList.class);
		undone.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		tabAllUndoneTask.setContent(undone);
		
		TabSpec tabAllMutedTask = host.newTabSpec(getString(R.string.done));
		tabAllMutedTask.setIndicator(getString(R.string.done));
		Intent done = new Intent(this, DoneAlertList.class);
		done.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		tabAllMutedTask.setContent(done);
		
		TabSpec tabAllNearTask = host.newTabSpec(getString(R.string.muted));
		tabAllNearTask.setIndicator(getString(R.string.muted));
		Intent muted = new Intent(this, MutedAlertList.class);
		muted.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		tabAllNearTask.setContent(muted);
	
		
		host.addTab(tabAllUndoneTask);
		host.addTab(tabAllMutedTask);
		host.addTab(tabAllNearTask);
	}
	
	public void onStop()
	{
		super.onStop();
		
	}
}

package org.georemindme.community.view;


import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;


public class AlertOverlay extends ItemizedOverlay<OverlayItem>
{
	private List<OverlayItem>	items	= new ArrayList<OverlayItem>();
	private Drawable			marker	= null;
	
	
	public AlertOverlay(Drawable defaultMarker)
	{
		super(defaultMarker);
		// TODO Auto-generated constructor stub
		
		marker = defaultMarker;
		
		populateChanges();
	}
	
	public void cleanOverlays()
	{
		items.clear();
		populateChanges();
	}
	
	public void addOverlayItem(OverlayItem overlayItem)
	{
		items.add(overlayItem);
	}
	
	public void populateChanges()
	{
		populate();
	}

	@Override
	protected OverlayItem createItem(int i)
	{
		// TODO Auto-generated method stub
		return items.get(i);
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow)
	{
		super.draw(canvas, mapView, shadow);
		boundCenterBottom(marker);
	}

	@Override
	public int size()
	{
		// TODO Auto-generated method stub
		return items.size();
	}
	
}

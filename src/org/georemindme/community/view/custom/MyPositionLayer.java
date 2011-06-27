package org.georemindme.community.view.custom;


import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;


public class MyPositionLayer extends ItemizedOverlay<OverlayItem>
{
	private OverlayItem					position;
	private Drawable					marker				= null;
	private OverlayItem					onDrag				= null;
	private ImageView					dragImage			= null;
	private int							xDragImageOffset	= 0;
	private int							yDragImageOffset	= 0;
	private int							xDragTouchOffset	= 0;
	private int							yDragTouchOffset	= 0;
	
	private UserSetNewLocationListener	newlocationlistener;
	
	private boolean						draggable;
	
	
	public MyPositionLayer(Drawable defaultMarker, OverlayItem item,
			ImageView imageView)
	{
		super(boundCenterBottom(defaultMarker));
		
		marker = defaultMarker;
		
		draggable = true;
		
		/*
		 * dragImage = imageView; xDragImageOffset =
		 * dragImage.getDrawable().getIntrinsicWidth() / 2; yDragImageOffset =
		 * dragImage.getDrawable().getIntrinsicHeight();
		 */
		position = item;
		
		populate();
		// TODO Auto-generated constructor stub
	}
	

	public void draw(Canvas canvas, MapView mapView, boolean shadow)
	{
		super.draw(canvas, mapView, shadow);
		
		boundCenterBottom(marker);
	}
	

	public void setOverlayItem(OverlayItem overlay)
	{
		position = overlay;
		populate();
	}
	

	public void setDraggable(boolean draggable)
	{
		this.draggable = draggable;
	}
	

	public boolean isDraggable()
	{
		return draggable;
	}
	

	public boolean onTouchEvent(MotionEvent event, MapView mapView)
	{
		final int action = event.getAction();
		final int x = (int) event.getX();
		final int y = (int) event.getY();
		boolean result = false;
		
		if (draggable)
		{
			if (action == MotionEvent.ACTION_DOWN)
			{
				Point p = new Point(0, 0);
				
				mapView.getProjection().toPixels(position.getPoint(), p);
				
				if (hitTest(position, marker, x - p.x, y - p.y))
				{
					result = true;
					onDrag = position;
					position = null;
					// populate();
					
					xDragTouchOffset = 0;
					yDragTouchOffset = 0;
					
					// setDragImagePosition(p.x, p.y);
					// dragImage.setVisibility(View.VISIBLE);
					
					xDragTouchOffset = x - p.x;
					yDragTouchOffset = y - p.y;
				}
				
			}
			else if (action == MotionEvent.ACTION_MOVE && onDrag != null)
			{
				// setDragImagePosition(x, y);
				GeoPoint pt = mapView.getProjection().fromPixels(x
						- xDragTouchOffset, y - yDragTouchOffset);
				OverlayItem toDrop = new OverlayItem(pt, onDrag.getTitle(), onDrag.getSnippet());
				position = toDrop;
				populate();
				result = true;
			}
			else if (action == MotionEvent.ACTION_UP && onDrag != null)
			{
				// dragImage.setVisibility(View.GONE);
				GeoPoint pt = mapView.getProjection().fromPixels(x
						- xDragTouchOffset, y - yDragTouchOffset);
				OverlayItem toDrop = new OverlayItem(pt, onDrag.getTitle(), onDrag.getSnippet());
				position = toDrop;
				// populate();
				
				newlocationlistener.userSetNewLocationListener(pt);
				
				onDrag = null;
				result = true;
			}
		}
		return (result || super.onTouchEvent(event, mapView));
	}
	

	private void setDragImagePosition(int x, int y)
	{
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) dragImage.getLayoutParams();
		
		lp.setMargins(x - xDragImageOffset - xDragTouchOffset, y
				- yDragImageOffset - yDragTouchOffset, 0, 0);
		dragImage.setLayoutParams(lp);
	}
	

	@Override
	protected OverlayItem createItem(int i)
	{
		// TODO Auto-generated method stub
		return position;
	}
	

	@Override
	public int size()
	{
		// TODO Auto-generated method stub
		return 1;
	}
	
	public interface UserSetNewLocationListener
	{
		public void userSetNewLocationListener(GeoPoint gp);
	}
	
	
	public void addUserSetNewLocationListener(UserSetNewLocationListener u)
	{
		newlocationlistener = u;
	}
}

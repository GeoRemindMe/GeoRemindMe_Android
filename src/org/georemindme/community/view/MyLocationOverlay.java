package org.georemindme.community.view;

import org.georemindme.community.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class MyLocationOverlay extends Overlay
{
	private GeoPoint p = null;
	private int drawable;
	private Context context;
	private int markerWidth = 0, markerHeight = 0;
	private long startTouch, endTouch;

	public MyLocationOverlay(Context context, GeoPoint p)
	{
		this.p = p;
		this.drawable = R.drawable.blue_small;
		this.context = context;
		
	}

	public boolean draw(Canvas canvas, MapView mapView, boolean shadow,
			long when)
	{
		super.draw(canvas, mapView, shadow);

		Bitmap marker = BitmapFactory.decodeResource(context.getResources(),
				drawable);

		Point out = new Point();

		markerWidth = marker.getWidth();
		markerHeight = marker.getHeight();

		mapView.getProjection().toPixels(p, out);
		canvas.drawBitmap(marker, out.x - markerWidth / 2, out.y - markerHeight
				/ 2, null);

		return true;
	}

	public boolean onTap(GeoPoint p, MapView mapView)
	{
		Point pointTap = mapView.getProjection().toPixels(p, null);
		Point pointMap = mapView.getProjection().toPixels(this.p, null);

		/*
		 * if (pointTap.x - pointMap.x >= 0 && pointTap.x - pointMap.x <=
		 * markerWidth && pointMap.y - pointTap.y >= 0 && pointMap.y -
		 * pointTap.y <= markerHeight)
		 */
		if ((pointTap.x > (pointMap.x - (markerWidth / 2)))
				&& (pointTap.x < (pointMap.x + (markerWidth / 2)))
				&& (pointTap.y > (pointMap.y - (markerHeight / 2)))
				&& (pointTap.y < (pointMap.y + (markerHeight / 2))))
		{
			// I have to ask for the dialog to the map
			Log.v(p.getLatitudeE6() +"", p.getLongitudeE6() + "");
			
			return true;
		}

		return super.onTap(p, mapView);
	}


}

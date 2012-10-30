package org.oscim.overlay;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;

import org.oscim.core.MapPosition;
import org.oscim.overlay.Overlay.Snappable;
import org.oscim.renderer.overlays.RenderOverlay;
import org.oscim.view.MapView;

import android.graphics.Point;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class OverlayManager extends AbstractList<Overlay> {

	// private TilesOverlay mTilesOverlay;

	/* package */final CopyOnWriteArrayList<Overlay> mOverlayList;

	public OverlayManager() {
		// final TilesOverlay tilesOverlay) {
		// setTilesOverlay(tilesOverlay);
		mOverlayList = new CopyOnWriteArrayList<Overlay>();

	}

	@Override
	public synchronized Overlay get(final int pIndex) {
		return mOverlayList.get(pIndex);
	}

	@Override
	public synchronized int size() {
		return mOverlayList.size();
	}

	@Override
	public synchronized void add(final int pIndex, final Overlay pElement) {
		mOverlayList.add(pIndex, pElement);
		mUpdateDrawLayers = true;
		mUpdateLayers = true;
	}

	@Override
	public synchronized Overlay remove(final int pIndex) {
		mUpdateDrawLayers = true;
		mUpdateLayers = true;
		return mOverlayList.remove(pIndex);
	}

	@Override
	public synchronized Overlay set(final int pIndex, final Overlay pElement) {
		mUpdateDrawLayers = true;
		mUpdateLayers = true;
		return mOverlayList.set(pIndex, pElement);
	}

	// /**
	// * Gets the optional TilesOverlay class.
	// *
	// * @return the tilesOverlay
	// */
	// public TilesOverlay getTilesOverlay() {
	// return mTilesOverlay;
	// }
	//
	// /**
	// * Sets the optional TilesOverlay class. If set, this overlay will be
	// drawn before all other
	// * overlays and will not be included in the editable list of overlays and
	// can't be cleared
	// * except by a subsequent call to setTilesOverlay().
	// *
	// * @param tilesOverlay
	// * the tilesOverlay to set
	// */
	// public void setTilesOverlay(final TilesOverlay tilesOverlay) {
	// mTilesOverlay = tilesOverlay;
	// }

	public Iterable<Overlay> overlaysReversed() {
		return new Iterable<Overlay>() {
			@Override
			public Iterator<Overlay> iterator() {
				final ListIterator<Overlay> i = mOverlayList.listIterator(mOverlayList.size());

				return new Iterator<Overlay>() {
					@Override
					public boolean hasNext() {
						return i.hasPrevious();
					}

					@Override
					public Overlay next() {
						return i.previous();
					}

					@Override
					public void remove() {
						i.remove();
					}
				};
			}
		};
	}

	private boolean mUpdateLayers;
	private boolean mUpdateDrawLayers;
	private List<RenderOverlay> mDrawLayers = new ArrayList<RenderOverlay>();

	public List<RenderOverlay> getRenderLayers() {
		if (mUpdateDrawLayers) {
			synchronized (this) {

				mUpdateDrawLayers = false;
				mDrawLayers.clear();

				for (Overlay o : mOverlayList) {
					RenderOverlay l = o.getLayer();
					if (l != null)
						mDrawLayers.add(l);
				}
			}
		}
		return mDrawLayers;
	}

	//	public void onDraw(final Canvas c, final MapView pMapView) {
	//		// if ((mTilesOverlay != null) && mTilesOverlay.isEnabled()) {
	//		// mTilesOverlay.draw(c, pMapView, true);
	//		// }
	//		//
	//		// if ((mTilesOverlay != null) && mTilesOverlay.isEnabled()) {
	//		// mTilesOverlay.draw(c, pMapView, false);
	//		// }
	//
	//		for (final Overlay overlay : mOverlayList) {
	//			if (overlay.isEnabled()) {
	//				overlay.draw(c, pMapView, true);
	//			}
	//		}
	//
	//		for (final Overlay overlay : mOverlayList) {
	//			if (overlay.isEnabled()) {
	//				overlay.draw(c, pMapView, false);
	//			}
	//		}
	//
	//	}

	public void onDetach(final MapView pMapView) {
		// if (mTilesOverlay != null) {
		// mTilesOverlay.onDetach(pMapView);
		// }

		for (final Overlay overlay : this.overlaysReversed()) {
			overlay.onDetach(pMapView);
		}
	}

	Overlay[] mOverlays;

	private synchronized void updateOverlays() {
		mOverlays = new Overlay[mOverlayList.size()];
		mOverlays = mOverlayList.toArray(mOverlays);
		mUpdateLayers = false;
	}

	public boolean onKeyDown(final int keyCode, final KeyEvent event, final MapView pMapView) {
		if (mUpdateLayers)
			updateOverlays();

		for (int i = mOverlays.length - 1; i >= 0; i--)
			if (mOverlays[i].onKeyDown(keyCode, event, pMapView))
				return true;

		return false;
	}

	public boolean onKeyUp(final int keyCode, final KeyEvent event, final MapView pMapView) {
		for (final Overlay overlay : this.overlaysReversed()) {
			if (overlay.onKeyUp(keyCode, event, pMapView)) {
				return true;
			}
		}

		return false;
	}

	public boolean onTouchEvent(final MotionEvent event, final MapView pMapView) {
		for (final Overlay overlay : this.overlaysReversed()) {
			if (overlay.onTouchEvent(event, pMapView)) {
				return true;
			}
		}

		return false;
	}

	public boolean onTrackballEvent(final MotionEvent event, final MapView pMapView) {
		for (final Overlay overlay : this.overlaysReversed()) {
			if (overlay.onTrackballEvent(event, pMapView)) {
				return true;
			}
		}

		return false;
	}

	public boolean onSnapToItem(final int x, final int y, final Point snapPoint,
			final MapView pMapView) {
		for (final Overlay overlay : this.overlaysReversed()) {
			if (overlay instanceof Snappable) {
				if (((Snappable) overlay).onSnapToItem(x, y, snapPoint, pMapView)) {
					return true;
				}
			}
		}

		return false;
	}

	/* GestureDetector.OnDoubleTapListener */

	public boolean onDoubleTap(final MotionEvent e, final MapView pMapView) {
		for (final Overlay overlay : this.overlaysReversed()) {
			if (overlay.onDoubleTap(e, pMapView)) {
				return true;
			}
		}

		return false;
	}

	public boolean onDoubleTapEvent(final MotionEvent e, final MapView pMapView) {
		for (final Overlay overlay : this.overlaysReversed()) {
			if (overlay.onDoubleTapEvent(e, pMapView)) {
				return true;
			}
		}

		return false;
	}

	public boolean onSingleTapConfirmed(final MotionEvent e, final MapView pMapView) {
		for (final Overlay overlay : this.overlaysReversed()) {
			if (overlay.onSingleTapConfirmed(e, pMapView)) {
				return true;
			}
		}

		return false;
	}

	/* OnGestureListener */

	public boolean onDown(final MotionEvent pEvent, final MapView pMapView) {
		for (final Overlay overlay : this.overlaysReversed()) {
			if (overlay.onDown(pEvent, pMapView)) {
				return true;
			}
		}

		return false;
	}

	public boolean onFling(final MotionEvent pEvent1, final MotionEvent pEvent2,
			final float pVelocityX, final float pVelocityY, final MapView pMapView) {
		for (final Overlay overlay : this.overlaysReversed()) {
			if (overlay.onFling(pEvent1, pEvent2, pVelocityX, pVelocityY, pMapView)) {
				return true;
			}
		}

		return false;
	}

	public boolean onLongPress(final MotionEvent pEvent, final MapView pMapView) {
		for (final Overlay overlay : this.overlaysReversed()) {
			if (overlay.onLongPress(pEvent, pMapView)) {
				return true;
			}
		}

		return false;
	}

	public boolean onScroll(final MotionEvent pEvent1, final MotionEvent pEvent2,
			final float pDistanceX, final float pDistanceY, final MapView pMapView) {
		for (final Overlay overlay : this.overlaysReversed()) {
			if (overlay.onScroll(pEvent1, pEvent2, pDistanceX, pDistanceY, pMapView)) {
				return true;
			}
		}

		return false;
	}

	public void onShowPress(final MotionEvent pEvent, final MapView pMapView) {
		for (final Overlay overlay : this.overlaysReversed()) {
			overlay.onShowPress(pEvent, pMapView);
		}
	}

	public boolean onSingleTapUp(final MotionEvent pEvent, final MapView pMapView) {
		if (mUpdateLayers)
			updateOverlays();

		for (int i = mOverlays.length - 1; i >= 0; i--)
			if (mOverlays[i].onSingleTapUp(pEvent, pMapView))
				return true;

		//		for (final Overlay overlay : this.overlaysReversed()) {
		//			if (overlay.onSingleTapUp(pEvent, pMapView)) {
		//				return true;
		//			}
		//		}

		return false;
	}

	public void onUpdate(MapPosition mapPosition, boolean changed) {
		if (mUpdateLayers)
			updateOverlays();

		for (int i = mOverlays.length - 1; i >= 0; i--)
			mOverlays[i].onUpdate(mapPosition, changed);

		//		for (final Overlay overlay : this.overlaysReversed()) {
		//			overlay.onUpdate(mapPosition);
		//		}
	}

	// ** Options Menu **//

	// public void setOptionsMenusEnabled(final boolean pEnabled) {
	// for (final Overlay overlay : mOverlayList) {
	// if ((overlay instanceof IOverlayMenuProvider)
	// && ((IOverlayMenuProvider) overlay).isOptionsMenuEnabled()) {
	// ((IOverlayMenuProvider) overlay).setOptionsMenuEnabled(pEnabled);
	// }
	// }
	// }
	//
	// public boolean onCreateOptionsMenu(final Menu pMenu, final int
	// menuIdOffset,
	// final MapView mapView) {
	// boolean result = true;
	// for (final Overlay overlay : this.overlaysReversed()) {
	// if ((overlay instanceof IOverlayMenuProvider)
	// && ((IOverlayMenuProvider) overlay).isOptionsMenuEnabled()) {
	// result &= ((IOverlayMenuProvider) overlay).onCreateOptionsMenu(pMenu,
	// menuIdOffset,
	// mapView);
	// }
	// }
	//
	// if ((mTilesOverlay != null) && (mTilesOverlay instanceof
	// IOverlayMenuProvider)
	// && ((IOverlayMenuProvider) mTilesOverlay).isOptionsMenuEnabled()) {
	// result &= mTilesOverlay.onCreateOptionsMenu(pMenu, menuIdOffset,
	// mapView);
	// }
	//
	// return result;
	// }
	//
	// public boolean onPrepareOptionsMenu(final Menu pMenu, final int
	// menuIdOffset,
	// final MapView mapView) {
	// for (final Overlay overlay : this.overlaysReversed()) {
	// if ((overlay instanceof IOverlayMenuProvider)
	// && ((IOverlayMenuProvider) overlay).isOptionsMenuEnabled()) {
	// ((IOverlayMenuProvider) overlay).onPrepareOptionsMenu(pMenu,
	// menuIdOffset, mapView);
	// }
	// }
	//
	// if ((mTilesOverlay != null) && (mTilesOverlay instanceof
	// IOverlayMenuProvider)
	// && ((IOverlayMenuProvider) mTilesOverlay).isOptionsMenuEnabled()) {
	// mTilesOverlay.onPrepareOptionsMenu(pMenu, menuIdOffset, mapView);
	// }
	//
	// return true;
	// }
	//
	// public boolean onOptionsItemSelected(final MenuItem item, final int
	// menuIdOffset,
	// final MapView mapView) {
	// for (final Overlay overlay : this.overlaysReversed()) {
	// if ((overlay instanceof IOverlayMenuProvider)
	// && ((IOverlayMenuProvider) overlay).isOptionsMenuEnabled()
	// && ((IOverlayMenuProvider) overlay).onOptionsItemSelected(item,
	// menuIdOffset,
	// mapView)) {
	// return true;
	// }
	// }
	//
	// if ((mTilesOverlay != null)
	// && (mTilesOverlay instanceof IOverlayMenuProvider)
	// && ((IOverlayMenuProvider) mTilesOverlay).isOptionsMenuEnabled()
	// && ((IOverlayMenuProvider) mTilesOverlay).onOptionsItemSelected(item,
	// menuIdOffset,
	// mapView)) {
	// return true;
	// }
	//
	// return false;
	// }
}

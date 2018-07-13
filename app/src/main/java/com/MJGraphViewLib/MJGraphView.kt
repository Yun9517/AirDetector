package com.mobile2box.MJGraphView

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import java.util.*

// ==================
// Class: MJGraphView
// ==================
class MJGraphView @JvmOverloads constructor(_context: Context, _attrs: AttributeSet? = null, _defStyleAttr: Int = 0) : HorizontalScrollView(_context, _attrs, _defStyleAttr) {

	// public constants
	// ----------------
	companion object {
		const val MODE_DAILY = 0
		const val MODE_MONTHLY = 2
		const val MODE_WEEKLY = 1

		const val DEFAULT_RANGE_DAILY = 30								// last 30 days * 1440 minutes per day = 43200 records
		const val DEFAULT_RANGE_MONTHLY = 365							// last 365 days * 1 day per day = 365 records
		const val DEFAULT_RANGE_WEEKLY = 365							// last 365 days * 24 hours per day = 8760 records
	}

	// private properties
	// ------------------
	private var viewGraph: MJGraphView.MJGraphSubView? = null
	private var oGestureDetector: ScaleGestureDetector? = null
	private var fScale: Float = 1.toFloat()

	// temporary properties
	// --------------------


	// private final Action properties
	// -------------------------------


	// private final properties
	// ------------------------
	private val hdlrOnScale = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {

		override fun onScale(_detector: ScaleGestureDetector?): Boolean {
			if ((viewGraph != null) && (_detector != null)) {
				// update scale factor & set subview's item gap
				// --------------------------------------------
				fScale *= _detector.scaleFactor
				viewGraph!!.SetScale(fScale)
			}
			return true
		}

		override fun onScaleBegin(_detector: ScaleGestureDetector): Boolean {
			fScale = 1.toFloat()
			viewGraph?.OnBeginScaling()
			return true
		}

		override fun onScaleEnd(_detector: ScaleGestureDetector) {
			viewGraph?.OnEndScaling(fScale)
		}
	}

	// ---------------------
	// Class: MJGraphSubView
	// ---------------------
	private class MJGraphSubView(_contex: Context) : View(_contex) {

		// private constants
		// -----------------
		private val MJGraphSubView_CURSOR_SIZE_MIN = 8
		private val MJGraphSubView_CURSOR_SIZE_MAX = 24
		private val MJGraphSubView_ITEM_GAP_MIN = 2
		private val MJGraphSubView_ITEM_GAP_MAX = 6
		private val MJGraphSubView_LINE_WIDTH_MIN = 2
		private val MJGraphSubView_LINE_WIDTH_MAX = 8

		// private properties
		// ------------------
		private var iInterval: Int = 1
		private var iMode: Int = -1

		private var bmapCursor: Bitmap? = null
		private val pantDash = Paint(Paint.ANTI_ALIAS_FLAG)
		private val pantDivider = Paint(Paint.ANTI_ALIAS_FLAG)
		private val pantLine = Paint(Paint.ANTI_ALIAS_FLAG)
		private val pantText = Paint(Paint.ANTI_ALIAS_FLAG)
		private var pathDash = Path()
		private var pathLine = Path()

		// temporary properties
		// --------------------
		private var view_Parent: HorizontalScrollView? = null

		private var a_Data: MutableList<MJGraphData>? = null
		private var a_Month: Array<String>? = null
		private var a_Week: Array<String>? = null
		private var s_Year: String? = null

		private var fCursorSize: Float = 0.toFloat()
		private var fCursorY: Float = 0.toFloat()
		private var fHalfCursorSize: Float = 0.toFloat()
		private var fHalfPageWidth: Float = 0.toFloat()
		private var fItemGap: Float = 0.toFloat()
		private var fItemScrollOffset: Float = 0.toFloat()
		private var fItemWidth: Float = 0.toFloat()
		private var fLineWidth: Float = 0.toFloat()
		private var fPixelScale: Float = 1.toFloat()
		private var fScaleWidth: Float = 0.toFloat()
		private var fScalingScrollX: Float = 0.toFloat()

		private var iContentWidth: Int = 0
		private var iIndexCurrent: Int = -1
		private var iIndexEnd: Int = 0
		private var iIndexInc: Int = 1
		private var iIndexStart: Int = 0
		private var iItemOffscreen: Int = 0
		private var iItemPerPage: Int = 0
		private var iScaling: Int = 0
		private var bCallOnUpdate: Boolean = false
		private var bShowCursor: Boolean = false
		private var hdlr_OnUpdate: MJGraphView.MJGraphUpdateCallback? = null

		// private final properties
		// ------------------------


		// constructors & destructors
		// --------------------------
		init {
			// initialize properties
			// ---------------------
			val rs: Resources? = resources
			if (rs != null) {
				val dm: DisplayMetrics? = rs.displayMetrics
				if (dm != null)
					fPixelScale = dm.density
			}

			fCursorSize = fPixelScale * 18
			fHalfCursorSize = fCursorSize / 2
			fItemGap = fPixelScale * MJGraphSubView_ITEM_GAP_MIN
			bShowCursor = true

			// set default LineWidth
			// ---------------------
			SetLineWidth(6)

			// initialize Paint and Path
			// -------------------------
			pantDash.color = 0xffffffff.toInt()
			pantDash.pathEffect = DashPathEffect(floatArrayOf(fLineWidth, fItemGap), 0f)
			pantDash.strokeJoin = Paint.Join.ROUND
			pantDash.strokeWidth = fLineWidth
			pantDash.style = Paint.Style.STROKE

			pantDivider.color = 0x66ffffff
			pantDivider.strokeWidth = fPixelScale
			pantDivider.style = Paint.Style.STROKE

			pantLine.color = 0xffffffff.toInt()
			pantLine.strokeJoin = Paint.Join.ROUND
			pantLine.strokeWidth = fLineWidth
			pantLine.style = Paint.Style.STROKE

			pantText.color = 0xffffffff.toInt()
			pantText.style = Paint.Style.FILL
			pantText.textSize = fPixelScale * 10

			// set default Mode
			// ----------------
			SetMode(MJGraphView.MODE_MONTHLY)
		}

		fun Destroy() {
			view_Parent = null
			a_Data = null
			a_Month = null
			a_Week = null
			s_Year = null
			hdlr_OnUpdate = null

			// release Bitmap
			// --------------
			DestroyCursor()
		}

		// protected View overrides
		// ------------------------
		override fun onDraw(_canvas: Canvas?) {
			super.onDraw(_canvas)
			if ((a_Data != null) && (view_Parent != null) && (_canvas != null)) {
				var o: MJGraphData?
				val fScrollX = view_Parent!!.scrollX.toFloat()
				var iCursorIndex = iIndexCurrent
				val h1 = height
				val h2 = h1 - (fCursorSize * 2).toInt()
				var i: Int
				var x1: Float
				var x2: Float
				var y1: Float
				var y2: Float
				var ya: Float = 0.toFloat()
				var yb: Float = 0.toFloat()

				// draw divider & label
				// --------------------
				x1 = fHalfPageWidth
				i = iIndexStart
				while (i <= iIndexEnd) {
					var s: String?
					try {
						o = a_Data!![i]
						s = o.Label()
						if (s != null) {
							_canvas.drawLine(x1, 0f, x1, h1.toFloat(), pantDivider)
							_canvas.drawText(s, (x1 + fScaleWidth), pantText.textSize, pantText)
						}
					}
					catch (_e: IndexOutOfBoundsException) {
						_e.printStackTrace()
					}
					catch (_e: NullPointerException) {
						_e.printStackTrace()
					}
					x1 += fItemWidth
					i += iIndexInc
				}

				// draw line graph
				// ---------------
				try {
					var bFutureTime = false
					val lCurrentTime = System.currentTimeMillis()
					val j = (fScrollX / fItemWidth).toInt()
					var k = j - iItemOffscreen
					if (k < 0)
						k = 0

					i = k * iIndexInc + iIndexStart
					if (i > iIndexEnd)
						i = iIndexEnd

					o = a_Data!![i]

					x1 = fHalfPageWidth + fItemWidth * k
					x2 = x1 + fItemWidth
					y1 = h2 * o.ScaledValue() + fCursorSize
					ya = y1
					yb = y1

					pathDash.reset()
					pathLine.reset()
					pathDash.moveTo(x1, y1)
					pathLine.moveTo(x1, y1)

					// check if the time is in future
					// ------------------------------
					if (o.TimeStamp() > lCurrentTime)
						bFutureTime = true

					iCursorIndex = j * iIndexInc + iIndexStart
					if (iCursorIndex > iIndexEnd)
						iCursorIndex = iIndexEnd

					k = iCursorIndex + iIndexInc

					i += iIndexInc
					var z = (iItemPerPage + iItemOffscreen) * iIndexInc + i
					if (z > iIndexEnd)
						z = iIndexEnd

					while (i <= z) {
						o = a_Data!![i]

						// check if the time is in future
						// ------------------------------
						if (!bFutureTime && o.TimeStamp() > lCurrentTime)
							bFutureTime = true

						// draw line
						// ---------
						y2 = h2 * o.ScaledValue() + fCursorSize
						val x0 = x1 + ((x2 - x1) / 2)

						if (bFutureTime)
							pathDash.cubicTo(x0, y1, x0, y2, x2, y2)
						else {
							pathDash.moveTo(x2, y2)
							pathLine.cubicTo(x0, y1, x0, y2, x2, y2)
						}

						// set Cursor Y coordinates
						// ------------------------
						if (i == iCursorIndex) {
							ya = y2
							yb = y2
						}
						else if (i == k)
							yb = y2

						x1 = x2
						y1 = y2
						x2 += fItemWidth
						i += iIndexInc
					}
					_canvas.drawPath(pathDash, pantDash)
					_canvas.drawPath(pathLine, pantLine)
				}
				catch (_e: IndexOutOfBoundsException) {
					_e.printStackTrace()
				}
				catch (_e: NullPointerException) {
					_e.printStackTrace()
				}
				if (bShowCursor && (bmapCursor != null)) {
					// draw Cursor
					// -----------
					if (iScaling == 0)
						fCursorY = (ya + ((yb - ya) * fItemScrollOffset) - fHalfCursorSize)

					_canvas.drawBitmap(bmapCursor!!, (fHalfPageWidth + fScrollX - fHalfCursorSize), fCursorY, null)
				}
				if ((iScaling == 0) && (hdlr_OnUpdate != null) && (bCallOnUpdate || (iIndexCurrent != iCursorIndex))) {
					// call parent view's OnUpdate callback
					// ------------------------------------
					if (bCallOnUpdate)
						bCallOnUpdate = false
					else
						iIndexCurrent = iCursorIndex

					try {
						hdlr_OnUpdate!!.OnUpdate(iIndexCurrent, a_Data!![iIndexCurrent])
					}
					catch (_e: IndexOutOfBoundsException) {
						_e.printStackTrace()
					}
					catch (_e: NullPointerException) {
						_e.printStackTrace()
					}
				}
			}
		}

		// private methods
		// ---------------
		private fun DestroyCursor() {
			if (bmapCursor != null) {
				bmapCursor!!.recycle()
				bmapCursor = null
			}
		}

		private fun UpdateContentWidth(_bResetScale: Boolean, _bRedraw: Boolean) {
			if (_bResetScale)
				fScaleWidth = fItemGap
			else {
				if (fScaleWidth < 0)
					fScaleWidth = 0f
				else {
					val f = fPixelScale * MJGraphSubView_ITEM_GAP_MAX * 2
					if (fScaleWidth > f)
						fScaleWidth = f
				}
			}
			fItemWidth = fLineWidth + fScaleWidth
			iContentWidth = (fItemWidth * (iIndexEnd - iIndexStart + 1) / iIndexInc).toInt()

			if ((view_Parent == null) && (parent != null))
				view_Parent = parent as HorizontalScrollView

			if (view_Parent != null) {
				minimumWidth = iContentWidth + view_Parent!!.measuredWidth
				iItemPerPage = (view_Parent!!.measuredWidth / fItemWidth).toInt()
				iItemOffscreen = iItemPerPage / 2
				fHalfPageWidth = view_Parent!!.measuredWidth.toFloat() / 2

				UpdateCurrentIndex(_bRedraw)
			}
		}

		private fun UpdateCurrentIndex(_bRedraw: Boolean) {
			if (view_Parent != null) {
				val m: Int = iContentWidth
				var n: Int
				if (iScaling > 0) {
					n = ((iIndexCurrent - iIndexStart) * fItemWidth / iIndexInc).toInt()
					if (n < 0) {
						n = 0
					}
					else if (n > m) {
						n = m
					}
					fScalingScrollX = n.toFloat()
				}
				else if (view_Parent!!.scrollX > m)
					fScalingScrollX = m.toFloat()

				if (iScaling == 2) {
					postDelayed({
						if (view_Parent != null) {
							val i = fScalingScrollX.toInt()
							view_Parent!!.scrollX = i
							fItemScrollOffset = (i % fItemWidth) / fItemWidth
							iScaling = 0
							invalidate()
						}
					}, 100)
				}
				else
					view_Parent!!.scrollX = fScalingScrollX.toInt()
			}
			if (_bRedraw) {
				invalidate()
			}
		}

		private fun UpdateMode(_bCallOnUpdate: Boolean) {
			if ((a_Data != null) && !a_Data!!.isEmpty()) {
				var o: MJGraphData?
				var i: Int
				val c = Calendar.getInstance()
				if (c != null) {
					val m: Int = (1440 / iInterval)

					// check Mode and populate Label
					// -----------------------------
					iIndexEnd = a_Data!!.size - 1

					when (iMode) {
						MJGraphView.MODE_DAILY -> {
							// Daily Mode (minutes)
							// --------------------
							iIndexInc = 1
							iIndexStart = iIndexEnd - (MJGraphView.DEFAULT_RANGE_DAILY * m) + 1
							if (iIndexStart < 0)
								iIndexStart = 0

							i = iIndexStart
							while (i <= iIndexEnd) {
								try {
									o = a_Data!![i]
									c.timeInMillis = o.TimeStamp()
									o.SetLabel(if (c.get(Calendar.MINUTE) == 0) String.format(Locale.getDefault(), "%02d:00", c.get(Calendar.HOUR_OF_DAY)) else null)
								}
								catch (_e: IllegalArgumentException) {
									_e.printStackTrace()
								}
								catch (_e: IndexOutOfBoundsException) {
									_e.printStackTrace()
								}
								catch (_e: NullPointerException) {
									_e.printStackTrace()
								}
								i++
							}
						}
						MJGraphView.MODE_WEEKLY -> {
							// Weekly Mode (hours)
							// -------------------
							while (iIndexEnd > 0) {
								try {
									o = a_Data!![iIndexEnd]
									c.timeInMillis = o.TimeStamp()
									if (c.get(Calendar.MINUTE) == 0)
										break
									else
										iIndexEnd--
								}
								catch (_e: IndexOutOfBoundsException) {
									_e.printStackTrace()
								}
							}
							iIndexInc = 60 / iInterval
							iIndexStart = iIndexEnd - (MJGraphView.DEFAULT_RANGE_WEEKLY * m)
							if (iIndexStart < 0)
								iIndexStart = 0

							if (a_Week != null) {
								i = iIndexStart
								while (i <= iIndexEnd) {
									try {
										o = a_Data!![i]
										c.timeInMillis = o.TimeStamp()
										o.SetLabel(if ((c.get(Calendar.HOUR_OF_DAY) == 0) && (c.get(Calendar.MINUTE) == 0)) a_Week!![c.get(Calendar.DAY_OF_WEEK) - 1] else null)
									}
									catch (_e: IllegalArgumentException) {
										_e.printStackTrace()
									}
									catch (_e: IndexOutOfBoundsException) {
										_e.printStackTrace()
									}
									catch (_e: NullPointerException) {
										_e.printStackTrace()
									}
									i += iIndexInc
								}
							}
						}
						MJGraphView.MODE_MONTHLY -> {
							// Monthly Mode (days)
							// -------------------
							while (iIndexEnd > 0) {
								try {
									o = a_Data!![iIndexEnd]
									c.timeInMillis = o.TimeStamp()
									if ((c.get(Calendar.HOUR_OF_DAY) == 0) && (c.get(Calendar.MINUTE) == 0))
										break
									else
										iIndexEnd--
								}
								catch (_e: IndexOutOfBoundsException) {
									_e.printStackTrace()
								}
							}
							iIndexInc = m
							iIndexStart = iIndexEnd - (MJGraphView.DEFAULT_RANGE_MONTHLY * m)
							if (iIndexStart < 0)
								iIndexStart = 0

							if (a_Month != null) {
								i = iIndexStart
								while (i <= iIndexEnd) {
									try {
										o = a_Data!![i]
										c.timeInMillis = o.TimeStamp()

										if ((s_Year == null) || (c.get(Calendar.MONTH) > 0))
											o.SetLabel(if (c.get(Calendar.DATE) == 1) a_Month!![c.get(Calendar.MONTH)] else null)
										else {
											o.SetLabel(if (c.get(Calendar.DATE) == 1)
														String.format(Locale.getDefault(), ("%s" + s_Year!!), a_Month!![c.get(Calendar.MONTH)], c.get(Calendar.YEAR))
														else null)
										}
									}
									catch (_e: IllegalArgumentException) {
										_e.printStackTrace()
									}
									catch (_e: IndexOutOfBoundsException) {
										_e.printStackTrace()
									}
									catch (_e: NullPointerException) {
										_e.printStackTrace()
									}
									i += iIndexInc
								}
							}
						}
					}

					// calculate high / low values
					// ---------------------------
					var l = 0
					var h = 100
					var j: Int
					i = iIndexStart
					while (i <= iIndexEnd) {
						try {
							o = a_Data!![i]
							j = o.Value()
							if (j < l)
								l = j
							else if (j > h)
								h = j
						}
						catch (_e: IndexOutOfBoundsException) {
							_e.printStackTrace()
						}
						catch (_e: NullPointerException) {
							_e.printStackTrace()
						}
						i += iIndexInc
					}

					// calculate scaled value
					// ----------------------
					var range = h - l
					i = iIndexStart
					while (i <= iIndexEnd) {
						try {
							o = a_Data!![i]
							val f = (range - o.Value()).toFloat() / range
							o.SetScaledValue(f)
						}
						catch (_e: IndexOutOfBoundsException) {
							_e.printStackTrace()
						}
						catch (_e: NullPointerException) {
							_e.printStackTrace()
						}
						i += iIndexInc
					}
				}

				if (_bCallOnUpdate && (hdlr_OnUpdate != null)) {
					// call parent view's OnUpdate callback
					// ------------------------------------
					try {
						i = CurrentIndex()
						o = a_Data!![i]
						hdlr_OnUpdate!!.OnUpdate(i, o)
					}
					catch (_e: IndexOutOfBoundsException) {
						_e.printStackTrace()
					}
					catch (_e: NullPointerException) {
						_e.printStackTrace()
					}
				}
				UpdateContentWidth(true, true)
			}
			invalidate()
		}

		// public methods
		// --------------
		fun AddData(_data: MJGraphData): Boolean {
			var bResult = false
			if (a_Data != null) {
				try {
					a_Data!!.add(_data)
					UpdateMode(false)
					bResult = true
				}
				catch (_e: ClassCastException) {
					_e.printStackTrace()
				}
				catch (_e: IllegalArgumentException) {
					_e.printStackTrace()
				}
				catch (_e: UnsupportedOperationException) {
					_e.printStackTrace()
				}
			}
			return bResult
		}

		fun CreateCursor(_iFillColor: Int, _iOutlineColor: Int, _iSize: Int) {
			val pa: Paint?
			val cv: Canvas?
			try {
				// make sure the size fits
				// -----------------------
				var i = _iSize
				if (_iSize < MJGraphSubView_CURSOR_SIZE_MIN)
					i = MJGraphSubView_CURSOR_SIZE_MIN
				else if (_iSize > MJGraphSubView_CURSOR_SIZE_MAX)
					i = MJGraphSubView_CURSOR_SIZE_MAX

				fCursorSize = fPixelScale * i
				fHalfCursorSize = fCursorSize / 2

				// create Cursor bitmap
				// --------------------
				var m = fCursorSize.toInt()
				val n = (fCursorSize / 10).toInt()

				DestroyCursor()

				bmapCursor = Bitmap.createBitmap(m, m, Bitmap.Config.ARGB_8888)
				if (bmapCursor != null) {
					pa = Paint(Paint.ANTI_ALIAS_FLAG)
					pa.style = Paint.Style.FILL
					pa.color = _iOutlineColor

					cv = Canvas(bmapCursor!!)
					cv.drawOval(0f, 0f, m.toFloat(), m.toFloat(), pa)
					pa.color = _iFillColor
					m -= n
					cv.drawOval(n.toFloat(), n.toFloat(), m.toFloat(), m.toFloat(), pa)
				}
			}
			catch (_e: IllegalArgumentException) {
				_e.printStackTrace()
			}
			catch (_e: OutOfMemoryError) {
				_e.printStackTrace()
			}
		}

		fun CurrentIndex(): Int {
			return (if ((iIndexCurrent >= iIndexStart) && (iIndexCurrent <= iIndexEnd)) iIndexCurrent else iIndexStart)
		}

		fun IsScaling(): Boolean {
			return (iScaling > 0)
		}

		fun Mode(): Int {
			return iMode
		}

		fun OnBeginScaling() {
			iScaling = 1

			if (view_Parent != null) {
				// save scrolling coordinate
				// -------------------------
				fScalingScrollX = view_Parent!!.scrollX.toFloat()
			}
		}

		fun OnEndScaling(_fScale: Float) {
			if (iScaling == 1)
				iScaling = 2

			if (view_Parent != null) {
				// restore scrolling coordinates
				// -----------------------------
				view_Parent!!.scrollX = fScalingScrollX.toInt()

				// set new Mode
				// ------------
				var m = Mode()
				if (_fScale < 0.75f)
					m += 2
				else if (_fScale < 1.0f)
					m++
				else if (_fScale > 1.5f)
					m -= 2
				else if (_fScale > 1.0f)
					m--

				if (m < MJGraphView.MODE_DAILY)
					m = MJGraphView.MODE_DAILY
				else if (m > MJGraphView.MODE_MONTHLY)
					m = MJGraphView.MODE_MONTHLY

				SetMode(m)
			}
		}

		fun OnScroll(_iX: Int) {
			if (iScaling == 0) {
				fItemScrollOffset = (_iX % fItemWidth) / fItemWidth
				invalidate()
			}
		}

		fun SetCurrentIndex(_index: Int) {
			if ((_index >= iIndexStart) && (_index <= iIndexEnd)) {
				iIndexCurrent = (_index / iIndexInc) * iIndexInc

				bCallOnUpdate = true
				iScaling = 2

				UpdateCurrentIndex(true)
			}
		}

		fun SetCursor(_bitmap: Bitmap?) {
			bmapCursor = _bitmap
		}

		fun SetData(_aData: MutableList<MJGraphData>?) {
			a_Data = _aData

			UpdateMode(true)
		}

		fun SetInterval(_iInterval: Int) {
			if (_iInterval < 1)
				iInterval = 1
			else if (_iInterval > 60)
				iInterval = 60
			else
				iInterval = _iInterval

			UpdateMode(true)
		}

		fun SetItemGap(_iItemGap: Int) {
			var i = _iItemGap
			if (_iItemGap < MJGraphSubView_ITEM_GAP_MIN)
				i = MJGraphSubView_ITEM_GAP_MIN
			else if (_iItemGap > MJGraphSubView_ITEM_GAP_MAX)
				i = MJGraphSubView_ITEM_GAP_MAX

			fItemGap = fPixelScale * i

			UpdateContentWidth(true, false)
		}

		fun SetLabelMonth(_aMonth: Array<String>?) {
			a_Month = _aMonth
		}

		fun SetLabelWeek(_aWeek: Array<String>?) {
			a_Week = _aWeek
		}

		fun SetLabelYear(_sYear: String?) {
			s_Year = _sYear
		}

		fun SetLineWidth(_iLineWidth: Int) {
			var i = _iLineWidth
			if (_iLineWidth < MJGraphSubView_LINE_WIDTH_MIN)
				i = MJGraphSubView_LINE_WIDTH_MIN
			else if (_iLineWidth > MJGraphSubView_LINE_WIDTH_MAX)
				i = MJGraphSubView_LINE_WIDTH_MAX

			fLineWidth = fPixelScale * i

			pantDash.strokeWidth = fLineWidth
			pantLine.strokeWidth = fLineWidth

			UpdateContentWidth(false, false)
		}

		fun SetMode(_iMode: Int) {
			if (iMode == _iMode)
				UpdateContentWidth(true, true)
			else {
				iMode = _iMode
				UpdateMode(true)
			}
		}

		fun SetOnUpdateCallback(_callback: MJGraphUpdateCallback?) {
			hdlr_OnUpdate = _callback
		}

		fun SetScale(_fScale: Float) {
			if (iScaling == 1) {
				fScaleWidth = fItemGap * _fScale * _fScale * _fScale
				UpdateContentWidth(false, true)
			}
		}

		fun ShowCursor(_bEnable: Boolean) {
			bShowCursor = _bEnable
		}
	}

	// --------------------------------
	// Interface: MJGraphUpdateCallback
	// --------------------------------
	interface MJGraphUpdateCallback {
		fun OnUpdate(_index: Int, _data: MJGraphData)
	}

	// constructors & destructors
	// --------------------------
	init {
		// initialize properties
		// ---------------------
		overScrollMode = View.OVER_SCROLL_NEVER
		fScale = 1.0f

		oGestureDetector = ScaleGestureDetector(context, hdlrOnScale)

		viewGraph = MJGraphSubView(context)
		if (viewGraph != null) {
			viewGraph!!.CreateCursor(0xffffff00.toInt(), 0x80ffff00.toInt(), 18)

			addView(viewGraph, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
		}
	}

	fun Destroy() {
		oGestureDetector = null

		if (viewGraph != null) {
			viewGraph!!.Destroy()
			viewGraph = null
		}
	}

	// public View overrides
	// ---------------------
	override fun dispatchTouchEvent(_event: MotionEvent): Boolean {
		var bResult = false
		super.dispatchTouchEvent(_event)

		if (oGestureDetector != null) {
			bResult = oGestureDetector!!.onTouchEvent(_event)

			if (bResult && (viewGraph != null) && viewGraph!!.IsScaling()) {
				// disable scrolling if we are scaling
				// -----------------------------------
				bResult = false
			}
		}
		return bResult
	}

	override fun onScrollChanged(_iLeft: Int, _iTop: Int, _iOldLeft: Int, _iOldTop: Int) {
		viewGraph?.OnScroll(_iLeft)
	}

	// public methods
	// --------------
	fun AddData(_data: MJGraphData, _bAutoScroll: Boolean) {
		if ((viewGraph != null) && viewGraph!!.AddData(_data)) {
			if (_bAutoScroll) {
				viewGraph!!.postDelayed({
					fullScroll(HorizontalScrollView.FOCUS_RIGHT)
				}, 100)
			}
		}
	}

	fun CreateCursor(_iFillColor: Int, _iOutlineColor: Int, _iSize: Int) {
		viewGraph?.CreateCursor(_iFillColor, _iOutlineColor, _iSize)
	}

	fun CurrentIndex(): Int {
		return (if (viewGraph == null) -1 else viewGraph!!.CurrentIndex())
	}

	fun Mode(): Int {
		return (if (viewGraph == null) -1 else viewGraph!!.Mode())
	}

	fun SetCurrentIndex(_index: Int) {
		viewGraph?.SetCurrentIndex(_index)
	}

	fun SetCursor(_bitmap: Bitmap?) {
		viewGraph?.SetCursor(_bitmap)
	}

	fun SetData(_aData : MutableList<MJGraphData>?) {
		viewGraph?.SetData(_aData)
	}

	fun SetInterval(_iInterval: Int) {
		viewGraph?.SetInterval(_iInterval)
	}

	fun SetItemGap(_iItemGap: Int) {
		viewGraph?.SetItemGap(_iItemGap)
	}

	fun SetLabelMonth(_aMonth: Array<String>?) {
		viewGraph?.SetLabelMonth(_aMonth)
	}

	fun SetLabelWeek(_aWeek: Array<String>?) {
		viewGraph?.SetLabelWeek(_aWeek)
	}

	fun SetLabelYear(_sYear: String?) {
		viewGraph?.SetLabelYear(_sYear)
	}

	fun SetLineWidth(_iLineWidth: Int) {
		viewGraph?.SetLineWidth(_iLineWidth)
	}

	fun SetMode(_iMode: Int) {
		viewGraph?.SetMode(_iMode)
	}

	fun SetOnUpdateCallback(_callback: MJGraphView.MJGraphUpdateCallback?) {
		viewGraph?.SetOnUpdateCallback(_callback)
	}

	fun ShowCursor(_bEnable: Boolean) {
		viewGraph?.ShowCursor(_bEnable)
	}
}

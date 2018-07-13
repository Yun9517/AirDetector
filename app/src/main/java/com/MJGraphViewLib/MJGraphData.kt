package com.mobile2box.MJGraphView

// ==================
// Class: MJGraphData
// ==================
class MJGraphData(private var lTimeStamp: Long, private var iValue: Int) {

	// private properties
	// ------------------


	// temporary properties
	// --------------------
	private var fValue: Float = 0.toFloat()
	private var sLabel: String? = null

	// constructors & destructors
	// --------------------------
	fun Destroy() {
		sLabel = null
	}

	// public methods
	// --------------
	fun Label(): String? {
		return sLabel
	}

	fun ScaledValue(): Float {
		return fValue
	}

	fun SetLabel(_sLabel: String?) {
		sLabel = _sLabel
	}

	fun SetScaledValue(_fValue: Float) {
		fValue = _fValue
	}

	fun TimeStamp(): Long {
		return lTimeStamp
	}

	fun Value(): Int {
		return iValue
	}
}

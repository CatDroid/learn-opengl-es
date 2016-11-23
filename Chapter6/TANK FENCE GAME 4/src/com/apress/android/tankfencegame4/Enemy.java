package com.apress.android.tankfencegame4;

public class Enemy {
	private float _sourcePositionX;
	private float _sourcePositionY;
	private float _sourcePositionZ;
	private float _destinationPositionX;
	private float _destinationPositionY;
	private float _destinationPositionZ;
	private float _slopeZ;

	public Enemy(float positionX, float positionY, float positionZ) {
		_sourcePositionX		= positionX;
		_sourcePositionY		= positionY;
		_sourcePositionZ		= positionZ;
		_destinationPositionX	= positionX;
		_destinationPositionY	= positionY;
		_destinationPositionZ	= positionZ;
		_slopeZ					= (float) _sourcePositionY / _sourcePositionX;
		_slopeZ					= filter(_slopeZ);
	}
	private float filter(float slope) {
		boolean sign;

		if(slope >= 0) {
			sign	= true;
		} else {
			sign	= false;
		}

		slope		= Math.abs(slope);
		if(slope <= 0.25f) {
			slope	= 0.25f;
		}
		if(slope >= 2.5f) {
			slope	= 2.5f;
		}

		if(sign) {
			return slope;
		} else {
			return 0 - slope;
		}
	}
	public float getSourcePositionX() {
		return _sourcePositionX;
	}
	public float getSourcePositionY() {
		return _sourcePositionY;
	}
	public float getSourcePositionZ() {
		return _sourcePositionZ;
	}
	public float getDestinationPositionX() {
		return _destinationPositionX;
	}
	public float getDestinationPositionY() {
		return _destinationPositionY;
	}
	public float getDestinationPositionZ() {
		return _destinationPositionZ;
	}

}
package com.apress.android.tankfencegame4;

public class Missile {
	private float _sourcePositionX;
	private float _sourcePositionY;
	private float _sourcePositionZ;
	private float _destinationPositionX;
	private float _destinationPositionY;
	private float _destinationPositionZ;
	private float _angleZ;
	private float _slopeZ;
	private float _interceptY;

	public Missile(float positionX, float positionY, float positionZ, float angleZ) {
		_sourcePositionX		= positionX;
		_sourcePositionY		= positionY;
		_sourcePositionZ		= positionZ;
		_destinationPositionX	= positionX;
		_destinationPositionY	= positionY;
		_destinationPositionZ	= positionZ;
		_angleZ					= angleZ;
		_slopeZ					= (float) Math.tan(Math.toRadians(_angleZ + 90));
		_slopeZ					= filter(_slopeZ);
		_interceptY				= positionY - (_slopeZ * positionX);
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
	public void interpolateXY() {
		if((_angleZ > 0 && _angleZ <= 180) || (_angleZ >= -360 && _angleZ <= -180)) {
			_destinationPositionX	= _destinationPositionX - 0.5f;
		}
		if((_angleZ > 180 && _angleZ <= 360) || (_angleZ > -180 && _angleZ <= 0)) {
			_destinationPositionX	= _destinationPositionX + 0.5f;
		}
		_destinationPositionY	= (_slopeZ * _destinationPositionX) + _interceptY;
	}

}
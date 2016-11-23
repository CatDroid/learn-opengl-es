package com.apress.android.tankfencegame6;

public class Enemy {
	private float _sourcePositionX;
	private float _sourcePositionY;
	private float _sourcePositionZ;
	private float _destinationPositionX;
	private float _destinationPositionY;
	private float _destinationPositionZ;
	private float _slopeZ;
	private final float _dx = 0.05f;
	private float _dy;

	public Enemy(float positionX, float positionY, float positionZ, float slopeZ) {
		_sourcePositionX		= positionX;
		_sourcePositionY		= positionY;
		_sourcePositionZ		= positionZ;
		_destinationPositionX	= positionX;
		_destinationPositionY	= positionY;
		_destinationPositionZ	= positionZ;
		_slopeZ					= slopeZ;
		_dy						= _dx * _slopeZ;
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
		if(_sourcePositionX >= 0) {
			_destinationPositionX	= _destinationPositionX - _dx;
			_destinationPositionY	= _destinationPositionY - _dy;
		}
		if(_sourcePositionX < 0) {
			_destinationPositionX	= _destinationPositionX + _dx;
			_destinationPositionY	= _destinationPositionY + _dy;
		}
	}

}
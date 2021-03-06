/*
 *    Copyright 2016 Jonathan Beaudoin
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.github.jonatino.misc;

import com.github.jonatino.process.DataSource;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

/**
 * Created by Jonathan on 1/10/2016.
 */
public final class MemoryBuffer extends Pointer {
	
	private int size;
	public DataSource _lastreadsrc;
	public long _lastreadaddress;

	public MemoryBuffer(int size) {
		super(Native.malloc(size));
		this.size = size;
	}

	public MemoryBuffer(byte[] arr) {
		this(arr.length);
		this.setBytes(arr);
	}
	
	public MemoryBuffer(Pointer ptr) {
		super(Pointer.nativeValue(ptr));
		this._lastreadaddress = Pointer.nativeValue(ptr); 
	}
	
	public MemoryBuffer(long addr, int size) {
		super(addr);
		this.size = size;
		this._lastreadaddress = addr;
	}

	public MemoryBuffer putBoolean(boolean value) {
		setByte(0, (byte) (value ? 1 : 0));
		return this;
	}

	public MemoryBuffer putByte(int value) {
		setByte(0, (byte) value);
		return this;
	}

	public MemoryBuffer putShort(int value) {
		setShort(0, (short) value);
		return this;
	}

	public MemoryBuffer putInt(int value) {
		setInt(0, value);
		return this;
	}

	public MemoryBuffer putLong(long value) {
		setLong(0, value);
		return this;
	}

	public MemoryBuffer putFloat(float value) {
		setFloat(0, value);
		return this;
	}

	public MemoryBuffer putDouble(double value) {
		setDouble(0, value);
		return this;
	}

	public MemoryBuffer setBytes(long offset, byte[] data) {
		for (int i = 0; i < data.length; ++i) {
			setByte(offset + i, data[i]);
		}
		return this;
	}
	
	public MemoryBuffer setBytes(MemoryBuffer data) {
		return setBytes(0, data, Math.min(this.size(), data.size()));
	}
	
	public MemoryBuffer setBytes(long offset, MemoryBuffer data) {
		return setBytes(0, data, Math.min(this.size(), offset + data.size()));
	}
	
	public MemoryBuffer setBytes(long offset, Pointer data, long length) {
		for (int i = 0; i < length; ++i) {
			this.setByte(offset + i, data.getByte(i));
		}
		return this;
	}

	public MemoryBuffer setBytes(byte[] data) {
		setBytes(0, data);
		return this;
	}

	public void get(byte[] dest) {
		read(0, dest, 0, dest.length);
	}

	public boolean getBoolean() {
		return getByte() == 1;
	}

	public boolean getBoolean(long offset) {
		return getByte(offset) == 1;
	}

	public int getByte() {
		return getByte(0);
	}

	public byte[] getByteArray() {
		return getByteArray(0, this.size);
	}

	public int getShort() {
		return getShort(0);
	}


	public int getInt() {
		return getInt(0);
	}

	public long getLong() {
		return getLong(0);
	}

	public float getFloat() {
		return getFloat(0);
	}

	public double getDouble() {
		return getDouble(0);
	}

	public int size() {
		return size;
	}

	public DataSource lastReadSource() {
		return _lastreadsrc;
	}

	public long lastReadAddress() {
		return _lastreadaddress;
	}

	public byte[] array() {
		byte[] data = Cacheable.array(size);
		get(data);
		return data;
	}

	public void free() {
		Native.free(Pointer.nativeValue(this));
	}

}

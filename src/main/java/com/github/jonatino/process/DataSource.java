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

package com.github.jonatino.process;

import com.github.jonatino.misc.Cacheable;
import com.github.jonatino.misc.MemoryBuffer;
import com.github.jonatino.misc.Strings;
import com.sun.jna.Pointer;

/**
 * Created by Jonathan on 3/24/2016.
 */
public interface DataSource {

	MemoryBuffer read(long address, int size);

	void read(long address, int size, long target);

	Process write(Pointer address, MemoryBuffer buffer);

	boolean canRead(Pointer address, int size);

	default boolean readBoolean(long address) {
		return read(address, 1).getBoolean();
	}

	default int readByte(long address) {
		return read(address, 1).getByte();
	}

	default int readShort(long address) {
		return read(address, 2).getShort();
	}

	default int readInt(long address) {
		return read(address, 4).getInt();
	}

	default long readUnsignedInt(long address) {
		return Integer.toUnsignedLong(read(address, 4).getInt());
	}

	default long readLong(long address) {
		return read(address, 8).getLong();
	}

	default float readFloat(long address) {
		return read(address, 4).getFloat();
	}

	default double readDouble(long address) {
		return read(address, 8).getDouble();
	}

	default String readString(long address, int length) {
		byte[] bytes = Cacheable.array(length);
		read(address, bytes.length).get(bytes);
		return Strings.transform(bytes);
	}

	default long readPointer(long address) {
		return read(address, 8).getLong();
	}

	default MemoryBuffer read(Pointer address, int size) {
		return read(Pointer.nativeValue(address), size);
	}

	default Process write(long address, MemoryBuffer buffer) {
		return write(Cacheable.pointer(address), buffer);
	}

	default MemoryBuffer read(long address, int size, MemoryBuffer target) {
		read(address, size, Pointer.nativeValue(target));
		target._lastreadaddress = address;
		target._lastreadsrc = this;
		return target;
	}

	default MemoryBuffer read(Pointer address, int size, MemoryBuffer target) {
		return read(Pointer.nativeValue(address), size, target);
	}

	default MemoryBuffer read(long address, MemoryBuffer target) {
		return read(address, target.size(), target);
	}

	default void read(long address, int size, Pointer target) {
		read(address, size, Pointer.nativeValue(target));
	}

	default Process writeBoolean(long address, boolean value) {
		return write(Cacheable.pointer(address), Cacheable.buffer(1).putBoolean(value));
	}

	default Process writeByte(long address, int value) {
		return write(Cacheable.pointer(address), Cacheable.buffer(1).putByte(value));
	}

	default Process writeShort(long address, int value) {
		return write(Cacheable.pointer(address), Cacheable.buffer(2).putShort(value));
	}

	default Process writeInt(long address, int value) {
		return write(Cacheable.pointer(address), Cacheable.buffer(4).putInt(value));
	}

	default Process writeLong(long address, long value) {
		return write(Cacheable.pointer(address), Cacheable.buffer(8).putLong(value));
	}

	default Process writeFloat(long address, float value) {
		return write(Cacheable.pointer(address), Cacheable.buffer(4).putFloat(value));
	}

	default Process writeDouble(long address, double value) {
		return write(Cacheable.pointer(address), Cacheable.buffer(8).putDouble(value));
	}

	default boolean canRead(long address, int size) {
		return canRead(Cacheable.pointer(address), size);
	}

}

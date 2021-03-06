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

package com.github.jonatino.process.impl.unix;

import com.github.jonatino.misc.Cacheable;
import com.github.jonatino.misc.MemoryBuffer;
import com.github.jonatino.natives.unix.unix;
import com.github.jonatino.process.AbstractProcess;
import com.github.jonatino.process.Module;
import com.github.jonatino.process.Process;
import com.sun.jna.Pointer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by Jonathan on 1/10/2016.
 */
public final class UnixProcess extends AbstractProcess {

	private ThreadLocal<unix.iovec> local = ThreadLocal.withInitial(() -> new unix.iovec());
	private ThreadLocal<unix.iovec> remote = ThreadLocal.withInitial(() -> new unix.iovec());

	public UnixProcess(int id) {
		super(id);
	}

	@Override
	public void initModules() {
		try {
			for (String line : Files.readAllLines(Paths.get("/proc/" + id() + "/maps"))) {
				String[] split = line.split(" ");
				String[] regionSplit = split[0].split("-");

				long start = -1, end = -1, offset = -1;
				try {
					start = Long.parseLong(regionSplit[0], 16);
					end = Long.parseLong(regionSplit[1], 16);
					offset = Long.parseLong(split[2], 16);
				} catch (NumberFormatException ex) {
					continue;
				}

				if (offset < 0 || start <= 0 || end <= 0) {
					continue;
				}

				String path = "";
				for (int i = 5; i < split.length; i++) {
					String s = split[i].trim();
					if (!s.isEmpty()) {
						path += split[i];
					}
					if (s.isEmpty() && ++i > split.length) {
						break;
					} else if (s.isEmpty() && !split[i].trim().isEmpty()) {
						path += split[i];
					}
				}

				String modulename = path.substring(path.lastIndexOf("/") + 1, path.length());
				// TODO: Fix for linux
				if (split[1].charAt(2) == 'x')
					modules.put(modulename, new Module(this, modulename, Pointer.createConstant(start), end - start, split[1]));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public MemoryBuffer read(long address, int size) {
		MemoryBuffer buffer = Cacheable.buffer(size);
		local.get().iov_base = buffer;
		remote.get().iov_base = Cacheable.pointer(address);
		remote.get().iov_len = local.get().iov_len = size;
		if (unix.process_vm_readv(id(), local.get(), 1, remote.get(), 1, 0) != size) {
			throw new RuntimeException("Read memory failed at address " + address + " size " + size);
		}

		return buffer;
	}

	@Override
	public void read(long address, int size, long target) {
		local.get().iov_base = Cacheable.pointer(target);
		remote.get().iov_base = Cacheable.pointer(address);
		remote.get().iov_len = local.get().iov_len = size;
		if (unix.process_vm_readv(id(), local.get(), 1, remote.get(), 1, 0) != size) {
			throw new RuntimeException("Read memory failed at address " + address + " size " + size);
		}
	}

	@Override
	public Process write(Pointer address, MemoryBuffer buffer) throws com.sun.jna.LastErrorException {
		local.get().iov_base = buffer;
		remote.get().iov_base = address;
		remote.get().iov_len = local.get().iov_len = buffer.size();
		if (unix.process_vm_writev(id(), local.get(), 1, remote.get(), 1, 0) != buffer.size()) {
			throw new RuntimeException("Write memory failed at address " + Pointer.nativeValue(address) + " size " + buffer.size());
		}
		return this;
	}

	@Override
	public boolean canRead(Pointer address, int size) {
		try {
			read(address, size);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
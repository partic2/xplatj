/*
 *******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package xplatj.gdxplat.pursuer.decoder.internal;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;

public class WavDecoder {

	static public class Music {

		private WavInputStream input;

		public boolean mono;
		public int sample;

		public Music(InputStream in) {
			input = new WavInputStream(in);
			mono = input.channels == 1;
			sample = input.sampleRate;
		}

		public int read(ByteBuffer buffer) {

			try {
				int r = input.read(buffer.array());
				buffer.position(buffer.position()+r<0?0:r);
				return r;
			} catch (IOException ex) {
				throw new GdxRuntimeException("Error reading WAV file");
			}
		}
	}

	static public class Sound {
		ByteBuffer data;
		public boolean mono;
		public int sample;

		public Sound(InputStream file) {
			WavInputStream input = null;
			try {
				input = new WavInputStream(file);
				mono = input.channels == 1;
				sample = input.sampleRate;
				data = ByteBuffer.wrap(StreamUtils.copyStreamToByteArray(input, input.dataRemaining));
			} catch (IOException ex) {
				throw new GdxRuntimeException("Error reading WAV file: " + file, ex);
			} finally {
				StreamUtils.closeQuietly(input);
			}
		}

	}

	/**
	 * @author Nathan Sweet
	 */
	static private class WavInputStream extends FilterInputStream {

		int channels, sampleRate, dataRemaining;

		WavInputStream(InputStream in) {
			super(in);
			try {
				if (read() != 'R' || read() != 'I' || read() != 'F' || read() != 'F') {
					throw new GdxRuntimeException("RIFF header not found: ");
				}

				skipFully(4);

				if (read() != 'W' || read() != 'A' || read() != 'V' || read() != 'E') {
					throw new GdxRuntimeException("Invalid wave file header: ");
				}

				int fmtChunkLength = seekToChunk('f', 'm', 't', ' ');

				int type = read() & 0xff | (read() & 0xff) << 8;
				if (type != 1) {
					throw new GdxRuntimeException("WAV files must be PCM: " + type);
				}

				channels = read() & 0xff | (read() & 0xff) << 8;
				if (channels != 1 && channels != 2) {
					throw new GdxRuntimeException("WAV files must have 1 or 2 channels: " + channels);
				}

				sampleRate = read() & 0xff | (read() & 0xff) << 8 | (read() & 0xff) << 16 | (read() & 0xff) << 24;

				skipFully(6);

				int bitsPerSample = read() & 0xff | (read() & 0xff) << 8;
				if (bitsPerSample != 16) {
					throw new GdxRuntimeException("WAV files must have 16 bits per sample: " + bitsPerSample);
				}

				skipFully(fmtChunkLength - 16);

				dataRemaining = seekToChunk('d', 'a', 't', 'a');
			} catch (Throwable ex) {
				StreamUtils.closeQuietly(this);
				throw new GdxRuntimeException("Error reading WAV file: ");
			}
		}

		private int seekToChunk(char c1, char c2, char c3, char c4) throws IOException {
			while (true) {
				boolean found = read() == c1;
				found &= read() == c2;
				found &= read() == c3;
				found &= read() == c4;
				int chunkLength = read() & 0xff | (read() & 0xff) << 8 | (read() & 0xff) << 16 | (read() & 0xff) << 24;
				if (chunkLength == -1) {
					throw new IOException("Chunk not found: " + c1 + c2 + c3 + c4);
				}
				if (found) {
					return chunkLength;
				}
				skipFully(chunkLength);
			}
		}

		private void skipFully(int count) throws IOException {
			while (count > 0) {
				long skipped = in.skip(count);
				if (skipped <= 0) {
					throw new EOFException("Unable to skip.");
				}
				count -= skipped;
			}
		}

		@Override
		public int read(byte[] buffer) throws IOException {
			if (dataRemaining == 0) {
				return -1;
			}
			int length = Math.min(super.read(buffer), dataRemaining);
			if (length == -1) {
				return -1;
			}
			dataRemaining -= length;
			return length;
		}

	}

}

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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.MP3Decoder;
import javazoom.jl.decoder.OutputBuffer;

import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * @author Nathan Sweet
 */
public class Mp3Decoder2 {

	static public class Music {
		// Note: This uses a slightly modified version of JLayer.

		private Bitstream bitstream;
		private OutputBuffer outputBuffer;
		private MP3Decoder decoder;

		public boolean mono;
		public int sample;

		public Music(InputStream in) {

			bitstream = new Bitstream(in);
			decoder = new MP3Decoder();
			try {
				Header header = bitstream.readFrame();
				if (header == null) {
					throw new GdxRuntimeException("empty ogg");
				}
				int channels = header.mode() == Header.SINGLE_CHANNEL ? 1 : 2;
				outputBuffer = new OutputBuffer(channels, false);
				decoder.setOutputBuffer(outputBuffer);
				if (channels == 1) {
					mono = true;
				} else {
					mono = false;
				}
				sample = header.getSampleRate();
			} catch (BitstreamException e) {
				throw new GdxRuntimeException("error while preloading mp3", e);
			}
		}

		private boolean dump = true;

		public int read(ByteBuffer buffer) {

			try {
				int totalLength = 0;
				int minRequiredLength = buffer.capacity() - OutputBuffer.BUFFERSIZE*2;

				while (totalLength <= minRequiredLength) {

					Header header = bitstream.readFrame();
					if (header == null) {
						break;
					}
					try {
						decoder.decodeFrame(header, bitstream);
					} catch (Exception ignored) {
					}
					bitstream.closeFrame();
					int length = outputBuffer.reset();
					buffer.position(totalLength);
					buffer.put(outputBuffer.getBuffer(), 0, length);
					totalLength += length;
				}
				return totalLength;
			} catch (Throwable ex) {
				throw new GdxRuntimeException("Error reading audio data.", ex);
			}
		}
	}

	static public class Sound {
		// Note: This uses a slightly modified version of JLayer.
		public ByteBuffer data;
		public int sample;
		public boolean mono;

		public Sound(InputStream in) {
			ByteArrayOutputStream output = new ByteArrayOutputStream(4096);
			Bitstream bitstream = new Bitstream(in);
			MP3Decoder decoder = new MP3Decoder();
			try {
				OutputBuffer outputBuffer = null;
				int sampleRate = -1, channels = -1;
				while (true) {
					Header header = bitstream.readFrame();
					if (header == null) {
						break;
					}
					if (outputBuffer == null) {
						channels = header.mode() == Header.SINGLE_CHANNEL ? 1 : 2;
						outputBuffer = new OutputBuffer(channels, false);
						decoder.setOutputBuffer(outputBuffer);
						sampleRate = header.getSampleRate();
					}
					try {
						decoder.decodeFrame(header, bitstream);
					} catch (Exception ignored) {
						// JLayer's decoder throws
						// ArrayIndexOutOfBoundsException sometimes!?
					}
					bitstream.closeFrame();
					output.write(outputBuffer.getBuffer(), 0, outputBuffer.reset());
				}
				bitstream.close();
				byte[] arr = output.toByteArray();
				data = ByteBuffer.wrap(arr);
				if (channels == 1) {
					mono = true;
				} else {
					mono = false;
				}
				sample = sampleRate;
			} catch (Throwable ex) {
				throw new GdxRuntimeException("Error reading audio data.", ex);
			}
		}

	}

}

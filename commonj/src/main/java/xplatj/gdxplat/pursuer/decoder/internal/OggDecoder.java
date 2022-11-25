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

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.StreamUtils;

/**
 * @author Nathan Sweet
 */
public class OggDecoder {

	static public class Music {

		private OggInputStream input;
		public boolean mono;
		public int sample;

		public Music(InputStream in) {
			input = new OggInputStream(in);
			if (input.getChannels() == 1) {
				mono = true;
			} else {
				mono = false;
			}
			sample = input.getSampleRate();
		}

		public int read(ByteBuffer buffer) {
			int r = input.read(buffer.array());
			buffer.position(buffer.position()+r<0?0:r);
			return r;
		}

	}

	static public class Sound {
		public ByteBuffer data;
		public boolean mono;
		public int sample;

		public Sound(InputStream in) {

			OggInputStream input = null;
			try {
				input = new OggInputStream(in);
				ByteArrayOutputStream output = new ByteArrayOutputStream(4096);
				byte[] buffer = new byte[2048];
				while (!input.atEnd()) {
					int length = input.read(buffer);
					if (length == -1) {
						break;
					}
					output.write(buffer, 0, length);
				}
				byte[] arr = output.toByteArray();
				data = ByteBuffer.wrap(arr);
				if (input.getChannels() == 1) {
					mono = true;
				} else {
					mono = false;
				}
				sample = input.getSampleRate();
			} finally {
				StreamUtils.closeQuietly(input);
			}
		}

	}

}

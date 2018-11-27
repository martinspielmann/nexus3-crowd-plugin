/*
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.nexus.plugins.crowd.client.rest;

public class RestException extends Exception {
	private static final long serialVersionUID = 8299574203640049391L;

	public RestException() {
		super();
	}

	public RestException(String message) {
		super(message);
	}

	public RestException(Throwable cause) {
		super(cause);
	}

	public RestException(String message, Throwable cause) {
		super(message, cause);
	}

	public RestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}

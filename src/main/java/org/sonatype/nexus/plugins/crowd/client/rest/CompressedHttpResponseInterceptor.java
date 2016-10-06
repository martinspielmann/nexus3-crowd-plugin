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

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.entity.DeflateDecompressingEntity;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.protocol.HttpContext;

/**
 * @author Issa Gorissen
 * 
 * Will take care of decompression of Http response (gzip/deflate)
 */
public class CompressedHttpResponseInterceptor implements HttpResponseInterceptor {

    @Override
    public void process(HttpResponse response, HttpContext hc) throws HttpException, IOException {
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            Header header = entity.getContentEncoding();
            
            if (header != null) {
                for (HeaderElement headerElement : header.getElements()) {
                    if (headerElement.getName().equalsIgnoreCase("gzip")) {
                        response.setEntity(new GzipDecompressingEntity(entity));
                        return;
                    }
                    
                    if (headerElement.getName().equalsIgnoreCase("deflate")) {
                        response.setEntity(new DeflateDecompressingEntity(entity));
                        return;
                    }
                }
            }
        }
    }

}

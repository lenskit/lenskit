/*
 * Build system for LensKit, and open-source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the
 *   distribution.
 *
 * - Neither the name of the University of Minnesota nor the names of
 *   its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.grouplens.lenskit.build

import com.google.common.io.BaseEncoding
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.apache.http.entity.mime.HttpMultipartMode
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.entity.mime.content.StringBody
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Task for uploading LensKit web site archives.
 */
class SiteUpload extends DefaultTask {
    @InputFile
    def File siteArchive
    def String uploadUrl
    def String authSecret
    def String branch
    private String digest

    def getHmacDigest() {
        if (digest != null) {
            return digest
        }

        if (authSecret == null) {
            logger.warn 'No upload secret'
            return null
        } else {
            def mac = Mac.getInstance('HmacSHA1')
            mac.init(new SecretKeySpec(authSecret.getBytes('UTF-8'), 'HmacSHA1'))
            siteArchive.eachByte(1024) { bytes, n ->
                mac.update(bytes, 0, n)
            }
            digest = BaseEncoding.base16().encode(mac.doFinal()).toLowerCase()
            logger.info 'HMAC of web archive: {}', hmacDigest
            return digest
        }
    }

    @TaskAction
    void upload() {
        def http = new HTTPBuilder(uploadUrl)
        http.request(Method.POST, 'multipart/form-data') { req ->
            def content = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE)
            content.addPart('branch', new StringBody(branch))
            content.addPart('hmac', new StringBody(hmacDigest))
            content.addPart('archive', new FileBody(siteArchive))
            req.setEntity(content)
            response.success = { res ->
                logger.info 'Succeeded with code {}', res.statusLine.statusCode
            }
        }
    }
}

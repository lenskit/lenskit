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

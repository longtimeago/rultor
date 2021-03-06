/**
 * Copyright (c) 2009-2013, rultor.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the rultor.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.rultor.shell.bash;

import com.google.common.io.Files;
import com.rultor.shell.Sequel;
import com.rultor.shell.ShellMocker;
import java.io.File;
import org.apache.commons.io.FileUtils;
import org.junit.Assume;
import org.junit.Test;

/**
 * Integration case for {@link S3CmdPut}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class S3CmdPutITCase {

    /**
     * S3 key.
     */
    private static final String KEY =
        System.getProperty("failsafe.s3.key");

    /**
     * S3 secret.
     */
    private static final String SECRET =
        System.getProperty("failsafe.s3.secret");

    /**
     * S3 bucket.
     */
    private static final String BUCKET =
        System.getProperty("failsafe.s3.bucket");

    /**
     * S3CmdPut can upload to S3.
     * @throws Exception If some problem inside
     */
    @Test
    public void uploadsToAmazon() throws Exception {
        Assume.assumeNotNull(S3CmdPutITCase.KEY);
        final Sequel sequel = new S3CmdPut(
            "test-relic",
            "./test/*.html",
            S3CmdPutITCase.BUCKET,
            "S3CmdPutITCase/",
            S3CmdPutITCase.KEY,
            S3CmdPutITCase.SECRET
        );
        final File dir = Files.createTempDir();
        FileUtils.write(new File(dir, "test/index.html"), "<html/>");
        FileUtils.write(new File(dir, "test/data.html"), "content-B");
        sequel.exec(new ShellMocker.Bash(dir));
    }

}

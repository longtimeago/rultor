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

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.rultor.shell.Sequel;
import com.rultor.shell.Shell;
import com.rultor.shell.Terminal;
import com.rultor.snapshot.TagLine;
import java.io.IOException;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.Validate;

/**
 * Put file(s) using s3cmd command line tool.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @see <a href="http://s3tools.org/s3cmd">s3cmd</a>
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "name", "path", "bucket", "prefix", "key", "secret" })
@Loggable(Loggable.DEBUG)
public final class S3CmdPut implements Sequel {

    /**
     * Tag name.
     */
    private final transient String name;

    /**
     * Path to files, like "./alpha/files/*.txt".
     */
    private final transient String path;

    /**
     * S3 bucket.
     */
    private final transient String bucket;

    /**
     * S3 prefix in the bucket.
     */
    private final transient String prefix;

    /**
     * S3 key.
     */
    private final transient String key;

    /**
     * S3 secret.
     */
    private final transient String secret;

    /**
     * Content ctype.
     */
    private final transient String ctype;

    /**
     * Encoding.
     */
    private final transient String encoding;

    /**
     * Public ctor.
     * @param label Name of the product to discover
     * @param pth Path to use
     * @param bkt Bucket name
     * @param pfx Prefix in S3 bucket
     * @param akey S3 authorization key
     * @param scrt S3 authorization secret
     * @checkstyle ParameterNumber (8 lines)
     */
    public S3CmdPut(final String label, final String pth, final String bkt,
        final String pfx, final String akey, final String scrt) {
        this(label, pth, bkt, pfx, akey, scrt, "text/plain", "UTF-8");
    }

    /**
     * Public ctor.
     * @param label Name of the product to discover
     * @param pth Path to use
     * @param bkt Bucket name
     * @param pfx Prefix in S3 bucket
     * @param akey S3 authorization key
     * @param scrt S3 authorization secret
     * @param type Content ctype
     * @param enc Encoding
     * @checkstyle ParameterNumber (8 lines)
     */
    public S3CmdPut(
        @NotNull(message = "name can't be NULL") final String label,
        @NotNull(message = "path can't be NULL") final String pth,
        @NotNull(message = "bucket can't be NULL") final String bkt,
        @NotNull(message = "prefix can't be NULL") final String pfx,
        @NotNull(message = "key can't be NULL") final String akey,
        @NotNull(message = "secret can't be NULL") final String scrt,
        @NotNull(message = "content type can't be NULL") final String type,
        @NotNull(message = "encoding can't be NULL") final String enc) {
        Validate.notBlank(label, "S3 label can't be empty");
        this.name = label;
        Validate.notBlank(bkt, "S3 bucket can't be empty");
        this.bucket = bkt;
        Validate.notBlank(pth, "search path can't be empty");
        this.path = pth;
        this.prefix = pfx;
        Validate.matchesPattern(
            akey, "[0-9A-Z]{20}",
            "wrong format of AWS key '%s'", akey
        );
        this.key = akey;
        Validate.matchesPattern(
            scrt, "[0-9A-Za-z=/\\+]{40}",
            "wrong format of AWS secret '%s'", scrt
        );
        this.secret = scrt;
        Validate.notBlank(type, "content type can't be empty");
        this.ctype = type;
        Validate.notBlank(enc, "encoding can't be empty");
        this.encoding = enc;
    }

    @Override
    public void exec(final Shell shell) throws IOException {
        final String dir = FilenameUtils.getFullPathNoEndSeparator(this.path);
        Validate.notBlank(dir, "directory part of path is empty");
        final String mask = FilenameUtils.getName(this.path);
        Validate.notBlank(mask, "file part of path is empty");
        final String url = String.format(
            "http://%s.s3.amazonaws.com/%s", this.bucket, this.prefix
        );
        final int files = new Terminal(shell).exec(
            new StringBuilder()
                .append("CONFIG=$(mktemp /tmp/s3cmdput-XXXX)")
                .append(" && cat > $CONFIG")
                .append(" && HEAD=")
                .append(
                    Terminal.quotate(
                        Terminal.escape(
                            String.format(
                                "s3://%s/%s",
                                this.bucket,
                                this.prefix
                            )
                        )
                    )
                )
                .append(" && cd ")
                .append(Terminal.quotate(Terminal.escape(dir)))
                .append(" && if ls -U ")
                .append(mask)
                .append(" >/dev/null 2>&1; then")
                .append(" FILES=$(find ")
                .append(mask)
                .append(" -type f)")
                .append(" && for f in $FILES; do")
                .append(" s3cmd --config=$CONFIG put $f \"$HEAD$f\" >/dev/null")
                .append(" && echo $f; done;")
                .append(" else echo No files found by mask ")
                .append(Terminal.quotate(Terminal.escape(mask)))
                .append(" 1>&2; fi;")
                .append("rm -f $CONFIG")
                .toString(),
            new StringBuilder()
                .append("[default]\n")
                .append("access_key=").append(this.key).append('\n')
                .append("secret_key=").append(this.secret).append('\n')
                .append("encoding=").append(this.encoding).append('\n')
                .append("mime-type=").append(this.ctype).append('\n')
                .toString()
        ).split("\n").length;
        if (mask.contains("*")) {
            final String href = String.format("%sindex.html", url);
            this.tag(String.format("see [%d files](%s)", files, href), href);
        } else {
            final String href = String.format("%s%s", url, mask);
            this.tag(String.format("see [%s](%s) file", mask, href), href);
        }
    }

    /**
     * Log a tag.
     * @param desc Markdown description
     * @param href URL of content
     * @throws IOException If fails
     */
    private void tag(final String desc, final String href) throws IOException {
        new TagLine(this.name)
            .markdown(desc)
            .fine(true)
            .attr("href", href)
            .log();
    }

}

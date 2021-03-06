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
package com.rultor.snapshot;

import com.rultor.tools.Exceptions;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.xml.transform.TransformerException;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.xembly.SyntaxException;

/**
 * Radar that listens to log events and organizes them in snapshots.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@ToString
@EqualsAndHashCode
public final class Radar {

    /**
     * Lines per thread group.
     */
    private static final ConcurrentMap<ThreadGroup, StringBuffer> LINES =
        new ConcurrentHashMap<ThreadGroup, StringBuffer>(0);

    /**
     * Current thread group we're in.
     */
    private final transient ThreadGroup group =
        Thread.currentThread().getThreadGroup();

    /**
     * Add new line.
     * @param line The line to append
     */
    public void append(final String line) {
        if (XemblyLine.existsIn(line)) {
            StringBuffer buffer = Radar.LINES.get(this.group);
            if (buffer == null) {
                buffer = new StringBuffer(0);
                Radar.LINES.put(this.group, buffer);
            }
            try {
                buffer.append(XemblyLine.parse(line).xembly());
            } catch (final SyntaxException ex) {
                assert ex != null;
            }
        }
    }

    /**
     * Get snapshot of the current thread.
     * @return The snapshot
     * @throws SyntaxException If fails
     */
    public Snapshot snapshot() throws SyntaxException {
        final StringBuffer buffer = Radar.LINES.get(this.group);
        final String input;
        if (buffer == null) {
            input = "XPATH '/snapshot';";
        } else {
            input = buffer.toString();
        }
        return new Snapshot(input);
    }

    /**
     * Remove all lines for the current thread group.
     */
    public void clean() {
        Radar.LINES.remove(this.group);
    }

    /**
     * Render current snapshot using this XSL.
     * @param stream Input stream with XSL
     * @return Rendered text
     */
    public String render(final InputStream stream) {
        String summary;
        try {
            summary = new XSLT(this.snapshot(), stream).xml();
        } catch (final TransformerException ex) {
            summary = Exceptions.stacktrace(ex);
        } catch (final XemblyException ex) {
            summary = Exceptions.stacktrace(ex);
        } catch (final SyntaxException ex) {
            summary = Exceptions.stacktrace(ex);
        }
        return summary;
    }

}

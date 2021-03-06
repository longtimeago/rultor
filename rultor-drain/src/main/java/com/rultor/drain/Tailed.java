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
package com.rultor.drain;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.rultor.spi.Drain;
import com.rultor.spi.Pageable;
import com.rultor.tools.Time;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;

/**
 * Drain with a trail.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "main", "tail" })
@Loggable(Loggable.DEBUG)
public final class Tailed implements Drain {

    /**
     * Main drain.
     */
    private final transient Drain main;

    /**
     * Tail drain.
     */
    private final transient Drain tail;

    /**
     * Public ctor.
     * @param body Main drain
     * @param extra Tail drain
     */
    public Tailed(
        @NotNull(message = "body drain can't be NULL") final Drain body,
        @NotNull(message = "tail can't be NULL") final Drain extra) {
        this.main = body;
        this.tail = extra;
    }

    @Override
    public Pageable<Time, Time> pulses() throws IOException {
        return this.main.pulses();
    }

    @Override
    public void append(final Iterable<String> lines)
        throws IOException {
        this.main.append(lines);
        this.tail.append(lines);
    }

    @Override
    public InputStream read() throws IOException {
        return new SequenceInputStream(
            IOUtils.toInputStream(
                String.format(
                    "Tailed: main='%s', tail='%s'\n",
                    this.main.getClass().getCanonicalName(),
                    this.tail.getClass().getCanonicalName()
                ),
                CharEncoding.UTF_8
            ),
            this.main.read()
        );
    }

}

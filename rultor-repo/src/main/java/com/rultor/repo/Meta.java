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
package com.rultor.repo;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.rultor.spi.Arguments;
import com.rultor.spi.SpecException;
import com.rultor.spi.Users;
import com.rultor.spi.Variable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.Validate;

/**
 * Meta.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "mnemo")
@Loggable(Loggable.DEBUG)
final class Meta implements Variable<Object> {

    /**
     * Pattern to use for parsing.
     */
    private static final Pattern PTN = Pattern.compile(
        "\\$\\{([a-z]+)\\}"
    );

    /**
     * Text.
     */
    private final transient String mnemo;

    /**
     * Public ctor.
     * @param text Text to parse
     */
    Meta(final String text) {
        final Matcher matcher = Meta.PTN.matcher(text);
        Validate.isTrue(matcher.matches(), "invalid meta '%s'", text);
        this.mnemo = matcher.group(1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public Object instantiate(
        @NotNull(message = "users can't be NULL") final Users users,
        @NotNull(message = "arguments can't be NULL") final Arguments args)
        throws SpecException {
        final Object obj;
        if ("work".equals(this.mnemo)) {
            obj = args.work();
        } else if ("wallet".equals(this.mnemo)) {
            obj = args.wallet();
        } else {
            throw new IllegalStateException(
                String.format("unsupported mnemo '%s'", this.mnemo)
            );
        }
        return obj;
    }

    @Override
    public String asText() {
        return String.format("${%s}", this.mnemo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Integer, String> arguments() throws SpecException {
        return new ConcurrentHashMap<Integer, String>(0);
    }

}

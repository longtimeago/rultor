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
package com.rultor.conveyer.audit;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.urn.URN;
import com.rultor.spi.Coordinates;
import com.rultor.spi.Rule;
import com.rultor.spi.Spec;
import com.rultor.spi.Wallet;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Rule with audit features.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "origin", "error" })
@Loggable(Loggable.DEBUG)
final class AuditRule implements Rule {

    /**
     * Original rule.
     */
    private final transient Rule origin;

    /**
     * Error message if there is a problem, or empty string otherwise.
     */
    private final transient String error;

    /**
     * Public ctor.
     * @param rule Rule
     * @param problem Problem if it exists (empty string otherwise)
     */
    protected AuditRule(final Rule rule, final String problem) {
        this.origin = rule;
        this.error = problem;
    }

    @Override
    public String name() {
        return this.origin.name();
    }

    @Override
    public void update(final Spec spec, final Spec drain) {
        this.origin.update(spec, drain);
    }

    @Override
    public Spec spec() {
        return this.origin.spec();
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (5 lines)
     */
    @Override
    public Wallet wallet(final Coordinates work, final URN taker,
        final String rule) throws Wallet.NotEnoughFundsException {
        if (!this.error.isEmpty()) {
            throw new Wallet.NotEnoughFundsException(this.error);
        }
        return this.origin.wallet(work, taker, rule);
    }

    @Override
    public Spec drain() {
        return this.origin.drain();
    }

    @Override
    public void failure(final String desc) {
        this.origin.failure(desc);
    }

    @Override
    public String failure() {
        return this.origin.failure();
    }

}

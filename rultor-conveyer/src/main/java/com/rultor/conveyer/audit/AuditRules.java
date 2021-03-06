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
import com.rultor.spi.Rule;
import com.rultor.spi.Rules;
import java.util.Iterator;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Rules with audit features.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "origin", "error" })
@Loggable(Loggable.DEBUG)
final class AuditRules implements Rules {

    /**
     * Original rules.
     */
    private final transient Rules origin;

    /**
     * Error, if any (empty string otherwise).
     */
    private final transient String error;

    /**
     * Public ctor.
     * @param rules Rules
     * @param problem Problem
     */
    protected AuditRules(final Rules rules, final String problem) {
        this.origin = rules;
        this.error = problem;
    }

    @Override
    public boolean contains(final String name) {
        return this.origin.contains(name);
    }

    @Override
    public Rule get(final String name) {
        return new AuditRule(this.origin.get(name), this.error);
    }

    @Override
    public void remove(final String name) {
        this.origin.remove(name);
    }

    @Override
    public void create(final String name) {
        this.origin.create(name);
    }

    @Override
    public Iterator<Rule> iterator() {
        final Iterator<Rule> iterator = this.origin.iterator();
        return new Iterator<Rule>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }
            @Override
            public Rule next() {
                return new AuditRule(iterator.next(), AuditRules.this.error);
            }
            @Override
            public void remove() {
                iterator.remove();
            }
        };
    }

}

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
package com.rultor.stateful.sdb;

import com.amazonaws.services.simpledb.model.SelectRequest;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.RetryOnFailure;
import com.jcabi.simpledb.Domain;
import com.jcabi.simpledb.Item;
import com.jcabi.simpledb.Region;
import com.rultor.spi.Coordinates;
import com.rultor.stateful.Notepad;
import com.rultor.tools.Time;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * SimpleDB {@link Notepad}.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "domain", "work" })
@Loggable(Loggable.DEBUG)
@SuppressWarnings("PMD.TooManyMethods")
public final class DomainNotepad implements Notepad {

    /**
     * Attribute name for the owner.
     */
    private static final String ATTR_OWNER = "owner";

    /**
     * Attribute name for the rule.
     */
    private static final String ATTR_RULE = "rule";

    /**
     * Attribute name for the text.
     */
    private static final String ATTR_TEXT = "text";

    /**
     * Attribute name for the text.
     */
    private static final String ATTR_TIME = "time";

    /**
     * SimpleDB domain.
     */
    private final transient Domain domain;

    /**
     * Coordinates we're in.
     */
    private final transient Coordinates work;

    /**
     * Public ctor.
     * @param wrk Coordinates
     * @param dmn Domain
     */
    public DomainNotepad(
        @NotNull(message = "work can't be NULL") final Coordinates wrk,
        @NotNull(message = "domain can't be NULL") final Domain dmn) {
        this.work = wrk;
        this.domain = dmn;
    }

    /**
     * Public ctor.
     * @param wrk Coordinates
     * @param region Region
     * @param name Name of domain
     */
    public DomainNotepad(final Coordinates wrk,
        @NotNull(message = "region can't be NULL") final Region region,
        @NotNull(message = "domain can't be NULL") final String name) {
        this(wrk, region.domain(name));
    }

    @Override
    public int size() {
        return Iterators.size(this.iterator());
    }

    @Override
    public boolean isEmpty() {
        return Iterators.size(this.iterator()) == 0;
    }

    @Override
    public boolean contains(final Object object) {
        return !this.domain.item(this.name(object.toString())).isEmpty();
    }

    @Override
    @RetryOnFailure(verbose = false)
    public Iterator<String> iterator() {
        final Iterable<Item> items = this.domain.select(
            new SelectRequest().withSelectExpression(
                String.format(
                    "SELECT `%s` FROM `%s` WHERE `%s`='%s' AND `%s`='%s'",
                    DomainNotepad.ATTR_TEXT,
                    this.domain.name(),
                    DomainNotepad.ATTR_OWNER,
                    this.work.owner(),
                    DomainNotepad.ATTR_RULE,
                    this.work.rule()
                )
            ).withConsistentRead(true)
        );
        final Collection<String> texts = new LinkedList<String>();
        for (final Item item : items) {
            final String text = this.name(item.get(DomainNotepad.ATTR_TEXT));
            if (item.name().equals(text)) {
                texts.add(item.get(DomainNotepad.ATTR_TEXT));
            }
        }
        return texts.iterator();
    }

    @Override
    public Object[] toArray() {
        return Iterators.toArray(this.iterator(), Object.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(final T[] array) {
        return (T[]) Iterators.toArray(this.iterator(), String.class);
    }

    @Override
    @RetryOnFailure(verbose = false)
    public boolean add(final String line) {
        this.domain.item(this.name(line)).putAll(
            new ImmutableMap.Builder<String, String>()
                .put(DomainNotepad.ATTR_TEXT, line)
                .put(DomainNotepad.ATTR_OWNER, this.work.owner().toString())
                .put(DomainNotepad.ATTR_RULE, this.work.rule())
                .put(DomainNotepad.ATTR_TIME, new Time().toString())
                .build()
        );
        return true;
    }

    @Override
    @RetryOnFailure(verbose = false)
    public boolean remove(final Object line) {
        this.domain.item(this.name(line.toString())).clear();
        return true;
    }

    @Override
    public boolean containsAll(final Collection<?> list) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(final Collection<? extends String> list) {
        for (final String line : list) {
            this.add(line);
        }
        return true;
    }

    @Override
    public boolean removeAll(final Collection<?> list) {
        for (final Object line : list) {
            this.remove(line);
        }
        return true;
    }

    @Override
    public boolean retainAll(final Collection<?> list) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        for (final String line : this) {
            this.remove(line);
        }
    }

    /**
     * Calculate name of an item.
     * @param text Text of the item
     * @return The name (possibly unique)
     */
    private String name(final String text) {
        return DigestUtils.md5Hex(
            String.format(
                "%s %s %s",
                this.work.owner(),
                this.work.rule(),
                text
            )
        );
    }

}

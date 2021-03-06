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
package com.rultor.spi;

import com.jcabi.aspects.Immutable;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Column in sheet.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
public interface Column {

    /**
     * Title of it.
     * @return Title
     */
    @NotNull(message = "title is never NULL")
    String title();

    /**
     * Can group by it?
     * @return TRUE if we can order by it
     */
    boolean isGroup();

    /**
     * Contains a summary?
     * @return TRUE if it contains a summary
     */
    boolean isSum();

    /**
     * Simple column.
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = { "name", "grp" })
    final class Simple implements Column {
        /**
         * Title of it.
         */
        private final transient String name;
        /**
         * Can be used in grouping.
         */
        private final transient boolean grp;
        /**
         * Contains a summary?
         */
        private final transient boolean sum;
        /**
         * Public ctor.
         * @param title Title of it
         * @param group Is it a group?
         * @param summ Contains summary
         */
        public Simple(final String title, final boolean group,
            final boolean summ) {
            this.name = title;
            this.grp = group;
            this.sum = summ;
        }
        @Override
        public String title() {
            return this.name;
        }
        @Override
        public boolean isGroup() {
            return this.grp;
        }
        @Override
        public boolean isSum() {
            return this.sum;
        }
    }

}

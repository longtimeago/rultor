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
package com.rultor.conveyer;

import com.jcabi.aspects.Loggable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Threads for conveyer.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@ToString
@EqualsAndHashCode
@Loggable(Loggable.DEBUG)
@SuppressWarnings("PMD.DoNotUseThreads")
final class ConveyerThreads implements ThreadFactory {

    /**
     * Labels per thread group.
     */
    private final transient ConcurrentMap<ThreadGroup, String> labels =
        new ConcurrentHashMap<ThreadGroup, String>(0);

    /**
     * Counter of all groups created.
     */
    private final transient AtomicLong group = new AtomicLong();

    @Override
    public Thread newThread(final Runnable runnable) {
        return new Thread(
            new ThreadGroup(Long.toString(this.group.incrementAndGet())) {
                @Override
                public String toString() {
                    String label = ConveyerThreads.this.labels.get(this);
                    if (label == null) {
                        label = super.toString();
                    }
                    return label;
                }
            },
            runnable,
            String.format("conveyer-%d", this.group.get())
        );
    }

    /**
     * Label current thread group.
     * @param txt Label to assign to the running thread group
     */
    public void label(final String txt) {
        this.labels.put(Thread.currentThread().getThreadGroup(), txt);
    }

}

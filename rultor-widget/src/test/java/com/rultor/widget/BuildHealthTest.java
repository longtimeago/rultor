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
package com.rultor.widget;

import com.jcabi.immutable.ArrayMap;
import com.jcabi.urn.URN;
import com.rexsl.test.XhtmlMatchers;
import com.rultor.spi.Coordinates;
import com.rultor.spi.Pulse;
import com.rultor.spi.Pulses;
import com.rultor.spi.Stand;
import com.rultor.spi.Tag;
import com.rultor.spi.Tags;
import com.rultor.spi.Widget;
import java.util.Arrays;
import java.util.logging.Level;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.mockito.Mockito;
import org.xembly.Directives;
import org.xembly.Xembler;

/**
 * Tests for {@link BuildHealth}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle MultipleStringLiterals (500 lines)
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class BuildHealthTest {

    /**
     * BuildHealth can find recent builds and their health.
     * @throws Exception If fails
     */
    @Test
    @SuppressWarnings("unchecked")
    public void findsBuildsAndTheirHealth() throws Exception {
        final Widget widget = new BuildHealth();
        final Pulse first = Mockito.mock(Pulse.class);
        final Coordinates coords = new Coordinates.Simple(
            new URN("urn:test:3"), "rule-a"
        );
        Mockito.doReturn(coords).when(first).coordinates();
        Mockito.doReturn(
            new Tags.Simple(
                Arrays.<Tag>asList(
                    new Tag.Simple(
                        "on-commit", Level.INFO,
                        new ArrayMap<String, String>()
                            .with("code", "127")
                            .with("duration", "9870")
                            .with("head", "98aeb7d")
                            .with("author", "Jeff")
                            .with("time", "2011-07-21T12:15:00Z"),
                        ""
                    )
                )
            )
        ).when(first).tags();
        final Pulse second = Mockito.mock(Pulse.class);
        Mockito.doReturn(coords).when(second).coordinates();
        Mockito.doReturn(
            new Tags.Simple(
                Arrays.<Tag>asList(
                    new Tag.Simple(
                        "on-commit", Level.INFO,
                        new ArrayMap<String, String>()
                            .with("code", "0")
                            .with("duration", "99892")
                            .with("head", "9ffeb7d")
                            .with("author", "Walter")
                            .with("time", "2011-07-21T12:15:00Z"),
                        ""
                    )
                )
            )
        ).when(second).tags();
        final Pulses pulses =
            new Pulses.Row(Arrays.asList(first, second));
        final Stand stand = Mockito.mock(Stand.class);
        Mockito.doReturn(pulses).when(stand).pulses();
        MatcherAssert.assertThat(
            new Xembler(
                new Directives().add("widget").append(widget.render(stand))
            ).xml(),
            XhtmlMatchers.hasXPaths(
                "/widget[not(title)]",
                "/widget[width='4']",
                "/widget/builds/build/coordinates[owner='urn:test:3']",
                "/widget/builds/build/coordinates[rule='rule-a']",
                "/widget/builds/build[head='98aeb7d']",
                "/widget/builds/build[author='Jeff']",
                "/widget/builds/build[duration='9870']",
                "/widget/builds/build[code='127']",
                "/widget/builds/build[health='0.5']"
            )
        );
    }

    /**
     * BuildHealth can find multiple builds.
     * @throws Exception If fails
     */
    @Test
    @SuppressWarnings("unchecked")
    public void findsMultipleBuilds() throws Exception {
        final Widget widget = new BuildHealth();
        final Pulse first = Mockito.mock(Pulse.class);
        Mockito.doReturn(
            new Coordinates.Simple(new URN("urn:test:44"), "rule-x")
        ).when(first).coordinates();
        Mockito.doReturn(
            new Tags.Simple(
                Arrays.<Tag>asList(
                    new Tag.Simple(
                        "on-commit", Level.INFO,
                        new ArrayMap<String, String>()
                            .with("code", "5")
                            .with("duration", "970")
                            .with("name", "x")
                            .with("author", "x")
                            .with("time", "2011-07-21T12:15:00Z"),
                        ""
                    )
                )
            )
        ).when(first).tags();
        final Pulse second = Mockito.mock(Pulse.class);
        Mockito.doReturn(
            new Coordinates.Simple(new URN("urn:test:55"), "rule-y")
        ).when(second).coordinates();
        Mockito.doReturn(
            new Tags.Simple(
                Arrays.<Tag>asList(
                    new Tag.Simple(
                        "on-commit", Level.INFO,
                        new ArrayMap<String, String>()
                            .with("code", "7")
                            .with("duration", "970")
                            .with("name", "y")
                            .with("author", "xy")
                            .with("time", "2011-07-21T12:15:00Z"),
                        ""
                    )
                )
            )
        ).when(second).tags();
        final Pulses pulses = new Pulses.Row(Arrays.asList(first, second));
        final Stand stand = Mockito.mock(Stand.class);
        Mockito.doReturn(pulses).when(stand).pulses();
        MatcherAssert.assertThat(
            new Xembler(
                new Directives().add("widget").append(widget.render(stand))
            ).xml(),
            XhtmlMatchers.hasXPaths(
                "/widget/builds[count(build) = 2]",
                "/widget/builds/build/coordinates[rule='rule-x']",
                "/widget/builds/build/coordinates[rule='rule-y']"
            )
        );
    }

    /**
     * BuildHealth can report empty widget.
     * @throws Exception If fails
     */
    @Test
    @SuppressWarnings("unchecked")
    public void reportsEmptyWidgetWhenNoTagsFound() throws Exception {
        final Widget widget = new BuildHealth();
        final Pulses pulses = new Pulses.Row();
        final Stand stand = Mockito.mock(Stand.class);
        Mockito.doReturn(pulses).when(stand).pulses();
        MatcherAssert.assertThat(
            new Xembler(
                new Directives().add("widget").append(widget.render(stand))
            ).xml(),
            XhtmlMatchers.hasXPath("/widget/builds[count(build) = 0]")
        );
    }

    /**
     * BuildHealth can render XML+XSL with Phandom.
     * @throws Exception If fails
     */
    @Test
    public void rendersXmlInPhandom() throws Exception {
        MatcherAssert.assertThat(
            WidgetMocker.xhtml(
                this.getClass().getResource("build-health.xml")
            ),
            XhtmlMatchers.hasXPath("//xhtml:table")
        );
    }

}

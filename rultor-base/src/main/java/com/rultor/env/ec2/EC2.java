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
package com.rultor.env.ec2;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.BlockDeviceMapping;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Placement;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.Tag;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.RetryOnFailure;
import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.rultor.aws.EC2Client;
import com.rultor.env.Environment;
import com.rultor.env.Environments;
import com.rultor.snapshot.Step;
import com.rultor.snapshot.TagLine;
import com.rultor.spi.Coordinates;
import com.rultor.spi.Wallet;
import com.rultor.tools.Time;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/**
 * Amazon EC2 environments.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "type", "ami", "group", "client" })
@Loggable(Loggable.DEBUG)
@SuppressWarnings("PMD.ExcessiveImports")
public final class EC2 implements Environments {

    /**
     * Coordinates we're in.
     */
    private final transient Coordinates work;

    /**
     * Wallet to charge.
     */
    private final transient Wallet wallet;

    /**
     * Type of EC2 instance.
     */
    private final transient String type;

    /**
     * Name of AMI.
     */
    private final transient String ami;

    /**
     * EC2 security group.
     */
    private final transient String group;

    /**
     * EC2 key pair.
     */
    private final transient String pair;

    /**
     * Availability zone to run in.
     */
    private final transient String zone;

    /**
     * EC2 client.
     */
    private final transient EC2Client client;

    /**
     * Public ctor.
     * @param wrk Coordinates we're in
     * @param wlt Wallet to charge
     * @param tpe Instance type, for example "t1.micro"
     * @param image AMI name
     * @param grp Security group
     * @param par Key pair
     * @param akey AWS key
     * @param scrt AWS secret
     * @checkstyle ParameterNumber (5 lines)
     */
    public EC2(final Coordinates wrk, final Wallet wlt, final String tpe,
        final String image, final String grp, final String par,
        final String akey, final String scrt) {
        this(
            wrk, wlt, tpe, image, grp, par,
            String.format("%sa", Regions.US_EAST_1.getName()),
            new EC2Client.Simple(akey, scrt)
        );
    }

    /**
     * Public ctor.
     * @param wrk Coordinates we're in
     * @param wlt Wallet to charge
     * @param tpe Instance type, for example "t1.micro"
     * @param image AMI name
     * @param grp Security group
     * @param par Key pair
     * @param azone Availability zone
     * @param akey AWS key
     * @param scrt AWS secret
     * @checkstyle ParameterNumber (5 lines)
     */
    public EC2(final Coordinates wrk, final Wallet wlt, final String tpe,
        final String image, final String grp, final String par,
        final String azone, final String akey, final String scrt) {
        this(
            wrk, wlt, tpe, image, grp, par,
            azone, new EC2Client.Simple(akey, scrt)
        );
    }

    /**
     * Public ctor.
     * @param wrk Coordinates we're in
     * @param wlt Wallet to charge
     * @param tpe Instance type, for example "t1.micro"
     * @param image AMI name
     * @param grp Security group
     * @param par Key pair
     * @param azone Availability zone
     * @param clnt EC2 client
     * @checkstyle ParameterNumber (10 lines)
     */
    public EC2(
        @NotNull(message = "work can't be NULL") final Coordinates wrk,
        @NotNull(message = "wallet can't be NULL") final Wallet wlt,
        @NotNull(message = "instance type can't be NULL") final String tpe,
        @NotNull(message = "AMI can't be NULL") final String image,
        @NotNull(message = "security group can't be NULL") final String grp,
        @NotNull(message = "key pair can't be NULL") final String par,
        @NotNull(message = "avlt zone can't be NULL") final String azone,
        @NotNull(message = "AWS client can't be NULL") final EC2Client clnt) {
        Validate.isTrue(
            image.matches("ami-[a-f0-9]{8}"),
            "AMI name `%s` is in wrong format", image
        );
        Validate.isTrue(
            azone.matches("[a-z]{2}\\-[a-z]+\\-\\d[a-z]"),
            "availability zone `%s` is in wrong format", azone
        );
        this.work = wrk;
        this.wallet = wlt;
        this.type = tpe;
        this.ami = image;
        this.group = grp;
        this.pair = par;
        this.zone = azone;
        this.client = new EC2Client.Regional(
            StringUtils.stripEnd(azone, "abcdef"), clnt
        );
    }

    @Override
    public Environment acquire() throws IOException {
        return this.env(this.create().getInstanceId());
    }

    @Override
    public Iterator<Environment> iterator() {
        final AmazonEC2 aws = this.client.get();
        try {
            final Collection<Environment> envs = new LinkedList<Environment>();
            final DescribeInstancesResult result = aws.describeInstances();
            for (final Reservation res : result.getReservations()) {
                for (final Instance inst : res.getInstances()) {
                    envs.add(this.env(inst.getInstanceId()));
                }
            }
            return Collections.unmodifiableCollection(envs).iterator();
        } finally {
            aws.shutdown();
        }
    }

    /**
     * Make an environment with this ID.
     * @param inst Instance ID
     * @return Environment
     */
    private Environment env(final String inst) {
        return new EC2Environment(this.work, this.wallet, inst, this.client);
    }

    /**
     * Create EC2 instance.
     * @return Instance created and in stable state
     */
    @Step(
        before = "creating EC2 instance `${this.type}` from `${this.ami}`",
        // @checkstyle LineLength (1 line)
        value = "EC2 `${result.instanceType}` instance `${result.instanceId}` created in `${this.zone}`"
    )
    private Instance create() {
        final AmazonEC2 aws = this.client.get();
        try {
            final RunInstancesResult result = aws.runInstances(
                new RunInstancesRequest()
                    .withInstanceType(this.type)
                    .withImageId(this.ami)
                    .withSecurityGroups(this.group)
                    .withKeyName(this.pair)
                    .withPlacement(
                        new Placement().withAvailabilityZone(this.zone)
                    )
                    .withMinCount(1)
                    .withMaxCount(1)
                    .withBlockDeviceMappings(this.devices(aws))
                    .withInstanceInitiatedShutdownBehavior("terminate")
            );
            final List<Instance> instances =
                result.getReservation().getInstances();
            if (instances.isEmpty()) {
                throw new IllegalStateException(
                    String.format(
                        "failed to run an EC2 instance `%s` with AMI `%s`",
                        this.type, this.ami
                    )
                );
            }
            final Instance instance = instances.get(0);
            new TagLine("ec2")
                .attr("id", instance.getInstanceId())
                .attr("type", instance.getInstanceType())
                .attr("image", instance.getImageId())
                .attr("key", instance.getKeyName())
                .attr("ip", instance.getPublicIpAddress())
                .attr("kernel", instance.getKernelId())
                .attr("az", instance.getPlacement().getAvailabilityZone())
                .markdown(
                    Logger.format(
                        "instance `%s` of type `%s` from `%s` in %s",
                        instance.getInstanceId(), instance.getInstanceType(),
                        instance.getImageId(),
                        instance.getPlacement().getAvailabilityZone()
                    )
                )
                .log();
            return this.wrap(aws, instance);
        } finally {
            aws.shutdown();
        }
    }

    /**
     * Get device mappings.
     * @param aws EC2 client
     * @return Block device mappings
     */
    private Collection<BlockDeviceMapping> devices(final AmazonEC2 aws) {
        final DescribeImagesResult result = aws.describeImages(
            new DescribeImagesRequest().withImageIds(this.ami)
        );
        if (result.getImages().isEmpty()) {
            throw new IllegalArgumentException(
                String.format("AMI %s not found", this.ami)
            );
        }
        final Collection<BlockDeviceMapping> devices =
            result.getImages().get(0).getBlockDeviceMappings();
        for (final BlockDeviceMapping device : devices) {
            if (device.getEbs() != null) {
                device.getEbs().setDeleteOnTermination(true);
            }
        }
        return devices;
    }

    /**
     * Add tags and do some other wrapping to the running instance.
     * @param aws AWS client
     * @param instance Instance running (maybe already)
     * @return The same instance
     */
    @RetryOnFailure(
        attempts = Tv.TEN,
        delay = 2,
        unit = TimeUnit.MINUTES,
        verbose = false
    )
    private Instance wrap(final AmazonEC2 aws, final Instance instance) {
        aws.createTags(
            new CreateTagsRequest()
                .withResources(instance.getInstanceId())
                .withTags(
                    new Tag()
                        .withKey("Name")
                        .withValue(this.work.rule()),
                    new Tag()
                        .withKey("urn:rultor:rule")
                        .withValue(this.work.rule()),
                    new Tag()
                        .withKey("urn:rultor:owner")
                        .withValue(this.work.owner().toString()),
                    new Tag()
                        .withKey("urn:rultor:scheduled")
                        .withValue(this.work.scheduled().toString()),
                    new Tag()
                        .withKey("urn:rultor:created")
                        .withValue(new Time().toString())
                )
        );
        return instance;
    }

}

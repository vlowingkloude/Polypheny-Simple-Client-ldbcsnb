/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019-3/28/23, 11:12 AM The Polypheny Project
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.polypheny.simpleclient.scenario.coms.simulation.entites;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.polypheny.simpleclient.scenario.coms.simulation.entites.Graph.Node;
import org.polypheny.simpleclient.scenario.coms.simulation.NetworkGenerator.Device;
import org.polypheny.simpleclient.scenario.coms.simulation.NetworkGenerator.Network;

@EqualsAndHashCode(callSuper = true)
@Value
public class Server extends Node {

    Random random;


    public Server( Random random, Network network ) {
        super(
                Network.generateProperties( random, Network.config.switchConfigs ),
                Network.generateNestedLogProperties( random, Network.config.nestingDepth ), network, true );
        this.random = random;
    }


    @Override
    public List<Device> getPossibleConnectionTypes() {
        return Arrays.asList( Device.SERVER, Device.SWITCH );
    }

}

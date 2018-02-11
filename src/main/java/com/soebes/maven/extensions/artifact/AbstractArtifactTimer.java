package com.soebes.maven.extensions.artifact;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.aether.RepositoryEvent;
import org.eclipse.aether.artifact.Artifact;

import com.soebes.maven.extensions.TimePlusSize;

/**
 * @author Karl Heinz Marbaise <a href="mailto:khmarbaise@apache.org">khmarbaise@apache.org</a>
 *
 */
public abstract class AbstractArtifactTimer
{
    private Map<String, TimePlusSize> timerEvents;

    public AbstractArtifactTimer()
    {
        this.timerEvents = new ConcurrentHashMap<>();
    }
    
    protected String getArtifactId( Artifact artifact )
    {
        StringBuilder sb = new StringBuilder( 128 );
        sb.append( artifact.getGroupId() );
        sb.append( ":" ).append( artifact.getArtifactId() ).append( ":" ).append( artifact.getVersion() );
        if ( artifact.getClassifier() != null )
        {
            sb.append( ':' ).append( artifact.getClassifier() );
        }
        sb.append( ':' ).append( artifact.getExtension() );
        return sb.toString();
    }

    protected Map<String, TimePlusSize> getTimerEvents()
    {
        return timerEvents;
    }

    public void start( RepositoryEvent event )
    {
        String artifactId = getArtifactId( event.getArtifact() );
        TimePlusSize systemTime = new TimePlusSize();
        systemTime.start();
        getTimerEvents().put( artifactId, systemTime );
    }

    public void stop( RepositoryEvent event )
    {
        String artifactId = getArtifactId( event.getArtifact() );
        if ( !getTimerEvents().containsKey( artifactId ) )
        {
            throw new IllegalArgumentException( "Unknown artifactId (" + artifactId + ")" );
        }
        getTimerEvents().get( artifactId ).stop();
        getTimerEvents().get( artifactId ).setSize( event.getArtifact().getFile().length() );
    }

    private final double MiB = 1024 * 1024;

    protected double calculateMegabytesPerSeconds( long timeInMilliseconds, long sizeInBytes )
    {
        double dividerTime = ( timeInMilliseconds / 1000.0 );
        return (double) sizeInBytes / dividerTime / MiB;
    }

}
